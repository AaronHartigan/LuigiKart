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

package ray.rage.asset.skeleton;

import ray.rage.asset.AssetLoader;
import ray.rage.asset.skeleton.Skeleton;

/**
 * A <i>skeleton loader</i> is an {@link AssetLoader asset-loader} that can load
 * {@link Skeleton skeleton}. A separate implementation of this interface is
 * expected for every different raw data format supported by the framework.
 *
 * @author Luis Gutierrez
 *
 */
public interface SkeletonLoader extends AssetLoader<Skeleton> {
}
