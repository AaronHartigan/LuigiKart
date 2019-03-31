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

package ray.rage.asset.texture;

import ray.rage.asset.*;
import ray.rage.asset.texture.Texture;
import ray.rage.asset.texture.TextureLoader;

/**
 * A <i>texture manager</i> is responsible for managing the life-cycle of
 * {@link Texture textures}.
 * <p>
 * A <i>texture manager</i> is an {@link AssetManager asset-manager} that owns
 * the {@link Texture textures} it creates and is responsible for making sure
 * that the same {@link Texture texture} is not loaded more than once. In the
 * event that a client requests a pre-existing {@link Texture textures}, this
 * manager will return the pre-existing instance.
 *
 * @author Raymond L. Rivera
 *
 * @see Texture
 * @see TextureLoader
 */
public final class TextureManager extends AbstractAssetManager<Texture> {

    @Override
    protected Texture createAssetImpl(String name) {
        return new Texture(this, name);
    }

}
