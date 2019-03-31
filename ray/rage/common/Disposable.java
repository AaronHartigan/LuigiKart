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
 * A <i>disposable</i> is an object that can be explicitly destroyed to release
 * their <i>internal</i> resources.
 *
 * @author Raymond L. Rivera
 *
 */
public interface Disposable {

    /**
     * Explicitly releases the internal resources held by <code>this</code>
     * instance, making it unusable afterwards.
     * <p>
     * <b>WARNING:</b> <i>This method is meant to be used internally by the
     * framework and not by game clients. Use directly only if you really know
     * what you're doing.</i>
     */
    void notifyDispose();

}
