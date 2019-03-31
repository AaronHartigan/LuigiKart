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
import java.util.*;

import ray.rage.asset.Asset;
import ray.rage.asset.AssetLoader;
import ray.rage.asset.AssetManager;
import ray.rage.asset.LoaderClassMapper;
import ray.rage.common.*;

/**
 * An abstract {@link AssetManager asset-manager} for extension by concrete
 * implementations.
 * <p>
 * Concrete implementations are expected to simply instantiate their concrete
 * {@link Asset assets}, rather than having to be concerned with specific
 * behaviors.
 *
 * @author Raymond L. Rivera
 *
 * @see Asset
 * @see AssetLoader
 *
 */
public abstract class AbstractAssetManager<T extends Asset> implements AssetManager<T>, Disposable {

    // a map of asset loader classes to asset loader instances
    private Map<Class<AssetLoader<T>>, AssetLoader<T>> assetLoaderMap    = new HashMap<>();

    // maps paths to assets and backwards for flexibility on searches
    private Map<Path, T>                               loadedAssetsMap   = new HashMap<>();
    private Map<T, Path>                               reverseAssetsMap  = new HashMap<>();

    // not all assets are created equal; these ones have no path associated with
    // them and are keyed by their names
    private Map<String, T>                             manualAssetsMap   = new HashMap<>();

    private LoaderClassMapper                          classMapper       = new LoaderClassMapper();
    private Path                                       baseDirectoryPath = Paths.get(System.getProperty("user.dir"));

    @Override
    public void setBaseDirectoryPath(Path path) {
        if (path == null)
            throw new NullPointerException("Path is null");
        if (path.toString().isEmpty())
            throw new IllegalArgumentException("Path is empty");

        baseDirectoryPath = path;
    }

    @Override
    public void setBaseDirectoryPath(String path) {
        setBaseDirectoryPath(Paths.get(path));
    }

    @Override
    public Path getBaseDirectoryPath() {
        return baseDirectoryPath;
    }

    @Override
    public T getAsset(Path path) throws IOException {
        if (path.toString().isEmpty())
            throw new IllegalArgumentException("Asset path is empty");

        // XXX: If this causes problems, note that according to docs, the
        // behavior of Path.resolve is highly dependent on implementation and,
        // therefore, unspecified in some cases.
        Path resolvedPath = baseDirectoryPath.resolve(path);

        if (loadedAssetsMap.containsKey(resolvedPath))
            return loadedAssetsMap.get(resolvedPath);

        Class<? extends AssetLoader<? extends Asset>> loaderClass = classMapper.getLoaderClass(resolvedPath);

        if (!assetLoaderMap.containsKey(loaderClass))
            throw new RuntimeException("No loader for: " + path);

        T asset = createAssetImpl(getNameFromPath(resolvedPath));
        assetLoaderMap.get(loaderClass).loadAsset(asset, resolvedPath);

        loadedAssetsMap.put(resolvedPath, asset);
        reverseAssetsMap.put(asset, resolvedPath);
        return asset;
    }

    @Override
    public T createManualAsset(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("Name is empty");

        T asset = manualAssetsMap.get(name);
        if (asset != null)
            throw new RuntimeException("Manual asset already exists: " + name);

        asset = createAssetImpl(name);

        manualAssetsMap.put(name, asset);
        return asset;
    }

    /**
     * Abstract method to be implemented by concrete {@link AssetManager
     * asset-managers} to instantiate concrete {@link Asset assets}.
     *
     * @param name
     *            The name to be given to the {@link Asset asset}.
     * @return A new concrete {@link Asset asset} instance.
     */
    protected abstract T createAssetImpl(String name);

    @Override
    public T getAssetByPath(String path) throws IOException {
        return getAsset(Paths.get(path));
    }

    @Override
    public T getAssetByName(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("Name is empty");

        if (manualAssetsMap.containsKey(name))
            return manualAssetsMap.get(name);

        for (T t : loadedAssetsMap.values())
            if (t.getName().equals(name))
                return t;

        throw new RuntimeException("No asset named: " + name);
    }

