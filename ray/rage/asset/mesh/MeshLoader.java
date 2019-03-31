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

/**
 * A <i>mesh loader</i> is an {@link AssetLoader asset-loader} that can load
 * {@link Mesh meshes}. A separate implementation of this interface is expected
 * for every different raw data format supported by the framework.
 *
 * @author Raymond L. Rivera
 *
 */
public interface MeshLoader extends AssetLoader<Mesh> {}
