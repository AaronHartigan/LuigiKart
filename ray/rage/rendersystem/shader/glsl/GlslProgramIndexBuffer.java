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

import ray.rage.rendersystem.*;
import ray.rage.rendersystem.shader.*;
import ray.rage.rendersystem.shader.glsl.AbstractGlslProgramStorageBuffer;

/**
 * A {@link GpuShaderProgram.StorageBuffer buffer} of integer values, as
 * expected by a GLSL shader program.
 * <p>
 * This is intended for GLSL vertex index buffer inputs, which may or may not
 * get used by the server depending on the call used by the {@link RenderSystem
 * render-system} implementation.
 *
 * @author Raymond L. Rivera
 *
 */
final class GlslProgramIndexBuffer extends AbstractGlslProgramStorageBuffer<IntBuffer> {

    GlslProgramIndexBuffer(GpuShaderProgram parent, GLCanvas canvas) {
        super(parent, canvas);
    }

    @Override
    protected void setImpl(GL4 gl, IntBuffer buff) {
        gl.glBindBuffer(GL4.GL_ELEMENT_ARRAY_BUFFER, getBufferId());
        gl.glBufferData(GL4.GL_ELEMENT_ARRAY_BUFFER, buff.capacity() * Integer.BYTES, buff, GL4.GL_DYNAMIC_DRAW);
    }

}
