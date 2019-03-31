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

package ray.rage.rendersystem.shader.glsl;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;

/**
 * A simple class with a utility method to retrieve a <i>current</i> rendering
 * {@link GLContext} from a {@link GLCanvas}.
 *
 * @author Raymond L. Rivera
 *
 */
public final class GlslContextUtil {

    private GlslContextUtil() {}

    /**
     * Gets the <i>current</i> {@link GLContext context}.
     * <p>
     * It is the responsibility of the <i>caller</i> to invoke
     * {@link GLContext#release()}. Not doing so can cause a deadlock.
     *
     * @param canvas
     *            The rendering surface that owns the {@link GLContext}.
     * @return A current {@link GLContext}.
     * @throws NullPointerException
     *             If the {@link GLCanvas} is <code>null</code>.
     * @throws RuntimeException
     *             If the {@link GLContext} cannot be made current.
     */
    public static GLContext getCurrentGLContext(GLCanvas canvas) {
        GLContext ctx = canvas.getContext();
        try {
            ctx.makeCurrent();
            return ctx;
        } catch (GLException e) {
            throw new RuntimeException(e);
        }
    }

}
