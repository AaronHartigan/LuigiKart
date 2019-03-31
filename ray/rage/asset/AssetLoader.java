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

import java.io.*;
import java.nio.file.*;

import ray.rage.asset.Asset;
import ray.rage.asset.AssetManager;
import ray.rage.common.*;

/**
 * An <i>asset loader</i> is responsible for loading the raw data of an
 * {@link Asset asset} directly from the file system and transforming it into a
 * format that's meaningful to, and usable by, the framework.
 * <p>
 * Implementation classes need to understand and process the internal storage
 * format used to represent the {@link Asset assets} directly in the file system
 * (e.g. XML document) in order to allow concrete {@link Asset asset} instances
 * to exist with meaningful values.
 * <p>
 * Loaders are not intended to be used directly. Rather, they're meant to be
 * registered with the corresponding {@link AssetManager asset-managers} so that
 * the {@link AssetManager asset-managers} can select the correct loader at
 * run-time based on the {@link Asset asset's} file extension.
 *
 * @param <T>
 *            A generic type that extends the {@link Asset asset} interface.
 *
 * @author Raymond L. Rivera
 *
 * @see Asset
 * @see AssetManager
 *
 */
public interface AssetLoader<T extends Asset> extends Disposable {

    /**
     * Reads the data from the specified {@link Path path} on disk, extracts the
     * necessary information, and assigns it to the specified {@link Asset
     * asset}.
     *
     * @param dst
     *            The {@link Asset asset} instance to be initialized.
     * @param src
     *            The file system {@link Path path} where the {@link Asset
     *            asset's} data will be loaded from.
     * @throws IOException
     *             If the data cannot be read from the file system.
     * @throws NullPointerException
     *             If the {@link Asset asset} is <code>null</code>
     */
    void loadAsset(T dst, Path src) throws IOException;

}
