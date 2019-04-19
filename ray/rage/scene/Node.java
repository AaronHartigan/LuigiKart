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

import ray.physics.PhysicsObject;
import ray.rage.common.*;
import ray.rage.scene.Node;
import ray.rage.scene.SceneManager;
import ray.rml.*;

/**
 * A <i>node</i> is a general-purpose object used to represent an articulated
 * graph.
 * <p>
 * A node contains information about the transformations that are applicable to
 * itself and all of its children. Child nodes combine their local-space
 * transforms with their parent's to derive their own world-space transforms.
 *
 * @author Raymond L. Rivera
 *
 */
public interface Node extends Nameable {

    /**
     * A listener for {@link Node} events.
     *
     * @author Raymond L. Rivera
     *
     */
    public interface Listener {

        /**
         * A {@link Node} has been attached to a new parent.
         * <p>
         * The child {@link Node node} must be attached <i>before</i> this
         * method is invoked. In other words,
         * <code>child.getParent() == newParent</code> must be true. The new
         * parent is passed separately for convenience and consistency.
         *
         * @param child
         *            The overly attached {@link Node node} ;)
         * @param newParent
         *            The {@link Node node} that just became the child's parent.
         */
        void onNodeAttached(Node child, Node newParent);

        /**
         * A {@link Node} has had its <strong>derived</strong> transforms
         * updated.
         * <p>
         * This method is invoked at the end of the {@link Node node's}
         * {@link Node#update(boolean, boolean)}, which generally takes place
         * when the scene graph is being updated. This is not triggered by
         * simple state changes made to the {@link Node node} outside of its
         * update method.
         *
         * @param n
         *            The updated {@link Node node}.
         */
        void onNodeUpdated(Node n);

        /**
         * A {@link Node} has been detached from its parent.
         * <p>
         * The orphan {@link Node node} must be detached <i>before</i> this
         * method is invoked. In other words,
         * <code>orphan.getParent() == null</code> must be true.
         *
         * @param orphan
         *            The overly detached {@link Node node} ;)
         * @param oldParent
         *            The {@link Node node} that used to be the child's parent.
         */
        void onNodeDetached(Node orphan, Node oldParent);

    }

    /**
     * A <i>controller</i> handles {@link Node nodes} make it easier to apply
     * behaviors by concrete implementations.
     * <p>
     * Implementations are expected to respect the parent/child relationships
     * controlled {@link Node nodes} had prior to being added. In other words,
     * controlled {@link Node nodes} do not get assigned to new parents or get
     * new children.
     *
     * @author Raymond L. Rivera
     *
     */
    public interface Controller extends Disposable {

        /**
         * Adds a {@link Node} for <code>this</code> {@link Controller
         * controller} to control.
         *
         * @param node
         *            The {@link Node} to be controlled by the {@link Controller
         *            controller}.
         * @throws NullPointerException
         *             If the {@link Node node} is <code>null</code>.
         */
        void addNode(Node node);

        /**
         * Removes the specified {@link Node node} from <code>this</code>
         * {@link Controller controller}'s control.
         *
         * @param node
         *            The {@link Node node} to be removed.
         * @throws NullPointerException
         *             If the {@link Node node} is <code>null</code>.
         * @see #removeNode(String)
         */
        void removeNode(Node node);

        /**
         * Removes the {@link Node node} with the specified name from
         * <code>this</code> {@link Controller controller}'s control.
         *
         * @param name
         *            The name of the {@link Node node} to be removed.
         * @throws IllegalArgumentException
         *             If the name is empty.
         * @throws NullPointerException
         *             If the {@link Node node} is <code>null</code>.
         * @see #removeNode(Node)
         */
        void removeNode(String name);

        /**
         * Removes all {@link Node nodes} from <code>this</code>
         * {@link Controller controller's} control.
         */
        void removeAllNodes();

        /**
         * Causes <code>this</code> {@link Controller controller} to update all
         * the {@link Node nodes} currently under its control.
         *
         * @param elapsedTimeMillis
         *            The time elapsed since the last update, in milliseconds.
         * @throws IllegalArgumentException
         *             If the argument is negative.
         */
        void update(float elapsedTimeMillis);

        /**
         * Sets whether the updates provided by <code>this</code>
         * {@link Controller controller} are being applied or not.
         *
         * @param enabled
         *            True to apply the updates. False otherwise.
         */
        void setEnabled(boolean enabled);

        /**
         * Gets whether <code>this</code> {@link Controller controller} is
         * currently applying its updates or not.
         *
         * @return True if {@link Node nodes} are being updated. False
         *         otherwise.
         */
        boolean isEnabled();

