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

package ray.rage.game;

import java.awt.*;
import java.io.*;

import ray.rage.*;
import ray.rage.asset.*;
import ray.rage.asset.shader.*;
import ray.rage.game.Game;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.shader.*;
import ray.rage.scene.*;
import ray.rage.util.*;

/**
 * An abstract class with convenience protected methods to provide a basic
 * logical structure to the steps needed for a {@link Game game} to set itself
 * up, run, and clean up after itself. The method signatures provide strong
 * hints to the user about what needs to be done during a particular step as
 * well as what components are needed to accomplish it. This also means that the
 * user is free to override specific parts with more granularity (e.g. choosing
 * to load a different {@link Configuration configuration}.
 *
 * @author Raymond L. Rivera
 *
 */
public abstract class AbstractGame implements Game {

    /**
     * Starts up the {@link Engine engine} and then the {@link Game game}, in
     * that order. It's the reverse order of {@link #shutdown(Engine)}.
     *
     * @param engine
     *            The {@link Engine engine}.
     * @throws IOException
     *             If the {@link Configuration configuration} cannot be loaded.
     * @see #loadConfiguration(Configuration)
     */
    protected abstract void startup(Engine engine) throws IOException;

    /**
     * Enters the main loop.
     *
     * @param engine
     *            The {@link Engine engine}.
     */
    protected abstract void run(Engine engine);

    /**
     * Allows the {@link Game game} to perform all necessary updates prior to
     * rendering a frame.
     *
     * @param engine
     *            The {@link Engine engine}.
     */
    protected abstract void update(Engine engine);

    /**
     * Shuts down the {@link Game game} and then the {@link Engine engine}, in
     * that order. It's the reverse order of {@link #startup(Engine)}.
     *
     * @param engine
     *            The {@link Engine engine}.
     */
    protected abstract void shutdown(Engine engine);

    /**
     * Loads the {@link Game game's} {@link Configuration configuration} data
     * from the specified source.
     *
     * @param config
     *            The {@link Configuration configuration} object that holds the
     *            data.
     * @throws IOException
     *             If the {@link Configuration configuration} cannot be loaded.
     */
    protected abstract void loadConfiguration(Configuration config) throws IOException;

    /**
     * Delegates creation of a {@link RenderSystemFactory factory} responsible
     * for the specific {@link RenderSystem render-system} implementation to the
     * {@link Game client}.
     *
     * @return A {@link RenderSystemFactory factory} that allows the framework
     *         to instantiate the {@link RenderSystem render-system} to be used.
     */
    protected abstract RenderSystemFactory createRenderSystemFactory();

    /**
     * This is where the {@link Game game's} {@link GpuShaderProgram
     * shader-programs} are specified and built.
     *
     * @param rs
     *            The {@link RenderSystem render-system} that actually creates
     *            the {@link GpuShaderProgram shader-programs}.
     * @param sm
     *            The {@link ShaderManager shader-manager} to retrieve the
     *            actual source code.
     * @throws IOException
     *             If the {@link ShaderManager shader-manager} cannot find the
     *             specified {@link Asset assets}.
     * @throws RuntimeException
     *             If the at least one {@link GpuShaderProgram shader-program}
     *             fails to build.
     */
    protected abstract void setupGpuShaderPrograms(RenderSystem rs, ShaderManager sm) throws IOException;

    /**
     * This is where the {@link Game game} creates its {@link RenderWindow
     * window} based on its current {@link GraphicsEnvironment environment},
     * including whether it wants to be <a href=
     * "https://docs.oracle.com/javase/tutorial/extra/fullscreen/exclusivemode.html">Full-Screen
     * Exclusive Mode</a> or not (default).
     *
     * @param rs
     *            The {@link RenderSystem render-system} that actually creates
     *            the {@link RenderWindow render-windows} with their proper
     *            drawing surface specifications and context.
     * @param ge
     *            The platform's {@link GraphicsEnvironment
     *            graphics-environment}.
     */
    protected abstract void setupWindow(RenderSystem rs, GraphicsEnvironment ge);

    /**
     * This is where the {@link Game game} logically divides a
     * {@link RenderWindow render-window's} real-estate into {@link Viewport
     * viewports}. Most {@link RenderWindow render-windows} only need one
     * (default).
     *
     * @param rw
     *            The {@link RenderWindow render-window} having its
     *            {@link Viewport viewports} set up.
     */
    protected abstract void setupWindowViewports(RenderWindow rw);

    /**
     * Delegates creation of a {@link SceneManagerFactory factory} responsible
     * for the specific {@link SceneManager scene-manager} implementation to the
     * {@link Game client}.
     *
     * @return A {@link SceneManagerFactory factory} that allows the framework
     *         to instantiate the {@link SceneManager scene-manager} to be used.
     */
    protected abstract SceneManagerFactory createSceneManagerFactory();

    /**
     * This is where the {@link Game game} creates {@link Camera cameras},
     * connects them to {@link SceneNode scene-nodes}, specifies into which
     * {@link Viewport viewports} their contents will be drawn, and so on.
     *
     * @param sm
     *            The {@link SceneManager scene-manager} used to create
     *            {@link Camera cameras}, the {@link SceneNode scene-nodes} to
     *            which they're attached, and so on.
     * @param rw
     *            The {@link RenderWindow render-window} containing the
     *            {@link Viewport viewports} into which a the {@link SceneObject
     *            scene-objects} in the {@link Camera camera's} field of view
     *            will be drawn.
     * @see #setupScene(Engine, SceneManager)
     */
    protected abstract void setupCameras(SceneManager sm, RenderWindow rw);

    /**
     * This is where the {@link Game game} sets up a scene by creating
     * {@link Entity entities}, {@link Light lights}, and other
     * {@link SceneObject scene-objects}.
     * <p>
     * {@link SceneObject Scene-objects} must be connected to {@link SceneNode
     * scene-nodes} so that they can be positioned in the world and be
     * considered to be part of the scene. {@link SceneNode Scene-nodes} can
     * (optionally) be added to {@link Node.Controller node-controllers}.
     *
     * @param engine
     *            The {@link Engine engine}.
     * @param sm
     *            The {@link SceneManager scene-manager}
     * @throws IOException
     *             If an {@link AssetManager asset-manager} cannot find the
     *             specified {@link Asset assets} in the file system.
     */
    protected abstract void setupScene(Engine engine, SceneManager sm) throws IOException;
}
