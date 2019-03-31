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

package ray.rage.asset.material;

import ray.rage.asset.*;
import ray.rage.asset.material.Material;
import ray.rage.asset.material.MaterialLoader;
import ray.rage.scene.*;

/**
 * A <i>material manager</i> is responsible for managing the life-cycle of
 * {@link Material materials}.
 * <p>
 * A <i>material manager</i> is an {@link AssetManager asset-manager} that owns
 * the {@link Material materials} it creates and is responsible for making sure
 * that the same {@link Material material} is not loaded more than once. In the
 * event that a client requests a pre-existing {@link Material material}, this
 * manager will return the pre-existing instance. This allows {@link Material
 * materials} to be shared across multiple {@link SubEntity sub-entities}.
 * <p>
 * There's a 1:1 relationship between {@link Material materials} and
 * {@link SubEntity sub-entities}, meaning that if the client wants to modify a
 * {@link Material material} without causing unintended side-effects on other
 * {@link SubEntity sub-entities}, it's the <i>client's responsibility</i> to
 * <i>manually</i> create separate {@link Material materials}, either using
 * {@link #createManualAsset(String)} or modifying the raw data in the file
 * system.
 *
 * @author Raymond L. Rivera
 *
 * @see Material
 * @see MaterialLoader
 * @see SubEntity
 *
 */
public final class MaterialManager extends AbstractAssetManager<Material> {

    @Override
    protected Material createAssetImpl(String name) {
        return new Material(this, name);
    }

}
