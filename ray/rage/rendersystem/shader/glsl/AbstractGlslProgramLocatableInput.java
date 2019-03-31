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

import java.util.logging.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;

import ray.rage.rendersystem.shader.*;
import ray.rage.rendersystem.shader.glsl.AbstractGlslProgramInput;
import ray.rage.rendersystem.shader.glsl.AbstractGlslProgramLocatableInput;
import ray.rage.rendersystem.shader.glsl.GlslContextUtil;

/**
 * Base implementation of the {@link GpuShaderProgram.LocatableInput} interface.
 *
 * @author Raymond L. Rivera
 *
 * @param <T>
 *            The generic type of input that can be passed to a GLSL shader
 *            program.
 */
abstract class AbstractGlslProgramLocatableInput<T> extends AbstractGlslProgramInput<T>
                                                implements GpuShaderProgram.LocatableInput<T> {

    private static final Logger logger     = Logger.getLogger(AbstractGlslProgramLocatableInput.class.getName());

    private String              name;
    private int                 locationId = AbstractGlslProgramInput.INVALID_ID;

    AbstractGlslProgramLocatableInput(GpuShaderProgram parent, GLCanvas glc, String inputName) {
        super(parent, glc);
        if (inputName.isEmpty())
            throw new IllegalArgumentException("Name is empty");

        name = inputName;

        GLContext ctx = GlslContextUtil.getCurrentGLContext(super.getGLCanvas());
        locationId = getLocationIdImpl(ctx.getGL().getGL4());
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

    /**
     * Lets concrete implementations query the underlying driver for their
     * locations.
     *
     * @param gl
     *            The {@link GL4} instance used to query the server.
     */
    protected abstract int getLocationIdImpl(GL4 gl);

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
