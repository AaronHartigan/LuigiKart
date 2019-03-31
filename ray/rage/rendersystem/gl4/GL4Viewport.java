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

package ray.rage.rendersystem.gl4;

import java.awt.*;
import java.nio.*;
import java.util.*;
import java.util.List;

import ray.rage.rendersystem.*;
import ray.rage.scene.*;
import ray.rage.util.*;

/**
 * A concrete implementation of the {@link Viewport viewport} interface for a
 * {@link GL4RenderSystem}.
 *
 * @author Raymond L. Rivera
 *
 */
public final class GL4Viewport implements Viewport {

    private Camera                  camera;
    private Canvas                  target;

    private FloatBuffer             colorBuffer = BufferUtil.directFloatBuffer(new float[] { 0f, 0f, 0f, 1f });
    private FloatBuffer             depthBuffer = BufferUtil.directFloatBuffer(new float[] { 1f });

    // relative viewport size, in screen-space: [0, 1] range
    private float                   relativeLeft;
    private float                   relativeBottom;
    private float                   relativeWidth;
    private float                   relativeHeight;

    // actual viewport screen size, in pixels
    private int                     actualLeft;
    private int                     actualBottom;
    private int                     actualWidth;
    private int                     actualHeight;

    // relative viewport scissor size, in screen-space: [0, 1] range; used to
    // limit screen updates to this viewport's section
    private float                   scissorRelLeft;
    private float                   scissorRelBottom;
    private float                   scissorRelWidth;
    private float                   scissorRelHeight;

    // actual viewport scissor screen size, in pixels; used to limit screen
    // updates to this viewport's section
    private int                     scissorActLeft;
    private int                     scissorActBottom;
    private int                     scissorActWidth;
    private int                     scissorActHeight;

    private List<Viewport.Listener> vpListeners = new ArrayList<>();

    /**
     * Creates a new viewport for the specified rendering target with the
     * specified dimensions.
     *
     * @param canvas
     *            The surface to which this viewport applies.
     * @param bottom
     *            The lower portion of the viewport.
     * @param left
     *            The left portion of the viewport.
     * @param width
     *            The width of the viewport.
     * @param height
     *            The height of the viewport.
     * @throws NullPointerException
     *             If the {@link Canvas canvas} is <code>null</code>.
     * @throws IllegalArgumentException
     *             If the dimensions are not within valid ranges.
     */
    public GL4Viewport(Canvas canvas, float bottom, float left, float width, float height) {
        if (canvas == null)
            throw new NullPointerException("Null canvas");

        target = canvas;
        setDimensions(bottom, left, width, height);
    }

    /**
     * Creates a new viewport for the specified rendering target. It's initially
     * set to occupy the whole screen.
     *
     * @param target
     *            The surface to which this viewport applies.
     */
    public GL4Viewport(Canvas target) {
        this(target, 0f, 0f, 1f, 1f);
    }

    @Override
    public void addListener(Viewport.Listener vpl) {
        vpListeners.add(vpl);
    }

    @Override
    public void removeListener(Viewport.Listener vpl) {
        vpListeners.remove(vpl);
    }

    @Override
    public void removeAllListeners() {
        vpListeners.clear();
    }

    @Override
    public void setDimensions(float bottom, float left, float width, float height, boolean overrideScissor) {
        verifyBounds(bottom, left, width, height);

        relativeBottom = bottom;
        relativeLeft = left;
        relativeWidth = width;
        relativeHeight = height;

        if (overrideScissor) {
            scissorRelBottom = bottom;
            scissorRelLeft = left;
            scissorRelWidth = width;
            scissorRelHeight = height;
        }

        notifyDimensionsChanged();
    }

    @Override
    public void setDimensions(float bottom, float left, float width, float height) {
        setDimensions(bottom, left, width, height, true);
    }

    @Override
    public void setScissors(float bottom, float left, float width, float height) {
        verifyBounds(bottom, left, width, height);

        scissorRelBottom = bottom;
        scissorRelLeft = left;
        scissorRelWidth = width;
        scissorRelHeight = height;

        notifyDimensionsChanged();
    }

    @Override
    public void notifyDimensionsChanged() {
        if (scissorRelBottom < relativeBottom || scissorRelLeft < relativeLeft || scissorRelWidth > relativeWidth
                || scissorRelHeight > relativeHeight) {
            throw new RuntimeException("Scissor rectangle must be inside viewport's");
        }

        final float width = target.getWidth();
        final float height = target.getHeight();

        // we convert from relative screen coordinates in the [0, 1] range
        // to actual screen size (i.e. pixels) for glViewport/glScissor, which
        // the render system must use
        actualBottom = (int) (relativeBottom * height);
        actualLeft = (int) (relativeLeft * width);
        actualWidth = (int) (relativeWidth * width);
        actualHeight = (int) (relativeHeight * height);

        scissorActBottom = (int) (scissorRelBottom * height);
        scissorActLeft = (int) (scissorRelLeft * width);
        scissorActWidth = (int) (scissorRelWidth * width);
        scissorActHeight = (int) (scissorRelHeight * height);

        if (camera != null && camera.getFrustum().getAutoAspectRatio())
            camera.getFrustum().setAspectRatio((float) actualWidth / (float) actualHeight);

        for (Viewport.Listener vpl : vpListeners)
            vpl.onViewportDimensionsChanged(this);
    }

