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
import ray.rage.rendersystem.shader.glsl.AbstractGlslProgramAttributeBuffer;

/**
 * An {@link GpuShaderProgram.Attribute attribute} buffer of 2-component
 * floating-point vectors, as expected by a GLSL shader program.
 * <p>
 * This is intended for GLSL <code>vec2</code> types with the <code>in</code>
 * type qualifier.
 *
 * @author Raymond L. Rivera
 *
 */
final class GlslProgramAttributeBufferVec2 extends AbstractGlslProgramAttributeBuffer {

    GlslProgramAttributeBufferVec2(GpuShaderProgram parent, GLCanvas canvas, String name) {
        super(parent, canvas, name);
    }

    @Override
    protected void setAttributePointer(GL4 gl, int locationId) {
        // location, vec2 component count, type, normalized?, stride, offset
        gl.glVertexAttribPointer(locationId, 2, GL4.GL_FLOAT, false, 0, 0);
    }

}
