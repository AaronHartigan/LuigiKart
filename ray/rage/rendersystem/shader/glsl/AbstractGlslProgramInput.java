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

package ray.rage.rendersystem.shader.glsl;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;

import ray.rage.rendersystem.shader.*;
import ray.rage.rendersystem.shader.glsl.GlslContextUtil;

/**
 * Base implementation of the {@link GpuShaderProgram.Input} interface.
 *
 * @author Raymond L. Rivera
 *
 * @param <T>
 *            The generic type of input that can be passed to a GLSL shader
 *            program.
 */
abstract class AbstractGlslProgramInput<T> implements GpuShaderProgram.Input<T> {

    protected final static int INVALID_ID = -1;

    private GpuShaderProgram   parentProgram;
    private GLCanvas           canvas;

    AbstractGlslProgramInput(GpuShaderProgram parent, GLCanvas glc) {
        if (parent == null)
            throw new NullPointerException("Null " + GpuShaderProgram.class.getSimpleName());
        if (glc == null)
            throw new NullPointerException("Null canvas");

        parentProgram = parent;
        canvas = glc;
    }

    @Override
    public GpuShaderProgram getManager() {
        return parentProgram;
    }

    @Override
    public void set(T value) {
        GLContext ctx = GlslContextUtil.getCurrentGLContext(canvas);
        setImpl(ctx.getGL().getGL4(), value);
        ctx.release();
    }

    /**
     * Lets concrete implementations submit values to the shader.
     *
     * @param gl
     *            The {@link GL4} instance.
     * @param v
     *            The value to set.
     */
    protected abstract void setImpl(GL4 gl, T v);

    protected GLCanvas getGLCanvas() {
        return canvas;
    }

    @Override
    public void notifyDispose() {
        parentProgram = null;
        canvas = null;
    }

}
