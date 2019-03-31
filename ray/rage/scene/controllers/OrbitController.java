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
import ray.rage.scene.controllers.OrbitController;
import ray.rage.scene.controllers.Throttleable;
import ray.rml.*;

/**
 * A {@link Node.Controller} implementation that makes controlled {@link Node
 * nodes} orbit another target {@link Node node}.
 *
 * @author Raymond L. Rivera
 *
 */
public class OrbitController extends AbstractController implements Throttleable {

    private final static double ORBITAL_RATE              = 1e3;

    private final static float  DEFAULT_ORBITAL_SPEED     = 1f;
    private final static float  DEFAULT_TARGET_DISTANCE   = 5f;
    private final static float  DEFAULT_VERTICAL_DISTANCE = 0f;

    private float               updateSpeed;
    private Node                target;
    private float               distanceFromTarget;
    private float               verticalDistance;
    private boolean             alwaysFacingTarget;

    /**
     * Creates a new {@link OrbitController controller} to orbit the specified
     * {@link Node node} at the specified speed and distances, with the option
     * to always keep facing the target.
     *
     * @param orbitTarget
     *            The target {@link Node node} to orbit.
     * @param orbitalSpeed
     *            The speed at which to orbit.
     * @param distanceFromTarget
     *            Distance from the target's center.
     * @param verticalDistance
     *            Distance from the orbital plane of the target's center.
     * @param faceTarget
     *            True if controlled {@link Node nodes} should always face the
     *            target. Otherwise false.
     * @throws NullPointerException
     *             If <code>orbitTarget</code> is <code>null</code>.
     * @throws IllegalArgumentException
     *             If <code>distanceFromTarget</code> is not positive.
     */
    public OrbitController(Node orbitTarget, float orbitalSpeed, float distanceFromTarget, float verticalDistance,
            boolean faceTarget) {
        super();
        setSpeed(orbitalSpeed);
        setTarget(orbitTarget);
        setDistanceFromTarget(distanceFromTarget);
        setVerticalDistance(verticalDistance);
        setAlwaysFacingTarget(faceTarget);
    }

    /**
     * Creates a new {@link OrbitController controller} to orbit the specified
     * {@link Node node} at the specified speed and distances.
     * <p>
     * Controlled {@link Node nodes} do <i>not</i> face the target by default.
     *
     * @param orbitTarget
     *            The target {@link Node node} to orbit.
     * @param orbitalSpeed
     *            The speed at which to orbit.
     * @param distanceFromTarget
     *            Distance from the target's center.
     * @param verticalDistance
     *            Distance from the orbital plane of the target's center.
     * @throws NullPointerException
     *             If <code>orbitTarget</code> is <code>null</code>.
     * @throws IllegalArgumentException
     *             If <code>distanceFromTarget</code> is not positive.
     */
    public OrbitController(Node orbitTarget, float orbitalSpeed, float distanceFromTarget, float verticalDistance) {
        this(orbitTarget, orbitalSpeed, distanceFromTarget, verticalDistance, false);
    }

    /**
     * Creates a new {@link OrbitController controller} to orbit the specified
     * {@link Node node} at the specified speed and distances.
     * <p>
     * Controlled {@link Node nodes} orbit the target's orbital plane by
     * default.
     *
     * @param orbitTarget
     *            The target {@link Node node} to orbit.
     * @param orbitalSpeed
     *            The speed at which to orbit.
     * @param distanceFromTarget
     *            Distance from the target's center.
     * @throws NullPointerException
     *             If <code>orbitTarget</code> is <code>null</code>.
     * @throws IllegalArgumentException
     *             If <code>distanceFromTarget</code> is not positive.
     */
    public OrbitController(Node orbitTarget, float orbitalSpeed, float distanceFromTarget) {
        this(orbitTarget, orbitalSpeed, distanceFromTarget, DEFAULT_VERTICAL_DISTANCE);
    }

    /**
     * Creates a new {@link OrbitController controller} to orbit the specified
     * {@link Node node} at the specified speed.
     *
     * @param orbitTarget
     *            The target {@link Node node} to orbit.
     * @param orbitalSpeed
     *            The speed at which to orbit.
     * @throws NullPointerException
     *             If <code>orbitTarget</code> is <code>null</code>.
     */
    public OrbitController(Node orbitTarget, float orbitalSpeed) {
        this(orbitTarget, orbitalSpeed, DEFAULT_TARGET_DISTANCE);
    }

