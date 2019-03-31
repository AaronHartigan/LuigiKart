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
 * An <i>manageable</i> object is one whose setup and tear down is intended to
 * be explicitly controlled to manage potentially expensive or
 * sequence-sensitive operations with more predictability.
 * <p>
 * This is mostly intended to be used by types that would benefit from being
 * able to manually control their own setup and tear-down explicitly. For
 * example, types that are expensive to set up or need to be initialized in a
 * specific sequence are good candidates for implementing this interface.
 *
 * @author Raymond L. Rivera
 *
 */
public interface Manageable {

    /**
     * Explicitly sets up <code>this</code> instance.
     */
    void startup();

    /**
     * Explicitly tears down <code>this</code> instance.
     */
    void shutdown();

}
