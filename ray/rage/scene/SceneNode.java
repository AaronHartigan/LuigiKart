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
 * A <i>scene node</i> is a {@link Node node} used as the basis for a tree-based
 * scene graph data structure, allowing {@link SceneObject scene-objects} to be
 * attached to the scene {@link Node nodes}.
 * <p>
 * Scene {@link Node nodes} are created and {@link Managed managed} by a
 * {@link SceneManager scene-manager} and may be aware of a {@link SceneManager
 * scene-manager's} space partitioning schemes, if any.
 *
 * @author Raymond L. Rivera
 *
 * @see SceneObject
 *
 */
public interface SceneNode extends Node, Managed<SceneManager> {

    /**
     * Creates a new child for <code>this</code> {@link SceneNode scene-node}.
     * <p>
     * Since {@link SceneNode scene-nodes} are aware of the manager that created
     * them, implementations use {@link SceneManager#createSceneNode(String)}
     * when instantiating children. This allows the manager to remain aware of
     * all the {@link SceneNode scene-nodes}.
     *
     * @param childName
     *            A unique name for the child.
     * @return A new {@link SceneNode scene-node}.
     * @throws IllegalArgumentException
     *             If the name is empty.
     * @throws RuntimeException
     *             If the name is already in use.
     * @throws NullPointerException
     *             If the name is <code>null</code>.
     */
    SceneNode createChildSceneNode(String childName);

    /**
     * Notifies <code>this</code> {@link SceneNode scene-node} that it is the
     * root {@link Node node}.
     * <p>
     * <b>WARNING:</b> <i>This method is meant to be used internally by the
     * framework and not by game clients. Only the {@link SceneManager
     * scene-manager} should use this.</i>
     *
     * @throws RuntimeException
     *             If <code>this</code> {@link SceneNode scene-node} has a
     *             parent.
     */
    void notifyRootNode();

    /**
     * Attaches a {@link SceneObject scene-object} to <code>this</code>
     * {@link SceneNode scene-node}.
     * <p>
     * The {@link SceneNode scene-node} is responsible for notifying the
     * {@link SceneObject scene-object} that it just got attached by invoking
     * its {@link SceneObject#notifyAttached(SceneNode)} method.
     *
     * @param so
     *            The {@link SceneObject scene-object} being attached.
     * @throws RuntimeException
     *             If the {@link SceneObject scene-object} is already attached.
     * @throws NullPointerException
     *             If the {@link SceneObject scene-object} is <code>null</code>.
     */
    void attachObject(SceneObject so);

    /**
     * Gets the named {@link SceneObject scene-object} attached to
     * <code>this</code> {@link SceneNode scene-node}.
     * 
     * @param name
     *            The name of the attached {@link SceneObject scene-object}.
     * @return The {@link SceneObject scene-object} attached.
     * @throws RuntimeException
     *             If the specified {@link SceneObject scene-object} is not
     *             attached.
     */
    SceneObject getAttachedObject(String name);

    /**
     * Gets the specified {@link SceneObject scene-object} attached to
     * <code>this</code> {@link SceneNode scene-node}.
     * 
     * @param index
     *            The position of the attached {@link SceneObject scene-object}.
     * @return The {@link SceneObject scene-object} attached, if any.
     * @throws IndexOutOfBoundsException
     *             If the index is outside the
     *             <code>[0, getAttachedObjectCount())</code> range.
     */
    SceneObject getAttachedObject(int index);

    /**
     * Gets the number of {@link SceneObject scene-objects} attached to
     * <code>this</code> {@link SceneNode scene-node}.
     *
     * @return The number of currently attached {@link SceneObject
     *         scene-objects}.
     */
    int getAttachedObjectCount();

    /**
     * Gets an {@link Iterable} of attached {@link SceneObject scene-objects}.
     *
     * @return The {@link Iterable}.
     */
    Iterable<SceneObject> getAttachedObjects();

    /**
     * Detaches the {@link SceneObject scene-object} from <code>this</code>
     * {@link SceneNode scene-node}.
     * <p>
     * The {@link SceneNode scene-node} is responsible for notifying the
     * {@link SceneObject scene-object} that it just got detached by invoking
     * {@link SceneObject#notifyDetached()}.
     *
     * @param so
     *            The {@link SceneObject scene-object} to be detached.
     * @throws NullPointerException
     *             If the specified {@link SceneObject scene-object} is
     *             <code>null</code>.
     * @throws RuntimeException
     *             If the specified {@link SceneObject scene-object} is not
     *             attached.
     */
    void detachObject(SceneObject so);

    /**
     * Detaches the {@link SceneObject scene-object} from <code>this</code>
     * {@link SceneNode scene-node}.
     * <p>
     * The {@link SceneNode scene-node} is responsible for notifying the
     * {@link SceneObject scene-object} that it just got detached by invoking
     * {@link SceneObject#notifyDetached()}.
     *
     * @param name
     *            The name of the {@link SceneObject scene-object} to detach.
     * @return The {@link SceneObject scene-object} that has been detached.
     * @throws RuntimeException
     *             If the specified {@link SceneObject scene-object} is not
     *             attached.
     */
    SceneObject detachObject(String name);

    /**
     * Detaches all the {@link SceneObject scene-objects} from <code>this</code>
     * {@link SceneNode scene-node}, <i>without destroying them</i>.
     * <p>
     * The {@link SceneNode scene-node} is responsible for notifying all
     * {@link SceneObject scene-objects} that they just got detached by invoking
     * their {@link SceneObject#notifyDetached()} method.
     */
    void detachAllObjects();

    /**
     * Notifies <code>this</code> {@link SceneNode scene-node} whether it's in
     * the scene graph or not.
     * <p>
     * A {@link SceneNode scene-node} is in the scene graph when there's an
     * unbroken link between <code>this</code> {@link SceneNode scene-node} all
     * the way to its ultimate ancestor, the root {@link SceneNode scene-node}.
     * <p>
     * <b>WARNING:</b> <i>This method is meant to be used internally by the
     * framework and not by game clients. Use directly only if you really know
     * what you're doing.</i>
     *
     * @param inGraph
     *            True if <code>this</code> {@link SceneNode scene-node} is in
     *            the scene graph. Otherwise false.
     */
    void notifyInSceneGraph(boolean inGraph);

    /**
     * Checks whether <code>this</code> {@link SceneNode scene-node} is in the
     * scene graph or not.
     * <p>
     * A {@link SceneNode scene-node} is in the scene graph when there's an
     * unbroken link between <code>this</code> {@link SceneNode scene-node} all
     * the way to its ultimate ancestor, the root {@link SceneNode scene-node}.
     *
     * @return True if <code>this</code> {@link SceneNode scene-node} is in the
     *         scene graph. Otherwise false.
     */
    boolean isInSceneGraph();

}
