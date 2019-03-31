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
import ray.rml.*;

/**
 * An {@link GpuShaderProgram.Input input} uniform for a 3-component
 * {@link Vector3 vector}.
 * <p>
 * This is intended for GLSL <code>vec3</code> types with the
 * <code>uniform</code> type qualifier.
 *
 * @author Raymond L. Rivera
 *
 */
final class GlslProgramUniformVec3 extends AbstractGlslProgramUniform<Vector3> {

    GlslProgramUniformVec3(GpuShaderProgram parent, GLCanvas canvas, String name) {
        super(parent, canvas, name);
    }

    @Override
    protected void setImpl(GL4 gl, Vector3 v) {
        // location, vec3 object count, values in a vec3, offset
        gl.glUniform3fv(getLocationId(), 1, v.toFloatArray(), 0);
    }

}
