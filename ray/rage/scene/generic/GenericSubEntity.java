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
 * A generic {@link SubEntity} implementation.
 *
 * @author Raymond L. Rivera
 *
 */
final class GenericSubEntity extends AbstractGenericRenderable implements SubEntity {

    private Entity  parentEntity;
    private SubMesh subMesh;
    
    // The matrices defining the current pose of this Renderable SubEntity
    private Matrix4[] poseSkinMatrices;
    private Matrix3[] poseSkinMatricesIT;

    /**
     * Creates a new {@link SubEntity sub-entity} with the given {@link Entity
     * entity} and {@link SubMesh sub-mesh}.
     *
     * @param ent
     *            The {@link Entity parent} that created <code>this</code>.
     * @param sm
     *            The {@link SubMesh sub-mesh} <code>this</code> is based on.
     * @throws NullPointerException
     *             If either argument is <code>null</code>.
     */
    GenericSubEntity(Entity ent, SubMesh sm) {
        super();
        if (ent == null)
            throw new NullPointerException("Null " + Entity.class.getSimpleName());
        if (sm == null)
            throw new NullPointerException("Null " + SubMesh.class.getSimpleName());

        parentEntity = ent;
        subMesh = sm;
    }

    public Entity getParent() {
        return parentEntity;
    }

    public SubMesh getSubMesh() {
        return subMesh;
    }

    @Override
    public FloatBuffer getVertexBuffer() {
        return subMesh.getVertexBuffer();
    }

    @Override
    public FloatBuffer getTextureCoordsBuffer() {
        return subMesh.getTextureCoordBuffer();
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
    public Matrix4 getWorldTransformMatrix() {
        return parentEntity.getParentSceneNode().getWorldTransform();
    }

    @Override
    public FloatBuffer getBoneWeightBuffer() {
    	return subMesh.getBoneWeightBuffer();
    }

    @Override
    public FloatBuffer getBoneIndexBuffer() {
    	return subMesh.getBoneIndexBuffer();
    }

    public void setPoseSkinMatrices(Matrix4[] psm) {
        if(psm == null)
            throw new NullPointerException("Null pose skin matrices array.");

        poseSkinMatrices = psm;
    }

    public Matrix4[] getPoseSkinMatrices() {
        return poseSkinMatrices;
    }

    public void setPoseSkinMatricesIT(Matrix3[] psmIT) {
        if(psmIT == null)
            throw new NullPointerException("Null pose skin matrices array.");

        poseSkinMatricesIT = psmIT;
    }

    public Matrix3[] getPoseSkinMatricesIT() {
        return poseSkinMatricesIT;
    }

    @Override
    public void notifyDispose() {
        parentEntity = null;

        // sub-meshes are owned by meshes, so sub-entities must NOT dispose of
        // them explicitly; nulls are enough
        subMesh = null;
        
        poseSkinMatrices = null;
        poseSkinMatricesIT = null;

        super.notifyDispose();
    }

}
