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

package ray.rage.scene.generic;

import java.util.*;

import ray.rage.asset.material.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.shader.*;
import ray.rage.rendersystem.states.*;
import ray.rage.rendersystem.states.RenderState.*;

abstract class AbstractGenericRenderable implements Renderable {

    private Primitive                          primitiveType   = Primitive.TRIANGLES;
    private DataSource                         dataSource      = DataSource.INDEX_BUFFER;
    private Material                           material;
    private GpuShaderProgram                   gpuProgram;
    private GpuShaderProgram                   depthProgram = null;

	private Map<RenderState.Type, RenderState> renderStatesMap = new HashMap<>();

    @Override
    public void setPrimitive(Primitive type) {
        if (type == null)
            throw new NullPointerException("Null " + Primitive.class.getSimpleName());

        primitiveType = type;
    }

    @Override
    public Primitive getPrimitive() {
        return primitiveType;
    }

    @Override
    public void setDataSource(DataSource source) {
        if (source == null)
            throw new NullPointerException("Null " + DataSource.class.getSimpleName());

        dataSource = source;
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void setMaterial(Material mat) {
        if (mat == null)
            throw new NullPointerException("Null " + Material.class.getSimpleName());

        material = mat;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public void setGpuShaderProgram(GpuShaderProgram prog) {
        if (prog == null)
            throw new NullPointerException("Null " + GpuShaderProgram.class.getSimpleName());

        gpuProgram = prog;
    }

    @Override
    public GpuShaderProgram getGpuShaderProgram() {
        return gpuProgram;
    }
    
    
    public GpuShaderProgram getDepthShaderProgram() {
		return depthProgram;
	}

	public void setDepthShaderProgram(GpuShaderProgram depthProgram) {
        if (depthProgram == null)
            throw new NullPointerException("Null " + GpuShaderProgram.class.getSimpleName());

		this.depthProgram = depthProgram;
	}

    @Override
    public void setRenderState(RenderState state) {
        clearRenderState(state.getType());
        renderStatesMap.put(state.getType(), state);
    }

    @Override
    public RenderState getRenderState(Type type) {
        if (type == null)
            throw new NullPointerException("Null " + Type.class.getSimpleName());

        RenderState state = renderStatesMap.get(type);

        if (state == null)
            throw new RuntimeException("Undefined " + RenderState.class.getSimpleName() + ": " + type);

        return state;
    }

    @Override
    public Iterable<RenderState> getRenderStates() {
        return renderStatesMap.values();
    }

    @Override
    public void clearRenderState(RenderState.Type type) {
        if (type == null)
            throw new NullPointerException("Null " + Type.class.getSimpleName());

        RenderState state = renderStatesMap.remove(type);

        if (state != null)
            state.notifyDispose();
    }

    @Override
    public void notifyDispose() {
        material = null;

        for (RenderState rs : renderStatesMap.values())
            rs.notifyDispose();

        renderStatesMap.clear();
        renderStatesMap = null;

        // do NOT notifyDispose the program, which is likely to be shared with
        // other renderables; let the render system clean them up when it gets
        // disposed
        gpuProgram = null;
    }
}