		boolean isShouldDelete();
    }

    /**
     * {@inheritDoc}
     *
     * In this case, the name of <code>this</code> {@link Node node}.
     *
     * @return The name of <code>this</code> {@link Node node}.
     */
    @Override
    String getName();

    /**
     * Creates a new {@link Node node} that is a direct descendant of
     * <code>this</code> one.
     *
     * @param childName
     *            A unique name for the child.
     * @return A new {@link Node node}.
     */
    Node createChildNode(String childName);

    /**
     * Sets the position of <code>this</code> {@link Node node} relative to its
     * parent.
     *
     * @param x
     *            The position along the x-axis.
     * @param y
     *            The position along the y-axis.
     * @param z
     *            The position along the z-axis.
     * @see #setLocalPosition(Vector3)
     */
    void setLocalPosition(float x, float y, float z);

    /**
     * Sets the {@link Vector3 position} of <code>this</code> {@link Node node}
     * relative to its parent.
     *
     * @param pv
     *            The position {@link Vector3 vector}.
     * @see #setLocalPosition(float, float, float)
     */
    void setLocalPosition(Vector3 pv);

    /**
     * Gets the {@link Vector3 position} of <code>this</code> {@link Node node}
     * relative to its parent.
     *
     * @return A vector specifying <code>this</code> {@link Node node's}
     *         {@link Vector3 position}.
     */
    Vector3 getLocalPosition();

    /**
     * Sets the scaling factor applied to <code>this</code> {@link Node node}
     * relative to its parent.
     *
     * @param x
     *            The scaling along the x-axis.
     * @param y
     *            The scaling along the y-axis.
     * @param z
     *            The scaling along the z-axis.
     * @see #setLocalScale(Vector3)
     */
    void setLocalScale(float x, float y, float z);

    /**
     * Sets the {@link Vector3 scaling} factor applied to <code>this</code>
     * {@link Node node} relative to its parent.
     *
     * @param sv
     *            A vector with the scaling components for each axis.
     * @see #setLocalScale(float, float, float)
     */
    void setLocalScale(Vector3 sv);

    /**
     * Gets <code>this</code> {@link Node node's} scale relative to its parent.
     *
     * @return The scale relative to its parent.
     */
    Vector3 getLocalScale();

    /**
     * Sets the {@link Matrix3 rotation} of <code>this</code> {@link Node node}
     * relative to its parent.
     *
     * @param rm
     *            The {@link Matrix3 rotation} of <code>this</code> {@link Node
     *            node}.
     */
    void setLocalRotation(Matrix3 rm);

    /**
     * Gets <code>this</code> {@link Node node's} {@link Matrix3 rotation}
     * relative to its parent.
     *
     * @return The {@link Matrix3 rotation}, in parent-space.
     */
    Matrix3 getLocalRotation();

    /**
     * Gets the combined <i>relative</i> translation, rotation, and scaling
     * transforms of <code>this</code> {@link Node node}.
     *
     * @return A {@link Matrix4 matrix} containing the accumulated
     *         transformations for <code>this</code> {@link Node node} relative
     *         to its parent.
     */
    Matrix4 getLocalTransform();

    /**
     * Gets <code>this</code> {@link Node node's} {@link Vector3 position}, as
     * derived from all of its parents.
     *
     * @return The derived {@link Vector3 position} of <code>this</code>
     *         {@link Node node}.
     */
    Vector3 getWorldPosition();

    /**
     * Gets <code>this</code> {@link Node node's} {@link Matrix3 rotation}, as
     * derived from all of its parents.
     *
     * @return The derived {@link Matrix3 rotation} of <code>this</code>
     *         {@link Node node}.
     */
    Matrix3 getWorldRotation();

    /**
     * Gets <code>this</code> {@link Node node's} {@link Vector3 scaling}, as
     * derived from all of its parents.
     *
     * @return The derived {@link Vector3 scale} of <code>this</code>
     *         {@link Node node}.
     */
    Vector3 getWorldScale();

    /**
     * Gets the combined <i>derived</i> translation, rotation, and scaling
     * transforms of <code>this</code> {@link Node node}, as derived from all
     * its parents, specifically in that order.
     *
     * @return A {@link Matrix4 matrix} containing the accumulated
     *         transformations for <code>this</code> {@link Node node} relative
     *         to the world.
     */
    Matrix4 getWorldTransform();

