/**
 * Copyright (C) 2016 Raymond L. Rivera <ray.l.rivera@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ray.rage.rendersystem.shader.glsl;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.List;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.awt.*;
import com.jogamp.opengl.util.glsl.ShaderCode;

import ray.rage.asset.material.Material;
import ray.rage.rendersystem.Renderable;
import ray.rage.rendersystem.shader.glsl.AbstractGlslProgram;
import ray.rage.rendersystem.shader.glsl.GlslContextUtil;
import ray.rage.rendersystem.shader.glsl.GlslProgramStorageBufferFloat;
import ray.rage.rendersystem.shader.glsl.GlslProgramUniformFloat;
import ray.rage.rendersystem.shader.glsl.GlslProgramUniformInt;
import ray.rage.rendersystem.shader.glsl.GlslProgramUniformMat4;
import ray.rage.rendersystem.shader.glsl.GlslProgramUniformVec4;
import ray.rage.scene.AmbientLight;
import ray.rage.scene.Light;
import ray.rage.scene.Tessellation;
import ray.rage.scene.TessellationBody;
import ray.rage.util.BufferUtil;
import ray.rml.Matrix4;
import ray.rml.Vector3;
import ray.rml.Vector4;
import ray.rml.Vector4f;

/**
 * Concrete implementation of a {@link GpuShaderProgram shader-program} to
 * process a {@link Tessellation tessellation}.
 * <p>
 * This implementation contains, and submits, all the input data for the
 * <code>tessellation.vert</code> vertex shader, <code>tessellation.tesc</code> 
 * control shader, <code>tessellation.tese</code> evaluation shader, and
 * <code>tessellation.frag</code> frag shader. Changes in the inputs of the
 * shader program will require changes to this implementation.
 *
 * @author Kevin S. Cheong
 *
 */
class GlslTessProgram extends AbstractGlslProgram {

	private boolean                 initialized;
	
	private GlslProgramUniformFloat multiplier;
	private GlslProgramUniformFloat subdivisions;
	
	private GlslProgramUniformInt   patchSize;
	
	private GlslProgramUniformInt   texTileX;    // The amount of time to tile the texture    in the X direction
	private GlslProgramUniformInt   texTileZ;    // The amount of time to tile the texture    in the Z direction
	private GlslProgramUniformInt   heightTileX; // The amount of time to tile the height map in the X direction
	private GlslProgramUniformInt   heightTileZ; // The amount of time to tile the height map in the Z direction
	private GlslProgramUniformInt   normalTileX; // The amount of time to tile the normal map in the X direction
	private GlslProgramUniformInt   normalTileZ; // The amount of time to tile the normal map in the Z direction
	
	private GlslProgramUniformInt   hasTexture;
	private GlslProgramUniformInt   hasHeightMap;
	private GlslProgramUniformInt   hasNormalMap;
	
	private GlslProgramStorageBufferFloat  lightsBuffer;
	private GlslProgramUniformVec4         ambientLightIntensity;
	
	private GlslProgramUniformMat4 mat4_norm;
	private GlslProgramUniformMat4 mat4_mvp;
	private GlslProgramUniformMat4 mat4_mv;
	private GlslProgramUniformMat4 mat4_m;
	private GlslProgramUniformMat4 mat4_p;
	private GlslProgramUniformMat4 lightSpaceMatrix;
	
	private GlslProgramUniformVec4  materialAmbient;
    private GlslProgramUniformVec4  materialDiffuse;
    private GlslProgramUniformVec4  materialSpecular;
    private GlslProgramUniformVec4  materialEmissive;
    private GlslProgramUniformFloat materialShininess;
    
    private GlslProgramUniformFloat textureMoveFactor;
	
    private long timeMS = System.currentTimeMillis();
    private long dTime = 0;
    private float MOVE_SPEED_MODIFIER = 0.0003f;
    private float moveFactor = 0;

    public GlslTessProgram(GLCanvas canvas) {
        super(canvas);
        initialized = false;
    }

    @Override
    public void build() {
        if (super.getShaderProgram().linked())
            throw new RuntimeException(getType() + " is already built");

        // a minimum of 1 vertex + 1 fragment + 1 control + 1 evaluation program are required, so if we
        // have anything less than 4, either one or both of them are missing
        if (super.getSourceMap().size() < 4)
            throw new IllegalStateException("Missing source code. Verify all stages are set.");

        GLContext ctx = GlslContextUtil.getCurrentGLContext(super.getCanvas());
        GL4 gl = ctx.getGL().getGL4();

        ShaderCode vs = compileShader(gl, super.getSourceMap().get(Stage.VERTEX_PROGRAM),     Stage.VERTEX_PROGRAM    );
        ShaderCode cs = compileShader(gl, super.getSourceMap().get(Stage.CONTROL_PROGRAM),    Stage.CONTROL_PROGRAM   );
        ShaderCode es = compileShader(gl, super.getSourceMap().get(Stage.EVALUATION_PROGRAM), Stage.EVALUATION_PROGRAM);
        ShaderCode fs = compileShader(gl, super.getSourceMap().get(Stage.FRAGMENT_PROGRAM),   Stage.FRAGMENT_PROGRAM  );

        linkProgram(gl, super.getShaderProgram(), vs, cs, es, fs);
        ctx.release();

        // no need to keep source code around anymore
        super.getSourceMap().clear();
    }
    
