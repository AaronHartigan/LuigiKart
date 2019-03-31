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
import java.nio.*;

import ray.rage.common.*;
import ray.rage.rendersystem.RenderSystem;
import ray.rage.rendersystem.RenderWindow;
import ray.rage.rendersystem.Viewport;
import ray.rage.scene.*;

/**
 * A <i>viewport</i> is a 2D rectangle that defines the size of the rendering
 * surface onto which a 3D scene being rendered from a {@link Camera camera's}
 * point of view is projected.
 * <p>
 * Most of the time, the region occupies the whole surface of the render target,
 * but this does not need to be the case. The viewport can also be a portion, or
 * sub-set, of the target (e.g. split screen multi-player).
 * <p>
 * The viewport works as a "connection bridge" across packages. It "connects"
 * the source of an image, provided by a {@link Camera camera}, and the target
 * surface onto which the {@link RenderSystem render-system} will be drawing
 * this image, specified by a {@link Canvas canvas}.
 *
 * @author Raymond L. Rivera
 *
 */
public interface Viewport extends Disposable {

    /**
     * Listener interface to notify clients of events affecting {@link Viewport
     * viewports}.
     *
     * @author Raymond L. Rivera
     *
     */
    public interface Listener {

        /**
         * A different {@link Camera camera} has been assigned to the
         * {@link Viewport viewport}.
         *
         * @param vp
         *            The {@link Viewport viewport} that had the camera changed.
         */
        void onViewportCameraChanged(Viewport vp);

        /**
         * Different <i>relative</i> dimensions have been set for the
         * {@link Viewport viewport}.
         *
         * @param vp
         *            The {@link Viewport viewport} that had the dimensions
         *            updated.
         */
        void onViewportDimensionsChanged(Viewport vp);

    }

    /**
     * Adds a new {@link Viewport.Listener viewport-listener} for events
     * affecting <code>this</code> {@link Viewport viewport}.
     *
     * @param vpl
     *            The {@link Viewport.Listener viewport-listener} being added.
     */
    void addListener(Viewport.Listener vpl);

    /**
     * Removes the specified {@link Viewport.Listener viewport-listener}.
     *
     * @param vpl
     *            The {@link Viewport.Listener viewport-listener} to remove.
     */
    void removeListener(Viewport.Listener vpl);

    /**
     * Removes all {@link Viewport.Listener viewport-listeners} currently
     * registered with <code>this</code> {@link Viewport viewport}.
     */
    void removeAllListeners();

    /**
     * Sets the coordinates of the <i>viewable area</i> in relative window
     * coordinates.
     * <p>
     * This method causes {@link #notifyDimensionsChanged()} to be invoked.
     *
     * @param bottom
     *            The lower portion of the {@link Viewport viewport}.
     * @param left
     *            The left portion of the {@link Viewport viewport}.
     * @param width
     *            The width of the {@link Viewport viewport}.
     * @param height
     *            The height of the {@link Viewport viewport}.
     * @param overrideScissor
     *            True if the values should <i>also</i> be used to define the
     *            scissor box. False otherwise.
     * @throws IllegalArgumentException
     *             If any of the values are outside the <code>[0, 1]</code>
     *             range.
     * @see #setDimensions(float, float, float, float)
     * @see #setScissors(float, float, float, float)
     * @see #notifyDimensionsChanged()
     */
    void setDimensions(float bottom, float left, float width, float height, boolean overrideScissor);

    /**
     * Sets the coordinates of the <i>viewable area</i> in relative window
     * coordinates. This automatically overrides the current scissor values.
     * <p>
     * This method causes {@link #notifyDimensionsChanged()} to be invoked.
     *
     * @param bottom
     *            The lower portion of the {@link Viewport viewport}.
     * @param left
     *            The left portion of the {@link Viewport viewport}.
     * @param width
     *            The width of the {@link Viewport viewport}.
     * @param height
     *            The height of the {@link Viewport viewport}.
     * @throws IllegalArgumentException
     *             If any of the values are outside the <code>[0, 1]</code>
     *             range.
     * @see #setDimensions(float, float, float, float, boolean)
     * @see #setScissors(float, float, float, float)
     * @see #notifyDimensionsChanged()
     */
    void setDimensions(float bottom, float left, float width, float height);

    /**
     * Sets the coordinates of the <i>scissor box</i> in relative window
     * coordinates. The rectangle defined by this box must either be of the
     * <i>same size</i> as the {@link Viewport viewport} or lie <i>inside</i> of
     * it.
     * <p>
     * This method causes {@link #notifyDimensionsChanged()} to be invoked.
     *
     * @param bottom
     *            The lower portion of the box.
     * @param left
     *            The left portion of the box.
     * @param width
     *            The width of the box.
     * @param height
     *            The height of the box.
     * @throws IllegalArgumentException
     *             If any of the values are outside the <code>[0, 1]</code>
     *             range or outside of the {@link Viewport viewport} dimensions.
     * @see #setDimensions(float, float, float, float)
     * @see #setDimensions(float, float, float, float, boolean)
     * @see #notifyDimensionsChanged()
     */
    void setScissors(float bottom, float left, float width, float height);

    /**
     * Lets <code>this</code> {@link Viewport viewport} know that the dimensions
     * of the rendering target have changed. For example, the
     * {@link RenderWindow} uses it to inform the {@link Viewport viewport} that
     * it has been resized, but it's not limited to that use. If the
     * {@link Viewport viewport's} dimensions are modified directly, then this
     * method is also invoked automatically.
     * <p>
     * Any existing {@link Viewport.Listener viewport-listeners} are also
     * notified via
     * {@link Viewport.Listener#onViewportDimensionsChanged(Viewport)}.
     * <p>
     * <b>WARNING:</b> <i>This method is meant to be used internally by the
     * framework and not by game clients. Use directly only if you really know
     * what you're doing.</i>
     */
    void notifyDimensionsChanged();

