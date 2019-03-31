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

/**
 * A <i>throttleable</i> object is one that can have its speed modified.
 * <p>
 * Since not all {@link Node.Controller node-controllers} need a speed setting,
 * this operation was moved to a common package-private interface so that only
 * those implementations that need it end up using it, rather than forcing this
 * requirement on all implementations.
 *
 * @author Raymond L. Rivera
 *
 */
interface Throttleable {

    /**
     * Sets a new speed value.
     *
     * @param speed
     *            The scaling factor.
     */
    void setSpeed(float speed);

    /**
     * Gets the current speed value.
     *
     * @return The current speed.
     */
    float getSpeed();

}