    /**
     * Moves <code>this</code> {@link Node node} in the forward direction
     * relative to its own axes.
     *
     * @param offset
     *            The amount by which <code>this</code> {@link Node node} should
     *            be moved.
     * @see #moveBackward(float)
     * @see #moveLeft(float)
     * @see #moveRight(float)
     * @see #moveUp(float)
     * @see #moveDown(float)
     */
    void moveForward(float offset);

    /**
     * Moves <code>this</code> {@link Node node} in the backwards direction
     * relative to its own axes.
     *
     * @param offset
     *            The amount by which <code>this</code> {@link Node node} should
     *            be moved.
     * @see #moveForward(float)
     * @see #moveLeft(float)
     * @see #moveRight(float)
     * @see #moveUp(float)
     * @see #moveDown(float)
     */
    void moveBackward(float offset);

    /**
     * Moves <code>this</code> {@link Node node} to the left relative to its own
     * axes.
     *
     * @param offset
     *            The amount by which <code>this</code> {@link Node node} should
     *            be moved.
     * @see #moveForward(float)
     * @see #moveBackward(float)
     * @see #moveRight(float)
     * @see #moveUp(float)
     * @see #moveDown(float)
     */
    void moveLeft(float offset);

    /**
     * Moves <code>this</code> {@link Node node} to the right relative to its
     * own axes.
     *
     * @param offset
     *            The amount by which <code>this</code> {@link Node node} should
     *            be moved.
     * @see #moveForward(float)
     * @see #moveBackward(float)
     * @see #moveLeft(float)
     * @see #moveUp(float)
     * @see #moveDown(float)
     */
    void moveRight(float offset);

    /**
     * Moves <code>this</code> {@link Node node} to the right relative to its
     * own axes.
     *
     * @param offset
     *            The amount by which <code>this</code> {@link Node node} should
     *            be moved.
     * @see #moveForward(float)
     * @see #moveBackward(float)
     * @see #moveLeft(float)
     * @see #moveRight(float)
     * @see #moveDown(float)
     */
    void moveUp(float offset);

    /**
     * Moves <code>this</code> {@link Node node} to the right relative to its
     * own axes.
     *
     * @param offset
     *            The amount by which <code>this</code> {@link Node node} should
     *            be moved.
     * @see #moveForward(float)
     * @see #moveBackward(float)
     * @see #moveLeft(float)
     * @see #moveRight(float)
     * @see #moveUp(float)
     */
    void moveDown(float offset);

    /**
     * Move <code>this</code> node along the axes by the specified amounts,
     * relative to its parent.
     *
     * @param x
     *            The amount to move along the x-axis.
     * @param y
     *            The amount to move along the y-axis.
     * @param z
     *            The amount to move along the z-axis.
     * @see #translate(Vector3)
     */
    void translate(float x, float y, float z);

    /**
     * Move <code>this</code> {@link Node node} along the axes by the specified
     * vector, relative to its parent.
     *
     * @param tv
     *            The translation vector.
     * @see #translate(float, float, float)
     */
    void translate(Vector3 tv);

    /**
     * Rotate <code>this</code> {@link Node node} around the y-axis.
     *
     * @param angle
     *            The {@link Angle} of rotation.
     */
    void yaw(Angle angle);

    /**
     * Rotate <code>this</code> {@link Node node} around the z-axis.
     *
     * @param angle
     *            The {@link Angle} of rotation.
     */
    void roll(Angle angle);

    /**
     * Rotate <code>this</code> {@link Node node} around the x-axis.
     *
     * @param angle
     *            The {@link Angle} of rotation.
     */
    void pitch(Angle angle);

    /**
     * Rotate <code>this</code> {@link Node node} by the specified angle around
     * the given axis.
     *
     * @param angle
     *            The {@link Angle} of rotation.
     * @param axis
     *            The axis of rotation.
     */
    void rotate(Angle angle, Vector3 axis);

    /**
     * Apply the specified scaling factor to <code>this</code> {@link Node
     * node's} current scaling.
     *
     * @param x
     *            The scaling along the x-axis.
     * @param y
     *            The scaling along the y-axis.
     * @param z
     *            The scaling along the z-axis.
     * @see #scale(Vector3)
     */
    void scale(float x, float y, float z);

    /**
     * Apply the specified {@link Vector3 scaling} factor to <code>this</code>
     * {@link Node node's} current scaling.
     *
     * @param sv
     *            The scaling factor for <code>this</code> {@link Node node}.
     * @see #scale(float, float, float)
     */
    void scale(Vector3 sv);

