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

package ray.rage.asset;

import ray.rage.asset.Asset;
import ray.rage.asset.AssetLoader;
import ray.rage.asset.AssetManager;
import ray.rage.common.*;

/**
 * An <i>asset</i> is external data that is loaded by the system, such as
 * meshes, textures, materials, etc.
 * <p>
 * Assets are not meant to be instantiated directly. Instead, they're meant to
 * be created and handled by {@link AssetManager asset-managers} that have
 * {@link AssetLoader asset-loaders} that understand the internal format of the
 * raw data and can transform them into formats this framework can understand
 * and use.
 *
 * @author Raymond L. Rivera
 *
 * @see AssetManager
 * @see AssetLoader
 *
 */
public interface Asset extends Managed<AssetManager<? extends Asset>>, Nameable, Disposable {

    /**
     * {@inheritDoc}
     *
     * In this case, the name of <code>this</code> {@link Asset asset}.
     * <p>
     * By default, the name of an {@link Asset asset} is the name of the file it
     * was actually loaded from, including the extension.
     *
     * @return The name of the {@link Asset asset}.
     */
    @Override
    String getName();

}
