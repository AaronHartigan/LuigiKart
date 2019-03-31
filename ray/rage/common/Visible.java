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

package ray.rage.common;

/**
 * A <i>visible</i> object is one that can be displayed in some way, such as an
 * object in a scene.
 *
 * @author Raymond L. Rivera
 *
 */
public interface Visible {

    /**
     * Set to <code>true</code> if <code>this</code> should be visible.
     * Otherwise <code>false</code>.
     */
    void setVisible(boolean visible);

    /**
     * Returns <code>true</code> if <code>this</code> is currently visible.
     * Otherwise <code>false</code>.
     */
    boolean isVisible();

}
