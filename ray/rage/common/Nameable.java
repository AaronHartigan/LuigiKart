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

package ray.rage.common;

/**
 * A <i>nameable</i> object is one that can be identified by a specific name.
 *
 * @author Raymond L. Rivera
 *
 */
public interface Nameable {

    /**
     * Gets the name used to identify <code>this</code> instance.
     *
     * @return The name of <code>this</code> instance.
     */
    String getName();

}
