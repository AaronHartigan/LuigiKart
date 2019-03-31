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
import ray.rage.asset.AssetLoader;
import ray.rage.asset.AssetManager;
import ray.rage.common.*;

/**
 * An <i>asset manager</i> is responsible for handling the {@link Asset assets}
 * and their life-cycle. Some of these {@link Asset assets} include meshes,
 * textures, materials, and others types that may be added in the future.
 * <p>
 * Note that a manager is <i>not</i> responsible for <i>loading</i> a particular
 * {@link Asset asset}. Rather, it relies on concrete {@link AssetLoader
 * asset-loaders} to do this work, thus, decoupling the role of <i>managing</i>
 * the life-cycle of a {@link Asset asset} from knowledge about its internal
 * storage format.
 *
 * @param <T>
 *            A generic type that extends the {@link Asset asset} interface.
 *
 * @author Raymond L. Rivera
 *
 * @see Asset
 * @see AssetLoader
 *
 */
public interface AssetManager<T extends Asset> extends Disposable {

    /**
     * Sets the {@link Path path} to the directory that contains the relevant
     * {@link Asset assets} for <code>this</code> {@link AssetManager
     * asset-manager}.
     *
     * @param path
     *            The {@link Path path} to the directory.
     * @throws NullPointerException
     *             If the {@link Path path} is <code>null</code>.
     * @throws IllegalArgumentException
     *             If the {@link Path path} is empty.
     */
    void setBaseDirectoryPath(Path path);

    /**
     * Sets the {@link Path path} to the directory that contains the relevant
     * {@link Asset assets} for <code>this</code> {@link AssetManager
     * asset-manager}.
     *
     * @param path
     *            The {@link Path path} to the directory, as a string.
     * @throws NullPointerException
     *             If the {@link Path path} string is <code>null</code>.
     * @throws IllegalArgumentException
     *             If the {@link Path path} string is empty.
     */
    void setBaseDirectoryPath(String path);

    /**
     * Gets the {@link Path path} to the {@link Asset asset} directory used by
     * <code>this</code> {@link AssetManager asset-manager}.
     *
     * @return The {@link Asset asset} directory {@link Path path}.
     */
    Path getBaseDirectoryPath();

    /**
     * Gets an {@link Asset asset} identified by the file system {@link Path
     * path}, creating a new instance the first time it's requested.
     * <p>
     * If it needs to be created, then the appropriate {@link AssetLoader
     * asset-loader} is automatically determined based on the file extension. If
     * the {@link Asset asset} at the specified {@link Path path} has already
     * been created, then the {@link AssetManager asset-manager} returns the
     * pre-existing {@link Asset asset} instead of re-creating it.
     *
     * @param path
     *            The file system {@link Path path} <code>this</code>
     *            {@link Asset asset} will be loaded from.
     * @return The {@link Asset asset}.
     * @throws IOException
     *             If <code>this</code> {@link AssetManager asset-manager}
     *             cannot find the {@link Asset asset} at the specified
     *             {@link Path path}
     * @throws RuntimeException
     *             If <code>this</code> {@link AssetManager asset-manager} does
     *             not have an {@link AssetLoader asset-loader} capable of
     *             loading the {@link Asset asset}.
     * @throws IllegalArgumentException
     *             If the {@link Path path} is empty.
     * @throws NullPointerException
     *             If the {@link Path path} is <code>null</code>.
     */
    T getAsset(Path path) throws IOException;

    /**
     * Gets a {@link Asset asset} identified by the file system {@link Path
     * path} as a string, creating it if it does not yet exist.
     * <p>
     * If it needs to be created, then the appropriate {@link AssetLoader} is
     * automatically determined based on common file extensions. If the
     * {@link Asset asset} at the specified {@link Path path} has already been
     * created, then the manager is expected to return the pre-existing
     * {@link Asset asset} instead of re-creating it.
     *
     * @param path
     *            A string specifying the file system {@link Path path}
     *            <code>this</code> {@link Asset asset} will be loaded from.
     * @return The {@link Asset asset}.
     * @throws IOException
     *             If <code>this</code> {@link AssetManager asset-manager}
     *             cannot find the {@link Asset asset} at the specified path, or
     *             does not have a {@link AssetLoader} capable of loading it.
     * @throws RuntimeException
     *             If <code>this</code> {@link AssetManager asset-manager} does
     *             not have an {@link AssetLoader asset-loader} capable of
     *             loading the {@link Asset asset}.
     * @throws IllegalArgumentException
     *             If the {@link Path path} string is empty.
     * @throws NullPointerException
     *             If the {@link Path path} string is <code>null</code>.
     */
    T getAssetByPath(String path) throws IOException;