    /**
     * Creates a new {@link OrbitController controller} to orbit the specified
     * {@link Node node}.
     *
     * @param orbitTarget
     *            The target {@link Node node} to orbit.
     * @throws NullPointerException
     *             If <code>orbitTarget</code> is <code>null</code>.
     */
    public OrbitController(Node orbitTarget) {
        this(orbitTarget, DEFAULT_ORBITAL_SPEED);
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
     * Sets the {@link Node node} others should orbit.
     *
     * @param orbitTarget
     *            The {@link Node node} to orbit.
     * @throws NullPointerException
     *             If <code>orbitTarget</code> is <code>null</code>.
     */
    public void setTarget(Node orbitTarget) {
        if (orbitTarget == null)
            throw new NullPointerException("Target is null");

        target = orbitTarget;
    }

    /**
     * Gets the target {@link Node node}.
     *
     * @return The target {@link Node node}.
     */
    public Node getTarget() {
        return target;
    }

    /**
     * Sets the distance the center of controlled {@link Node nodes} should be
     * from the center of the target {@link Node node}.
     * <p>
     * The greater the distance, the larger the orbit.
     *
     * @param distance
     *            The distance, in world-space units.
     * @throws IllegalArgumentException
     *             If the distance is not positive.
     */
    public void setDistanceFromTarget(float distance) {
        if (distance <= 0)
            throw new IllegalArgumentException("Distance from target <= 0");

        distanceFromTarget = distance;
    }

    /**
     * Gets the distance from the target {@link Node node's} center.
     *
     * @return The distance, in world-space units.
     */
    public float getDistanceFromTarget() {
        return distanceFromTarget;
    }

    /**
     * Sets the "altitude" from the controlled {@link Node node's} orbital plane
     * based on its center.
     * <p>
     * This value can be used to make an orbit go "higher" or "lower" relative
     * to the target {@link Node node's} center.
     *
     * @param distance
     *            The distance, in world-space units.
     */
    public void setVerticalDistance(float distance) {
        verticalDistance = distance;
    }

    /**
     * Gets the "altitude" from the controlled {@link Node node's} orbital
     * plane.
     *
     * @return The distance, in world-space units.
     */
    public float getVerticalDistance() {
        return verticalDistance;
    }

    /**
     * Sets whether controlled {@link Node nodes} should always "look-at" their
     * target {@link Node node}.
     *
     * @param enabled
     *            True if controlled {@link Node nodes} should always "look at"
     *            their target. Otherwise false.
     * @see Node#lookAt(Node)
     */
    public void setAlwaysFacingTarget(boolean enabled) {
        alwaysFacingTarget = enabled;
    }

    /**
     * Gets whether controlled {@link Node nodes} always face their target
     * {@link Node node}.
     *
     * @return True if controlled {@link Node nodes} auto-face the target
     *         {@link Node node}. Otherwise false.
     */
    public boolean isAlwaysFacingTarget() {
        return alwaysFacingTarget;
    }

    @Override
    protected void updateImpl(float elapsedTimeMillis) {
        final int nodeCount = super.controlledNodesList.size();

        // delta space is the azimuth/horizontal angle, in radians, between
        // the center position of different controlled nodes along the same
        // orbit, so that they're spaced evenly depending on their order
        // instead of ending up in the same place going through each other
        final float ds = Degreef.createFrom(360.0f / nodeCount).valueRadians();
        final double dt = System.currentTimeMillis() / ORBITAL_RATE * updateSpeed;

        // we want controlled nodes to orbit their target without being turned
        // into the target's children, which would be a "surprise!" to
        // unsuspecting users, so we use the target's world-space position to
        // calculate the controlled node's new relative position
        Vector3 wp = target.getWorldPosition();
        for (int i = 0; i < nodeCount; ++i) {
            // delta values for x/z plane
            final float dx = (float) (Math.sin(dt + ds * i) * distanceFromTarget);
            final float dz = (float) (Math.cos(dt + ds * i) * distanceFromTarget);
            Node n = super.controlledNodesList.get(i);
            n.setLocalPosition(wp.x() + dx, wp.y() + verticalDistance, wp.z() + dz);
            if (alwaysFacingTarget)
                n.lookAt(target);
        }
    }

    @Override
    public void notifyDispose() {
        target = null;
        super.notifyDispose();
    }

}
