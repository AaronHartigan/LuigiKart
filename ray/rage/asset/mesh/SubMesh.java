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

package ray.rage.asset.mesh;

import java.nio.*;

import ray.rage.asset.material.*;
import ray.rage.asset.mesh.Mesh;
import ray.rage.asset.mesh.SubMesh;
import ray.rage.common.*;
import ray.rage.scene.*;
import ray.rage.util.*;
import ray.rml.*;

/**
 * A <i>sub-mesh</i> defines a part of a {@link Mesh mesh}.
 * <p>
 * Sub-Meshes exist because a single {@link Mesh mesh} may be using multiple
 * {@link Material materials} and each sub-mesh can have its own {@link Material
 * material} file name specified.
 * <p>
 * Sub-Meshes have a 1:1 relationship with {@link SubEntity sub-entities} and
 * provide the actual data that allow {@link SubEntity sub-entities} to be
 * rendered.
 *
 * @author Raymond L. Rivera
 *
 * @see Mesh
 * @see SubEntity
 */
public final class SubMesh implements Nameable, Disposable {

    private Mesh        parentMesh;
    private String      name;
    private String      materialFilename;

    private FloatBuffer vertexBuffer;
    private FloatBuffer normalBuffer;
    private FloatBuffer texCoordBuffer;
    private IntBuffer   indexBuffer;

    //Used for Skeletal Animations
    private FloatBuffer boneIndexBuffer;
    private FloatBuffer boneWeightBuffer;
    // Used for compatability checking
    private int boneCount = 0;
    
    private Matrix4[] poseSkinMatrices;
    private Matrix3[] poseSkinMatricesIT;

    SubMesh(Mesh m, String n) {
        if (m == null)
            throw new NullPointerException("Parent mesh is null");
        if (n.isEmpty())
            throw new IllegalArgumentException("Name is empty");

        parentMesh = m;
        name = n;
    }

    /**
     * Gets the {@link Mesh mesh} <code>this</code> {@link SubMesh sub-mesh} is
     * a part of.
     *
     * @return The parent {@link Mesh mesh} of <code>this</code> {@link SubMesh
     *         sub-mesh}.
     */
    public Mesh getParent() {
        return parentMesh;
    }

    /**
     * {@inheritDoc}
     *
     * In this case, the name of <code>this</code> {@link SubMesh sub-mesh}.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the {@link FloatBuffer} with vertex data.
     *
     * @param vb
     *            The vertex buffer.
     * @throws IllegalArgumentException
     *             If the buffer is not direct.
     * @throws NullPointerException
     *             If the buffer is <code>null</code>.
     */
    public void setVertexBuffer(FloatBuffer vb) {
        if (!vb.isDirect())
            throw new IllegalArgumentException("Buffer is not direct. Use " + BufferUtil.class.getName());

        vertexBuffer = vb;
    }

    /**
     * Gets the buffer holding vertex data for <code>this</code> {@link SubMesh
     * sub-mesh}, if any.
     *
     * @return The vertex buffer, if set. Otherwise <code>null</code>.
     */
    public FloatBuffer getVertexBuffer() {
        if (vertexBuffer != null)
            return vertexBuffer.duplicate();

        return null;
    }

    /**
     * Sets the {@link FloatBuffer} with normal vector data.
     *
     * @param vb
     *            The normals buffer.
     * @throws IllegalArgumentException
     *             If the buffer is not direct.
     * @throws NullPointerException
     *             If the buffer is <code>null</code>.
     */
    public void setNormalBuffer(FloatBuffer nb) {
        if (!nb.isDirect())
            throw new IllegalArgumentException("Buffer is not direct. Use " + BufferUtil.class.getName());

        normalBuffer = nb;
    }

    /**
     * Gets the buffer holding normal data for <code>this</code> {@link SubMesh
     * sub-mesh}, if any.
     *
     * @return The normals buffer, if set. Otherwise <code>null</code>.
     */
    public FloatBuffer getNormalBuffer() {
        if (normalBuffer != null)
            return normalBuffer.duplicate();

        return null;
    }

    /**
     * Sets the {@link FloatBuffer} with texture coordinate data.
     *
     * @param vb
     *            The texture coordinates buffer.
     * @throws IllegalArgumentException
     *             If the buffer is not direct.
     * @throws NullPointerException
     *             If the buffer is <code>null</code>.
     */
    public void setTextureCoordBuffer(FloatBuffer tb) {
        if (!tb.isDirect())
            throw new IllegalArgumentException("Buffer is not direct. Use " + BufferUtil.class.getName());

        texCoordBuffer = tb;
    }

    /**
     * Gets the buffer holding texture coordinate data for <code>this</code>
     * {@link SubMesh sub-mesh}, if any.
     *
     * @return The texture coordinates buffer, if set. Otherwise
     *         <code>null</code>.
     */
    public FloatBuffer getTextureCoordBuffer() {
        if (texCoordBuffer != null)
            return texCoordBuffer.duplicate();

        return null;
    }

    /**
     * Sets the {@link IntBuffer} with vertex index data.
     *
     * @param vb
     *            The vertex index buffer.
     * @throws IllegalArgumentException
     *             If the buffer is not direct.
     * @throws NullPointerException
     *             If the buffer is <code>null</code>.
     */
    public void setIndexBuffer(IntBuffer ib) {
        if (!ib.isDirect())
            throw new IllegalArgumentException("Buffer is not direct. Use " + BufferUtil.class.getName());

        indexBuffer = ib;
    }

    /**
     * Gets the buffer holding vertex index data for <code>this</code>
     * {@link SubMesh sub-mesh}, if any.
     *
     * @return The vertex index buffer, if set. Otherwise <code>null</code>.
     */
    public IntBuffer getIndexBuffer() {
        if (indexBuffer != null)
            return indexBuffer.duplicate();

        return null;
    }

