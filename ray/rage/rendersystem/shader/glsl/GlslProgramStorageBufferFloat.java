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

import java.nio.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;

import ray.rage.rendersystem.shader.*;
import ray.rage.rendersystem.shader.glsl.AbstractGlslProgramStorageBuffer;

/**
 * A {@link GpuShaderProgram.StorageBuffer buffer} of floating-point values, as
 * expected by a GLSL shader program.
 * <p>
 * This is intended for GLSL inputs with the <code>buffer</code> type qualifier.
 *
 * @author Raymond L. Rivera
 *
 */
final class GlslProgramStorageBufferFloat extends AbstractGlslProgramStorageBuffer<FloatBuffer> {

    GlslProgramStorageBufferFloat(GpuShaderProgram parent, GLCanvas canvas) {
        super(parent, canvas);
    }

    @Override
    protected void setImpl(GL4 gl, FloatBuffer buff) {
       gl.glBindBufferBase(GL4.GL_SHADER_STORAGE_BUFFER, 0, getBufferId());
       gl.glBufferData(GL4.GL_SHADER_STORAGE_BUFFER, buff.capacity() * Float.BYTES, buff, GL4.GL_DYNAMIC_DRAW);
       // SCOTT
       //gl.glBindBufferBase(GL4.GL_UNIFORM_BUFFER, 0, getBufferId());
       //gl.glBufferData(GL4.GL_UNIFORM_BUFFER, buff.capacity() * Float.BYTES, buff, GL4.GL_DYNAMIC_DRAW);
    }

}