    /**
     * Points the {@link Node node} towards the specified location.
     *
     * @param x
     *            The x-coordinate of the point to look at.
     * @param y
     *            The y-coordinate of the point to look at.
     * @param z
     *            The z-coordinate of the point to look at.
     */
    void lookAt(float x, float y, float z);

    /**
     * Points the {@link Node node} towards the specified location.
     *
     * @param target
     *            A vector specifying the look-at point.
     */
    void lookAt(Vector3 target);

    /**
     * Points the {@link Node node} towards the specified location using the
     * specified world "up" {@link Vector3 vector}.
     *
     * @param target
     *            A vector specifying the look-at point.
     * @param up
     *            A vector specifying the "up" direction in the world.
     */
    void lookAt(Vector3 target, Vector3 up);

    /**
     * Makes <code>this</code> {@link Node node} look at the specified
     * {@link Node node}.
     *
     * @param target
     *            The {@link Node node} <code>this</code> one should look at.
     * @throws IllegalArgumentException
     *             If the argument is <code>null</code>.
     */
    void lookAt(Node target);

    /**
     * Makes <code>this</code> {@link Node node} look at the specified
     * {@link Node node} using the specified world "up" {@link Vector3 vector}.
     *
     * @param target
     *            The {@link Node node} <code>this</code> one should look at.
     * @param up
     *            A vector specifying the "up" direction in the world.
     * @throws IllegalArgumentException
     *             If the argument is <code>null</code>.
     */
    void lookAt(Node target, Vector3 up);

    /**
     * Gets a side {@link Vector3 vector} pointing to the right side, in
     * parent-space.
     *
     * @return A {@link Vector3 vector} with the right direction.
     */
    Vector3 getLocalRightAxis();

    /**
     * Gets a {@link Vector3 vector} representing the up direction, in
     * parent-space.
     *
     * @return A {@link Vector3 vector} with the up direction.
     */
    Vector3 getLocalUpAxis();

    /**
     * Gets a {@link Vector3 vector} representing the forward direction, in
     * parent-space.
     *
     * @return A {@link Vector3 vector} with the forward direction.
     */
    Vector3 getLocalForwardAxis();

    /**
     * Gets a side {@link Vector3 vector} pointing to the right side, in
     * world-space.
     *
     * @return A {@link Vector3 vector} with the right direction.
     */
    Vector3 getWorldRightAxis();

    /**
     * Gets a {@link Vector3 vector} representing the up direction, in
     * world-space.
     *
     * @return A {@link Vector3 vector} with the up direction.
     */
    Vector3 getWorldUpAxis();

    /**
     * Gets a {@link Vector3 vector} representing the forward direction, in
     * world-space.
     *
     * @return A {@link Vector3 vector} with the forward direction.
     */
    Vector3 getWorldForwardAxis();

    /**
     * Tells <code>this</code> {@link Node node} that it has been attached to
     * the specified parent. If <code>this</code> {@link Node node} has a
     * {@link Node.Listener} <i>and</i> its new parent is different to the
     * current one, then it emits {@link Node.Listener#onNodeAttached(Node)}. If
     * both the new and current parents are the same, then no "re-parenting" has
     * really taken place and the listener does nothing.
     * <p>
     * The root {@link Node node} does not have a parent. Adding a parent to the
     * root {@link Node node} wouldn't make sense and could cause infinite
     * cycles through the graph.
     * <p>
     * <b>WARNING:</b> <i>This method is meant to be used internally by the
     * framework and not by game clients. Use directly only if you really know
     * what you're doing.</i>
     *
     * @param newParent
     *            The new parent for <code>this</code> {@link Node node}.
     * @throws IllegalArgumentException
     *             If the new parent is <code>null</code>.
     * @see #attachChild(Node)
     * @see #notifyDetached()
     */
    void notifyAttached(Node newParent);

    /**
     * Tells <code>this</code> {@link Node node} that it has been detached from
     * its current parent. If <code>this</code> {@link Node node} has a
     * {@link Node.Listener} <i>and</i> a non-<code>null</code> parent, then it
     * emits {@link Node.Listener#onNodeDetached(Node)}. If <code>this</code>
     * {@link Node node} was already detached, then the listener does nothing.
     * <p>
     * <b>WARNING:</b> <i>This method is meant to be used internally by the
     * framework and not by game clients. Use directly only if you really know
     * what you're doing.</i>
     *
     * @see #detachChild(Node)
     * @see #notifyAttached(Node)
     */
    void notifyDetached();

