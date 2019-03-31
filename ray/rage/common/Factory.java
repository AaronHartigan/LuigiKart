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
 * A <i>factory</i> is an object that's specifically meant to instantiate other
 * objects of a specified type while minimizing coupling between the client and
 * specific implementations.
 *
 * @author Raymond L. Rivera
 *
 * @param <T>
 *            The type of object the concrete factory will instantiate.
 */
public interface Factory<T> {

    /**
     * Creates a new instance of the specified type.
     *
     * @return A new instance.
     */
    T createInstance();

}
