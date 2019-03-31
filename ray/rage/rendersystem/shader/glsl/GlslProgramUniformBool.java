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
import ray.rage.rendersystem.shader.glsl.AbstractGlslProgramUniform;

/**
 * An {@link GpuShaderProgram.Input input} uniform for boolean types.
 * <p>
 * This is intended for GLSL <code>bool</code> types with the
 * <code>uniform</code> type qualifier.
 *
 * @author Raymond L. Rivera
 *
 */
final class GlslProgramUniformBool extends AbstractGlslProgramUniform<Boolean> {

    GlslProgramUniformBool(GpuShaderProgram parent, GLCanvas canvas, String name) {
        super(parent, canvas, name);
    }

    @Override
    protected void setImpl(GL4 gl, Boolean flag) {
        // § 4.1.2 Booleans To make conditional execution of code easier to
        // express, the type bool is supported. There is no expectation that
        // hardware directly supports variables of this type. (...)
        //
        // § 2.2.1 (...) When state values are specified using a different
        // parameter type than the actual type of that state, data conversions
        // are performed as follows:
        //
        // When the type of internal state is boolean, zero integer or
        // floating-point values are converted to FALSE and non-zero values are
        // converted to TRUE.
        //
        // http://stackoverflow.com/a/33690786/4594973
        gl.glUniform1i(getLocationId(), flag ? 1 : 0);
    }

}
