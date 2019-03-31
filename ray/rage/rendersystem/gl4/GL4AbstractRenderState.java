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

package ray.rage.rendersystem.gl4;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;

import ray.rage.rendersystem.states.*;

/**
 * A base implementation of the {@link RenderState render-state} interface for a
 * {@link GL4RenderSystem}.
 *
 * @author Raymond L. Rivera
 *
 */
abstract class GL4AbstractRenderState implements RenderState {

    private GLCanvas canvas;
    private boolean  stateEnabled = true;

    protected GL4AbstractRenderState(GLCanvas surface) {
        if (surface == null)
            throw new NullPointerException("Null canvas");

        canvas = surface;
    }

    @Override
    public void setEnabled(boolean enabled) {
        stateEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return stateEnabled;
    }

    @Override
    public void apply() {
        if (!stateEnabled)
            return;

        GLContext ctx = canvas.getContext();
        try {
            ctx.makeCurrent();
        } catch (GLException e) {
            throw new RuntimeException(e);
        }

        GL4 gl = ctx.getGL().getGL4();
        applyImpl(gl);
        ctx.release();
    }

    @Override
    public void notifyDispose() {
        // this render state may've been shared, so it could already be
        // disposed; do nothing if we already have a nullified canvas
        if (canvas == null)
            return;

        stateEnabled = false;
        GLContext ctx = canvas.getContext();
        try {
            ctx.makeCurrent();
        } catch (GLException e) {
            throw new RuntimeException(e);
        }

        GL4 gl = ctx.getGL().getGL4();
        disposeImpl(gl);
        ctx.release();

        // never forget
        canvas = null;
    }

    /**
     * Abstract method to be implemented by a concrete {@link RenderState
     * render-state}. It actually applies the {@link RenderState render-state}.
     * <p>
     * This is an example of the Template Method design pattern.
     *
     * @param gl
     *            The {@link GL4} object, from the JOGL bindings.
     * @see #apply()
     */
    protected abstract void applyImpl(GL4 gl);

    /**
     * Abstract method to be implemented by a concrete {@link RenderState
     * render-state}. It actually destroys the {@link RenderState render-state}.
     * <p>
     * This is an example of the Template Method design pattern.
     *
     * @param gl
     *            The {@link GL4} object, from the JOGL bindings.
     * @see #notifyDispose()
     */
    protected abstract void disposeImpl(GL4 gl);

}
