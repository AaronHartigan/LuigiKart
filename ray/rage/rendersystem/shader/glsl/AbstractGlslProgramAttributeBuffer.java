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

import java.nio.*;
import java.util.logging.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;

import ray.rage.rendersystem.shader.*;
import ray.rage.rendersystem.shader.glsl.AbstractGlslProgramAttribute;
import ray.rage.rendersystem.shader.glsl.AbstractGlslProgramAttributeBuffer;
import ray.rage.rendersystem.shader.glsl.AbstractGlslProgramInput;

/**
 * Base implementation for an {@link GpuShaderProgram.Attribute attribute}
 * buffer of floating-point values.
 *
 * @author Raymond L. Rivera
 *
 */
abstract class AbstractGlslProgramAttributeBuffer extends AbstractGlslProgramAttribute<FloatBuffer> {

    private static final Logger logger = Logger.getLogger(AbstractGlslProgramAttributeBuffer.class.getName());

    AbstractGlslProgramAttributeBuffer(GpuShaderProgram parent, GLCanvas canvas, String name) {
        super(parent, canvas, name);
    }

    @Override
    protected void setImpl(GL4 gl, FloatBuffer buffer) {
        final int bufferId = getBufferId();
        final int locationId = getLocationId();

        if (bufferId == AbstractGlslProgramInput.INVALID_ID) {
            logger.finest("Invalid buffer ID: " + getName());
            return;
        }

        if (locationId == AbstractGlslProgramInput.INVALID_ID) {
            logger.finest("Invalid locationId ID: " + getName());
            return;
        }

        gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, bufferId);
        gl.glBufferData(GL4.GL_ARRAY_BUFFER, buffer.capacity() * Float.BYTES, buffer, GL4.GL_DYNAMIC_DRAW);
        setAttributePointer(gl, locationId);
        gl.glEnableVertexAttribArray(locationId);
    }

    /**
     * Specifies how the the generic attribute data should be interpreted.
     *
     * @param gl
     *            The {@link GL4} instance.
     * @param locationId
     *            The ID for <code>this</code> buffer, as specified by the
     *            underlying driver implementation.
     */
    protected abstract void setAttributePointer(GL4 gl, int locationId);

}
