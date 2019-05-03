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

import ray.rage.rendersystem.*;
import ray.rage.rendersystem.shader.*;
import ray.rage.rendersystem.shader.glsl.GlslRenderingProgram;
import ray.rage.rendersystem.shader.glsl.GlslSkeletalRenderingProgram;
import ray.rage.rendersystem.shader.glsl.GlslSkyBoxProgram;
import ray.rage.rendersystem.shader.glsl.GlslTessProgram;

/**
 * Concrete implementation of the {@link GpuShaderProgramFactory
 * shader-program-factory} interface.
 *
 * @author Raymond L. Rivera
 *
 */
public final class GlslProgramFactory implements GpuShaderProgramFactory {

    @Override
    public GpuShaderProgram createInstance(RenderSystem rs, GpuShaderProgram.Type type) {
        switch (type) {
	        case DEPTH:
	        	return new GlslDepthProgram((GLCanvas) rs.getCanvas());
            case RENDERING:
                return new GlslRenderingProgram((GLCanvas) rs.getCanvas());
            case ITEM_BOX:
                return new GlslItemBoxProgram((GLCanvas) rs.getCanvas());
            case SKYBOX:
                return new GlslSkyBoxProgram((GLCanvas) rs.getCanvas());
            case SKELETAL_RENDERING:
                return new GlslSkeletalRenderingProgram((GLCanvas) rs.getCanvas());
            case TESSELLATION:
                return new GlslTessProgram((GLCanvas) rs.getCanvas());
            case FRAMEBUFFER:
            	return new GlslFramebufferProgram((GLCanvas) rs.getCanvas());
            case TRANSPARENT:
            	return new GlslTransparentProgram((GLCanvas) rs.getCanvas());
            case GUI:
            	return new GlslGUIProgram((GLCanvas) rs.getCanvas());
            case GUI_BACKGROUND:
            	return new GlslGUIBackgroundProgram((GLCanvas) rs.getCanvas());
            default:
                throw new UnsupportedOperationException(type + " not implemented");
        }
    }

}