    /**
     * Gets an <i>existing</i> {@link Asset asset} with the specified name.
     *
     * @param name
     *            The name of the asset to search for.
     * @return The {@link Asset asset} with the specified name.
     * @throws IllegalArgumentException
     *             If the name is empty.
     * @throws RuntimeException
     *             If <code>this</code> {@link AssetManager asset-manager} does
     *             not currently own a {@link Asset asset} with the specified
     *             name.
     * @throws NullPointerException
     *             If the name is <code>null</code>.
     */
    T getAssetByName(String name);

    /**
     * Creates a new {@link Asset asset} instance using the specified name.
     * <p>
     * A <i>manual</i> {@link Asset asset} is one that is <i>not</i> loaded from
     * the file system using an {@link AssetLoader}. Instead, it's created in an
     * <i>ad-hoc</i> way, allowing the clients to create {@link Asset assets}
     * arbitrarily.
     * <p>
     * Note that this will <i>always</i> create a new instance. It's offered
     * purely for convenience and practical purposes.
     *
     * @param name
     *            The name of the new {@link Asset asset}.
     * @return The {@link Asset asset}.
     * @throws IllegalArgumentException
     *             If the name is empty.
     * @throws RuntimeException
     *             If a manual asset by the specified name already exists.
     * @throws NullPointerException
     *             If the name is <code>null</code>.
     */
    T createManualAsset(String name);

    /**
     * Checks whether <code>this</code> {@link AssetManager asset-manager}
     * already knows about the {@link Asset asset} identified by the given
     * {@link Path path}.
     *
     * @param path
     *            The {@link Path path} where the {@link Asset asset} would've
     *            been loaded from.
     * @return True if the {@link Asset asset} is already loaded. False
     *         otherwise.
     * @throws IllegalArgumentException
     *             If the {@link Path path} is empty.
     * @throws NullPointerException
     *             If the {@link Path path} is <code>null</code>.
     */
    boolean hasAsset(Path path);

    /**
     * Checks whether <code>this</code> {@link AssetManager asset-manager}
     * already knows about the {@link Asset asset} identified by the given
     * {@link Path path}, as a string.
     *
     * @param path
     *            The {@link Path path}, as a string, where the {@link Asset
     *            asset} would've been loaded from.
     * @return True if the {@link Asset asset} is already loaded. False
     *         otherwise.
     * @throws IllegalArgumentException
     *             If the {@link Path path} string is empty.
     * @throws NullPointerException
     *             If the {@link Path path} string is <code>null</code>.
     */
    boolean hasAssetByPath(String path);

    /**
     * Checks whether <code>this</code> {@link AssetManager asset-manager}
     * already knows about the {@link Asset asset} identified by the given name.
     *
     * @param name
     *            The name that identifies the {@link Asset asset}.
     * @return True if the {@link Asset asset} is already loaded. False
     *         otherwise.
     * @throws IllegalArgumentException
     *             If the name is empty.
     * @throws NullPointerException
     *             If the name is <code>null</code>.
     */
    boolean hasAssetByName(String name);

    /**
     * Gets the number of existing {@link Asset assets} managed by
     * <code>this</code> instance.
     *
     * @return The number of loaded {@link Asset assets}.
     */
    int getAssetCount();

    /**
     * Gets an {@link Iterable} for the {@link Asset assets} being managed by
     * <code>this</code> instance.
     *
     * @return An {@link Iterable} of the loaded {@link Asset assets}.
     */
    Iterable<T> getAssets();

    /**
     * Requests that <code>this</code> manager stop managing a {@link Asset
     * asset}.
     * <p>
     * Note that this simply removes the {@link Asset asset} from
     * <code>this</code> {@link AssetManager manager's} ownership, but does not
     * explicitly cause the {@link Asset asset} to be {@link Disposable
     * disposed}. It's up to the client to manually manage the asset to avoid it
     * getting garbage-collected if all other references go out of scope.
     *
     * @param asset
     *            The {@link Asset asset} to be removed.
     * @throws NullPointerException
     *             If the {@link Asset asset} is null.
     */
    void removeAsset(Asset asset);

    /**
     * Requests that <code>this</code> manager stop managing a {@link Asset
     * asset} by the specified file system {@link Path path}, as a string.
     * <p>
     * Note that this simply removes the {@link Asset asset} from
     * <code>this</code> {@link AssetManager manager's} ownership, but does not
     * explicitly cause the {@link Asset asset} to be {@link Disposable
     * disposed}. It's up to the client to manually manage the asset to avoid it
     * getting garbage-collected if all other references go out of scope.
     *
     * @param pathString
     *            The file system {@link Path path}, as a string, of the
     *            {@link Asset asset} to remove.
     * @throws IllegalArgumentException
     *             If the path string is empty.
     * @throws NullPointerException
     *             If the {@link Path path} string is <code>null</code>.
     */
    void removeAssetByPath(String pathString);