    @Override
    public Type getType() {
        return Type.TESSELLATION;
    }

    @Override
    public void fetchImpl(Context ctx) {
    	if (!initialized)
            init();

    	final Renderable r  = ctx.getRenderable();
    	
        long currentTimeMS = System.currentTimeMillis();
        dTime = currentTimeMS - timeMS;
        moveFactor += dTime * MOVE_SPEED_MODIFIER;
        moveFactor %= 1;
        timeMS = currentTimeMS;
        textureMoveFactor.set(moveFactor);
    	
    	texTileX.set(((TessellationBody) r).getTextureTilingX());
    	texTileZ.set(((TessellationBody) r).getTextureTilingZ()); 
    	heightTileX.set(((TessellationBody) r).getHeightMapTilingX());
    	heightTileZ.set(((TessellationBody) r).getHeightMapTilingZ());
    	normalTileX.set(((TessellationBody) r).getNormalMapTilingX());
    	normalTileZ.set(((TessellationBody) r).getNormalMapTilingZ());
    	
    	setupIntegerSwitches(r);
    	
    	multiplier.set(((TessellationBody) r).getMultiplier()); 
    	subdivisions.set(((TessellationBody) r).getSubdivisions()); 
        
        final Matrix4 model = r.getWorldTransformMatrix();
        final Matrix4 view  = ctx.getViewMatrix();
        final Matrix4 proj  = ctx.getProjectionMatrix();
        final Matrix4 lightSpace = ctx.getLightSpaceMatrix();
        
        mat4_norm.set(         ((view.mult(model)).inverse()).transpose());
        mat4_mvp.set ( proj.mult(view.mult(model))                       );
        mat4_mv.set  (           view.mult(model)                        );
        mat4_m.set   (                     model                         );
        mat4_p.set   ( proj                                              );
        lightSpaceMatrix.set(lightSpace);

        setLocalLights(ctx.getLightsList(), view);
        setGlobalAmbientLight(ctx.getAmbientLight());
        setMaterial(r.getMaterial());
    }

    public void setupIntegerSwitches(Renderable r) {
    	TessellationBody tb = (TessellationBody) r;
    	int intState;
    	
    	if (tb.hasTexture()) intState = 1;
    	else                 intState = 0;
    	hasTexture.set(intState);

    	if (tb.hasHeightMap()) intState = 1;
    	else                 intState = 0;
    	hasHeightMap.set(intState);

    	if (tb.hasNormalMap()) intState = 1;
    	else                 intState = 0;
    	hasNormalMap.set(intState);

    	patchSize.set( tb.getQualityTotal() );
    }
    
    private void setGlobalAmbientLight(AmbientLight ambientLight) {
        if (ambientLight == null)
            return;

        ambientLightIntensity.set(toVec4(ambientLight.getIntensity()));
    }
    
    private void setLocalLights(List<Light> lights, Matrix4 view) {
        if (lights == null || lights.size() == 0)
            return;

        // XXX: See renderables.frag, struct light_t member definition,
        // specifically the total size in bytes for the structure.
        //
        // If the number of fields and/or total byte size changes,
        // then the code below must be adjusted to match, including alignment.
        //
        // 25 floats + 3 floats of padding for alignment
        final int shaderStructFieldCount = 25 + 3;

        FloatBuffer ssbo = BufferUtil.directFloatBuffer(shaderStructFieldCount * lights.size());
        for (Light l : lights) {
            ssbo.put(l.getAmbient().getColorComponents(null));
            ssbo.put(1f);

            ssbo.put(l.getDiffuse().getColorComponents(null));
            ssbo.put(1f);

            ssbo.put(l.getSpecular().getColorComponents(null));
            ssbo.put(1f);

            final float w = l.getType() == Light.Type.DIRECTIONAL ? 0 : 1;
            Vector4 pos = view.mult(Vector4f.createFrom(l.getParentNode().getWorldPosition(), w));
            ssbo.put(pos.toFloatArray());

            ssbo.put(l.getConstantAttenuation());
            ssbo.put(l.getLinearAttenuation());
            ssbo.put(l.getQuadraticAttenuation());
            ssbo.put(l.getRange());

            Vector3 dir = view.toMatrix3().mult(l.getParentNode().getWorldForwardAxis()).normalize();
            ssbo.put(dir.toFloatArray());

            ssbo.put(l.getConeCutoffAngle().valueRadians());
            ssbo.put(l.getFalloffExponent());

            // padding required for 16-byte alignment in SSBO
            ssbo.put(0);
            ssbo.put(0);
            ssbo.put(0);
        }
        ssbo.rewind();
        lightsBuffer.set(ssbo);
    }
    
