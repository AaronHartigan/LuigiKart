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

import ray.rage.asset.*;
import ray.rage.asset.mesh.Mesh;
import ray.rage.asset.mesh.MeshLoader;

/**
 * A <i>mesh manager</i> is an {@link AssetManager asset-manager} responsible
 * for managing the life-cycle of {@link Mesh meshes}.
 * <p>
 * A <i>mesh manager</i> owns the {@link Mesh meshes} it creates and is
 * responsible for making sure that the same {@link Mesh mesh} is not loaded
 * more than once. In the event that a client requests a pre-existing
 * {@link Mesh mesh}, this manager will return the pre-existing instance.
 * <p>
 * This allows {@link Mesh meshes} to serve as the basis for multiple objects in
 * the world, each represented by an {@link Entity entity}.
 *
 * @author Raymond L. Rivera
 *
 * @see Mesh
 * @see MeshLoader
 * @see Entity
 *
 */
public final class MeshManager extends AbstractAssetManager<Mesh> {

    @Override
    protected Mesh createAssetImpl(String name) {
        return new Mesh(this, name);
    }

}
