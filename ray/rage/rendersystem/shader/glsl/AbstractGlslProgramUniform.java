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
import ray.rage.rendersystem.shader.glsl.AbstractGlslProgramLocatableInput;

/**
 * Base implementation for the {@link GpuShaderProgram.Uniform} interface.
 * <p>
 * These values do not change during a given rendering call, that is, they
 * remain "uniform".
 *
 * @author Raymond L. Rivera
 *
 */
abstract class AbstractGlslProgramUniform<T> extends AbstractGlslProgramLocatableInput<T>
                                         implements GpuShaderProgram.Uniform<T> {

    AbstractGlslProgramUniform(GpuShaderProgram parent, GLCanvas canvas, String name) {
        super(parent, canvas, name);
    }

    @Override
    protected int getLocationIdImpl(GL4 gl) {
        final int pid = getManager().getId();
        return gl.glGetUniformLocation(pid, getName());
    }

}
