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

import java.awt.*;
import java.nio.*;
import java.util.List;

import com.jogamp.opengl.awt.*;

import ray.rage.asset.material.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.shader.*;
import ray.rage.rendersystem.shader.glsl.AbstractGlslProgram;
import ray.rage.rendersystem.shader.glsl.GlslProgramAttributeBufferVec2;
import ray.rage.rendersystem.shader.glsl.GlslProgramAttributeBufferVec3;
import ray.rage.rendersystem.shader.glsl.GlslProgramIndexBuffer;
import ray.rage.rendersystem.shader.glsl.GlslProgramStorageBufferFloat;
import ray.rage.rendersystem.shader.glsl.GlslProgramUniformFloat;
import ray.rage.rendersystem.shader.glsl.GlslProgramUniformMat4;
import ray.rage.rendersystem.shader.glsl.GlslProgramUniformVec3;
import ray.rage.rendersystem.shader.glsl.GlslProgramUniformVec4;
import ray.rage.scene.*;
import ray.rage.util.*;
import ray.rml.*;

/**
 * Concrete implementation of a {@link GpuShaderProgram shader-program} to
 * process {@link Renderable renderables}.
 * <p>
 * This implementation contains, and submits, all the input data for the
 * <code>renderables.vert</code> vertex shader. Changes in the inputs of that
 * program will require changes to this implementation.
 *
 * @author Raymond L. Rivera
 *
 */
class GlslItemBoxProgram extends AbstractGlslProgram {

    private boolean                        initialized = false;

    private GlslProgramAttributeBufferVec3 positionsBuffer;
    private GlslProgramAttributeBufferVec2 texcoordsBuffer;
    private GlslProgramAttributeBufferVec3 normalsBuffer;
    private GlslProgramIndexBuffer         indexBuffer;

    private GlslProgramStorageBufferFloat  lightsBuffer;

    private GlslProgramUniformMat4         modelMatrix;
    private GlslProgramUniformMat4         viewMatrix;
    private GlslProgramUniformMat4         projMatrix;
    private GlslProgramUniformMat4         normalMatrix;
    private GlslProgramUniformMat4         lightSpaceMatrix;

    private GlslProgramUniformVec4         ambientLightIntensity;

    private GlslProgramUniformVec4         materialAmbient;
    private GlslProgramUniformVec4         materialDiffuse;
    private GlslProgramUniformVec4         materialSpecular;
    private GlslProgramUniformVec4         materialEmissive;
    private GlslProgramUniformFloat        materialShininess;
    
    private GlslProgramUniformFloat        textureMoveFactor;
    
    private GlslProgramUniformVec3         lightPos;
    private GlslProgramUniformVec3         viewPos;
    
    private float MOVE_SPEED = 0.0005f;
    private float moveFactor = 0;

    public GlslItemBoxProgram(GLCanvas canvas) {
        super(canvas);
    }

    @Override
    public Type getType() {
        return Type.ITEM_BOX;
    }

    @Override
    public void fetchImpl(Context ctx) {
        if (!initialized)
            init();

        final Renderable r = ctx.getRenderable();
        final Matrix4 model = r.getWorldTransformMatrix();
        final Matrix4 view = ctx.getViewMatrix();
        final Matrix4 proj = ctx.getProjectionMatrix();
        final Matrix4 lightSpace = ctx.getLightSpaceMatrix();
        
        moveFactor += MOVE_SPEED;
        moveFactor %= 1;
        textureMoveFactor.set(moveFactor);

        setRenderable(r);
        setGlobalAmbientLight(ctx.getAmbientLight());
        setLocalLights(ctx.getLightsList(), view);
        setMaterial(r.getMaterial());
        setMatrixUniforms(model, view, proj, lightSpace);
    }

	private void setRenderable(Renderable r) {
        FloatBuffer fb = r.getVertexBuffer();
        if (canSubmitBuffer(fb)) {
            positionsBuffer.set(fb);
        }

        fb = r.getTextureCoordsBuffer();
        if (canSubmitBuffer(fb)) {
            texcoordsBuffer.set(fb);
        }

        fb = r.getNormalsBuffer();
        if (canSubmitBuffer(fb)) {
            normalsBuffer.set(fb);
        }

        IntBuffer ib = r.getIndexBuffer();
        if (canSubmitBuffer(ib)) {
            indexBuffer.set(ib);
        }
    }

    private void setGlobalAmbientLight(AmbientLight ambientLight) {
        if (ambientLight == null)
            return;

        ambientLightIntensity.set(toVec4(ambientLight.getIntensity()));
    }

