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

import java.util.logging.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;

import ray.rage.rendersystem.shader.*;
import ray.rage.rendersystem.shader.glsl.AbstractGlslProgramAttribute;
import ray.rage.rendersystem.shader.glsl.AbstractGlslProgramInput;
import ray.rage.rendersystem.shader.glsl.AbstractGlslProgramStorageBuffer;
import ray.rage.rendersystem.shader.glsl.GlslContextUtil;

/**
 * Base implementation of the {@link GpuShaderProgram.Attribute attribute}
 * interface.
 *
 * @author Raymond L. Rivera
 *
 * @param <T>
 *            The generic type of attribute that can be passed to a GLSL shader
 *            program.
 */
abstract class AbstractGlslProgramAttribute<T> extends AbstractGlslProgramStorageBuffer<T>
                                           implements GpuShaderProgram.Attribute<T> {

    private static final Logger logger     = Logger.getLogger(AbstractGlslProgramAttribute.class.getName());

    // FIXME: These members and related logic is a duplication of
    // AbstractGlslProgramLocatableInput
    private String              name;
    private int                 locationId = AbstractGlslProgramInput.INVALID_ID;

    AbstractGlslProgramAttribute(GpuShaderProgram parent, GLCanvas canvas, String inputName) {
        super(parent, canvas);
        if (inputName.isEmpty())
            throw new IllegalArgumentException("Name is empty");

        name = inputName;

        GLContext ctx = GlslContextUtil.getCurrentGLContext(super.getGLCanvas());
        locationId = ctx.getGL().getGL4().glGetAttribLocation(parent.getId(), name);
        ctx.release();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getLocationId() {
        return locationId;
    }

    @Override
    public void set(T value) {
        if (locationId == AbstractGlslProgramInput.INVALID_ID) {
            logger.finest("Invalid location ID: " + name);
            return;
        }
        super.set(value);
    }

    @Override
    public void notifyDispose() {
        locationId = AbstractGlslProgramInput.INVALID_ID;
        super.notifyDispose();
    }

}
