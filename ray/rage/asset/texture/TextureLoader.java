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
import ray.rage.asset.texture.TextureManager;

/**
 * A <i>texture loader</i> is an {@link AssetLoader asset-loader} that can load
 * {@link Image images} from the file system to be used as {@link Texture
 * textures}. All implementations are responsible for processing the
 * {@link Image image} as necessary to comply with a {@link Texture texture's}
 * requirements.
 * <p>
 * A separate implementation of this interface is expected for every different
 * raw data format supported by the framework.
 *
 * @author Raymond L. Rivera
 *
 * @see Texture
 * @see TextureManager
 */
public interface TextureLoader extends AssetLoader<Texture> {}