    @Override
    public boolean hasAsset(Path path) {
        if (path.toString().isEmpty())
            throw new IllegalArgumentException("Path is empty");

        return loadedAssetsMap.containsKey(path);
    }

    @Override
    public boolean hasAssetByPath(String path) {
        return hasAsset(Paths.get(path));
    }

    @Override
    public boolean hasAssetByName(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("Name is empty");

        if (manualAssetsMap.containsKey(name))
            return true;

        for (T t : loadedAssetsMap.values())
            if (t.getName().equals(name))
                return true;

        return false;
    }

    @Override
    public int getAssetCount() {
        return loadedAssetsMap.size() + manualAssetsMap.size();
    }

    @Override
    public Iterable<T> getAssets() {
        return loadedAssetsMap.values();
    }

    @Override
    public void removeAsset(Asset asset) {
        if (asset == null)
            throw new NullPointerException("Null asset");

        if (reverseAssetsMap.containsKey(asset)) {
            Path assetPath = reverseAssetsMap.remove(asset);
            loadedAssetsMap.remove(assetPath);
        }
    }

    @Override
    public void removeAssetByPath(String pathStr) {
        if (pathStr.isEmpty())
            throw new IllegalArgumentException("Path is empty");

        Path path = Paths.get(pathStr);
        if (loadedAssetsMap.containsKey(path)) {
            T asset = loadedAssetsMap.remove(path);
            reverseAssetsMap.remove(asset);
        }
    }

    @Override
    public void removeAssetByName(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("Name is empty");

        if (manualAssetsMap.containsKey(name)) {
            manualAssetsMap.remove(name);
            return;
        }

        for (T t : loadedAssetsMap.values())
            if (t.getName().equals(name))
                removeAsset(t);
    }

    @Override
    public void removeAllAssets() {
        loadedAssetsMap.clear();
        reverseAssetsMap.clear();
        manualAssetsMap.clear();
    }

    @Override
    public void addAssetLoader(AssetLoader<T> loader) {
        if (loader == null)
            throw new NullPointerException("Null loader");

        // This cast is correct because the local variable we're creating is of
        // the same class as the input argument.
        @SuppressWarnings(value = "unchecked")
        Class<AssetLoader<T>> loaderClass = (Class<AssetLoader<T>>) loader.getClass();

        if (assetLoaderMap.containsKey(loaderClass))
            throw new RuntimeException(loaderClass.getSimpleName() + " already added");

        assetLoaderMap.put(loaderClass, loader);
    }

    @Override
    public AssetLoader<T> getAssetLoader(Class<AssetLoader<T>> cls) {
        return assetLoaderMap.get(cls);
    }

    @Override
    public int getAssetLoaderCount() {
        return assetLoaderMap.size();
    }

    @Override
    public void removeAssetLoader(Class<AssetLoader<T>> cls) {
        assetLoaderMap.remove(cls);
    }

    @Override
    public void removeAssetLoader(AssetLoader<T> loader) {
        if (loader == null)
            throw new NullPointerException("Null loader");

        // This cast is correct because the local variable we're creating is of
        // the same class as the input argument.
        @SuppressWarnings("unchecked")
        Class<AssetLoader<T>> cls = (Class<AssetLoader<T>>) loader.getClass();
        removeAssetLoader(cls);
    }

    @Override
    public void removeAllAssetLoaders() {
        assetLoaderMap.clear();
    }

    @Override
    public void notifyDispose() {
        for (T t : loadedAssetsMap.values())
            t.notifyDispose();
        removeAllAssets();

        for (AssetLoader<T> tl : assetLoaderMap.values())
            tl.notifyDispose();
        removeAllAssetLoaders();

        assetLoaderMap = null;
        loadedAssetsMap = null;
        reverseAssetsMap = null;
        manualAssetsMap = null;
        classMapper = null;
    }

    private static String getNameFromPath(Path path) {
        // get the file's name, e.g. filename.txt
        String str = path.toString();
        int i = str.lastIndexOf(File.separator);
        return str.substring(i + 1);
    }

}
