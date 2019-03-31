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

package ray.rage.scene;

import ray.rage.common.*;
import ray.rage.rendersystem.*;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneObject;
import ray.rml.*;

/**
 * A <i>camera</i> is viewpoint from which the scene will be rendered.
 *
 * @author Raymond L. Rivera
 *
 */
public interface Camera extends SceneObject {

    /**
     * Listener interface to notify clients of {@link Camera camera} events.
     *
     * @author Raymond L. Rivera
     *
     */
    interface Listener {

        /**
         * Called prior to the scene being rendered with <code>this</code>
         * {@link Camera camera}.
         *
         * @param cam
         *            {@link Camera} from which the scene was rendered.
         */
        void onCameraPreRenderScene(Camera cam);

        /**
         * Called after the scene has been rendered with <code>this</code>
         * {@link Camera camera}.
         *
         * @param cam
         *            {@link Camera} from which the scene was rendered.
         */
        void onCameraPostRenderScene(Camera cam);

    }

    /**
     * A <a href=
     * "https://en.wikipedia.org/wiki/Viewing_frustum"><i>frustum</i></a>
     * represents a pyramid, capped at the near and far end, which is used to
     * represent either a visible area or a projection area.
     * <p>
     * The <i>frustum</i> defines a <i>viewing volume</i> where
     * {@link SceneObjects scene-objects} within its bounds are considered to be
     * {@link Visible visible}. In other words, the <i>frustum</i> specifies the
     * region of space in the modeled world that may appear on the screen by
     * representing a field of view.
     *
     * @author Raymond L. Rivera
     *
     */
    interface Frustum {

        /**
         * Specifies the projection the {@link Camera camera} should use.
         *
         * @author Raymond L. Rivera
         *
         */
        enum Projection {
            /**
             * Realistic projection where object shapes and sizes vary depending
             * on their distance relative to the {@link Camera camera}.
             * <p>
             * Objects that are closer will appear larger in size than those
             * farther away.
             */
            PERSPECTIVE,

            /**
             * Architectural projection where objects are projected parallel to
             * the {@link Camera camera's} projection plane.
             * <p>
             * Objects that are closer will appear to be the same size as those
             * that are farther away.
             */
            ORTHOGRAPHIC
        }

        /**
         * Tells the {@link Frustum frustum} which {@link Camera camera} it
         * belongs to.
         *
         * @param cam
         *            The {@link Camera camera}.
         * @throws NullPointerException
         *             If the {@link Camera camera} is <code>null</code>.
         */
        void notifyCamera(Camera cam);

        /**
         * Gets the {@link Projection projection} of <code>this</code>
         * {@link Camera camera}.
         *
         * @return The {@link Projection projection}.
         */
        Projection getProjection();

        /**
         * Sets the aspect ratio for the {@link Frustum frustum} {@link Viewport
         * viewport}.
         * <p>
         * The width to height ratio of a {@link Viewport viewport} is usually
         * defined as <code>1.333333</code> for <code>4:3</code> displays or
         * <code>1.77777</code> for <code>16:9</code> displays. Mathematically,
         * it's <code>ratio = width / height</code>
         *
         * @param ratio
         *            The width to height ratio.
         * @throws IllegalArgumentException
         *             If <code>ratio <= 0</code>.
         */
        void setAspectRatio(float ratio);

        /**
         * Returns the width to height ratio of the {@link Viewport viewport}.
         *
         * @return The <code>width:height</code> ratio.
         */
        float getAspectRatio();

        /**
         * If set to <code>true</code>, the {@link Viewport viewport} will
         * automatically recalculate the aspect ratio of <code>this</code>
         * {@link Frustum frustum} whenever it's resized.
         * <p>
         * Set this to <code>true</code> only when <code>this</code> object is
         * in use by a single {@link Viewport viewport}.
         *
         * @param enabled
         *            True to enable automatic recalculation of the aspect
         *            ratio. False otherwise.
         */
        void setAutoAspectRatio(boolean enabled);

        /**
         * Gets whether the automatic calculation of aspect ratio is currently
         * enabled or not.
         *
         * @return True when automatic recalculation of aspect ratio is enabled.
         *         False otherwise.
         */
        boolean getAutoAspectRatio();

