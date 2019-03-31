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

package ray.rage.scene.controllers;

import java.util.*;
import java.util.logging.*;

import ray.rage.scene.*;
import ray.rml.*;

/**
 * A {@link Node.Controller} implementation that uses <i>linear
 * interpolation</i> (i.e. LERP) to move {@link Node nodes} along a sequence of
 * positions, in <i>world-space</i>, as a function of time. After the last
 * waypoint has been reached, the cycle is restarted.
 * <p>
 * Note that LERP does <i>not</i> produce constant-speed motion. Rather, the
 * speed of movement is determined by the <i>distance between the waypoints</i>
 * under consideration, based on time restrictions.
 * <p>
 * For example, suppose <code>this</code> {@link Node.Controller controller}
 * only has 15 seconds to move a {@link Node node} between two positions,
 * <code>A</code> and <code>B</code>. The greater the distance between them, the
 * faster the {@link Node nodes} will be moved to ensure they arrive at their
 * destination within the specified time frame.
 * <p>
 * To keep movement speed roughly "constant" between waypoints, the client must
 * make sure to either keep waypoints at roughly equal distances from each
 * other, or use different time values.
 * <p>
 * A minimum of 2 waypoints are required for <code>this</code>
 * {@link Node.Controller controller} to work.
 *
 * @author Raymond L. Rivera
 *
 */
public class WaypointController extends AbstractController {

    private static final Logger logger              = Logger.getLogger(WaypointController.class.getName());

    // By default, we take 15 seconds to travel between waypoints,
    // which implies that movement speed depends on the distance that needs to
    // be covered (i.e. this controller does not provide constant-speed motion).
    //
    // It's up to the client to make sure the waypoints are at roughly the same
    // distance from each other
    private static final float  DEFAULT_TIME_MILLIS = 15000f;

    private List<Vector3>       waypointList        = new ArrayList<>();
    private int                 startIndex          = 0;
    private int                 endIndex            = 1;
    private float               intervalTimeMs      = 0;
    private float               totalTimeMs         = 0;

    /**
     * Creates a new {@link WaypointController} with the specified interpolation
     * time limit.
     *
     * @param intervalLimitMs
     *            The time <code>this</code> has to interpolate movement between
     *            each pair of waypoints, in milliseconds.
     * @throws IllegalArgumentException
     *             If the time is not positive.
     */
    public WaypointController(float intervalLimitMs) {
        super();
        setIntervalTimeMillis(intervalLimitMs);
    }

    /**
     * Creates a new {@link WaypointController} with the default interpolation time
     * limit.
     */
    public WaypointController() {
        this(DEFAULT_TIME_MILLIS);
    }

    /**
     * Sets the time given for {@link Node nodes} to be interpolated between each
     * pair of waypoints.
     *
     * @param intervalMs
     *            The time <code>this</code> has to interpolate movement between
     *            each pair of waypoints, in milliseconds.
     * @throws IllegalArgumentException
     *             If the time is not positive.
     */
    public void setIntervalTimeMillis(float intervalMs) {
        if (intervalMs <= 0)
            throw new IllegalArgumentException("Time limit is not positive");

        intervalTimeMs = intervalMs;
    }

    /**
     * Adds a new position {@link Vector3 vector}, in <i>world-space</i>, as a
     * waypoint.
     *
     * @param wp
     *            The position {@link Vector3 vector}, in <i>world-space</i>.
     * @throws NullPointerException
     *             If the {@link Vector3 vector} is <code>null</code>.
     */
    public void addWaypoint(Vector3 wp) {
        if (wp == null)
            throw new NullPointerException("Null waypoint " + Vector3.class.getSimpleName());

        waypointList.add(wp);
    }

    /**
     * Removes all the waypoints.
     */
    public void removeAllWaypoints() {
        waypointList.clear();
        resetWaypoints();
    }

    @Override
    protected void updateImpl(float elapsedTimeMillis) {
        // check whether any movement is actually possible;
        // need at least 2 waypoints
        if (waypointList.size() <= 1) {
            logger.warning("Not enough waypoints: " + waypointList.size());
            return;
        }

        Vector3 start = waypointList.get(startIndex);
        Vector3 end = waypointList.get(endIndex);

        totalTimeMs += elapsedTimeMillis;

        // interpolation parameter 't' is auto-clamped to the valid [0, 1]
        // range; 't' is how far along the trajectory between start/end the
        // mid-point position is, as a percentage (e.g. 0.75 = 75% along the
        // way)
        final float t = totalTimeMs / intervalTimeMs;
        Vector3 mid = start.lerp(end, t);

        for (Node n : super.controlledNodesList)
            n.setLocalPosition(mid);

        updateWaypoints();
    }

    @Override
    public void notifyDispose() {
        waypointList.clear();
        waypointList = null;
        super.notifyDispose();
    }

    private void updateWaypoints() {
        if (totalTimeMs >= intervalTimeMs) {
            // we're done interpolating between the current start/end positions,
            // so we reset the time to go back to the beginning
            totalTimeMs = 0;

            // update the current start/end positions to interpolate along,
            // cycling back from the last position to the first if necessary
            startIndex = endIndex;
            if (++endIndex == waypointList.size())
                endIndex = 0;
        }
    }

    private void resetWaypoints() {
        startIndex = 0;
        endIndex = 1;
        totalTimeMs = 0;
    }

}
