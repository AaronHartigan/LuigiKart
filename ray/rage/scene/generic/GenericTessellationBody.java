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

import java.nio.*;

import ray.rage.asset.mesh.*;
import ray.rage.scene.*;
import ray.rage.scene.generic.AbstractGenericRenderable;
import ray.rml.*;

/**
 * A generic {@link TessellationBody} implementation.
 *
 * @author Kevin S. Cheong
 *
 */
final class GenericTessellationBody extends AbstractGenericRenderable implements TessellationBody {

    private Tessellation tessellationParent;
    private SubMesh      subMesh;
    
    private float   multiplier;
    private float   subdivisions;

    private int     quality;
    
    private boolean hasTexture;
	private boolean hasHeightMap;
	private boolean hasNormalMap;
	
	private int tileTexX;
	private int tileTexZ;
	private int tileHeightX;
	private int tileHeightZ;
	private int tileNormalX;
	private int tileNormalZ;

    /**
     * Creates a new <i>tessellation body</i> with the
     * given {@link Tessellation parent tessellation}.
     *
     * @param parent
     *            The {@link Tessellation tessellation} that created
     *            <code>this</code>.
     * @param sm
     *            The {@link SubMesh sub-mesh} <code>this</code> uses to become {@Link Renderable renderable}.
     * @throws NullPointerException
     *             If either argument is <code>null</code>.
     */
    GenericTessellationBody(Tessellation parent, SubMesh sm) {
        super();
        if (parent == null)
            throw new NullPointerException("Null " + ManualObject.class.getSimpleName());
        if (sm == null)
            throw new NullPointerException("Null " + SubMesh.class.getSimpleName());

        tessellationParent = parent;
        subMesh = sm;

        tileTexX    = 1;
        tileTexZ    = 1;
        tileHeightX = 1;
        tileHeightZ = 1;
        tileNormalX = 1;
        tileNormalZ = 1;
        
        setDataSource(DataSource.TESS_VERT_BUFFER);
    }

    @Override
    public void setTextureTilingX(int amount) {
    	tileTexX = amount;
    }
    
    @Override
    public void setTextureTilingZ(int amount) {
    	tileTexZ = amount;
    }
    
    @Override
    public void setHeightMapTilingX(int amount) {
    	tileHeightX = amount;
    }
    
    @Override
    public void setHeightMapTilingZ(int amount) {
    	tileHeightZ = amount;
    }
    
    @Override
    public void setNormalMapTilingX(int amount) {
    	tileNormalX = amount;
    }
    
    @Override
    public void setNormalMapTilingZ(int amount) {
    	tileNormalZ = amount;
    }
    
    @Override
    public int getTextureTilingX() {
    	return tileTexX;
    }
    
    @Override
    public int getTextureTilingZ() {
    	return tileTexZ;
    }
    
    @Override
    public int getHeightMapTilingX() {
    	return tileHeightX;
    }
    
    @Override
    public int getHeightMapTilingZ() {
    	return tileHeightZ;
    }
    
    @Override
    public int getNormalMapTilingX() {
    	return tileNormalX;
    }
    
    @Override
    public int getNormalMapTilingZ() {
    	return tileNormalZ;
    }
    
    @Override
    public FloatBuffer getNormalsBuffer() {
        return subMesh.getNormalBuffer();
    }

    @Override
    public IntBuffer getIndexBuffer() {
        return subMesh.getIndexBuffer();
    }

    @Override
    public FloatBuffer getBoneWeightBuffer() {
        return subMesh.getBoneWeightBuffer();
    }

    @Override
    public FloatBuffer getBoneIndexBuffer() {
        return subMesh.getBoneIndexBuffer();
    }

    @Override
    public void setPoseSkinMatrices(Matrix4[] psm) {
        subMesh.setPoseSkinMatrices(psm);
    }

    @Override
    public Matrix4[] getPoseSkinMatrices() {
        return subMesh.getPoseSkinMatrices();
    }

    @Override
    public void setPoseSkinMatricesIT(Matrix3[] psmIT) {
        subMesh.setPoseSkinMatricesIT(psmIT);
    }

    @Override
    public Matrix3[] getPoseSkinMatricesIT() {
        return subMesh.getPoseSkinMatricesIT();
    }

    @Override
    public Matrix4 getWorldTransformMatrix() {
        return tessellationParent.getParentSceneNode().getWorldTransform();
    }

    @Override
    public void notifyDispose() {
    	tessellationParent = null;
        subMesh = null;
        super.notifyDispose();
    }

	@Override
	public FloatBuffer getVertexBuffer() {
		return null;
	}

	@Override
	public FloatBuffer getTextureCoordsBuffer() {
		return null;
	}

	@Override
	public Tessellation getParent() {
		return tessellationParent;
	}

	@Override
	public void setMultiplier(float amount) {
		multiplier = amount;
	}
	
	@Override
	public void setSubdivisions(float amount) {
		subdivisions = amount;
	}
	
	@Override
	public void setHasTexture(boolean bool) {
		hasTexture = bool;
	}

	@Override
	public void setHasHeightMap(boolean bool) {
		hasHeightMap = bool;
	}

	@Override
	public void setHasNormalMap(boolean bool) {
		hasNormalMap = bool;
	}
	
	@Override
	public void setQuality(int amount) {
		if (amount < 5) {
			System.err.println("Patch Count must be greater than or equal to 5!");
			amount = 5;
		}
		else if (amount > 9) {
			System.err.println("WARNING: Quality levels of 10 or higher may cause significant memory usage!");
		}
		quality = amount; // The quality is defined by 2^amount;
	}
	
	@Override
	public boolean hasTexture() {
		return hasTexture;
	}

	@Override
	public boolean hasHeightMap() {
		return hasHeightMap;
	}

	@Override
	public boolean hasNormalMap() {
		return hasNormalMap;
	}

	@Override
	public float getMultiplier() {
		return multiplier;
	}
	
	@Override
	public float getSubdivisions() {
		return subdivisions;
	}
	
	@Override
	public int getQualityLevel() {
		return quality;
	}
	
	@Override
	public int getQualityTotal() {
		return (int) Math.pow(2, quality);
	}
}