    private void setLocalLights(List<Light> lights, Matrix4 view) {
    	int numLights = lights.size();
        if (lights == null || numLights == 0)
            return;

        // XXX: See renderables.frag, struct light_t member definition,
        // specifically the total size in bytes for the structure.
        //
        // If the number of fields and/or total byte size changes,
        // then the code below must be adjusted to match, including alignment.
        //
        // 25 floats + 3 floats of padding for alignment
        final int shaderStructFieldCount = 25 + 3;

// SCOTT
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
        // pad to fill buffer completely - SCOTT
/*        for (int i=numLights; i<maxLights; i++) {
        	Light l = lights.get(0);
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
*/
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
        //numberOfLights.set(numLights);	// SCOTT
    }

    private void setMatrixUniforms(Matrix4 model, Matrix4 view, Matrix4 proj, Matrix4 lightSpace) {
        final Matrix4 modelView = view.mult(model);
        modelMatrix.set(model);
        viewMatrix.set(view);
        projMatrix.set(proj);
        lightSpaceMatrix.set(lightSpace);
        normalMatrix.set(modelView.inverse().transpose());
    }

    private void init() {
        final GLCanvas canvas = getCanvas();

        positionsBuffer = new GlslProgramAttributeBufferVec3(this, canvas, "vertex_position");
        texcoordsBuffer = new GlslProgramAttributeBufferVec2(this, canvas, "vertex_texcoord");
        normalsBuffer = new GlslProgramAttributeBufferVec3(this, canvas, "vertex_normal");
        indexBuffer = new GlslProgramIndexBuffer(this, canvas);

        ambientLightIntensity = new GlslProgramUniformVec4(this, canvas, "global_light.intensity");
        lightsBuffer = new GlslProgramStorageBufferFloat(this, canvas);

        materialAmbient = new GlslProgramUniformVec4(this, canvas, "material.ambient");
        materialDiffuse = new GlslProgramUniformVec4(this, canvas, "material.diffuse");
        materialSpecular = new GlslProgramUniformVec4(this, canvas, "material.specular");
        materialEmissive = new GlslProgramUniformVec4(this, canvas, "material.emissive");
        materialShininess = new GlslProgramUniformFloat(this, canvas, "material.shininess");
        
        textureMoveFactor = new GlslProgramUniformFloat(this, canvas, "textureMoveFactor");
        
   //     numberOfLights = new GlslProgramUniformInt(this, canvas, "material.numLights");  // SCOTT

        modelMatrix = new GlslProgramUniformMat4(this, canvas, "matrix.model");
        viewMatrix = new GlslProgramUniformMat4(this, canvas, "matrix.view");
        projMatrix = new GlslProgramUniformMat4(this, canvas, "matrix.projection");
        lightSpaceMatrix = new GlslProgramUniformMat4(this, canvas, "matrix.lightSpaceMatrix");
        normalMatrix = new GlslProgramUniformMat4(this, canvas, "matrix.normal");
        
        lightPos = new GlslProgramUniformVec3(this, canvas, "lightPos");
        viewPos = new GlslProgramUniformVec3(this, canvas, "viewPos");

        initialized = true;
    }

    @Override
    public void notifyDispose() {
        if (initialized) {
            positionsBuffer.notifyDispose();
            texcoordsBuffer.notifyDispose();
            normalsBuffer.notifyDispose();
            indexBuffer.notifyDispose();

            ambientLightIntensity.notifyDispose();
            lightsBuffer.notifyDispose();

            materialAmbient.notifyDispose();
            materialDiffuse.notifyDispose();
            materialSpecular.notifyDispose();
            materialEmissive.notifyDispose();
            materialShininess.notifyDispose();
            
            textureMoveFactor.notifyDispose();

            modelMatrix.notifyDispose();
            viewMatrix.notifyDispose();
            projMatrix.notifyDispose();
            normalMatrix.notifyDispose();
            lightSpaceMatrix.notifyDispose();
            
            lightPos.notifyDispose();
            viewPos.notifyDispose();

            positionsBuffer = null;
            texcoordsBuffer = null;
            normalsBuffer = null;
            indexBuffer = null;

            ambientLightIntensity = null;
            lightsBuffer = null;

            materialAmbient = null;
            materialDiffuse = null;
            materialSpecular = null;
            materialEmissive = null;
            materialShininess = null;
            
            textureMoveFactor = null;

            modelMatrix = null;
            viewMatrix = null;
            projMatrix = null;
            normalMatrix = null;
            lightSpaceMatrix = null;
            
            lightPos = null;
            viewPos = null;

            initialized = false;
        }
        super.notifyDispose();
    }

    private static Vector4 toVec4(Color c) {
        final float[] rgb = c.getColorComponents(null);
        return Vector4f.createFrom(rgb[0], rgb[1], rgb[2]);
    }

    private static boolean canSubmitBuffer(Buffer buff) {
        return buff != null && buff.capacity() > 0;
    }

}
