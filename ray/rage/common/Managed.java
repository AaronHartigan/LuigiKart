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
 * A <i>managed</i> instance is one that is created by another object that's
 * considered to be its "owner" and whose life-cycle is meant to be controlled
 * by it.
 *
 * @author Raymond L. Rivera
 *
 * @param <T>
 *            The type representing the owner.
 */
public interface Managed<T> {

    /**
     * Returns the object that created <code>this</code> instance.
     *
     * @return The object that owns <code>this</code> instance.
     */
    T getManager();

}
