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
import ray.rage.scene.Node;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneNode;
import ray.rage.scene.SceneObject;

/**
 * A <i>scene object</i> is an object that can be placed in a scene by getting
 * <i>attached</i> to a {@link SceneNode scene-node}.
 * <p>
 * Scene objects don't have any information about a scene, such as a position or
 * a rotation. Rather, they rely on {@link SceneNode scene-nodes} for it.
 *
 * @author Raymond L. Rivera
 *
 * @see SceneNode
 *
 */
public interface SceneObject extends Nameable, Visible, Managed<SceneManager>, Disposable {

    /**
     * Listener for {@link SceneObject scene-object}-related events.
     *
     * @author Raymond L. Rivera
     *
     */
    public interface Listener {

        /**
         * A {@link SceneObject scene-object} has been attached to a
         * {@link SceneNode scene-node}.
         *
         * @param so
         *            The attached {@link SceneObject scene-object}.
         */
        void onObjectAttached(SceneObject so);

        /**
         * A {@link SceneObject scene-object} has been detached from a
         * {@link SceneNode scene-node}.
         *
         * @param so
         *            The detached {@link SceneObject scene-object}.
         */
        void onObjectDetached(SceneObject so);

    }

    /**
     * {@inheritDoc}
     *
     * In this case, the name of <code>this</code> {@link SceneObject
     * scene-object}.
     *
     * @return The name of <code>this</code> {@link SceneObject scene-object}.
     */
    @Override
    String getName();

    /**
     * Gets the {@link SceneNode scene-node} to which <code>this</code>
     * {@link SceneObject scene-object} is attached, if any, as a regular
     * {@link Node node}.
     *
     * @return The parent {@link Node node}, if any. Otherwise
     *         <code>null</code>.
     */
    Node getParentNode();

    /**
     * Gets the {@link SceneNode scene-node} to which <code>this</code>
     * {@link SceneObject scene-object} is attached, if any.
     *
     * @return The parent {@link SceneNode scene-node}, if any. Otherwise
     *         <code>null</code>.
     */
    SceneNode getParentSceneNode();

    /**
     * Notifies <code>this</code> {@link SceneObject scene-object} that it has
     * been attached to a parent {@link SceneNode scene-node}.
     * <p>
     * If <code>this</code> {@link SceneObject scene-object} is already
     * attached, then it's responsible for detaching itself from its current
     * parent before attaching itself to the new one.
     * <p>
     * The {@link Listener listeners} must be invoked <i>before</i> the old
     * parent is discarded and <i>after</i> the new one assigned, so that
     * {@link Listener listeners} can see who the old parent originally was and
     * who the new parent currently is.
     * <p>
     * <b>WARNING:</b> <i>This method is meant to be used internally by the
     * framework and not by game clients. Use directly only if you really know
     * what you're doing.</i>
     *
     * @param parent
     *            The {@link SceneNode scene-node} to which <code>this</code>
     *            {@link SceneObject scene-object} has been attached.
     * @throws NullPointerException
     *             If the parent is <code>null</code>.
     * @see #detachFromParent()
     * @see #getParentNode()
     * @see #getParentSceneNode()
     */
    void notifyAttached(SceneNode parent);

    /**
     * Lets <code>this</code> {@link SceneObject scene-object} know that it has
     * been detached from its parent {@link SceneNode scene-node}.
     * <p>
     * If a {@link Listener listener} has been set, then this method is
     * responsible for calling
     * {@link SceneObject.Listener#onObjectDetached(SceneObject)}. If
     * <code>this</code> {@link SceneObject scene-object} is already detached,
     * then it does nothing.
     * <p>
     * The {@link Listener listener} must be invoked <i>before</i> the new
     * parent has been assigned, so that {@link SceneObject scene-objects}
     * listening in can see who the previous parent used to be.
     * <p>
     * <b>WARNING:</b> <i>This method is meant to be used internally by the
     * framework and not by game clients. Use directly only if you really know
     * what you're doing.</i>
     *
     * @see #notifyAttached(SceneNode)
     * @see #getParentNode()
     * @see #getParentSceneNode()
     */
    void notifyDetached();

    /**
     * Determines if <code>this</code> {@link SceneObject scene-object} is
     * attached to a {@link SceneNode scene-node} or not.
     *
     * @return True if <code>this</code> {@link SceneObject scene-object} is
     *         attached to a {@link SceneNode scene-node}. Otherwise
     *         <code>false</code>.
     * @see #isInScene()
     */
    boolean isAttached();

    /**
     * Causes <code>this</code> {@link SceneObject scene-object} to detach
     * itself from its parent {@link SceneNode scene-node}, if attached.
     * <p>
     * The {@link SceneObject scene-object} is responsible for invoking
     * {@link SceneNode#detachObject(SceneObject)}.
     */
    void detachFromParent();

    /**
     * Determines if <code>this</code> {@link SceneObject scene-object} is in
     * the scene or not.
     * <p>
     * For <code>this</code> {@link SceneObject scene-object} to be in a scene
     * it must be attached to a {@link SceneNode scene-node} that is in the
     * scene graph. In other words, {@link #isAttached()} and
     * {@link SceneNode#isInSceneGraph()} must both be <code>true</code>.
     *
     * @return True if {@link #isAttached()} and <code>this</code> parent's
     *         {@link SceneNode#isInSceneGraph()} both return <code>true</code>.
     *         Otherwise <code>false</code>.
     * @see #isAttached()
     * @see {@link SceneNode#isInSceneGraph()}
     */
    boolean isInScene();

    /**
     * Sets a {@link Listener listener} for <code>this</code> {@link SceneObject
     * scene-object}.
     *
     * @param listener
     *            The active {@link Listener listener} for <code>this</code>
     *            {@link SceneObject scene-object}.
     */
    void setListener(Listener listener);

    /**
     * Gets <code>this</code> {@link SceneObject scene-object's} {@link Listener
     * listener}, if one has been set.
     *
     * @return The {@link Listener listener}, if any. Otherwise
     *         <code>null</code>.
     */
    Listener getListener();

}
