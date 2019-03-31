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

package ray.rage.rendersystem.states;

import ray.rage.common.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.states.RenderState;

/**
 * A <i>render state</i> has information about settings that should be
 * {@link #apply() applied} to the underlying graphics driver in use by the
 * {@link RenderSystem render-system} implementation.
 * <p>
 * Render states are meant to modify the underlying settings, or
 * "configuration", of the {@link RenderSystem render-system}, such as enabling
 * or disabling functionality to allow specific {@link Renderable renderables}
 * to be processed as intended (e.g. depth tests, front faces).
 *
 * @author Raymond L. Rivera
 *
 */
public interface RenderState extends Disposable {

    /**
     * The <i>type</i> of {@link RenderState state} that a particular instance
     * may {@link #apply() apply}.
     *
     * @author Raymond L. Rivera
     *
     */
    enum Type {
        /**
         * A {@link RenderState render-state} for depth testing.
         */
        ZBUFFER,

        /**
         * A {@link RenderState render-state} for {@link Texture texture}
         * mapping.
         */
        TEXTURE,

        /**
         * A {@link RenderState render-state} to identify front-faces and
         * perform back-face culling.
         */
        FRONT_FACE
    }

    /**
     * Gets the {@link RenderState.Type type} of {@link RenderState state}
     * <code>this</code> can {@link #apply() apply}.
     *
     * @return The {@link RenderState.Type state-type}.
     */
    Type getType();

    /**
     * Applies <code>this</code> {@link RenderState state}.
     */
    void apply();

    /**
     * Sets whether <code>this</code> {@link RenderState state} can be
     * {@link #apply() applied}.
     *
     * @param enabled
     *            <code>true</code> if <code>this</code> {@link RenderState
     *            state} can be applied. Otherwise <code>false</code>.
     */
    void setEnabled(boolean enabled);

    /**
     * Gets whether <code>this</code> {@link RenderState state} can be
     * {@link #apply() applied} or not.
     *
     * @return <code>true</code> if <code>this</code> {@link RenderState state}
     *         can be applied. Otherwise <code>false</code>.
     */
    boolean isEnabled();

}
