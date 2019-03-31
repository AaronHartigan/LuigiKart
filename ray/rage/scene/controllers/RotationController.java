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

package ray.rage.scene.controllers;

import ray.rage.scene.*;
import ray.rage.scene.controllers.AbstractController;
import ray.rage.scene.controllers.RotationController;
import ray.rage.scene.controllers.Throttleable;
import ray.rml.*;

/**
 * A {@link Node.Controller} implementation that makes controlled {@link Node
 * nodes} rotate about an arbitrary local {@link Vector3 axis}.
 *
 * @author Raymond L. Rivera
 *
 */
public class RotationController extends AbstractController implements Throttleable {

    private static final float DEFAULT_ROTATION_SPEED = .1f;

    private float              updateSpeed;
    private Vector3            rotationAxis;

    /**
     * Creates a new {@link RotationController} with the given {@link Vector3
     * axis} and speed.
     *
     * @param axis
     *            The rotation {@link Vector3 axis}.
     * @param rotationSpeed
     *            The speed of rotation.
     * @throws NullPointerException
     *             If the {@link Vector3 axis} is <code>null</code>.
     */
    public RotationController(Vector3 axis, float rotationSpeed) {
        super();
        setSpeed(rotationSpeed);
        setRotationAxis(axis);
    }

    /**
     * Creates a new {@link RotationController} with the given {@link Vector3
     * axis} and default speed.
     *
     * @param axis
     *            The rotation {@link Vector3 axis}.
     * @throws NullPointerException
     *             If the {@link Vector3 axis} is <code>null</code>.
     */
    public RotationController(Vector3 axis) {
        this(axis, DEFAULT_ROTATION_SPEED);
    }

    /**
     * Creates a new {@link RotationController} rotating about a default
     * {@link Vector3 axis} and speed.
     * <p>
     * The default {@link Vector3 axis} is +Y.
     */
    public RotationController() {
        this(Vector3f.createUnitVectorY());
    }

    @Override
    public void setSpeed(float speed) {
        updateSpeed = speed;
    }

    @Override
    public float getSpeed() {
        return updateSpeed;
    }

    /**
     * Sets the rotation {@link Vector3 axis}.
     *
     * @param axis
     *            The {@link Vector3 axis}.
     * @throws NullPointerException
     *             If the {@link Vector3 axis} is <code>null</code>.
     */
    public void setRotationAxis(Vector3 axis) {
        if (axis == null)
            throw new NullPointerException("Null " + Vector3.class.getSimpleName() + " axis");

        rotationAxis = axis;
    }

    /**
     * Gets the rotation {@link Vector3 axis}.
     *
     * @return The rotation {@link Vector3 axis}.
     */
    public Vector3 getRotationAxis() {
        return rotationAxis;
    }

    @Override
    protected void updateImpl(float elapsedTimeMillis) {
        Angle rotationAngle = Degreef.createFrom(updateSpeed * elapsedTimeMillis);
        for (Node n : super.controlledNodesList)
            n.rotate(rotationAngle, rotationAxis);
    }

    @Override
    public void notifyDispose() {
        rotationAxis = null;
        super.notifyDispose();
    }

}
