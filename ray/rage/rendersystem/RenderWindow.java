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
import java.awt.event.*;

import ray.rage.common.*;
import ray.rage.rendersystem.RenderWindow;
import ray.rage.rendersystem.Viewport;

/**
 * A <i>render window</i> is what's presented as the main application widget for
 * user interaction.
 * <p>
 * Windows contain a drawing surface called a {@link Canvas canvas}. A
 * {@link Canvas canvas} is where the results of rendering operations are
 * performed. Windows can also be sub-divided into, and act as containers for,
 * several {@link Viewport viewports}. Windows have only one {@link Viewport
 * viewport} by default, but the client application can add more as needed.
 *
 * @author Raymond L. Rivera
 *
 */
public interface RenderWindow extends Visible, Disposable {

    /**
     * Creates a new {@link Viewport viewport} for <code>this</code> based on
     * the specified dimensions. These dimensions are in screen-space and must
     * be in the <code>[0, 1]</code> range.
     *
     * @param bottom
     *            The relative starting position of the {@link Viewport
     *            viewport's} <i>bottom</i> edge, in screen-space.
     * @param left
     *            The relative starting position of the {@link Viewport
     *            viewport's} <i>left</i> edge, in screen-space.
     * @param width
     *            The how far to the <i>right</i> side the {@link Viewport
     *            viewport's} right border extends, in screen-space.
     * @param height
     *            The how far <i>up</i> the {@link Viewport viewport's} top
     *            border extends, in screen-space.
     * @returns The newly added {@link Viewport viewport}.
     * @throws IllegalArgumentException
     *             If any values are outside the <code>[0, 1]</code> range.
     */
    Viewport createViewport(float bottom, float left, float width, float height);

    /**
     * Gets the {@link Viewport viewport} at the specified index, if it exists.
     * <p>
     * {@link Viewport}s are expected to be stored in the order in which they
     * were created, so that the client can rely on that known order when
     * requesting {@link Viewport viewport} references.
     *
     * @param index
     *            The zero-based index of the {@link Viewport viewport}.
     * @return The {@link Viewport viewport} at the indexed position.
     * @throws IndexOutOfBoundsException
     *             If the index is negative or greater than the number of
     *             {@link Viewport viewports} that have been created for
     *             <code>this</code> {@link RenderWindow window}.
     */
    Viewport getViewport(int index);

    /**
     * Gets the current number of {@link Viewport viewports} <code>this</code>
     * {@link RenderWindow window} has.
     *
     * @return The number of {@link Viewport viewports}.
     */
    int getViewportCount();

    /**
     * Allows clients to iterate over all the {@link Viewport viewports} that
     * have been added to <code>this</code>.
     *
     * @return An {@link Iterable iterable} of {@link Viewport viewports}.
     */
    Iterable<Viewport> getViewports();

    /**
     * Removes the {@link Viewport viewport} at the specified index.
     * <p>
     * Implementations are expected to invoke {@link #notifyDispose()} on the
     * {@link Viewport viewport}.
     *
     * @param index
     *            The position of the {@link Viewport viewport} to remove.
     * @throws IndexOutOfBoundsException
     *             If the index is negative or greater than the number of
     *             {@link Viewport viewports} that have been created for
     *             <code>this</code> {@link RenderWindow window}.
     */
    void removeViewport(int index);

    /**
     * The {@link Viewport viewport} to be removed.
     * <p>
     * Implementations are expected to invoke {@link #notifyDispose()} on the
     * {@link Viewport viewport}.
     *
     * @param vp
     *            The {@link Viewport viewport} to be removed.
     * @throws NullPointerException
     *             If the {@link Viewport viewport} is <code>null</code>.
     */
    void removeViewport(Viewport vp);

    /**
     * Removes all {@link Viewport viewports}.
     * <p>
     * Implementations are expected to invoke {@link #notifyDispose()} on each
     * {@link Viewport viewport}.
     */
    void removeAllViewports();

    /**
     * Sets the specified title on the title bar.
     *
     * @param title
     *            The text to use as a title.
     */
    void setTitle(String title);

    /**
     * Gets the current width of <code>this</code> {@link RenderWindow
     * render-window}.
     *
     * @return The width, in pixels.
     */
    int getWidth();

    /**
     * Gets the current height of <code>this</code> {@link RenderWindow
     * render-window}.
     *
     * @return The height, in pixels.
     */
    int getHeight();

    /**
     * Gets the current number of bits per color component in use by
     * <code>this</code> {@link RenderWindow render-window}.
     *
     * @return The bit-depth.
     */
    int getBitDepth();

    /**
     * Gets the current refresh rate of <code>this</code> {@link RenderWindow
     * render-window}.
     *
     * @return The refresh rate, in Hertz.
     */
    int getRefreshRate();

    /**
     * Sets the icon image for the title bar.
     *
     * @param image
     *            The icon image to use.
     */
    void setIconImage(Image image);

    /**
     * Gets whether <code>this</code> {@link RenderWindow render-window} is in
     * <a href=
     * "https://docs.oracle.com/javase/tutorial/extra/fullscreen/exclusivemode.html">Full-Screen
     * Exclusive Mode</a>.
     *
     * @return <code>true</code> if <code>this</code> is in FSEM. Otherwise
     *         <code>false</code>.
     */
    boolean isFullScreen();

    /**
     * Adds a {@link KeyListener} to <code>this</code>.
     *
     * @param kl
     *            The {@link KeyListener}.
     */
    void addKeyListener(KeyListener kl);

    /**
     * Adds a {@link MouseListener} to <code>this</code>.
     *
     * @param ml
     *            The {@link MouseListener}.
     */
    void addMouseListener(MouseListener ml);

    /**
     * Adds a {@link MouseMotionListener} to <code>this</code>.
     *
     * @param mml
     *            The {@link MouseMotionListener}.
     */
    void addMouseMotionListener(MouseMotionListener mml);

    /**
     * Adds a {@link MouseWheelListener} to <code>this</code>.
     *
     * @param mwl
     *            The {@link MouseWheelListener}.
     */
    void addMouseWheelListener(MouseWheelListener mwl);

    public int getLocationTop();
    public int getLocationLeft();
}
