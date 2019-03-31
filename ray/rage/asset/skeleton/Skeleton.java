/**
 * Copyright (C) 2017 Luis Gutierrez <lg24834@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ray.rage.asset.skeleton;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import ray.rage.asset.AbstractAsset;
import ray.rage.asset.mesh.SubMesh;
import ray.rage.asset.skeleton.Skeleton;
import ray.rage.asset.skeleton.SkeletonManager;
import ray.rage.util.BufferUtil;
import ray.rml.*;

/**
 * A <i>Skeleton</i> describes the hierarchy and structure of any number of bones.
 * These bones are to be used to modify the vertices of {@link SubMesh submesh} for skeletal animation.
 *
 * Within the {@link SubMesh submesh}, each vertex has up to 3 bone parents and corresponding weights that determine
 * the influence that up to 3 bones have on said vertex.
 * This is kept track through the vertex having 3 bone indices that directly correlate to data stored in various
 * buffers within the <i>Skeleton</i> class.
 *
 * A vertex can be influenced by no bones, or up to 3 bones.
 *
 * @author Luis Gutierrez
 *
 */
public final class Skeleton extends AbstractAsset {

    Skeleton(SkeletonManager sm, String name) {
        super(sm, name);
        this.name = name;
    }

    // The Skeleton's name
    private String name;
    // The Skeleton's bone count
    private int boneCount;
    // The Skeleton's bone names
    private String[] boneNames;
    // The bone's length (distance between bone head and bone tail)
    private FloatBuffer boneLengthsBuffer;
    // The rest rotation of each bone(a list of quaternions)
    private FloatBuffer boneRestRotationsBuffer;
    // The rest position of each bone ( a list of 3D vectors, relative parent bone tail)
    private FloatBuffer boneRestLocationsBuffer;
    // The index of a bone's parent bone
    private IntBuffer boneParentsBuffer;

    /**
     * {@inheritDoc}
     *
     * In this case, the name of <code>this</code> {@link Skeleton skeleton}.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the number of bones that this Skeleton has.
     *
     * @param boneCount
     *            The number of bones that this skeleton has.
     *
     */
    public void setBoneCount(int boneCount) {
        this.boneCount = boneCount;
    }
    /**
     * Gets the number of bones <code>this</code> {@link Skeleton skeleton} has.
     *
     * @return The number of bones.
     */
    public int getBoneCount() {
        return boneCount;
    }

    /**
     * Sets the {@link FloatBuffer} with vertex data.
     *
     * @param bn
     *            The array of bone names.
     * @throws IllegalArgumentException
     *             If the buffer is not direct.
     * @throws NullPointerException
     *             If the array is <code>null</code>.
     */
    /**
     * Gets the buffer holding vertex data for <code>this</code> {@link SubMesh
     * sub-mesh}, if any.
     *
     * @return The vertex buffer, if set. Otherwise <code>null</code>.
     */
    public void setBoneNames(String[] bn) {
        boneNames = bn;
    }

    /**
     * Gets the array holding bone names data for <code>this</code> {@link Skeleton
     * skeleton}, if any.
     *
     * @return The bone names array, if set. Otherwise <code>null</code>.
     */

    public String[] getBoneNames() {
        return boneNames;
    }

    /**
     * Sets the {@link FloatBuffer} with bone length data.
     *
     * @param bl
     *            The buffer of bone lengths.
     * @throws IllegalArgumentException
     *             If the buffer is not direct.
     * @throws NullPointerException
     *             If the buffer is <code>null</code>.
     */
    public void setBoneLengthsBuffer(FloatBuffer bl) {
        if (!bl.isDirect())
            throw new IllegalArgumentException("Buffer is not direct. Use " + BufferUtil.class.getName());

        boneLengthsBuffer = bl;
    }

    /**
     * Gets the buffer holding bone length data for <code>this</code> {@link Skeleton
     * skeleton}, if any.
     *
     * @return The bone lengths buffer, if set. Otherwise <code>null</code>.
     */
    public FloatBuffer getBoneLengthsBuffer() {
        if (boneLengthsBuffer != null)
            return boneLengthsBuffer.duplicate();

        return null;
    }

    /**
     * Sets the {@link FloatBuffer} with bone rest rotation data. (This is a list of quaternions)
     *
     * @param brr
     *            The buffer of bone rest rotation data
     * @throws IllegalArgumentException
     *             If the buffer is not direct.
     * @throws NullPointerException
     *             If the buffer is <code>null</code>.
     */
    public void setBoneRestRotationsBuffer(FloatBuffer brr) {
        if (!brr.isDirect())
            throw new IllegalArgumentException("Buffer is not direct. Use " + BufferUtil.class.getName());

        boneRestRotationsBuffer = brr;
    }

    /**
     * Gets the buffer holding bone rest rotation data for <code>this</code> {@link Skeleton
     * skeleton}, if any.
     *
     * @return The bone rest rotations buffer, if set. Otherwise <code>null</code>.
     */
    public FloatBuffer getBoneRestRotationsBuffer() {
        if (boneRestRotationsBuffer != null)
            return boneRestRotationsBuffer.duplicate();

        return null;
    }

