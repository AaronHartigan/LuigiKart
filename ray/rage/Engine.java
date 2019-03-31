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

package ray.rage;

import java.util.*;

import ray.rage.Engine;
import ray.rage.asset.animation.AnimationManager;
import ray.rage.asset.animation.loaders.*;
import ray.rage.asset.material.*;
import ray.rage.asset.material.loaders.*;
import ray.rage.asset.mesh.*;
import ray.rage.asset.mesh.loaders.*;
import ray.rage.asset.shader.*;
import ray.rage.asset.shader.loaders.*;
import ray.rage.asset.skeleton.SkeletonManager;
import ray.rage.asset.skeleton.loaders.*;
import ray.rage.asset.texture.*;
import ray.rage.asset.texture.loaders.*;
import ray.rage.common.*;
import ray.rage.rendersystem.*;
import ray.rage.scene.*;
import ray.rage.util.*;

/**
 * The <i>engine</i> class is a singleton object and the main point of access to
 * the framework and its sub-systems.
 * <p>
 * From here, the application can gain access to other framework components
 * including {@link RenderSystem render-systems}, {@link SceneManager
 * scene-management}, and other functionality at a higher level of abstraction.
 * It also doubles as an example of the
 * <a href="https://en.wikipedia.org/wiki/Facade_pattern">Facade pattern</a>.
 * This allows users to more easily do certain things, while also making
 * lower-level sub-system interfaces accessible.
 * <p>
 * Note that this class avoids following the singleton <i>pattern</i> to avoid
 * introducing global objects/states into the framework and the applications
 * that use it.
 *
 * @author Raymond L. Rivera
 *
 */
public final class Engine implements Manageable {

    private Map<RenderSystem.API, RenderSystem>         renderSystemMap;
    private RenderSystem                                activeRenderSystem;

    private Map<SceneManager.Environment, SceneManager> sceneManagerMap;
    private SceneManager                                activeSceneManager;

    private Configuration                               configuration;
    private MeshManager                                 meshManager;
    private SkeletonManager                             skeletonManager;
    private AnimationManager                            animationManager;
    private MaterialManager                             materialManager;
    private ShaderManager                               shaderManager;
    private TextureManager                              textureManager;

    private float                                       elapsedTimeMillis;

    public Engine() {}

    @Override
    public void startup() {
        renderSystemMap = new HashMap<>();
        sceneManagerMap = new HashMap<>();

        configuration = new Configuration();

        meshManager = new MeshManager();
        meshManager.addAssetLoader(new WavefrontMeshLoader());
        meshManager.addAssetLoader(new SkeletalMeshLoader());

        skeletonManager = new SkeletonManager();
        skeletonManager.addAssetLoader(new SkeletalSkeletonLoader());

        animationManager = new AnimationManager();
        animationManager.addAssetLoader(new SkeletalAnimationLoader());

        materialManager = new MaterialManager();
        materialManager.addAssetLoader(new WavefrontMaterialLoader());

        shaderManager = new ShaderManager();
        shaderManager.addAssetLoader(new GlslShaderLoader());

        textureManager = new TextureManager();
        textureManager.addAssetLoader(new RgbaTextureLoader());
    }

    /**
     * Notifies the {@link Engine engine} about the elapsed time since the last
     * update.
     * <p>
     * <b>WARNING:</b> <i>This method is meant to be used internally by the
     * framework and not by game clients. Use directly only if you really know
     * what you're doing.</i>
     *
     * @param elapsedTimeMs
     *            The elapsed time, in milliseconds.
     * @throws IllegalArgumentException
     *             If the time is negative.
     */
    public void notifyElapsedTimeMillis(float elapsedTimeMs) {
        if (elapsedTimeMs < 0)
            throw new IllegalArgumentException("Elapsed time < 0");

        elapsedTimeMillis = elapsedTimeMs;
    }