        /**
         * Sets the distance to the near clipping plane.
         * <p>
         * The view {@link Frustum frustum} is a pyramid created from the
         * {@link Frustum frustum} position and the edges of the {@link Viewport
         * viewport}, but with its top cut off flat. This method sets the
         * distance between the {@link Frustum frustum's} position and the near
         * end (or top) of that pyramid.
         * <p>
         * The position of the near clipping plane is the distance from the
         * {@link Frustum frustum's} position to the screen on which the world
         * is projected. The {@link Frustum frustum's} {@link Viewport viewport}
         * should have the same aspect ratio as the screen {@link Viewport
         * viewport} it renders into to avoid distortion.
         *
         * @param nearClipDistance
         *            The plane's offset from the {@link Frustum frustum's}
         *            position, in world coordinates. This value
         *            <strong>must</strong> be greater than zero and less than
         *            the far clipping plane.
         * @throws IllegalArgumentException
         *             If <code>nearClipDistance <= 0</code> or
         *             <code>nearClipDistance >= farClipDistance</code>.
         * @see #setFarClipDistance(float)
         */
        void setNearClipDistance(float nearClipDistance);

        /**
         * Gets the value of the {@link Frustum frustum's} near clipping plane.
         *
         * @return The distance from <code>this</code> {@link Frustum frustum's}
         *         current position to the near end of its clipping plane.
         */
        float getNearClipDistance();

        /**
         * Sets the distance to the far clipping plane.
         * <p>
         * The view {@link Frustum frustum} is a pyramid created from the
         * {@link Frustum frustum} position and the edges of the {@link Viewport
         * viewport}. This method sets the distance for the far end (or base) of
         * that pyramid.
         * <p>
         * Be mindful of the values you choose here. Increasing the ratio
         * between the near and far planes lowers the accuracy of the Z-buffer
         * used for depth testing pixels. An observable side-effect of this
         * might be
         * <a href="https://en.wikipedia.org/wiki/Z-fighting">Z-Fighting</a>.
         *
         * @param farClipDistance
         *            The plane's offset from the {@link Frustum frustum's}
         *            position, in world coordinates. This value
         *            <strong>must</strong> be greater than the near clipping
         *            plane.
         * @throws IllegalArgumentException
         *             If <code>farClipDistance <= nearClipDistance</code>.
         * @see #setNearClipDistance(float)
         */
        void setFarClipDistance(float farClipDistance);

        /**
         * Gets the value of the {@link Frustum frustum's} far clipping plane.
         *
         * @return The distance from <code>this</code> {@link Frustum frustum's}
         *         current position to the far end of its clipping plane.
         */
        float getFarClipDistance();

        /**
         * Sets the <i>vertical</i> field-of-view (FOV) of <code>this</code>
         * {@link Frustum frustum}.
         * <p>
         * The Field Of View (FOV) is the {@link Angle angle} made between the
         * {@link Frustum frustum's} position and the edges of the 'screen' onto
         * which the scene is projected. Typical values are between 45 and 60
         * degrees.
         * <p>
         * The horizontal field of view is calculated from <code>this</code>
         * {@link Viewport viewport's} dimensions. They will only be the same if
         * the {@link Viewport viewport} is square.
         *
         * @param angle
         *            The {@link Angle angle} of the field of view.
         * @throws NullPointerException
         *             If the {@link Angle angle} is <code>null</code>.
         */
        void setFieldOfViewY(final Angle angle);

        /**
         * Gets the {@link Angle angle} of the vertical field-of-view (FOV) of
         * <code>this</code> {@link Frustum frustum}.
         *
         * @return The {@link Angle angle} measuring the vertical field of view.
         */
        Angle getFieldOfViewY();

        /**
         * Gets the view {@link Matrix4 matrix} for <code>this</code>
         * {@link Frustum frustum}.
         *
         * @return The {@link Frustum frustum's} current view {@link Matrix4
         *         matrix}.
         */
        Matrix4 getViewMatrix();

        /**
         * Gets the projection {@link Matrix4 matrix} for <code>this</code>
         * {@link Frustum frustum}.
         *
         * @return The {@link Frustum frustum's} current projection
         *         {@link Matrix4 matrix}.
         */
        Matrix4 getProjectionMatrix();

    }

