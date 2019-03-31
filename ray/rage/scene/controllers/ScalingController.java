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
import ray.rage.scene.controllers.ScalingController;
import ray.rage.scene.controllers.Throttleable;

/**
 * A {@link Node.Controller} implementation that scales controlled {@link Node
 * nodes}.
 * <p>
 * The scaling process alternates based on a time limit, called the cycle, at
 * which point, the "direction" of the scaling process is reverted. In other
 * words, an object being scaled "up" (i.e. getting progressively larger) will
 * be scaled "down" (i.e. get progressively smaller) until the cycle time limit
 * is reached again.
 *
 * @author Raymond L. Rivera
 *
 */
public class ScalingController extends AbstractController implements Throttleable, Periodic {

    private final static float DEFAULT_SCALING_SPEED      = .0005f;
    private final static float DEFAULT_PERIOD_LENGTH_MSEC = 750f;

    private float              updateSpeed;
    private float              periodLengthMillis;
    private float              totalTimeMillis            = 0;

    /**
     * Creates a new {@link ScalingController} with the given scaling speed and
     * cycle time.
     *
     * @param scalingSpeed
     *            How quickly scaling should be.
     * @param periodLengthMillis
     *            The time limit before the scaling is reversed.
     * @throws IllegalArgumentException
     *             If the <code>cycleTime</code> is not positive.
     */
    public ScalingController(float scalingSpeed, float periodLengthMillis) {
        super();
        setPeriodLengthMillis(periodLengthMillis);
        setSpeed(scalingSpeed);
    }

    /**
     * Creates a new {@link ScalingController} with the given scaling speed and
     * default cycle time.
     *
     * @param scalingSpeed
     *            How quickly scaling should be.
     */
    public ScalingController(float scalingSpeed) {
        this(scalingSpeed, DEFAULT_PERIOD_LENGTH_MSEC);
    }

    /**
     * Creates a new {@link ScalingController} with default scaling speed and
     * cycle time.
     */
    public ScalingController() {
        this(DEFAULT_SCALING_SPEED);
    }

    @Override
    public void setSpeed(float speed) {
        updateSpeed = speed;
    }

    @Override
    public float getSpeed() {
        return updateSpeed;
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

        float delta = 1.0f + updateSpeed;
        for (Node n : super.controlledNodesList)
            n.scale(delta, delta, delta);
    }

}
