/**
 * Copyright (C) 2017 Luis Gutierrez <lg24834@gmail.com>
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

package ray.rage.asset.animation;

import ray.rage.asset.AbstractAssetManager;
import ray.rage.asset.AssetManager;
import ray.rage.asset.animation.Animation;
import ray.rage.asset.animation.AnimationLoader;
import ray.rage.scene.SubEntity;

/**
 * A <i>animation manager</i> is responsible for managing the life-cycle of
 * {@link Animation animations}.
 * <p>
 * A <i>animation manager</i> is an {@link AssetManager asset-manager} that owns
 * the {@link Animation animations} it creates and is responsible for making sure
 * that the same {@link Animation animation} is not loaded more than once. In the
 * event that a client requests a pre-existing {@link Animation animation}, this
 * manager will return the pre-existing instance. This allows {@link Animation
 * animations} to be shared across multiple {@link ray.rage.scene.SkeletalEntity skeletal-entities}.
 * <p>
 *
 * @author Luis Gutierrez
 *
 * @see Animation
 * @see AnimationLoader
 * @see SubEntity
 *
 */
public final class AnimationManager extends AbstractAssetManager<Animation> {

    @Override
    protected Animation createAssetImpl(String name) {
        return new Animation(this, name);
    }

}
