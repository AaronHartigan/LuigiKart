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

import java.nio.*;
import java.util.*;

import ray.rage.asset.material.*;
import ray.rage.asset.mesh.*;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.rendersystem.shader.*;
import ray.rage.rendersystem.states.*;
import ray.rage.scene.*;
import ray.rage.scene.generic.AbstractGenericSceneObject;
import ray.rage.scene.generic.GenericManualObjectSection;

/**
 * A generic {@link ManualObject} implementation.
 *
 * @author Raymond L. Rivera
 *
 */
final class GenericManualObject extends AbstractGenericSceneObject implements ManualObject {

    private Mesh                             mesh;

    // the LHM implementation provides predictability to
    // #getManualSection(int)
    private Map<String, ManualObjectSection> sectionMap = new LinkedHashMap<>();

    /**
     * Creates a new {@link ManualObject manual-object} with the given parent
     * {@link SceneManager manager}, name, and {@link Mesh mesh}.
     *
     * @param manager
     *            The parent {@link SceneManager manager}.
     * @param name
     *            The name for <code>this</code> {@link ManualObject
     *            manual-object}.
     * @param m
     *            The {@link Mesh mesh} <code>this</code> {@link ManualObject
     *            manual-object} is based on.
     * @throws NullPointerException
     *             If any of the arguments is <code>null</code>.
     * @throws IllegalArgumentException
     *             If the name is empty.
     */
    GenericManualObject(SceneManager sm, String name, Mesh m) {
        super(sm, name);
        if (m == null)
            throw new NullPointerException("Null " + Mesh.class.getSimpleName());

        mesh = m;
    }

    @Override
    public ManualObjectSection createManualSection(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("Name is empty");
        if (sectionMap.containsKey(name))
            throw new RuntimeException(ManualObjectSection.class.getSimpleName() + " already exists: " + name);

        SubMesh sm = mesh.createSubMesh(name + SubMesh.class.getSimpleName());
        ManualObjectSection section = new GenericManualObjectSection(this, sm);
        sectionMap.put(name, section);
        return section;
    }

    @Override
    public Iterable<ManualObjectSection> getManualSections() {
        return sectionMap.values();
    }

    @Override
    public ManualObjectSection getManualSection(String name) {
        return sectionMap.get(name);
    }

    @Override
    public ManualObjectSection getManualSection(int idx) {
        String name = new ArrayList<>(sectionMap.keySet()).get(idx);
        return sectionMap.get(name);
    }

    @Override
    public int getManualSectionCount() {
        return sectionMap.size();
    }

    @Override
    public void setPrimitive(Primitive prim) {
        for (ManualObjectSection sec : sectionMap.values())
            sec.setPrimitive(prim);
    }

    @Override
    public void setDataSource(DataSource ds) {
        for (ManualObjectSection sec : sectionMap.values())
            sec.setDataSource(ds);
    }

    @Override
    public void setVertexBuffer(FloatBuffer vertices) {
        for (ManualObjectSection sec : sectionMap.values())
            sec.setVertexBuffer(vertices);
    }

    @Override
    public void setTextureCoordBuffer(FloatBuffer texCoords) {
        for (ManualObjectSection sec : sectionMap.values())
            sec.setTextureCoordsBuffer(texCoords);
    }

    @Override
    public void setNormalsBuffer(FloatBuffer normals) {
        for (ManualObjectSection sec : sectionMap.values())
            sec.setNormalsBuffer(normals);
    }

    @Override
    public void setIndexBuffer(IntBuffer indices) {
        for (ManualObjectSection sec : sectionMap.values())
            sec.setIndexBuffer(indices);
    }

    @Override
    public void setRenderState(RenderState rs) {
        for (ManualObjectSection sec : sectionMap.values())
            sec.setRenderState(rs);
    }

    @Override
    public void setMaterial(Material mat) {
        for (ManualObjectSection sec : sectionMap.values())
            sec.setMaterial(mat);
    }

    @Override
    public void setGpuShaderProgram(GpuShaderProgram prog) {
        for (ManualObjectSection sec : sectionMap.values())
            sec.setGpuShaderProgram(prog);
    }
    
    @Override
    public void setDepthShaderProgram(GpuShaderProgram prog) {
        for (ManualObjectSection sec : sectionMap.values())
            sec.setDepthShaderProgram(prog);
    }

    @Override
    public void notifyDispose() {
        // meshes/sub-meshes are shared assets and may be used in multiple
        // places, so do NOT destroy destroy mesh/sub-mesh instances
        mesh = null;

        // we DO dispose of the render states assigned to the manual sections,
        // some of which may include communication with lower level systems
        // such as GPU texture buffers depending on implementations, and
        // require clean-up
        for (ManualObjectSection sec : sectionMap.values())
            sec.notifyDispose();

        sectionMap.clear();
        sectionMap = null;

        super.notifyDispose();
    }
    
    @Override
    public void setCanReceiveShadows(Boolean setCanReceiveShadows) {
        for (ManualObjectSection sec : sectionMap.values())
            sec.setCanReceiveShadows(setCanReceiveShadows);
    }
}
