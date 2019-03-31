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

package ray.rage.rendersystem;

import java.awt.*;
import java.util.List;

import ray.rage.common.*;
import ray.rage.rendersystem.RenderQueue;
import ray.rage.rendersystem.RenderSystem;
import ray.rage.rendersystem.RenderWindow;
import ray.rage.rendersystem.Renderable;
import ray.rage.rendersystem.Viewport;
import ray.rage.rendersystem.shader.*;
import ray.rage.rendersystem.states.*;
import ray.rage.scene.*;
import ray.rml.*;

/**
 * A <i>render system</i> defines the functionality of a low-level 3D API.
 * <p>
 * Low-level APIs, such as OpenGL or Vulkan, are meant to be thin abstraction
 * layers between the platform and the actual graphics processing hardware. It
 * is at this level that less abstracted concepts such as raw vertex geometry,
 * data buffers, drawing surfaces, frame buffer swapping, view/projection
 * matrices, coordinate system handedness, and {@link DisplayMode display-modes}
 * become more relevant.
 * <p>
 * The <i>render system</i> in this framework is intentionally decoupled from
 * knowledge about the contents, organization, and structure of a scene, which
 * are higher-level concepts delegated to the {@link SceneManager
 * scene-manager}. Rather, render systems take care of the "dirty work", i.e.
 * lower-level details of how to deal with graphics hardware, GPU memory
 * buffers, raw data storage, {@link RenderState rendering-states}, drawing
 * surfaces, and {@link GpuShaderProgram shader-programs}, among others.
 * <p>
 * The key concept that bridges the gap between the {@link SceneManager
 * scene-manager} and low-level rendering, allowing both of them to be decoupled
 * from each other, is the {@link Renderable} object. These are the objects that
 * {@link RenderSystem render-systems} can process and, ultimately, draw on
 * screen.
 * <p>
 * Concrete implementations must have automatic back buffer swapping <i>disabled
 * by default</i>. The decision of when to swap a back buffer is meant to be
 * made at a higher-level. For example, this would allow a {@link RenderWindow
 * render-window} with multiple {@link Viewport viewports} to have the contents
 * of each {@link Viewport viewport} completely rendered before presenting the
 * full result to the screen, rather than swapping the back buffers repeatedly
 * for the partial results of each {@link Viewport viewport} during a single
 * pass.
 *
 * @author Raymond L. Rivera
 *
 */
public interface RenderSystem extends Disposable {

    /**
     * The <i>API</i> is the <a href=
     * "https://en.wikipedia.org/wiki/Application_programming_interface">application
     * programming interface</a> the {@link RenderSystem render-system} is
     * expected to use for its interactions with the GPU.
     * <p>
     * Currently, only an {@link #OPENGL_4}-based implementation is available,
     * but others may be added in the future, such as Vulkan.
     *
     * @author Raymond L. Rivera
     *
     */
    enum API {
        /**
         * The {@link RenderSystem render-system} is based on the OpenGL 4 API.
         */
        OPENGL_4
    }

    /**
     * The <i>capabilities</i> represent a description of the attributes
     * supported by a {@link RenderSystem render-system}.
     *
     * @author Raymond L. Rivera
     *
     */
    interface Capabilities {

        /**
         * Gets the number of texture units supported by in the graphics card.
         *
         * @return The number of texture units.
         */
        int getTextureUnitCount();

    }

    /**
     * Gets the {@link RenderSystem.API} of implementation this
     * {@link RenderSystem render-system} is based on.
     *
     * @return The {@link RenderSystem.API}.
     */
    API getAPI();

    /**
     * Gets the {@link Canvas canvas} on which drawing operations are performed.
     *
     * @return The {@link Canvas canvas}.
     */
    Canvas getCanvas();

    /**
     * Creates a {@link RenderWindow render-window} with the current
     * {@link DisplayMode display-mode} of the default {@link GraphicsDevice
     * screen-device}.
     *
     * @param fullScreen
     *            <code>true</code> if requesting <a href=
     *            "https://docs.oracle.com/javase/tutorial/extra/fullscreen/exclusivemode.html">Full-Screen
     *            Exclusive Mode</a>. Otherwise <code>false</code>.
     * @return A {@link RenderWindow render-window}.
     * @see #createRenderWindow(DisplayMode, boolean)
     */
    RenderWindow createRenderWindow(boolean fullScreen);

    /**
     * Creates a {@link RenderWindow render-window} with the specified
     * {@link DisplayMode display-mode}.
     *
     * @param displayMode
     *            The {@link DisplayMode display-mode} being requested.
     * @param fullScreen
     *            <code>true</code> if requesting <a href=
     *            "https://docs.oracle.com/javase/tutorial/extra/fullscreen/exclusivemode.html">Full-Screen
     *            Exclusive Mode</a>. Otherwise <code>false</code>.
     * @return A {@link RenderWindow render-window}.
     * @see #createRenderWindow(boolean)
     */
    RenderWindow createRenderWindow(DisplayMode displayMode, boolean fullScreen);

    /**
     * Creates a new {@link RenderQueue render-queue}.
     *
     * @return The {@link RenderQueue render-queue}.
     */
    RenderQueue createRenderQueue();

    /**
     * Gets whether the {@link RenderSystem render-system} is ready for use or
     * not.
     *
     * @return <code>true</code> if the {@link RenderSystem render-system} is
     *         ready for use. Otherwise <code>false</code>.
     */
    boolean isInitialized();

    /**
     * Clears a section of the back buffer as defined by the dimensions of the
     * specified {@link Viewport viewport}.
     *
     * @param vp
     * @throws NullPointerException
     *             If the {@link Viewport viewport} is <code>null</code>.
     */
    void clearViewport(Viewport vp);