    /**
     * Gets the elapsed time since the last set of updates.
     *
     * @return The elapsed time, in milliseconds.
     * @see #notifyElapsedTimeMillis(float)
     */
    public float getElapsedTimeMillis() {
        return elapsedTimeMillis;
    }

    /**
     * Registers a new {@link SceneManager scene-manager} for later use. This
     * may be any built-in implementation or one provided by you. If providing
     * your own, be sure to use the appropriate {@link SceneManager.Environment
     * environment}.
     * <p>
     * To actually <i>use</i> the {@link SceneManager scene-manager}, it needs
     * to be made "active" with
     * {@link #setActiveSceneManager(SceneManager.Environment)}.
     *
     * @param sm
     *            The {@link SceneManager scene-manager} to be registered.
     * @throws RuntimeException
     *             If a {@link SceneManager scene-manager} of type
     *             {@link SceneManager.Environment environment} has already been
     *             registered.
     * @throws NullPointerException
     *             If the {@link SceneManager scene-manager} is
     *             <code>null</code>.
     * @see #setActiveSceneManager(SceneManager.Environment)
     */
    public void registerSceneManager(SceneManager sm) {
        if (sm == null)
            throw new NullPointerException("Null scene manager");

        final SceneManager.Environment env = sm.getEnvironment();
        if (sceneManagerMap.containsKey(env))
            throw new RuntimeException(env + " " + SceneManager.class.getSimpleName() + " already added");

        sm.setMeshManager(meshManager);
        sm.setSkeletonManager(skeletonManager);
        sm.setAnimationManager(animationManager);
        sm.setMaterialManager(materialManager);
        sm.setTextureManager(textureManager);
        sm.setConfiguration(configuration);

        sceneManagerMap.put(env, sm);
    }

    /**
     * Specifies the {@link SceneManager.Environment environment} that should be
     * in use by the system to actively manage scenes.
     *
     * @param env
     *            The {@link SceneManager.Environment environment} to use.
     * @throws RuntimeException
     *             If a {@link SceneManager scene-manager} of the specified
     *             {@link SceneManager.Environment environment} has not been
     *             registered.
     * @see #registerSceneManager(SceneManager)
     */
    public void setActiveSceneManager(SceneManager.Environment env) {
        if (!sceneManagerMap.containsKey(env))
            throw new RuntimeException(env + " " + SceneManager.class.getSimpleName() + " not yet registered");

        activeSceneManager = sceneManagerMap.get(env);
    }

    /**
     * Gets the {@link SceneManager scene-manager} that's currently active.
     * Otherwise, <code>null</code>
     *
     * @return The active {@link SceneManager scene-manager}. Otherwise
     *         <code>null</code>.
     */
    public SceneManager getSceneManager() {
        return activeSceneManager;
    }

    /**
     * Registers a new {@link RenderSystem render-system} for later use. This
     * may be any built-in implementation or one provided by you.
     * <p>
     * To actually <i>use</i> the system, it needs to be made "active" with
     * {@link #setActiveRenderSystem(RenderSystem.API)}.
     *
     * @param rs
     *            The {@link RenderSystem render-system} to be registered.
     * @throws RuntimeException
     *             If a {@link RenderSystem render-system} of type
     *             {@link RenderSystem.API} has already been registered.
     * @throws NullPointerException
     *             If the {@link RenderSystem render-system} is
     *             <code>null</code>.
     * @see #setActiveRenderSystem(RenderSystem.API)
     */
    public void registerRenderSystem(RenderSystem rs) {
        if (rs == null)
            throw new NullPointerException("Null " + RenderSystem.class.getSimpleName());

        if (renderSystemMap.containsKey(rs.getAPI()))
            throw new RuntimeException(rs.getAPI() + " " + RenderSystem.class.getSimpleName() + " already added");

        renderSystemMap.put(rs.getAPI(), rs);
    }