    /**
     * Requests that <code>this</code> manager stop managing a {@link Asset
     * asset} by the specified name.
     * <p>
     * Note that this simply removes the {@link Asset asset} from
     * <code>this</code> {@link AssetManager manager's} ownership, but does not
     * explicitly cause the {@link Asset asset} to be {@link Disposable
     * disposed}. It's up to the client to manually manage the asset to avoid it
     * getting garbage-collected if all other references go out of scope.
     *
     * @param name
     *            The name of the {@link Asset asset} to remove.
     * @throws IllegalArgumentException
     *             If the name string is empty.
     * @throws NullPointerException
     *             If the name is <code>null</code>.
     */
    void removeAssetByName(String name);

    /**
     * Requests that <code>this</code> manager stop managing <i>all</i> its
     * {@link Asset assets}.
     * <p>
     * Note that this simply removes the {@link Asset asset} from
     * <code>this</code> {@link AssetManager manager's} ownership, but does not
     * explicitly cause the {@link Asset asset} to be {@link Disposable
     * disposed}. It's up to the client to manually manage the asset to avoid it
     * getting garbage-collected if all other references go out of scope. To
     * explicitly destroy them, see {@link #notifyDispose()}
     *
     * @see #notifyDispose()
     *
     */
    void removeAllAssets();

    /**
     * Adds a {@link AssetLoader asset-loader} to <code>this</code> manager.
     *
     * @param loader
     *            The {@link AssetLoader asset-loader} being added.
     * @throws NullPointerException
     *             If the {@link AssetLoader asset-loader} is <code>null</code>.
     * @throws RuntimeException
     *             If the {@link AssetLoader asset-loader} had already been
     *             added.
     */
    void addAssetLoader(AssetLoader<T> loader);

    /**
     * Gets the {@link AssetLoader asset-loader} for the specified {@link Class
     * class}.
     *
     * @param cls
     *            The {@link Class class} the {@link AssetLoader asset-loader}
     *            is responsible for loading.
     * @return The {@link AssetLoader asset-loader} of the specified
     *         {@link Class class}, if available. Otherwise <code>null</code>.
     */
    AssetLoader<T> getAssetLoader(Class<AssetLoader<T>> cls);

    /**
     * Gets the number of {@link AssetLoader asset-loaders} currently available
     * to <code>this</code> {@link AssetManager asset-manager}.
     *
     * @return The number of {@link AssetLoader asset-loaders}.
     */
    int getAssetLoaderCount();

    /**
     * Removes the specified {@link AssetLoader asset-loader} from
     * <code>this</code> manager, if one is found to match the specified
     * {@link Class class}.
     * <p>
     * A manager without an appropriate {@link AssetLoader asset-loader} will
     * not be able to create <i>new</i> {@link Asset assets} of the specified
     * type until the required {@link AssetLoader asset-loader} is reinstated.
     * <p>
     * Note that this simply removes the {@link AssetLoader asset-loader} from
     * <code>this</code> manager. It does not cause the {@link AssetLoader
     * asset-loader} to be explicitly destroyed.
     *
     * @param cls
     *            The {@link Class class} the {@link AssetLoader asset-loader}
     *            is responsible for loading.
     *
     */
    void removeAssetLoader(Class<AssetLoader<T>> cls);

    /**
     * Removes the specified {@link AssetLoader asset-loader} from
     * <code>this</code> manager.
     * <p>
     * A manager without an appropriate {@link AssetLoader asset-loader} will
     * not be able to create <i>new</i> {@link Asset assets} of the specified
     * type until the required {@link AssetLoader asset-loader} is reinstated.
     * <p>
     * Note that this simply removes the {@link AssetLoader asset-loader} from
     * <code>this</code> manager. It does not cause the {@link AssetLoader
     * asset-loader} to be explicitly destroyed. To explicitly destroy them, see
     * {@link #notifyDispose()}
     *
     * @param loader
     *            The {@link AssetLoader asset-loader} to be removed.
     * @throws NullPointerException
     *             If the {@link AssetLoader asset-loader} is <code>null</code>.
     * @see #notifyDispose()
     */
    void removeAssetLoader(AssetLoader<T> loader);

    /**
     * Removes <i>all</i> the {@link AssetLoader asset-loaders} from
     * <code>this</code> manager.
     * <p>
     * A manager without an appropriate {@link AssetLoader asset-loader} will
     * not be able to create <i>new</i> {@link Asset assets} of the specified
     * type until the required {@link AssetLoader asset-loader} is reinstated.
     * <p>
     * Note that this simply removes the {@link AssetLoader asset-loader} from
     * <code>this</code> manager. It does not cause the {@link AssetLoader
     * asset-loader} to be explicitly destroyed.
     */
    void removeAllAssetLoaders();

}
