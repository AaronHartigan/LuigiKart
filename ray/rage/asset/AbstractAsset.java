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
import ray.rage.asset.AssetManager;

/**
 * An abstract {@link Asset asset} for extension by concrete implementations.
 *
 * @author Raymond L. Rivera
 *
 */
public abstract class AbstractAsset implements Asset {

    private AssetManager<? extends Asset> assetManager;
    private String                        name;

    protected AbstractAsset(AssetManager<? extends Asset> manager, String assetName) {
        if (manager == null)
            throw new NullPointerException("Null asset manager");
        if (assetName == null)
            throw new NullPointerException("Null asset name");
        if (assetName.isEmpty())
            throw new IllegalArgumentException("Empty asset name");

        assetManager = manager;
        name = assetName;
    }

    @Override
    public AssetManager<? extends Asset> getManager() {
        return assetManager;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void notifyDispose() {
        assetManager = null;
        name = null;
    }

}
