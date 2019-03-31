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

package ray.rage.rendersystem.states;

import ray.rage.rendersystem.*;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.rendersystem.states.RenderState;
import ray.rage.scene.*;

/**
 * A <i>front-face state</i> determines which side, or "face", of a
 * {@link Primitive primitive} should be drawn.
 * <p>
 * Culling back faces avoid wasting GPU cycles in trying to render surfaces that
 * are invisible because they're facing away from the {@link Camera camera}.
 *
 * @author Raymond L. Rivera
 *
 */
public interface FrontFaceState extends RenderState {

    /**
     * Specifies the vertex or index sequence pattern to use to determine what
     * the "front" and "back" faces of a {@link Renderable renderable} are.
     *
     * @author Raymond L. Rivera
     *
     */
    enum VertexWinding {
        /**
         * The front of a primitive is determined by a <i>clockwise</i> sequence
         */
        CLOCKWISE,

        /**
         * The front of a primitive is determined by a <i>counter-clockwise</i>
         * sequence. This is the default.
         */
        COUNTER_CLOCKWISE
    }

    /**
     * Sets the {@link VertexWinding vertex-winding}.
     *
     * @param vw
     *            The {@link VertexWinding vertex-winding}.
     * @throws NullPointerException
     *             If the argument is <code>null</code>.
     */
    void setVertexWinding(VertexWinding vw);

    /**
     * Gets the {@link VertexWinding vertex-winding}.
     *
     * @return The {@link VertexWinding vertex-winding}.
     */
    VertexWinding getVertexWinding();

}
