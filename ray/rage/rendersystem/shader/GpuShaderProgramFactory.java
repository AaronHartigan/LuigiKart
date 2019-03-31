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

package ray.rage.rendersystem.shader;

import ray.rage.rendersystem.*;
import ray.rage.rendersystem.shader.GpuShaderProgram;

/**
 * A factory that allows the creation of {@link GpuShaderProgram
 * shader-programs}.
 *
 * @author Raymond L. Rivera
 *
 */
public interface GpuShaderProgramFactory {

    /**
     * Creates a new {@link GpuShaderProgram shader-program}.
     *
     * @param renderSystem
     *            The {@link RenderSystem render-system} containing the drawing
     *            surface.
     * @param type
     *            The {@link GpuShaderProgram.Type type}.
     * @return A new {@link GpuShaderProgram shader-program}.
     */
    GpuShaderProgram createInstance(RenderSystem renderSystem, GpuShaderProgram.Type type);

}