    /**
     * Specifies the {@link RenderSystem.API} that should be in use by the
     * system to actively render frames.
     *
     * @param api
     *            The {@link RenderSystem.API} to use.
     * @throws RuntimeException
     *             If a {@link RenderSystem renderer} of type
     *             {@link RenderSystem.API} has not been registered.
     * @see #registerRenderSystem(RenderSystem)
     */
    public void setActiveRenderSystem(RenderSystem.API api) {
        if (!renderSystemMap.containsKey(api))
            throw new RuntimeException(api + " " + RenderSystem.class.getSimpleName() + " not yet registered");

        activeRenderSystem = renderSystemMap.get(api);
    }

    /**
     * Gets the {@link RenderSystem render-system} that's currently active.
     * Otherwise <code>null</code>.
     *
     * @return The active {@link RenderSystem render-system}. Otherwise,
     *         <code>null</code>
     */
    public RenderSystem getRenderSystem() {
        return activeRenderSystem;
    }

    /**
     * Gets the current {@link MeshManager mesh-manager}, if one has been set.
     *
     * @return The current {@link MeshManager mesh-manager}. Otherwise,
     *         <code>null</code>
     */
    public MeshManager getMeshManager() {
        return meshManager;
    }

    /**
     * Gets the current {@link AnimationManager animation-manager}, if one has been set.
     *
     * @return The current {@link AnimationManager animation-manager}. Otherwise,
     *         <code>null</code>
     */
    public AnimationManager getAnimationManager() {
        return animationManager;
    }

    /**
     * Gets the current {@link SkeletonManager skeleton-manager}, if one has been set.
     *
     * @return The current {@link SkeletonManager skeleton-manager}. Otherwise,
     *         <code>null</code>
     */
    public SkeletonManager getSkeletonManager() {
        return skeletonManager;
    }

    /**
     * Gets the current {@link MaterialManager material-manager}.
     *
     * @return The {@link MaterialManager material-manager}. Otherwise,
     *         <code>null</code>
     */
    public MaterialManager getMaterialManager() {
        return materialManager;
    }

    /**
     * Gets the current {@link ShaderManager shader-manager}.
     *
     * @return The {@link ShaderManager shader-manager}. Otherwise,
     *         <code>null</code>
     */
    public ShaderManager getShaderManager() {
        return shaderManager;
    }

    /**
     * Gets the current {@link TextureManager texture-manager}.
     *
     * @return The {@link TextureManager texture-manager}. Otherwise,
     *         <code>null</code>
     */
    public TextureManager getTextureManager() {
        return textureManager;
    }

    /**
     * Gets the current {@link Configuration configuration}.
     *
     * @return The {@link Configuration configuration}. Otherwise,
     *         <code>null</code>
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Causes the {@link SceneManager scene-manager} to update any registered
     * {@link Node.Controller node-controllers}, update the {@link SceneNode
     * scene-nodes} in the graph, and send data into the {@link RenderSystem
     * render-system} pipeline for rendering.
     */
    public void renderOneFrame() {
        activeSceneManager.updateControllers(elapsedTimeMillis);
        activeSceneManager.updateSceneGraph();
        activeSceneManager.renderScene();
    }

    @Override
    public void shutdown() {
        // INFO: destroy the scene manager before the render system to allow
        // render system-dependent resources to be destroyed before destroying
        // the render system itself (you'll get hit by a race condition if you
        // don't)
        for (SceneManager sm : sceneManagerMap.values())
            sm.notifyDispose();
        sceneManagerMap.clear();

        for (RenderSystem rs : renderSystemMap.values())
            rs.notifyDispose();
        renderSystemMap.clear();

        meshManager.notifyDispose();
        skeletonManager.notifyDispose();
        animationManager.notifyDispose();
        materialManager.notifyDispose();
        shaderManager.notifyDispose();
        textureManager.notifyDispose();

        sceneManagerMap = null;
        renderSystemMap = null;
        configuration = null;
        meshManager = null;
        skeletonManager = null;
        animationManager = null;
        materialManager = null;
        shaderManager = null;
        textureManager = null;
        activeRenderSystem = null;
        activeSceneManager = null;
    }

}