    /**
     * Gets <code>this</code> {@link Node node's} parent {@link Node node}, if
     * any. Otherwise <code>null</code>.
     * <p>
     * The root {@link Node node} always returns <code>null</code>.
     *
     * @return The parent {@link Node node} of <code>this</code> node, if any.
     */
    Node getParent();

    /**
     * Adds a {@link Node node} as a child of <code>this</code> {@link Node
     * node}. The child is automatically notified via
     * {@link #notifyAttached(Node)}.
     * <p>
     * A child can have only one parent. If it's already attached to another
     * {@link Node node}, it must be detached first.
     *
     * @param child
     *            The {@link Node node} to be a child of <code>this</code> one.
     * @throws IllegalArgumentException
     *             If the child is already attached or is <code>null</code>.
     * @see #detachChild(Node)
     * @see #notifyAttached(Node)
     */
    void attachChild(Node child);

    /**
     * Gets a <i>direct</i> descendant of <code>this</code> {@link Node node}.
     *
     * @param name
     *            The name of the descendant.
     *
     * @return A descendant of <code>this</code> {@link Node node}.
     * @throws IllegalArgumentException
     *             If a descendant with the given name is not found.
     * @see #getChild(int)
     * @see #attachChild(Node)
     */
    Node getChild(String name);

    /**
     * Gets a <i>direct</i> descendant of <code>this</code> {@link Node node},
     * based on the order in which they were attached.
     *
     * @param index
     *            The index of the child {@link Node node}.
     *
     * @return A direct child of <code>this</code> {@link Node node} at the
     *         specified index.
     * @throws IndexOutOfBoundsException
     *             If a <code>index < 0 || index >= getChildCount()</code>.
     * @see #getChild(String)
     * @see #attachChild(Node)
     */
    Node getChild(int index);

    /**
     * Gets the number of <i>direct</i> descendants <code>this</code>
     * {@link Node node} has.
     *
     * @return The current number of child {@link Node nodes}.
     */
    int getChildCount();

    /**
     * Gets an {@link Iterable} to go over the <i>direct</i> descendants of
     * <code>this</code> {@link Node node}.
     *
     * @return The {@link Iterable}.
     */
    Iterable<Node> getChildNodes();

    /**
     * Detaches the specified {@link Node node} from <code>this</code>
     * {@link Node node}. The child is automatically notified via
     * {@link #notifyDetached()}.
     *
     * @param child
     *            The {@link Node node} to be detached.
     * @throws IllegalArgumentException
     *             If the {@link Node node} is not a <i>direct</i> child of
     *             <code>this</code> {@link Node node} or is <code>null</code>.
     * @see #detachChild(Node)
     * @see #notifyAttached(Node)
     */
    void detachChild(Node child);

    /**
     * Detaches all child {@link Node nodes} from <code>this</code> {@link Node
     * node}, if any.
     */
    void detachAllChildren();

    PhysicsObject getPhysicsObject();
    
    void setPhysicsObject(PhysicsObject physicsObject);
	
    /**
     * Updates the {@link Node node} and (optionally) causes the updates to
     * propagate through the graph.
     *
     * @param updateChildren
     *            If true, the update cascades down to all children. False
     *            otherwise. Specific {@link SceneManager} implementations may
     *            want to update {@link Node nodes} manually.
     * @param parentHasChanged
     *            This flag indicates that the parent transform has changed and
     *            that the child should retrieve the parent's transform to
     *            combine it with its own, even if it hasn't changed itself.
     */
    void update(boolean updateChildren, boolean parentHasChanged);

    /**
     * Updates the {@link Node node} and automatically causes updates to
     * propagate through the graph. This {@link Node node} will let its children
     * know if it has changed in some way, so that the children can know whether
     * they need to update themselves.
     *
     * <pre>
     * boolean changed = parentHasChanged || thisHasChanged;
     * node.update(true, changed);
     * </pre>
     *
     * @see #update(boolean, boolean)
     */
    void update();

    /**
     * Update <code>this</code> {@link Node node's} transforms using its
     * parent's derived transforms.
     */
    void updateFromParent();

    /**
     * Sets the current listener for <code>this</code> {@link Node node}.
     *
     * @param listener
     *            The {@link Node.Listener} of <code>this</code> {@link Node
     *            node}.
     */
    void setListener(Listener listener);

    /**
     * Gets the current {@link Node.Listener} of <code>this</code> {@link Node
     * node}, if any.
     *
     * @return The current {@link Node.Listener}, if any, or <code>null</code>.
     */
    Listener getListener();

}