    @Override
    public void setCamera(Camera cam) {
        if (camera != null)
            camera.notifyViewport(null);

        camera = cam;
        if (cam != null) {
            if (cam.getFrustum().getAutoAspectRatio())
                cam.getFrustum().setAspectRatio((float) actualWidth / (float) actualHeight);
            cam.notifyViewport(this);
        }

        for (Viewport.Listener vpl : vpListeners)
            vpl.onViewportCameraChanged(this);
    }

    @Override
    public Camera getCamera() {
        return camera;
    }

    @Override
    public Canvas getTarget() {
        return target;
    }

    @Override
    public int getActualLeft() {
        return actualLeft;
    }

    @Override
    public int getActualBottom() {
        return actualBottom;
    }

    @Override
    public int getActualWidth() {
        return actualWidth;
    }

    @Override
    public int getActualHeight() {
        return actualHeight;
    }

    @Override
    public int getActualScissorLeft() {
        return scissorActLeft;
    }

    @Override
    public int getActualScissorBottom() {
        return scissorActBottom;
    }

    @Override
    public int getActualScissorWidth() {
        return scissorActWidth;
    }

    @Override
    public int getActualScissorHeight() {
        return scissorActHeight;
    }

    @Override
    public void setClearColor(float red, float green, float blue, float alpha) {
        verifyBounds(red, green, blue, alpha);

        colorBuffer.put(0, red);
        colorBuffer.put(1, green);
        colorBuffer.put(2, blue);
        colorBuffer.put(3, alpha);
    }

    @Override
    public void setClearColor(final Color c) {
        final float inv = 1f / 255f;
        colorBuffer.put(0, c.getRed() * inv);
        colorBuffer.put(1, c.getGreen() * inv);
        colorBuffer.put(2, c.getBlue() * inv);
        colorBuffer.put(3, c.getAlpha() * inv);
    }

    @Override
    public Color getClearColor() {
        final float r = colorBuffer.get(0);
        final float g = colorBuffer.get(1);
        final float b = colorBuffer.get(2);
        final float a = colorBuffer.get(3);
        return new Color(r, g, b, a);
    }

    @Override
    public FloatBuffer getClearColorBuffer() {
        return colorBuffer;
    }

    @Override
    public void setClearDepth(float depth) {
        // depth is only valid within the [0, 1] range
        if (depth < 0)
            throw new IllegalArgumentException("depth < 0");
        if (depth > 1)
            throw new IllegalArgumentException("depth > 1");

        depthBuffer.put(0, depth);
    }

    @Override
    public float getClearDepth() {
        return depthBuffer.get(0);
    }

    @Override
    public FloatBuffer getClearDepthBuffer() {
        return depthBuffer;
    }

    @Override
    public void notifyDispose() {
        // do not use #setCamera(null) so we can avoid invoking onCameraChanged
        // listeners; at this point, no one should care about the fact that this
        // viewport's camera has changed (i.e. been removed)
        if (camera != null)
            camera.notifyViewport(null);

        vpListeners.clear();
        vpListeners = null;
        target = null;
        camera = null;

        depthBuffer.clear();
        colorBuffer.clear();
        depthBuffer = null;
        colorBuffer = null;
    }

    private static void verifyBounds(float a, float b, float c, float d) {
        // all arguments must be in the [0, 1] range
        if (a < 0)
            throw new IllegalArgumentException("first value is < 0");
        if (a > 1)
            throw new IllegalArgumentException("first value is > 1");

        if (b < 0)
            throw new IllegalArgumentException("second value is < 0");
        if (b > 1)
            throw new IllegalArgumentException("second value  is > 1");

        if (c < 0)
            throw new IllegalArgumentException("third value  is < 0");
        if (c > 1)
            throw new IllegalArgumentException("third value  is > 1");

        if (d < 0)
            throw new IllegalArgumentException("fourth value  is < 0");
        if (d > 1)
            throw new IllegalArgumentException("fourth value  is > 1");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(Viewport.class.getSimpleName());
        sb.append(": RelLeft=" + relativeLeft);
        sb.append(", RelBottom=" + relativeBottom);
        sb.append(", RelWidth=" + relativeWidth);
        sb.append(", RelHeight=" + relativeHeight);

        sb.append(", ActLeft=" + actualLeft);
        sb.append(", ActBottom=" + actualBottom);
        sb.append(", ActWidth=" + actualWidth);
        sb.append(", ActHeight=" + actualHeight);

        sb.append(", SciActLeft=" + scissorActLeft);
        sb.append(", SciActBottom=" + scissorActBottom);
        sb.append(", SciActWidth=" + scissorActWidth);
        sb.append(", SciActHeight=" + scissorActHeight);

        return sb.toString();
    }

}
