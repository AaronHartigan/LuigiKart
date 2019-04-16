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

import java.util.*;

import ray.rage.rendersystem.*;
import ray.rage.rendersystem.shader.*;
import ray.rage.scene.*;
import ray.rml.*;

/**
 * Concrete implementation of the {@link GpuShaderProgram.Context context}
 * interface.
 *
 * @author Raymond L. Rivera
 *
 */
final class GlslProgramContext implements GpuShaderProgram.Context {

    private Renderable   renderable;
    private Matrix4      viewMatrix;
    private Matrix4      projMatrix;
    private Matrix4      lightSpaceMatrix;
    private Vector3      lightPosVec;
    private Vector3      viewPosVec;
    private Boolean      canReceiveShadows = true;

    private AmbientLight ambientLight;
    private List<Light>  lightsList;

    @Override
    public void setRenderable(Renderable r) {
        if (r == null)
            throw new NullPointerException("Renderable is null");

        renderable = r;
    }

    @Override
    public Renderable getRenderable() {
        return renderable;
    }

    @Override
    public void setViewMatrix(Matrix4 vm) {
        if (vm == null)
            throw new NullPointerException("View matrix is null");

        viewMatrix = vm;
    }

    @Override
    public Matrix4 getViewMatrix() {
        return viewMatrix;
    }

    @Override
    public void setProjectionMatrix(Matrix4 pm) {
        if (pm == null)
            throw new NullPointerException("Projection matrix is null");

        projMatrix = pm;
    }

    @Override
    public Matrix4 getProjectionMatrix() {
        return projMatrix;
    }

    @Override
    public void setAmbientLight(AmbientLight ambient) {
        ambientLight = ambient;
    }

    @Override
    public AmbientLight getAmbientLight() {
        return ambientLight;
    }

    @Override
    public void setLightsList(List<Light> lights) {
        // null assignments are allowed for convenience, so that if the client
        // forgets to add light sources in a scene, the system will not crash;
        // it's GpuShaderProgram's responsibility to provide acceptable values
        // to the shader it interacts with if the light source is non-existent
        lightsList = lights;
    }

    @Override
    public List<Light> getLightsList() {
        return lightsList;
    }

    @Override
    public void notifyDispose() {
        renderable = null;
        viewMatrix = null;
        projMatrix = null;
        ambientLight = null;
        lightsList = null;
    }

	@Override
	public void setLightSpaceMatrix(Matrix4 lightSpace) {
        if (lightSpace == null)
            throw new NullPointerException("Light Space matrix is null");

		lightSpaceMatrix = lightSpace;
	}

	@Override
	public Matrix4 getLightSpaceMatrix() {
		return lightSpaceMatrix;
	}

	@Override
	public Vector3 getViewPos() {
		return viewPosVec;
	}

	@Override
	public Vector3 getLightPos() {
		return lightPosVec;
	}

	@Override
	public void setViewPos(Vector3 viewPos) {
		viewPosVec = viewPos;
	}

	@Override
	public void setLightPos(Vector3 lightPos) {
		lightPosVec = lightPos;
	}

	@Override
	public void setCanReceiveShadows(boolean canReceiveShadows) {
		this.canReceiveShadows = canReceiveShadows;
	}

	@Override
	public Boolean getCanReceiveShadows() {
		return canReceiveShadows;
	}
}
