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

package ray.rage.asset.mesh;

import java.util.*;

import ray.rage.asset.*;
import ray.rage.asset.material.*;
import ray.rage.asset.mesh.Mesh;
import ray.rage.asset.mesh.MeshManager;
import ray.rage.asset.mesh.SubMesh;
import ray.rage.scene.*;

/**
 * A <i>mesh</i> is an {@link Asset asset} holding data about a <i>discrete</i>
 * 3-dimensional object. This class is <i>not</i> intended (or appropriate) for
 * large-scale sprawling geometry found in 'static' level data (e.g. terrains,
 * the world itself, etc).
 * <p>
 * Mesh data usually has more than just vertex and triangle information. This
 * can include references to {@link Material materials}, animations, and other
 * things. Multiple world objects can (and should) be created from a single mesh
 * object.
 * <p>
 * Meshes are split into {@link SubMesh sub-meshes} and have a 1:M relationship,
 * because different parts of a mesh may use different {@link Material
 * materials}, which may require rendering state changes. Meshes also have a 1:M
 * relationship with {@link Entity entities}, where each mesh may be shared by
 * one or more {@link Entity entities}.
 *
 * @author Raymond L. Rivera
 *
 * @see SubMesh
 * @see Entity
 *
 */
public final class Mesh extends AbstractAsset {

    // the LHM implementation provides predictability to
    // #getSubMesh(int)
    private Map<String, SubMesh> subMeshMap = new LinkedHashMap<>();

    Mesh(MeshManager manager, String name) {
        super(manager, name);
    }

    /**
     * Creates a new {@link SubMesh sub-mesh} with <code>this</code> {@link Mesh
     * mesh} as the parent.
     * <p>
     * This method should be used carefully, as it allows the manual definition
     * of geometry. If used for this purpose, make sure you've <i>correctly</i>
     * defined the geometry.
     *
     * @param name
     *            The name for the {@link SubMesh sub-mesh}.
     * @return The new {@link SubMesh sub-mesh}.
     * @throws IllegalArgumentException
     *             If the name is empty.
     */

    public SubMesh createSubMesh(String name) {
        SubMesh sm = new SubMesh(this, name);
        subMeshMap.put(name, sm);
        return sm;
    }

    /**
     * Gets an {@link Iterable} for the {@link SubMesh sub-meshes} that
     * <code>this</code> {@link Mesh mesh} is made of.
     *
     * @return An {@link Iterable} of the {@link SubMesh sub-meshes} in
     *         <code>this</code> {@link Mesh mesh}.
     */
    public Iterable<SubMesh> getSubMeshes() {
        return subMeshMap.values();
    }

    /**
     * Gets the {@link SubMesh sub-mesh} at the specified index. They are
     * returned in the same order they were added.
     *
     * @param idx
     *            The index specifying which {@link SubMesh sub-mesh} to return.
     * @return The {@link SubMesh} at the specified index.
     * @throws IndexOutOfBoundsException
     *             If the index is not within an acceptable range.
     */
    public SubMesh getSubMesh(int idx) {
        String name = new ArrayList<>(subMeshMap.keySet()).get(idx);
        return subMeshMap.get(name);
    }

    /**
     * Gets the specified {@link SubMesh sub-mesh}, if owned by
     * <code>this</code> {@link Mesh mesh}.
     *
     * @param name
     *            The name of the {@link SubMesh sub-mesh}.
     * @return The {@link SubMesh sub-mesh} with the specified name. Otherwise
     *         <code>null</code>.
     */
    public SubMesh getSubMesh(String name) {
        return subMeshMap.get(name);
    }

    /**
     * Gets the number of {@link SubMesh sub-meshes} <code>this</code>
     * {@link Mesh mesh} is made of.
     *
     * @return The number of {@link SubMesh sub-meshes}.
     */
    public int getSubMeshCount() {
        return subMeshMap.size();
    }

    @Override
    public void notifyDispose() {
        for (SubMesh sm : subMeshMap.values())
            sm.notifyDispose();

        super.notifyDispose();
    }

}