    /**
     * Gets the {@link Camera.Frustum frustum} in use by <code>this</code>
     * {@link Camera camera}.
     *
     * @return The {@link Camera.Frustum frustum}.
     */
    Frustum getFrustum();

    /**
     * Causes the {@link SceneManager scene-manager} to render the scene from
     * <code>this</code> {@link Camera camera's} perspective.
     *
     * @throws IllegalStateException
     *             If the {@link Viewport viewport} has not been set.
     */
    void renderScene();

    /**
     * Notifies <code>this</code> {@link Camera camera} that a {@link Viewport
     * viewport} is using it.
     * <p>
     * <b>WARNING:</b> <i>This method is meant to be used internally by the
     * framework and not by game clients. Use directly only if you really know
     * what you're doing.</i>
     *
     * @param vp
     *            The {@link Viewport viewport} using <code>this</code>
     *            {@link Camera camera}.
     */
    void notifyViewport(Viewport vp);

    /**
     * Gets the {@link Viewport viewport} using <code>this</code> {@link Camera
     * camera}.
     *
     * @return The {@link Viewport viewport} if <code>this</code> {@link Camera
     *         camera} is being used. Otherwise <code>null</code>.
     */
    Viewport getViewport();

    /**
     * Adds a listener to <code>this</code> {@link Camera camera}.
     *
     * @param listener
     *            The {@link Camera.Listener} being added.
     */
    void addListener(Camera.Listener listener);

    /**
     * Removes the specified listener from <code>this</code> {@link Camera
     * camera}.
     *
     * @param listener
     *            The {@link Camera.Listener} to remove.
     */
    void removeListener(Camera.Listener listener);
 
    /**
     * Sets the forward (N) vector for <code>this</code> {@link Camera
     * camera}.
     *
     * @param v
     *            A {@link Vector3f} specifying the desired forward camera axis.
     *            Note that the three vectors U, V, N must be orthogonal.
     */
    void setFd(Vector3f v);
    
    /**
     * Sets the sideways/right (U) vector for <code>this</code> {@link Camera
     * camera}.
     *
     * @param v
     *            A {@link Vector3f} specifying the desired sideways (right) camera axis.
     *            Note that the three vectors U, V, N must be orthogonal.
     */
    void setRt(Vector3f v);
    
    /**
     * Sets the vertical/up (V) vector for <code>this</code> {@link Camera
     * camera}.
     *
     * @param v
     *            A {@link Vector3f} specifying the desired vertical camera axis.
     *            Note that the three vectors U, V, N must be orthogonal.
     */
    void setUp(Vector3f v);
    
    /**
     * Sets the position/location of <code>this</code> {@link Camera
     * camera}.
     *
     * @param v
     *            A {@link Vector3f} specifying the desired camera location.
     */
    void setPo(Vector3f v);
    
    /**
     * Sets the current operating mode of <code>this</code> {@link Camera
     * camera}.
     *
     * @param m
     *            A char specifying the desired camera mode.
     *            "c" means that the view matrix will be generated from camera axes U,V,N.
     *            "r" means that the view matrix will be generated from the node orientation.
     *            Mode "r" is generally used when attaching the camera to a game object.
     */
    void setMode(char m);

    /**
     * Gets the current forward vector N for <code>this</code> {@link Camera
     * camera}.
     *
     */
    Vector3f getFd();
    
    /**
     * Gets the current sideways (right) vector U for <code>this</code> {@link Camera
     * camera}.
     *
     */
    Vector3f getRt();
    
    /**
     * Gets the current vertical (up) vector V for <code>this</code> {@link Camera
     * camera}.
     *
     */
    Vector3f getUp();
    
    /**
     * Gets the current position/location of <code>this</code> {@link Camera
     * camera}.
     *
     */
    Vector3f getPo();
    
    /**
     * Gets the current operating mode for <code>this</code> {@link Camera
     * camera}.
     * 
     *             Returns a char specifying the current camera mode.
     *            "c" means that the view matrix will be generated from camera axes U,V,N.
     *            "r" means that the view matrix will be generated from the node orientation.
     *            Mode "r" is generally used when attaching the camera to a game object.
     *
     */
    char getMode();
}