    /**
    * Sets the boneCount that identifies how many bones a {@link ray.rage.asset.skeleton.Skeleton skeleton} must have to be compatible
    * to deform this mesh.
    *
    * @param boneCount
    *            The boneCount that this mesh requires to be deformed.
    */
    public void setBoneCount(int boneCount) {
        this.boneCount = boneCount;
    }

    /**
    * Gets the bone count that <code>this</code> {@link SubMesh} requires for Skeletal transformations.
    *
    * @return The bone count.
    */
    public int getBoneCount() {
        return boneCount;
    }

    /**
    * Sets the {@link FloatBuffer} with vertex bone weight data.
    *
    * @param bw
    *            The vertex bone weights buffer.
    * @throws IllegalArgumentException
    *             If the buffer is not direct.
    * @throws NullPointerException
    *             If the buffer is <code>null</code>.
    */
    public void setBoneWeightBuffer(FloatBuffer bw) {
        if (!bw.isDirect())
            throw new IllegalArgumentException("Buffer is not direct. Use " + BufferUtil.class.getName());

        boneWeightBuffer = bw;
    }

    /**
    * Gets the buffer holding vertex bone weight data for <code>this</code> {@link SubMesh
    * sub-mesh}, if any.
    *
    * @return The vertex bone weights buffer, if set. Otherwise <code>null</code>.
    */
    public FloatBuffer getBoneWeightBuffer() {
        if (boneWeightBuffer != null)
            return boneWeightBuffer.duplicate();

        return null;
    }

    /**
    * Sets the {@link IntBuffer} with bone index data.
    *
    * @param bib
    *            The bone index buffer.
    * @throws IllegalArgumentException
    *             If the buffer is not direct.
    * @throws NullPointerException
    *             If the buffer is <code>null</code>.
    */
    public void setBoneIndexBuffer(FloatBuffer bib) {
        if (!bib.isDirect())
            throw new IllegalArgumentException("Buffer is not direct. Use " + BufferUtil.class.getName());

        boneIndexBuffer = bib;
    }

    /**
    * Gets the buffer holding bone index data for <code>this</code>
    * {@link SubMesh sub-mesh}, if any.
    *
    * @return The bone index buffer, if set. Otherwise <code>null</code>.
    */
    public FloatBuffer getBoneIndexBuffer() {
        if (boneIndexBuffer != null)
            return boneIndexBuffer.duplicate();

        return null;
    }

    /**
     * Sets the file name of the {@link Material material} that should be used
     * for <code>this</code> {@link SubMesh sub-mesh}.
     *
     * @param name
     *            Name of the {@link Material material} file to use.
     * @throws IllegalArgumentException
     *             If the name is empty.
     * @throws NullPointerException
     *             If the buffer is <code>null</code>.
     */
    public void setMaterialFilename(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("Empty material filename");

        materialFilename = name;
    }

    /**
     * Gets the file name of the {@link Material material} specified for
     * <code>this</code> {@link SubMesh sub-mesh} for loading, if any.
     *
     * @return The file name of the {@link Material material} to be loaded, if
     *         set. Otherwise <code>null</code>.
     */
    public String getMaterialFilename() {
        return materialFilename;
    }
    
    /**
     * Sets the pose Skinning Matrices that should be used
     * for <code>this</code> {@link SubMesh sub-mesh}.
     *
     * @param psm
     *            Array of Matrix4 to use.
     * @throws NullPointerException
     *             If the array is <code>null</code>.
     */
    public void setPoseSkinMatrices(Matrix4[] psm) {
        if(psm == null)
            throw new NullPointerException("Null pose skin matrices array.");

        poseSkinMatrices = psm;
    }

    /**
     * Gets the pose Skin Matrices Array specified for
     * <code>this</code> {@link SubMesh sub-mesh}, if any.
     *
     * @return The array of skinning matrices, if
     *         set. Otherwise <code>null</code>.
     */
    public Matrix4[] getPoseSkinMatrices() {
        return poseSkinMatrices;
    }

    /**
     * Sets the inverse-transpose pose Skinning Matrices that should be used
     * for <code>this</code> {@link SubMesh sub-mesh}.
     *
     * @param psmIT
     *            Array of Matrix3 to use.
     * @throws NullPointerException
     *             If the array is <code>null</code>.
     */
    public void setPoseSkinMatricesIT(Matrix3[] psmIT) {
        if(psmIT == null)
            throw new NullPointerException("Null pose skin matrices array.");

        poseSkinMatricesIT = psmIT;
    }

    /**
     * Gets the pose inverse-transpose Skin Matrices Array specified for
     * <code>this</code> {@link SubMesh sub-mesh}, if any.
     *
     * @return The array of IT normal vector skinning matrices, if
     *         set. Otherwise <code>null</code>.
     */
    public Matrix3[] getPoseSkinMatricesIT() {
        return poseSkinMatricesIT;
    }

    @Override
    public void notifyDispose() {
        if (vertexBuffer != null)
            vertexBuffer.clear();
        if (normalBuffer != null)
            normalBuffer.clear();
        if (texCoordBuffer != null)
            texCoordBuffer.clear();
        if (indexBuffer != null)
            indexBuffer.clear();
        if (boneIndexBuffer != null)
            boneIndexBuffer.clear();
        if (boneWeightBuffer != null)
            boneWeightBuffer.clear();

        vertexBuffer = null;
        normalBuffer = null;
        texCoordBuffer = null;
        indexBuffer = null;
        boneIndexBuffer = null;
        boneWeightBuffer = null;
        materialFilename = null;
    }

}