    /**
     * Sets the {@link FloatBuffer} with bone rest location data. (This is a list of 3D Vectors)
     *
     * @param brl
     *            The buffer of bone rest location data
     * @throws IllegalArgumentException
     *             If the buffer is not direct.
     * @throws NullPointerException
     *             If the buffer is <code>null</code>.
     */
    public void setBoneRestLocationsBuffer(FloatBuffer brl) {
        if (!brl.isDirect())
            throw new IllegalArgumentException("Buffer is not direct. Use " + BufferUtil.class.getName());

        boneRestLocationsBuffer = brl;
    }

    /**
     * Gets the buffer holding bone rest locations data for <code>this</code> {@link Skeleton
     * skeleton}, if any.
     *
     * @return The bone rest locations buffer, if set. Otherwise <code>null</code>.
     */
    public FloatBuffer getBoneRestLocationsBuffer() {
        if (boneRestLocationsBuffer != null)
            return boneRestLocationsBuffer.duplicate();

        return null;
    }

    /**
     * Sets the {@link IntBuffer} with bone parent data. (This is a list of indices to the parent bone)
     *
     * @param bp
     *            The buffer of parent bone indices
     * @throws IllegalArgumentException
     *             If the buffer is not direct.
     * @throws NullPointerException
     *             If the buffer is <code>null</code>.
     */
    public void setBoneParentsBuffer(IntBuffer bp) {
        if (!bp.isDirect())
            throw new IllegalArgumentException("Buffer is not direct. Use " + BufferUtil.class.getName());

        boneParentsBuffer = bp;
    }

    /**
     * Gets the buffer holding bone parent index data for <code>this</code> {@link Skeleton
     * skeleton}, if any.
     *
     * @return The bone parent index buffer, if set. Otherwise <code>null</code>.
     */
    public IntBuffer getBoneParentsBuffer() {
        if (boneParentsBuffer != null)
            return boneParentsBuffer.duplicate();

        return null;
    }


    /**
     * Returns the rest rotation {@link Quaternion quaternion} of the bone with the index boneIndex in <code>this</code>
     * {@link Skeleton skeleton}'s data.
     *
     * @throws IndexOutOfBoundsException
     *                 If the boneIndex is not valid given boneCount
     *
     * @return The bone rest rotation Quaternion.
     */
    public Quaternion getBoneRestRot(int boneIndex) {
        if(boneIndex < 0 || boneIndex > boneCount)
            throw new IndexOutOfBoundsException("Attempted to get rest rot of out of bounds bone " + boneIndex + ".");

        float w = boneRestRotationsBuffer.get(4*boneIndex);
        float x = boneRestRotationsBuffer.get(4*boneIndex + 1);
        float y = boneRestRotationsBuffer.get(4*boneIndex + 2);
        float z = boneRestRotationsBuffer.get(4*boneIndex + 3);
        return Quaternionf.createFrom( w,x,y,z );
    }

    /**
     * Returns the rest location {@link Vector3 vector3} of the bone with the index boneIndex in <code>this</code>
     * {@link Skeleton skeleton}'s data.
     *
     * @throws IndexOutOfBoundsException
     *                 If the boneIndex is not valid given boneCount
     *
     * @return The bone rest location Vector3.
     */
    public Vector3 getBoneRestLoc(int boneIndex) {
        if(boneIndex < 0 || boneIndex > boneCount)
            throw new IndexOutOfBoundsException("Attempted to get rest loc of out of bounds bone " + boneIndex + ".");

        float x = boneRestLocationsBuffer.get(3*boneIndex);
        float y = boneRestLocationsBuffer.get(3*boneIndex + 1);
        float z = boneRestLocationsBuffer.get(3*boneIndex + 2);
        return Vector3f.createFrom(x,y,z);
    }

    /**
     * Returns the bone length of the bone with the index boneIndex in <code>this</code>
     * {@link Skeleton skeleton}'s data.
     *
     * @throws IndexOutOfBoundsException
     *                 If the boneIndex is not valid given boneCount
     *
     * @return The bone length.
     */
    public float getBoneLength(int boneIndex) {
        if(boneIndex < 0 || boneIndex > boneCount)
            throw new IndexOutOfBoundsException("Attempted to get bone length of out of bounds bone " + boneIndex + ".");

        return boneLengthsBuffer.get(boneIndex);
    }

    /**
     * Returns the parent bone index of the bone with the index boneIndex in <code>this</code>
     * {@link Skeleton skeleton}'s data.
     *
     * @throws IndexOutOfBoundsException
     *                 If the boneIndex is not valid given boneCount
     *
     * @return The bone's parent's index if set, -1 otherwise.
     */
    public int getBoneParentIndex(int boneIndex) {
        if(boneIndex < 0 || boneIndex > boneCount)
            throw new IndexOutOfBoundsException("Attempted to get bone length of out of bounds bone " + boneIndex + ".");

        return boneParentsBuffer.get(boneIndex);
    }


    @Override
    public void notifyDispose() {
        if (boneLengthsBuffer != null)
            boneLengthsBuffer.clear();
        if (boneRestRotationsBuffer != null)
            boneRestRotationsBuffer.clear();
        if (boneRestLocationsBuffer != null)
            boneRestLocationsBuffer.clear();
        if (boneParentsBuffer != null)
            boneParentsBuffer.clear();

        boneCount = 0;
        boneLengthsBuffer = null;
        boneRestRotationsBuffer = null;
        boneRestLocationsBuffer = null;
        boneParentsBuffer = null;
        boneNames = null;
    }
}
