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
import ray.rage.asset.mesh.*;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.rendersystem.shader.*;
import ray.rage.rendersystem.states.*;
import ray.rage.scene.*;
import ray.rage.scene.generic.AbstractGenericSceneObject;
import ray.rage.scene.generic.GenericSubEntity;

/**
 * A generic implementation of the {@link Entity} interface.
 *
 * @author Raymond L. Rivera
 *
 */
final class GenericEntity extends AbstractGenericSceneObject implements Entity {

    private Mesh            mesh;
    private List<SubEntity> subEntityList;

    /**
     * Creates a new {@link Entity entity} with the given parent
     * {@link SceneManager manager}, name, and {@link Mesh mesh}.
     *
     * @param manager
     *            The parent {@link SceneManager manager}.
     * @param name
     *            The name for <code>this</code> {@link Entity entity}.
     * @param m
     *            The {@link Mesh mesh} <code>this</code> {@link Entity entity}
     *            is based on.
     * @throws NullPointerException
     *             If any of the arguments is <code>null</code>.
     * @throws IllegalArgumentException
     *             If the name is empty.
     * @throws RuntimeException
     *             If the {@link Mesh mesh} has no {@link SubMesh sub-meshes}.
     */
    GenericEntity(SceneManager manager, String name, Mesh m) {
        super(manager, name);
        if (m == null)
            throw new NullPointerException("Null " + Mesh.class.getSimpleName());
        if (m.getSubMeshCount() == 0)
            throw new RuntimeException(Mesh.class.getSimpleName() + " has 0 " + SubMesh.class.getSimpleName());

        mesh = m;
        subEntityList = new ArrayList<>(mesh.getSubMeshCount());
        for (SubMesh subMesh : mesh.getSubMeshes())
            subEntityList.add(new GenericSubEntity(this, subMesh));
    }

    @Override
    public Mesh getMesh() {
        return mesh;
    }

    @Override
    public Iterable<SubEntity> getSubEntities() {
        return subEntityList;
    }

    @Override
    public SubEntity getSubEntity(int idx) {
        return subEntityList.get(idx);
    }

    @Override
    public int getSubEntityCount() {
        return subEntityList.size();
    }

    @Override
    public void setMaterial(Material mat) {
        for (SubEntity se : subEntityList)
            se.setMaterial(mat);
    }

    @Override
    public void setGpuShaderProgram(GpuShaderProgram prog) {
        for (SubEntity se : subEntityList)
            se.setGpuShaderProgram(prog);
    }

    @Override
    public void setPrimitive(Primitive prim) {
        for (SubEntity se : subEntityList)
            se.setPrimitive(prim);
    }

    @Override
    public void visitSubEntities(SubEntity.Visitor visitor) {
        for (SubEntity se : subEntityList)
            visitor.visit(se);
    }

    @Override
    public void setRenderState(RenderState rs) {
        for (SubEntity se : subEntityList)
            se.setRenderState(rs);
    }

    @Override
    public void notifyDispose() {
        // meshes/sub-meshes are shared assets and may be in use by multiple
        // entities/sub-entities respectively, so do NOT destroy/dispose
        // mesh/sub-mesh instances here; the mesh manager takes care of those
        mesh = null;

        // we DO dispose of the sub-entities which are owned by this entity;
        // these include render states assigned to the sub-entities,
        // some of which may include communication with lower level systems
        // such as GPU texture buffers, depending on implementations, and
        // require clean-up
        for (SubEntity se : subEntityList)
            se.notifyDispose();

        subEntityList.clear();
        subEntityList = null;

        super.notifyDispose();
    }

    @Override
    public void setDepthShaderProgram(GpuShaderProgram prog) {
        for (SubEntity se : subEntityList)
            se.setDepthShaderProgram(prog);
    }

	@Override
	public void setCanReceiveShadows(boolean canReceiveShadows) {
		for (SubEntity se : subEntityList)
			se.setCanReceiveShadows(canReceiveShadows);
	}
}
