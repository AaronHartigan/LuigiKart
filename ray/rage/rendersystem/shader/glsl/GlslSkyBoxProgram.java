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

import com.jogamp.opengl.awt.*;

import ray.rage.rendersystem.shader.*;
import ray.rage.rendersystem.shader.glsl.AbstractGlslProgram;
import ray.rage.rendersystem.shader.glsl.GlslProgramUniformMat4;
import ray.rage.scene.*;

/**
 * Concrete implementation of a {@link GpuShaderProgram shader-program} to
 * process a {@link SkyBox sky-box}.
 * <p>
 * This implementation contains, and submits, all the input data for the
 * <code>skybox.vert</code> vertex shader. Changes in the inputs of that program
 * will require changes to this implementation.
 *
 * @author Raymond L. Rivera
 *
 */
class GlslSkyBoxProgram extends AbstractGlslProgram {

    private GlslProgramUniformMat4 viewMatrix;

    public GlslSkyBoxProgram(GLCanvas canvas) {
        super(canvas);
    }

    @Override
    public Type getType() {
        return Type.SKYBOX;
    }

    @Override
    public void fetchImpl(Context ctx) {
        if (viewMatrix == null)
            viewMatrix = new GlslProgramUniformMat4(this, getCanvas(), "matrix.view");

        viewMatrix.set(ctx.getViewMatrix());
    }

    @Override
    public void notifyDispose() {
        if (viewMatrix != null)
            viewMatrix.notifyDispose();

        viewMatrix = null;
        super.notifyDispose();
    }

}