    /**
     * Gets the {@link RenderSystem render-system} to swap the front and back
     * buffers in order to display a completed scene.
     * <p>
     * Drawing operations take place in an off-screen buffer, called the "back
     * buffer", in order to avoid displaying scenes that are only
     * <i>partially</i> drawn and other artifacts (e.g. flickering) to the
     * viewer. See <a href=
     * "https://en.wikipedia.org/wiki/Multiple_buffering#Double_buffering_in_computer_graphics">Multiple
     * Buffering in Computer Graphics</a>.
     */
    void swapBuffers();

    /**
     * Gets the {@link RenderWindow render-window} created by <code>this</code>.
     * If a {@link RenderWindow render-window} has not yet been created,
     * <code>null</code> is returned.
     *
     * @return The {@link RenderWindow render-window}, if it exists. Otherwise
     *         <code>null</code>.
     * @see #createRenderWindow(boolean)
     * @see #createRenderWindow(DisplayMode, boolean)
     */
    RenderWindow getRenderWindow();

    /**
     * Gets the {@link RenderSystemCaps capabilities} of <code>this</code>
     * {@link RenderSystem render-system}, if <code>this</code> has been
     * initialized.
     *
     * @return The {@link RenderSystemCaps capabilities} of <code>this</code>
     *         {@link RenderSystem render-system}, if <code>this</code> has been
     *         initialized. Otherwise <code>null</code>.
     */
    Capabilities getCapabilities();

    /**
     * Submits the {@link Renderable renderables} in the given
     * {@link RenderQueue render-queue} to the graphics pipeline so that they
     * can be rendered on screen.
     *
     * @param rq
     *            The {@link RenderQueue render-queue} with the
     *            {@link Renderable renderables} to be rendered.
     * @param view
     *            The view {@link Matrix4 matrix}.
     * @param proj
     *            The projection {@link Matrix4 matrix}.
     * @throws NullPointerException
     *             If any of the arguments is <code>null</code>.
     * @throws RuntimeException
     *             If an unexpected problem arises during rendering. Note that
     *             these may be thrown from a separate thread, depending on
     *             implementation.
     */
    void processRenderQueue(RenderQueue rq, Viewport vp, Vector3 pos, Matrix4 view, Matrix4 proj);

    /**
     * Creates a {@link RenderSystem render-system}-specific {@link RenderState
     * render-state} that can be assigned to, and applied by, a
     * {@link Renderable renderable} when it's being rendered.
     *
     * @param type
     *            The {@link RenderState.Type type}.
     * @return A new @link RenderState render-state}.
     * @throws NullPointerException
     *             If the {@link RenderState.Type type} is <code>null</code>.
     * @throws RuntimeException
     *             If the {@link RenderState.Type type} has not been implemented
     *             by the {@link RenderSystem render-system}.
     */
    RenderState createRenderState(RenderState.Type type);

    /**
     * Creates a {@link RenderSystem render-system}-specific
     * {@link GpuShaderProgram shader-program} meant to be associated with a
     * {@link Renderable renderable}.
     * <p>
     * {@link GpuShaderProgram GPU shader-programs} are required to render
     * {@link Renderable renderables}. Any {@link Renderable renderable}
     * submitted to the {@link RenderSystem render-system} without an associated
     * {@link GpuShaderProgram shader-program} will be logged and skipped.
     *
     * @param type
     *            {@link GpuShaderProgram.Type type}.
     * @return A {@link GpuShaderProgram shader-program}.
     * @throws IllegalArgumentException
     *             If the name is empty.
     * @throws RuntimeException
     *             If the name has already been used.
     */
    GpuShaderProgram createGpuShaderProgram(GpuShaderProgram.Type type);

    /**
     * Gets an existing {@link GpuShaderProgram shader-program} by the specified
     * {@link GpuShaderProgram.Type type}.
     *
     * @param type
     *            The {@link GpuShaderProgram.Type type} used when creating the
     *            {@link GpuShaderProgram shader-program}.
     * @return The {@link GpuShaderProgram shader-program}.
     * @throws RuntimeException
     *             If a {@link GpuShaderProgram shader-program} by the specified
     *             {@link GpuShaderProgram.Type type} does not exist.
     * @see #createGpuShaderProgram(String, GpuShaderProgram.Type)
     */
    GpuShaderProgram getGpuShaderProgram(GpuShaderProgram.Type type);

    /**
     * Sets the {@link AmbientLight ambient-light} to be used for global scene
     * illumination.
     *
     * @param ambientLight
     *            The {@link AmbientLight ambient-light}.
     */
    void setAmbientLight(AmbientLight ambientLight);

    /**
     * Sets the {@link Light light} source to be used for rendering.
     *
     * @param lights
     *            The {@link Light light}.
     */
    void setActiveLights(List<Light> lights);

    /**
     * Sets the Heads-Up-Display (HUD #1) to the specified string at the specified location.
     * The X and Y location is (0,0) at the lower left of the window.
     */
    public void setHUD(String string, int x, int y);
    
    /**
     * Sets the Heads-Up-Display (HUD #1) to the specified string.
     * The location of the string does not change, or it is at the default location.
     */
    public void setHUD(String string);
    
    /**
     * Sets the Heads-Up-Display (HUD #2) to the specified string at the specified location.
     * The X and Y location is (0,0) at the lower left of the window.
     */
    public void setHUD2(String string, int x, int y);
    
    /**
     * Sets the Heads-Up-Display (HUD #2) to the specified string.
     * The location of the string does not change, or it is at the default location.
     */
    public void setHUD2(String string);
}
