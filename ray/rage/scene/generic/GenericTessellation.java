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

package ray.rage.scene.generic;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import ray.rage.Engine;
import ray.rage.asset.mesh.Mesh;
import ray.rage.asset.mesh.SubMesh;
import ray.rage.asset.texture.*;
import ray.rage.rendersystem.Renderable;
import ray.rage.rendersystem.shader.*;
import ray.rage.rendersystem.states.FrontFaceState;
import ray.rage.rendersystem.states.RenderState;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.rendersystem.states.TextureState.WrapMode;
import ray.rage.scene.*;
import ray.rage.scene.generic.AbstractGenericSceneObject;
import ray.rage.scene.generic.GenericTessellationBody;
import ray.rml.Matrix3;
import ray.rml.Matrix4;
import ray.rml.Matrix4f;
import ray.rml.Vector3;

/**
 * A generic implementation of the {@link Tessellation} interface.
 *
 * @author Kevin S. Cheong
 *
 */
final class GenericTessellation extends AbstractGenericSceneObject implements Tessellation {
	
	private TessellationBody body;
	private Texture tex_diffuse;
	private Texture map_height;
	private Texture map_normal;
	private TextureState tstate;
	
	/**
     * Creates a new {@link Tessellation tessellation} with the given parent
     * {@link SceneManager manager}, name, and {@link TessellationBody tessellation
     * body}.
     *
     * @param sm
     *            The parent {@link SceneManager manager}.
     * @param name
     *            The name for <code>this</code> {@link Tessellation tessellation}.
     * @throws NullPointerException
     *             If any of the arguments is <code>null</code>.
     * @throws IllegalArgumentException
     *             If the name is empty.
     */
    GenericTessellation(SceneManager sm, String name) {
        super(sm, name);
        if (sm == null)
            throw new NullPointerException("Null " + SceneManager.class.getSimpleName());
        
        createGenericTessellation(sm, name, 7);
    }
    
    /**
     * Creates a new {@link Tessellation tessellation} with the given parent
     * {@link SceneManager manager}, name, and {@link TessellationBody tessellation
     * body}.
     *
     * @param sm
     *            The parent {@link SceneManager manager}.
     * @param name
     *            The name for <code>this</code> {@link Tessellation tessellation}.
     * @param quality
     *            The quality level to be used. It must be a minimum of five, but it is
     *            advised to use a level of ten or less.
     * @throws NullPointerException
     *             If any of the arguments is <code>null</code>.
     * @throws IllegalArgumentException
     *             If the name is empty.
     */
    GenericTessellation(SceneManager sm, String name, int quality) {
        super(sm, name);
        if (sm == null)
            throw new NullPointerException("Null " + SceneManager.class.getSimpleName());
        
        createGenericTessellation(sm, name, quality);
    }
    
