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
import ray.rage.scene.controllers.Periodic;
import ray.rage.scene.controllers.Throttleable;
import ray.rage.scene.controllers.TranslationController;
import ray.rml.*;

/**
 * A {@link Node.Controller} implementation that translates controlled
 * {@link Node nodes}.
 * <p>
 * The translation process alternates based on a time limit, called the cycle,
 * at which point, the "direction" of the translation process is reverted. For
 * example, an object being translated in one direction will be translated in
 * the opposite direction when the cycle time limit is reached.
 *
 * @author Raymond L. Rivera
 *
 */
public class TranslationController extends AbstractController implements Throttleable, Periodic {

    private static final float DEFAULT_PERIOD_LENGTH_MSEC = 750f;
    private static final float DEFAULT_TRANSLATION_SPEED  = 3e-3f;

    private float              updateSpeed;
    private Vector3            directionAxis;
    private float              periodLengthMillis;
    private float              totalTimeMillis            = 0;

    /**
     * Creates a new {@link TranslationController} along the specified
     * {@link Vector3 axis}, speed, and cycle time.
     *
     * @param axis
     *            The {@link Vector3 axis} along which translation takes place.
     * @param translationSpeed
     *            The speed of movement.
     * @param periodLengthMillis
     *            Time limit, in milliseconds, before reverting the movement
     *            direction.
     * @throws NullPointerException
     *             If the {@link Vector3 axis} is <code>null</code>.
     * @throws IllegalArgumentException
     *             If the cycle time is not positive.
     */
    public TranslationController(Vector3 axis, float translationSpeed, float periodLengthMillis) {
        super();
        setTranslationAxis(axis);
        setPeriodLengthMillis(periodLengthMillis);
    }

    /**
     * Creates a new {@link TranslationController} along the specified
     * {@link Vector3 axis} and speed.
     * <p>
     * This method relies on a default cycle time.
     *
     * @param axis
     *            The {@link Vector3 axis} along which translation takes place.
     * @param translationSpeed
     *            The speed of movement.
     * @throws NullPointerException
     *             If the {@link Vector3 axis} is <code>null</code>.
     */
    public TranslationController(Vector3 axis, float translationSpeed) {
        this(axis, translationSpeed, DEFAULT_PERIOD_LENGTH_MSEC);
    }

    /**
     * Creates a new {@link TranslationController} along the specified
     * {@link Vector3 axis}.
     *
     * @param axis
     *            The {@link Vector3 axis} along which translation takes place.
     * @throws NullPointerException
     *             If the {@link Vector3 axis} is <code>null</code>.
     */
    public TranslationController(Vector3 axis) {
        this(axis, DEFAULT_TRANSLATION_SPEED);
    }

    /**
     * Creates a new {@link TranslationController} along a default
     * {@link Vector3 axis}.
     */
    public TranslationController() {
        this(Vector3f.createUnitVectorX());
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
     * Sets the {@link Vector3 axis} along which to move.
     *
     * @param axis
     *            The {@link Vector3 axis}.
     * @throws NullPointerException
     *             If the {@link Vector3 axis} is <code>null</code>.
     */
    public void setTranslationAxis(Vector3 axis) {
        if (axis == null)
            throw new NullPointerException("Null " + Vector3.class.getSimpleName() + " axis");

        directionAxis = axis;
    }

    /**
     * Gets the {@link Vector3 axis}.
     *
     * @return The {@link Vector3 axis}.
     */
    public Vector3 getTranslationAxis() {
        return directionAxis;
    }

    @Override
    public void setPeriodLengthMillis(float periodMillis) {
        if (periodMillis <= 0)
            throw new IllegalArgumentException("period length <= 0");

        periodLengthMillis = periodMillis;
    }

    @Override
    public float getPeriodLengthMillis() {
        return periodLengthMillis;
    }

    @Override
    protected void updateImpl(float elapsedTimeMillis) {
        totalTimeMillis += elapsedTimeMillis;
        if (totalTimeMillis > periodLengthMillis) {
            updateSpeed = -updateSpeed;
            totalTimeMillis = 0.0f;
        }

        Vector3 delta = directionAxis.mult(updateSpeed * elapsedTimeMillis);
        for (Node n : super.controlledNodesList)
            n.translate(delta);
    }

    @Override
    public void notifyDispose() {
        directionAxis = null;
        super.notifyDispose();
    }

}
