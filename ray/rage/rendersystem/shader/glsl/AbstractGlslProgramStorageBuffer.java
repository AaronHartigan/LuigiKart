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

import ray.rage.rendersystem.shader.*;
import ray.rage.rendersystem.shader.glsl.AbstractGlslProgramInput;
import ray.rage.rendersystem.shader.glsl.GlslContextUtil;

/**
 * Base implementation of the {@link GpuShaderProgram.StorageBuffer} interface.
 *
 * @author Raymond L. Rivera
 *
 * @param <T>
 *            The generic type of input that can be passed to a GLSL shader
 *            program.
 */
abstract class AbstractGlslProgramStorageBuffer<T> extends AbstractGlslProgramInput<T>
                                               implements GpuShaderProgram.StorageBuffer<T> {

    private int bufferId = AbstractGlslProgramInput.INVALID_ID;

    AbstractGlslProgramStorageBuffer(GpuShaderProgram parent, GLCanvas glc) {
        super(parent, glc);
        createBuffer();
    }

    @Override
    public int getBufferId() {
        return bufferId;
    }

    @Override
    public void notifyDispose() {
        if (bufferId != AbstractGlslProgramInput.INVALID_ID)
            destroyBuffer();

        super.notifyDispose();
    }

    private void createBuffer() {
        final int[] buffers = new int[1];

        GLContext ctx = GlslContextUtil.getCurrentGLContext(super.getGLCanvas());
        GL4 gl = ctx.getGL().getGL4();
        gl.glGenBuffers(buffers.length, buffers, 0);
        bufferId = buffers[0];
        ctx.release();

        if (bufferId == AbstractGlslProgramInput.INVALID_ID)
            throw new IllegalStateException("Invalid buffer ID: " + bufferId);
    }

    private void destroyBuffer() {
        final int[] buffers = new int[] { bufferId };

        GLContext ctx = GlslContextUtil.getCurrentGLContext(super.getGLCanvas());
        GL4 gl = ctx.getGL().getGL4();
        gl.glDeleteBuffers(buffers.length, buffers, 0);
        ctx.release();

        bufferId = AbstractGlslProgramInput.INVALID_ID;
    }

}