    private void setMaterial(Material mat) {
        if (mat == null)
            return;

        materialAmbient.set(toVec4(mat.getAmbient()));
        materialDiffuse.set(toVec4(mat.getDiffuse()));
        materialSpecular.set(toVec4(mat.getSpecular()));
        materialEmissive.set(toVec4(mat.getEmissive()));
        materialShininess.set(mat.getShininess());
    }
    
    private void init() {
        final GLCanvas canvas = getCanvas();
        
        multiplier   = new GlslProgramUniformFloat(this, canvas, "multiplier");
        subdivisions = new GlslProgramUniformFloat(this, canvas, "subdivisions");
        
        patchSize    = new GlslProgramUniformInt( this, canvas, "patchSize" );
        
        texTileX    = new GlslProgramUniformInt( this, canvas, "tileTx");
        texTileZ    = new GlslProgramUniformInt( this, canvas, "tileTy");
        heightTileX = new GlslProgramUniformInt( this, canvas, "tileHx");
        heightTileZ = new GlslProgramUniformInt( this, canvas, "tileHy");
        normalTileX = new GlslProgramUniformInt( this, canvas, "tileNx");
        normalTileZ = new GlslProgramUniformInt( this, canvas, "tileNy");
        
        hasTexture   = new GlslProgramUniformInt( this, canvas, "hasTexture");
        hasHeightMap = new GlslProgramUniformInt( this, canvas, "hasHeightM");
        hasNormalMap = new GlslProgramUniformInt( this, canvas, "hasNormalM");
        
        mat4_norm = new GlslProgramUniformMat4(this, canvas, "mat4_norm");
        mat4_mvp  = new GlslProgramUniformMat4(this, canvas, "mat4_mvp" );
        mat4_mv   = new GlslProgramUniformMat4(this, canvas, "mat4_mv"  );
        mat4_m    = new GlslProgramUniformMat4(this, canvas, "mat4_m"   );
        mat4_p    = new GlslProgramUniformMat4(this, canvas, "mat4_p"   );
        lightSpaceMatrix = new GlslProgramUniformMat4(this, canvas, "matrix.lightSpaceMatrix");
        
        materialAmbient = new GlslProgramUniformVec4(this, canvas, "material.ambient");
        materialDiffuse = new GlslProgramUniformVec4(this, canvas, "material.diffuse");
        materialSpecular = new GlslProgramUniformVec4(this, canvas, "material.specular");
        materialEmissive = new GlslProgramUniformVec4(this, canvas, "material.emissive");
        materialShininess = new GlslProgramUniformFloat(this, canvas, "material.shininess");
        
        textureMoveFactor = new GlslProgramUniformFloat(this, canvas, "textureMoveFactor");
        
        ambientLightIntensity = new GlslProgramUniformVec4(this, canvas, "global_light.intensity");
        lightsBuffer = new GlslProgramStorageBufferFloat(this, canvas);
        
        initialized = true;
    }
    
    @Override
    public void notifyDispose() {
        if (initialized) {
        	
        	multiplier.notifyDispose();   multiplier   = null;
        	subdivisions.notifyDispose(); subdivisions = null;
        	
        	patchSize.notifyDispose();    patchSize    = null;
        	
        	texTileX.notifyDispose();    texTileX    = null;
        	texTileZ.notifyDispose();    texTileZ    = null;
        	heightTileX.notifyDispose(); heightTileX = null;
        	heightTileZ.notifyDispose(); heightTileZ = null;
        	normalTileX.notifyDispose(); normalTileX = null;
        	normalTileZ.notifyDispose(); normalTileZ = null;
        	
        	hasTexture.notifyDispose();   hasTexture   = null;
        	hasHeightMap.notifyDispose(); hasHeightMap = null;
        	hasNormalMap.notifyDispose(); hasNormalMap = null;
        	
        	mat4_norm.notifyDispose(); mat4_norm = null;
        	mat4_mvp.notifyDispose();  mat4_mvp  = null;
        	mat4_mv.notifyDispose();   mat4_mv   = null;
        	mat4_m.notifyDispose();    mat4_m    = null;
        	mat4_p.notifyDispose();    mat4_p    = null;
        	lightSpaceMatrix.notifyDispose(); lightSpaceMatrix = null;
        	
        	materialAmbient.notifyDispose();   materialAmbient   = null;
            materialDiffuse.notifyDispose();   materialDiffuse   = null;
            materialSpecular.notifyDispose();  materialSpecular  = null;
            materialEmissive.notifyDispose();  materialEmissive  = null;
            materialShininess.notifyDispose(); materialShininess = null;
            
            textureMoveFactor.notifyDispose(); textureMoveFactor = null;
        	
        	ambientLightIntensity.notifyDispose(); ambientLightIntensity = null;
        	lightsBuffer.notifyDispose(); lightsBuffer = null;
        	
        	initialized = false;
        }
        super.notifyDispose();
    }

    private static Vector4 toVec4(Color c) {
        final float[] rgb = c.getColorComponents(null);
        return Vector4f.createFrom(rgb[0], rgb[1], rgb[2]);
    }
}