    /* This assists with the above constructors. */
    private void createGenericTessellation(SceneManager sm, String name, int quality) {
        Mesh mesh = sm.getMeshManager().createManualAsset(name + Mesh.class.getSimpleName());
        body = new GenericTessellationBody(this, mesh.createSubMesh(name + SubMesh.class.getSimpleName()));
        body.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.TESSELLATION));
        body.setDepthShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.DEPTH));

        // Assign the default material by... well... default...
        try {
			body.setMaterial(sm.getMaterialManager().getAssetByPath("default.mtl"));
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
        FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);
        body.setRenderState(tstate);
    	body.setRenderState(faceState);
        
        body.setHasTexture  (false);
        body.setHasHeightMap(false);
        body.setHasNormalMap(false);

        setQuality(quality);
        
        setSubdivisions(0f);
        setMultiplier(0f);
    }
    
    @Override
    public void setTextureState(TextureState ts) {
    	tstate = ts;
    }
    
    @Override
    public TextureState getTextureState() {
    	return tstate;
    }
    
    @Override
    public void setTextureTiling(int dimension) {
    	body.setTextureTilingX(dimension);
    	body.setTextureTilingZ(dimension);
    }
    
    @Override
    public void setTextureTiling(int dimensionX, int dimensionZ) {
    	body.setTextureTilingX(dimensionX);
    	body.setTextureTilingZ(dimensionZ);
    }
    
    @Override
    public void setTextureTilingX(int dimension) {
    	body.setTextureTilingX(dimension);
    }
    
    @Override
    public void setTextureTilingZ(int dimension) {
    	body.setTextureTilingZ(dimension);
    }
    
    @Override
    public void setHeightMapTiling(int dimension) {
    	body.setHeightMapTilingX(dimension);
    	body.setHeightMapTilingZ(dimension);
    }
    
    @Override
    public void setHeightMapTiling(int dimensionX, int dimensionZ) {
    	body.setHeightMapTilingX(dimensionX);
    	body.setHeightMapTilingZ(dimensionZ);
    }
    
    @Override
    public void setHeightMapTilingX(int dimension) {
    	body.setHeightMapTilingX(dimension);
    }
    
    @Override
    public void setHeightMapTilingZ(int dimension) {
    	body.setHeightMapTilingZ(dimension);
    }
    
    @Override
    public void setNormalMapTiling(int dimension) {
    	body.setNormalMapTilingX(dimension);
    	body.setNormalMapTilingZ(dimension);
    }
    
    @Override
    public void setNormalMapTiling(int dimensionX, int dimensionZ) {
    	body.setNormalMapTilingX(dimensionX);
    	body.setNormalMapTilingZ(dimensionZ);
    }
    
    @Override
    public void setNormalMapTilingX(int dimension) {
    	body.setNormalMapTilingX(dimension);
    }
    
    @Override
    public void setNormalMapTilingZ(int dimension) {
    	body.setNormalMapTilingZ(dimension);
    }
    
    @Override
    public int getTextureTilingX() {
    	return body.getTextureTilingX();
    }
    
    @Override
    public int getTextureTilingZ() {
    	return body.getTextureTilingZ();
    }
    
    @Override
    public int getHeightMapTilingX() {
    	return body.getHeightMapTilingX();
    }
    
    @Override
    public int getHeightMapTilingZ() {
    	return body.getHeightMapTilingZ();
    }
    
    @Override
    public int getNormalMapTilingX() {
    	return body.getNormalMapTilingX();
    }
    
    @Override
    public int getNormalMapTilingZ() {
    	return body.getNormalMapTilingZ();
    }
    
    @Override
    public void setTexture(Engine eng, String texName) {
    	Texture tex;
		try { tex = eng.getTextureManager().getAssetByPath(texName); }
		catch (IOException e) { e.printStackTrace(); return; }
    	if (tex == null) throw new NullPointerException("Null " + Texture.class.getSimpleName());
    	
        tex_diffuse = tex;
        tstate.setTexture(tex_diffuse, 0);    
    	
    	body.setHasTexture  (true);
    }
    
	@Override
	public void setHeightMap(Engine eng, String texName) {
    	Texture tex;
		try { tex = eng.getTextureManager().getAssetByPath(texName); } 
		catch (IOException e) { e.printStackTrace(); return; }
    	if (tex == null) throw new NullPointerException("Null " + Texture.class.getSimpleName());
    	
    	// Check if texture exists
    	if (!body.hasTexture()) {
    		removeTexture(eng);
    	}
    	
    	// Bind the Height Map
    	map_height = tex;
        tstate.setTexture(map_height, 1);
        
        // If this is the first time a height map was applied, initialize the multiplier
        if (!hasHeightMap()) { setMultiplier(1.0f); }
        
        body.setHasHeightMap(true);
	}

	@Override
	public void setNormalMap(Engine eng, String texName) {
    	Texture tex;
		try { tex = eng.getTextureManager().getAssetByPath(texName); }
		catch (IOException e) { e.printStackTrace(); return; }
    	if (tex == null) throw new NullPointerException("Null " + Texture.class.getSimpleName());
    	
    	// Check if texture exists
    	if (!body.hasTexture()  ) {
    		removeTexture(eng);
    	}
    	
    	// Check if Height Map exists
    	if (!body.hasHeightMap()) {
    		removeHeightMap(eng);
    	}
    	
    	// Bind the Normal Map
    	map_normal = tex;
        tstate.setTexture(map_normal, 2);
        
        body.setHasNormalMap(true);
	}
	
	@Override
	public void removeTexture(Engine eng) {
		setTexture(eng, "default.png");
		body.setHasTexture(false);
	}
	
	@Override
	public void removeHeightMap(Engine eng) {
		setHeightMap(eng, "default.png");
		body.setHasHeightMap(false);
		setMultiplier(0f);
	}
	
	@Override
	public void removeNormalMap(Engine eng) {
		setNormalMap(eng, "default.png");
		body.setHasNormalMap(false);
	}
	
    @Override
    public void setGpuShaderProgram(GpuShaderProgram program) {
    	 body.setGpuShaderProgram(program);
    }
    
    @Override
    public void setDepthShaderProgram(GpuShaderProgram program) {
    	 body.setDepthShaderProgram(program);
    }

    @Override
    public void setMultiplier(float amount) {
    	body.setMultiplier(amount);
    }
    
    @Override
    public void setSubdivisions(float amount) {
    	body.setSubdivisions(amount);
    }
	
	@Override
	public void setQuality(int amount) {
		body.setQuality(amount);
	}
    
    @Override
    public Texture getTexture() {
        return tex_diffuse;
    }
    
	@Override
	public Texture getHeighMap() {
		return map_height;
	}
	
	@Override
	public Texture getNormalMap() {
		return map_normal;
	}

    @Override
    public Renderable getTessellationBody() {
    	return body;
    }
    
    @Override
	public boolean hasTexture() {
		return body.hasTexture();
	}
    
    @Override
    public boolean hasHeightMap() {
    	return body.hasHeightMap();
    }
    
    @Override
    public boolean hasNormalMap() {
    	return body.hasNormalMap();
    }
	
    @Override
    public float getMultiplier() {
    	return body.getMultiplier();
    }
    
    @Override
    public float getSubdivisions() {
    	return body.getSubdivisions();
    }

    @Override
	public int getQualityLevel() {
		return body.getQualityLevel();
	}
	
	@Override
	public int getQualityTotal() {
		return body.getQualityTotal();
	}
	
	@Override
	public float getWorldHeight(float globalX, float globalZ) {
		
		// Determine true X-Z coordinate that intersects the tessellated plane
		Matrix4 parentTransform = this.getParentNode().getWorldTransform();
		Vector3 parentTranslate = this.getParentNode().getWorldPosition();
		Matrix4 translateGlobal  = Matrix4f.createIdentityMatrix().translate(globalX-parentTranslate.x(), 0f, parentTranslate.z()-globalZ);

		// Drop the Translation details, and concatenate the rotation and scaling details.
		Matrix3 dropped = parentTransform.toMatrix3();
		parentTransform = Matrix4f.createFrom(dropped);
		translateGlobal = parentTransform.mult(translateGlobal);
		Vector3 trueXZ  = translateGlobal.column(3).toVector3();
		
		// Get size bounds
		Vector3 scale = this.getParentNode().getWorldScale();
		float targetX;
		float targetZ;
		
		// Normalize the target values
		if (scale.x() >= 0) { targetX = ((trueXZ.x() /          scale.x())  + (0.5f * scale.x())) /          scale.x() ; }
		else                { targetX = ((trueXZ.x() / Math.abs(scale.x())) - (0.5f * scale.x())) / Math.abs(scale.x()); }
		
		if (scale.z() >= 0) { targetZ = ((trueXZ.z() /          scale.z())  + (0.5f * scale.z())) /          scale.z() ; }
		else                { targetZ = ((trueXZ.z() / Math.abs(scale.z())) - (0.5f * scale.z())) / Math.abs(scale.z()); }
		
		// Now that the user's provided coordinates have been converted to local coordinates, perform the height estimation.
		return getAverageHeight(targetX, targetZ);
	}
	
	public boolean getIsSpeedBoost(float globalX, float globalZ) {
		if (!body.hasHeightMap()) {
			return false;
		}
		// Determine true X-Z coordinate that intersects the tessellated plane
		Matrix4 parentTransform = this.getParentNode().getWorldTransform();
		Vector3 parentTranslate = this.getParentNode().getWorldPosition();
		Matrix4 translateGlobal  = Matrix4f.createIdentityMatrix().translate(globalX-parentTranslate.x(), 0f, parentTranslate.z()-globalZ);

		// Drop the Translation details, and concatenate the rotation and scaling details.
		Matrix3 dropped = parentTransform.toMatrix3();
		parentTransform = Matrix4f.createFrom(dropped);
		translateGlobal = parentTransform.mult(translateGlobal);
		Vector3 trueXZ  = translateGlobal.column(3).toVector3();
		
		// Get size bounds
		Vector3 scale = this.getParentNode().getWorldScale();
		float targetX;
		float targetZ;
		
		// Normalize the target values
		if (scale.x() >= 0) { targetX = ((trueXZ.x() /          scale.x())  + (0.5f * scale.x())) /          scale.x() ; }
		else                { targetX = ((trueXZ.x() / Math.abs(scale.x())) - (0.5f * scale.x())) / Math.abs(scale.x()); }
		
		if (scale.z() >= 0) { targetZ = ((trueXZ.z() /          scale.z())  + (0.5f * scale.z())) /          scale.z() ; }
		else                { targetZ = ((trueXZ.z() / Math.abs(scale.z())) - (0.5f * scale.z())) / Math.abs(scale.z()); }
		
		switch (body.getQualityLevel()) {
		case 5:
			targetZ += 0.030f;
			break;
		case 6:
			targetZ += 0.020f;
			break;
		case 7:
			targetZ += 0.010f;
			break;
		case 8:
			targetZ += 0.006f;
			break;
		case 9:
			targetZ += 0.004f;
			break;
		case 10:
			targetZ += 0.002f;
			break;
		case 11:
			targetZ += 0.001f;
			break;
		default: // 12+
			break;
		}
		
		// Normalize parameters (and constrain invalid parameters between 0.0 and 1.0)
		while (targetX < 0.0f) {targetX += 1.0f;}
		while (targetX > 1.0f) {targetX -= 1.0f;}
		while (targetZ < 0.0f) {targetZ += 1.0f;}
		while (targetZ > 1.0f) {targetZ -= 1.0f;}
		// If the heightmap is tiled...
		if (tstate.getWrapMode() == WrapMode.REPEAT || tstate.getWrapMode() == WrapMode.REPEAT_MIRRORED) {
			targetX *= this.getHeightMapTilingX();
			targetZ *= this.getHeightMapTilingZ();
			while (targetX < 0.0f) targetX += 1.0f;
			while (targetX > 1.0f) targetX %= 1.0f;
			while (targetZ < 0.0f) targetZ += 1.0f;
			while (targetZ > 1.0f) targetZ %= 1.0f;
		}

		// Obtain the buffered image
		BufferedImage img = map_height.getImage();
		
		// Estimate the closest HeightMap pixel
		float xPixel  = (img.getWidth()  - 1) * targetX;
		float zPixel  = (img.getHeight() - 1) * targetZ;
		int   xPixelT = (int) xPixel;
		int   zPixelT = (int) zPixel;

		float blue = new Color(img.getRGB(xPixelT, zPixelT)).getBlue() / 255.0f;
		return (blue > 0.9f);
	}
	
    @Override
    public float getAverageHeight(float xPercent, float zPercent) {
    	
    	float amount = 0f;
    	
    	if (body.hasHeightMap()) {
    		
    		// Z Hack, since the tessellation shaders are apparently not perfectly centered. Depends on Patch Sizes
    		switch (body.getQualityLevel()) {
    		case 5:
    			zPercent += 0.030f;
    			break;
    		case 6:
    			zPercent += 0.020f;
    			break;
    		case 7:
    			zPercent += 0.010f;
    			break;
    		case 8:
    			zPercent += 0.006f;
    			break;
    		case 9:
    			zPercent += 0.004f;
    			break;
    		case 10:
    			zPercent += 0.002f;
    			break;
    		case 11:
    			zPercent += 0.001f;
    			break;
    		default: // 12+
    			break;
    		}

    		// Normalize parameters (and constrain invalid parameters between 0.0 and 1.0)
    		while (xPercent < 0.0f) {xPercent += 1.0f;}
    		while (xPercent > 1.0f) {xPercent -= 1.0f;}
    		while (zPercent < 0.0f) {zPercent += 1.0f;}
    		while (zPercent > 1.0f) {zPercent -= 1.0f;}
    		// If the heightmap is tiled...
    		if (tstate.getWrapMode() == WrapMode.REPEAT || tstate.getWrapMode() == WrapMode.REPEAT_MIRRORED) {
    			xPercent *= this.getHeightMapTilingX();
    			zPercent *= this.getHeightMapTilingZ();
    			while (xPercent < 0.0f) xPercent += 1.0f;
    			while (xPercent > 1.0f) xPercent %= 1.0f;
    			while (zPercent < 0.0f) zPercent += 1.0f;
    			while (zPercent > 1.0f) zPercent %= 1.0f;
    		}

    		// Obtain the buffered image
    		BufferedImage img = map_height.getImage();
    		
    		// Estimate the closest HeightMap pixel
    		float xPixel  = (img.getWidth()  - 1) * xPercent;
    		float zPixel  = (img.getHeight() - 1) * zPercent;
    		int   xPixelT = (int) xPixel;
    		int   zPixelT = (int) zPixel;

    		// Estimate how exact the position is
    		float xDepth = xPixel % 1.0f;
    		float zDepth = zPixel % 1.0f;
    		
    		// Discover how much weight to put on the targeted pixel
    		float xWeightT = getWeight(xDepth);
    		float zWeightT = getWeight(zDepth);
    		
    		// Discover nearest neighbors
    		int xPixelN;
    		int zPixelN;
    		if      (xDepth <  0.5f)  xPixelN = (int) xPixelT - 1;
    		else if (xDepth == 0.5f)  xPixelN = (int) xPixelT    ;
    		else                      xPixelN = (int) xPixelT + 1;
    		
    		if      (zDepth <  0.5f)  zPixelN = (int) zPixelT - 1;
    		else if (zDepth == 0.5f)  zPixelN = (int) zPixelT    ;
    		else                      zPixelN = (int) zPixelT + 1;
    		
    		// Verify neighboring pixels exist, if they don't, wrap around
    		if      (xPixelN < 0)                xPixelN = img.getWidth()  - 1;
    		else if (xPixelN >= img.getWidth() ) xPixelN = 0;
    		if      (zPixelN < 0)                zPixelN = img.getHeight() - 1;
    		else if (zPixelN >= img.getHeight()) zPixelN = 0;
    		
    		// Average the weighted heights
    		{
    			// Calculate target pixel's height
    			float total0 = new Color(img.getRGB(xPixelT, zPixelT)).getRed() / 255.0f;
    			float total1 = new Color(img.getRGB(xPixelN, zPixelT)).getRed() / 255.0f;
    			float total2 = new Color(img.getRGB(xPixelT, zPixelN)).getRed() / 255.0f;
    			float total3 = new Color(img.getRGB(xPixelN, zPixelN)).getRed() / 255.0f;
    			
    			// Weight the heights of the target and neighboring pixels
    			float weightXZ = (xWeightT + zWeightT) / 2;
    			float avgX     = ((xWeightT * total0) + ((1.0f - xWeightT) * total1));
    			float avgZ     = ((zWeightT * total0) + ((1.0f - zWeightT) * total2));
    			float avgXZ    = ((weightXZ * total0) + ((1.0f - weightXZ) * total3));
    			
    			// Get the final total
    			amount = (avgX + avgZ + avgXZ) / (3.0f);
    		}
    	}
    	
    	return (amount * body.getMultiplier() * this.getParentNode().getWorldScale().y()) + this.getParentNode().getWorldPosition().y();
    }
    
    private float getWeight(float depth) {
    	return 1.0f - Math.abs(depth - 0.5f);
    }
    
    @Override
    public void notifyDispose() {
        super.notifyDispose();
    }
}