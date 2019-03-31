/**
 * Copyright (C) 2017 Raymond L. Rivera <ray.l.rivera@gmail.com>
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

package ray.rage.math;

import ray.rage.math.Transform;
import ray.rml.*;

/**
 * A <i>transform</i> represents a translation, rotation, and scale as a single
 * object.
 *
 * @author Raymond L. Rivera
 *
 */
public class Transform {

    private Vector3 position;
    private Matrix3 rotation;
    private Vector3 scale;

    private Matrix4 cachedXform = Matrix4f.createIdentityMatrix();
    private boolean needsUpdate = true;

    private Transform(Vector3 position, Matrix3 rotation, Vector3 scale) {
        if (position == null)
            throw new NullPointerException("Null position " + Vector3.class.getSimpleName());
        if (rotation == null)
            throw new NullPointerException("Null rotation " + Matrix3.class.getSimpleName());
        if (scale == null)
            throw new NullPointerException("Null scale " + Vector3.class.getSimpleName());

        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
    }

    /**
     * Creates a new {@link Transform transform} based on the given
     * {@link Transform transform}.
     *
     * @param xform
     *            The source {@link Transform transform}.
     * @return A new {@link Transform transform}.
     * @throws NullPointerException
     *             If the argument is <code>null</code>.
     */
    public static Transform createFrom(Transform xform) {
        return createFrom(xform.position, xform.rotation, xform.scale);
    }

    /**
     * Creates a new {@link Transform transform} with the given position,
     * rotation, and scaling.
     *
     * @param position
     *            The position {@link Vector3 vector}.
     * @param rotation
     *            The rotation {@link Matrix3 matrix}.
     * @param scale
     *            The scaling {@link Vector3 vector}.
     * @return A new {@link Transform transform}.
     * @throws NullPointerException
     *             If any of the arguments is <code>null</code>.
     */
    public static Transform createFrom(Vector3 position, Matrix3 rotation, Vector3 scale) {
        return new Transform(position, rotation, scale);
    }

    /**
     * Creates a new {@link Transform transform} with the given position and
     * rotation, but no scaling.
     *
     * @param position
     *            The position {@link Vector3 vector}.
     * @param rotation
     *            The rotation {@link Matrix3 matrix}.
     * @return A new {@link Transform transform}.
     * @throws NullPointerException
     *             If any of the arguments is <code>null</code>.
     */
    public static Transform createFrom(Vector3 position, Matrix3 rotation) {
        return createFrom(position, rotation, Vector3f.createFrom(1, 1, 1));
    }

    /**
     * Creates a new {@link Transform transform} with the given position, no
     * rotation, and no scaling.
     *
     * @param position
     *            The position {@link Vector3 vector}.
     * @return A new {@link Transform transform}.
     * @throws NullPointerException
     *             If the argument is <code>null</code>.
     */
    public static Transform createFrom(Vector3 position) {
        return createFrom(position, Matrix3f.createIdentityMatrix());
    }

    /**
     * Creates a new {@link Transform transform} with its position at the
     * origin, no rotation, and no scaling.
     *
     * @return A new {@link Transform transform}.
     */
    public static Transform createDefault() {
        return createFrom(Vector3f.createZeroVector());
    }

    /**
     * Sets the position {@link Vector3 vector} of <code>this</code>
     * {@link Transform transform}.
     *
     * @param position
     *            The position {@link Vector3 vector}.
     * @return This {@link Transform transform} for method chaining.
     * @throws NullPointerException
     *             If the argument is <code>null</code>.
     */
    public Transform setPosition(Vector3 position) {
        if (position == null)
            throw new NullPointerException("Null position " + Vector3.class.getSimpleName());

        this.position = position;
        needsUpdate = true;

        return this;
    }

    /**
     * Sets the position of <code>this</code> {@link Transform transform} using
     * primitive types.
     *
     * @param x
     *            The position along the <code>x</code> axis.
     * @param y
     *            The position along the <code>y</code> axis.
     * @param z
     *            The position along the <code>z</code> axis.
     * @return This {@link Transform transform} for method chaining.
     */
    public Transform setPosition(float x, float y, float z) {
        return setPosition(Vector3f.createFrom(x, y, z));
    }

    /**
     * Gets the position {@link Vector3 vector} of <code>this</code>
     * {@link Transform transform}.
     *
     * @return The position {@link Vector3 vector}.
     */
    public Vector3 position() {
        return position;
    }

    /**
     * Sets the rotation {@link Matrix3 matrix} of <code>this</code>
     * {@link Transform transform}.
     *
     * @param rotation
     *            The position {@link Matrix3 matrix}.
     * @return This {@link Transform transform} for method chaining.
     * @throws NullPointerException
     *             If the argument is <code>null</code>.
     */
    public Transform setRotation(Matrix3 rotation) {
        if (rotation == null)
            throw new NullPointerException("Null rotation " + Matrix3.class.getSimpleName());

        this.rotation = rotation;
        needsUpdate = true;

        return this;
    }

    /**
     * Gets the rotation {@link Matrix3 matrix} of <code>this</code>
     * {@link Transform transform}.
     *
     * @return The rotation {@link Matrix3 matrix}.
     */
    public Matrix3 rotation() {
        return rotation;
    }

    /**
     * Sets the scaling {@link Vector3 vector} of <code>this</code>
     * {@link Transform transform}.
     *
     * @param position
     *            The scaling {@link Vector3 vector}.
     * @return This {@link Transform transform} for method chaining.
     * @throws NullPointerException
     *             If the argument is <code>null</code>.
     */
    public Transform setScale(Vector3 scale) {
        if (scale == null)
            throw new NullPointerException("Null scale " + Vector3.class.getSimpleName());

        this.scale = scale;
        needsUpdate = true;

        return this;
    }

    /**
     * Sets the scaling of <code>this</code> {@link Transform transform} using
     * primitive types.
     *
     * @param x
     *            The scaling along the <code>x</code> axis.
     * @param y
     *            The scaling along the <code>y</code> axis.
     * @param z
     *            The scaling along the <code>z</code> axis.
     * @return This {@link Transform transform} for method chaining.
     */
    public Transform setScale(float x, float y, float z) {
        return setScale(Vector3f.createFrom(x, y, z));
    }

    /**
     * Gets the scaling {@link Vector3 vector} of <code>this</code>
     * {@link Transform transform}.
     *
     * @return The scaling {@link Vector3 vector}.
     */
    public Vector3 scale() {
        return scale;
    }

    /**
     * Converts <code>this</code> {@link Transform transform} to a
     * {@link Matrix4 matrix} that combines <b>T</b>ranslation, <b>R</b>otation,
     * and <b>S</b>caling, in that order.
     *
     * @return A {@link Matrix4 matrix} representation of <code>this</code>
     *         {@link Transform transform}.
     */
    public Matrix4 toMatrixTRS() {
        if (needsUpdate) {
            Matrix4 t = Matrix4f.createTranslationFrom(position);
            Matrix4 r = Matrix4f.createFrom(rotation);
            Matrix4 s = Matrix4f.createScalingFrom(scale);
            cachedXform = t.mult(r).mult(s);
            needsUpdate = false;
        }
        return cachedXform;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 3;
        result = prime * result + position.hashCode();
        result = prime * result + rotation.hashCode();
        result = prime * result + scale.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Transform))
            return false;

        Transform other = (Transform) obj;
        if (!position.equals(other.position))
            return false;
        if (!rotation.equals(other.rotation))
            return false;
        if (!scale.equals(other.scale))
            return false;

        return true;
    }

}
