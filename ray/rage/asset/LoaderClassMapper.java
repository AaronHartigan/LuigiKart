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

import java.nio.file.*;
import java.util.*;

import ray.rage.asset.Asset;
import ray.rage.asset.AssetLoader;
import ray.rage.asset.AssetManager;
import ray.rage.asset.animation.loaders.*;
import ray.rage.asset.material.loaders.*;
import ray.rage.asset.mesh.loaders.*;
import ray.rage.asset.shader.loaders.*;
import ray.rage.asset.skeleton.loaders.*;
import ray.rage.asset.texture.loaders.*;

/**
 * A package-private utility class that maps common file extensions to specific
 * {@link Class classes}, allowing {@link AssetManager asset-managers} to choose
 * the correct {@link AssetLoader asset-loaders} for the {@link Asset assets}
 * they need to process.
 *
 * @author Raymond L. Rivera
 *
 */
final class LoaderClassMapper {

    private final Map<String, Class<? extends AssetLoader<? extends Asset>>> classMap = new HashMap<>();

    public LoaderClassMapper() {
        classMap.put(".jpg", RgbaTextureLoader.class);
        classMap.put(".jpeg", RgbaTextureLoader.class);
        classMap.put(".png", RgbaTextureLoader.class);

        classMap.put(".vert", GlslShaderLoader.class);
        classMap.put(".tesc", GlslShaderLoader.class);
        classMap.put(".tese", GlslShaderLoader.class);
        classMap.put(".geom", GlslShaderLoader.class);
        classMap.put(".frag", GlslShaderLoader.class);
        classMap.put(".glsl", GlslShaderLoader.class);

        classMap.put(".obj", WavefrontMeshLoader.class);
        classMap.put(".mesh.xml", null);

        classMap.put(".mtl", WavefrontMaterialLoader.class);
        classMap.put(".material", null);

        // For Skeletal entity types
        classMap.put(".rkm", SkeletalMeshLoader.class);
        classMap.put(".rks", SkeletalSkeletonLoader.class);
        classMap.put(".rka", SkeletalAnimationLoader.class);
    }

    public Class<? extends AssetLoader<? extends Asset>> getLoaderClass(String path) {
        if (path == null)
            throw new NullPointerException("Null path");
        if (path.isEmpty())
            throw new IllegalArgumentException("Empty path");

        int idx = path.lastIndexOf(".");
        if (idx == -1)
            throw new IllegalArgumentException("Missing file extension: " + path);

        // If you have problems with file extensions not being found, make sure
        // they're not being caused by locale settings due to locale-sensitive
        // strings in your system. Check javadocs for more information.
        String ext = path.substring(idx).toLowerCase(Locale.ROOT);
        Class<? extends AssetLoader<? extends Asset>> loaderClass = classMap.get(ext);
        if (loaderClass == null)
            throw new RuntimeException("Unknown file extension: " + path);

        return loaderClass;
    }

    public Class<? extends AssetLoader<? extends Asset>> getLoaderClass(Path path) {
        return getLoaderClass(path.toString());
    }

}