    /**
     * Specifies the {@link Camera camera} that will be used to render into
     * <code>this</code> {@link Viewport viewport}.
     * <p>
     * This also notifies the {@link Camera camera} that a {@link Viewport
     * viewport} has been assigned to it via
     * {@link Camera#notifyViewport(Viewport)}, notifies
     * {@link Viewport.Listener viewport-listeners} the {@link Camera camera}
     * for <code>this</code> {@link Viewport viewport} has changed via
     * {@link Viewport.Listener#onViewportCameraChanged(Viewport)}, and
     * automatically adjusts the {@link Camera camera's} aspect ratio if the
     * {@link Camera#getAutoAspectRatio()} returns <code>true</code>.
     *
     * @param cam
     *            The {@link Camera camera} that will be rendering the scene
     *            through <code>this</code> {@link Viewport viewport}.
     */
    void setCamera(Camera cam);

    /**
     * Gets the {@link Camera camera} used to render the scene through
     * <code>this</code> {@link Viewport viewport}, if any. Null otherwise.
     *
     * @return The {@link Camera camera} currently assigned to use
     *         <code>this</code> {@link Viewport viewport}.
     */
    Camera getCamera();

    /**
     * Gets the target drawing surface <code>this</code> {@link Viewport
     * viewport} allows {@link Camera cameras} to render scenes into.
     *
     * @return The {@link Canvas} onto which drawing operations are performed.
     */
    Canvas getTarget();

    /**
     * Gets the left coordinate position of <code>this</code> {@link Viewport
     * viewport}, in pixels.
     *
     * @return The actual left of the {@link Viewport viewport}, in pixels.
     */
    int getActualLeft();

    /**
     * Gets the bottom coordinate position of <code>this</code> {@link Viewport
     * viewport}, in pixels.
     *
     * @return The actual bottom of the {@link Viewport viewport}, in pixels.
     */
    int getActualBottom();

    /**
     * Gets the width of <code>this</code> {@link Viewport viewport}, in pixels.
     *
     * @return The actual width of the {@link Viewport viewport}, in pixels.
     */
    int getActualWidth();

    /**
     * Gets the height of <code>this</code> {@link Viewport viewport}, in
     * pixels.
     *
     * @return The actual height of the {@link Viewport viewport}, in pixels.
     */
    int getActualHeight();

    /**
     * Gets the left coordinate position of <code>this</code> {@link Viewport
     * viewport}'s scissor box, in pixels.
     *
     * @return The left location of the scissor box, in pixels.
     */
    int getActualScissorLeft();

    /**
     * Gets the bottom coordinate position of <code>this</code> {@link Viewport
     * viewport}'s scissor box, in pixels.
     *
     * @return The bottom location of the scissor box, in pixels.
     */
    int getActualScissorBottom();

    /**
     * Gets the width of <code>this</code> {@link Viewport viewport's} scissor
     * box, in pixels.
     *
     * @return The width of the scissor box, in pixels.
     */
    int getActualScissorWidth();

    /**
     * Gets the height of <code>this</code> {@link Viewport viewport's} scissor
     * box, in pixels.
     *
     * @return The height of the scissor box, in pixels.
     */
    int getActualScissorHeight();

    /**
     * Sets the color to use when clearing the background, or color buffer, in
     * the RGBA color space. The final color is produced by interpolating all
     * the components.
     *
     * @param red
     *            The red component.
     * @param green
     *            The green component.
     * @param blue
     *            The blue component.
     * @param alpha
     *            The value for the alpha component. A value of 1 means its
     *            fully opaque and 0 means fully transparent.
     * @throws IllegalArgumentException
     *             If any of the values is outside the <code>[0, 1]</code>
     *             range.
     * @see #setClearColor(Color)
     */
    void setClearColor(float red, float green, float blue, float alpha);

    /**
     * Sets the {@link Color} to use when clearing the background, or color
     * buffer, in the <code>sRGB</code> {@link ColorSpace}.
     *
     * @param c
     *            The background {@link Color}
     * @see #setClearColor(float, float, float, float)
     */
    void setClearColor(final Color c);

    /**
     * Gets the {@link Color} used for clearing the color buffer.
     *
     * @return The {@link Color} for the background buffer.
     * @see #getClearColorBuffer()
     */
    Color getClearColor();

    /**
     * Gets the value to clear the color buffer as a {@link FloatBuffer}.
     *
     * @return A {@link FloatBuffer} with the {@link Color} for the background
     *         buffer.
     * @see #getClearColor()
     */
    FloatBuffer getClearColorBuffer();

    /**
     * Sets the value to clear the depth buffer.
     *
     * @param depth
     *            The value to clear the depth buffer.
     * @throws IllegalArgumentException
     *             If the value is outside the <code>[0, 1]} range.
     */
    void setClearDepth(float depth);

    /**
     * Gets the value to clear the depth buffer.
     *
     * @return A scalar value within the <code>[0, 1]</code> range.
     * @see #getClearDepthBuffer()
     */
    float getClearDepth();

    /**
     * Gets the value to clear the depth buffer as a {@link FloatBuffer}.
     *
     * @return A {@link FloatBuffer} with a scalar value in the
     *         <code>[0, 1]</code> range.
     * @see #getClearDepth()
     */
    FloatBuffer getClearDepthBuffer();

}
