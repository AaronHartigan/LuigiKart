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
 * A generic {@link ManualObjectSection} implementation.
 *
 * @author Raymond L. Rivera
 *
 */
final class GenericManualObjectSection extends AbstractGenericRenderable implements ManualObjectSection {

    private ManualObject manualParent;
    private SubMesh      subMesh;

    private Matrix4[] poseSkinMatrices;
    private Matrix3[] poseSkinMatricesIT;

    /**
     * Creates a new {@link ManualObjectSection manual-object-section} with the
     * given {@link ManualObject parent} and {@link SubMesh sub-mesh}.
     *
     * @param parent
     *            The {@link ManualObject parent} that created
     *            <code>this</code>.
     * @param sm
     *            The {@link SubMesh sub-mesh} <code>this</code> is based on.
     * @throws NullPointerException
     *             If either argument is <code>null</code>.
     */
    GenericManualObjectSection(ManualObject parent, SubMesh sm) {
        super();
        if (parent == null)
            throw new NullPointerException("Null " + ManualObject.class.getSimpleName());
        if (sm == null)
            throw new NullPointerException("Null " + SubMesh.class.getSimpleName());

        manualParent = parent;
        subMesh = sm;

        setDataSource(DataSource.VERTEX_BUFFER);
    }

    @Override
    public ManualObject getParent() {
        return manualParent;
    }

    @Override
    public void setVertexBuffer(FloatBuffer vertices) {
        subMesh.setVertexBuffer(vertices);
    }

    @Override
    public FloatBuffer getVertexBuffer() {
        return subMesh.getVertexBuffer();
    }

    @Override
    public void setTextureCoordsBuffer(FloatBuffer texcoords) {
        subMesh.setTextureCoordBuffer(texcoords);
    }

    @Override
    public FloatBuffer getTextureCoordsBuffer() {
        return subMesh.getTextureCoordBuffer();
    }

    @Override
    public void setNormalsBuffer(FloatBuffer normals) {
        subMesh.setNormalBuffer(normals);
    }

    @Override
    public FloatBuffer getNormalsBuffer() {
        return subMesh.getNormalBuffer();
    }

    @Override
    public void setIndexBuffer(IntBuffer indices) {
        subMesh.setIndexBuffer(indices);
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
    public Matrix4 getWorldTransformMatrix() {
        return manualParent.getParentSceneNode().getWorldTransform();
    }

    @Override
    public void notifyDispose() {
        manualParent = null;
        subMesh = null;
        poseSkinMatrices = null;
        poseSkinMatricesIT = null;
        super.notifyDispose();
    }
}
