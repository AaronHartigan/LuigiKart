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

import java.io.*;
import java.nio.file.*;

import ray.rage.asset.animation.*;
import ray.rage.asset.material.*;
import ray.rage.asset.mesh.*;
import ray.rage.asset.skeleton.*;
import ray.rage.asset.texture.*;
import ray.rage.common.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.shader.*;
import ray.rage.scene.AmbientLight;
import ray.rage.scene.Camera;
import ray.rage.scene.Entity;
import ray.rage.scene.Light;
import ray.rage.scene.ManualObject;
import ray.rage.scene.ManualObjectSection;
import ray.rage.scene.Node;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneNode;
import ray.rage.scene.SceneObject;
import ray.rage.scene.SkeletalEntity;
import ray.rage.scene.SkyBox;
import ray.rage.scene.Tessellation;
import ray.rage.util.*;

/**
 * A <i>scene manager</i> handles the creation, organization, and rendering of a
 * <i>scene</i>, i.e., a collection of objects and potentially world geometry.
 * <p>
 * Generally speaking, scene managers responsibilities include:
 * <ul>
 * <li>Creating and placing objects (e.g. {@link Camera cameras},
 * {@link SceneObject scene-objects}, etc.) in the scene so that they can be
 * accessed efficiently (e.g. during graph traversal);</li>
 * <li>Loading and assembling world geometry, which is generally
 * large/sprawling/static;</li>
 * <li>Implementing <i>scene queries</i> to answer questions about the contents
 * or state of a scene;</li>
 * <li>Culling non-visible objects and placing visible ones into
 * {@link RenderQueue render-queues} for rendering;</li>
 * <li>Setup and rendering of other objects in the scene (e.g. {@link SkyBox
 * sky-boxes}, {@link Light lights}, etc.);</li>
 * <li>Passing this organized content to a {@link RenderSystem render-system}
 * for rendering;</li>
 * </ul>
 *
 * @author Raymond L. Rivera
 *
 */
public interface SceneManager extends Disposable {

    /**
     * The <i>environment</i> is the kind of world or setting the
     * {@link SceneManager scene-manager} is expected to handle.
     * <p>
     * Currently, only a {@link #GENERIC} implementation is available, but other
     * implementations can be tailored specifically for <i>indoor</i> or
     * <i>outdoor</i> environments, with appropriate space partitioning
     * algorithms for each.
     *
     * @author Raymond L. Rivera
     *
     */
    enum Environment {
        /**
         * Used for a {@link SceneManager scene-manager} that's not optimized
         * for any particular {@link SceneManager.Environment environment}.
         */
        GENERIC
    };

    /**
     * A listener for {@link SceneManager scene-manager} events.
     *
     * @author Raymond L. Rivera
     *
     */
    public interface Listener {

        /**
         * Event emitted <i>before</i> the scene graph is updated by
         * <code>this</code> {@link SceneManager manager}.
         *
         * @param sm
         *            The {@link SceneManager scene-manager} raising this event.
         * @param cam
         *            The {@link Camera camera} being updated.
         */
        void onPreUpdateSceneGraph(SceneManager sm, Camera cam);

        /**
         * Event emitted <i>after</i> the scene graph has been updated by
         * <code>this</code> {@link SceneManager manager}.
         *
         * @param sm
         *            The {@link SceneManager scene-manager} raising this event.
         * @param cam
         *            The {@link Camera camera} being updated.
         */
        void onPostUpdateSceneGraph(SceneManager sm, Camera cam);

    }

    /**
     * Gets the {@link SceneManager.Environment environment} <code>this</code>
     * {@link SceneManager scene-manager} is most appropriate for.
     *
     * @return The {@link SceneManager.Environment environment}.
     */
    Environment getEnvironment();

    // ------------------------------------------------------------------------
    // Camera region
    // ------------------------------------------------------------------------

    /**
     * Creates a new {@link Camera camera} with the specified
     * {@link Camera.Frustum.Projection projection} for its
     * {@link Camera.Frustum frustum}.
     *
     * @param name
     *            The name that identifies the new {@link Camera camera}.
     * @param proj
     *            The {@link Camera.Frustum.Projection projection} for the new
     *            {@link Camera camera}.
     * @throws RuntimeException
     *             If the name already exists.
     * @throws IllegalArgumentException
     *             If the name is empty.
     * @throws NullPointerException
     *             If any of the arguments is <code>null</code>.
     * @return A new {@link Camera camera}.
     */
    Camera createCamera(String name, Camera.Frustum.Projection proj);

    /**
     * Checks whether <code>this</code> {@link SceneManager manager} owns a
     * {@link Camera camera} with the specified name.
     *
     * @param name
     *            The name of the {@link Camera camera}.
     * @return True if the {@link Camera camera} is owned by <code>this</code>
     *         {@link SceneManager manager}. Otherwise false.
     */
    boolean hasCamera(String name);

    /**
     * Gets the {@link Camera camera} with the specified name, if it exists.
     *
     * @param name
     *            The name of the {@link Camera camera}.
     * @return The {@link Camera camera}, if it exists. Otherwise
     *         <code>null</code>.
     * @throws RuntimeException
     *             If the {@link SceneManager manager} does not own a
     *             {@link Camera camera} with the specified name.
     */
    Camera getCamera(String name);

    /**
     * Lets the caller iterate over all the {@link Camera cameras} owned by
     * <code>this</code> {@link SceneManager manager}.
     *
     * @return An {@link Iterable} of {@link Camera cameras}.
     */
    Iterable<Camera> getCameras();

    /**
     * Gets the number of {@link Camera cameras} owned by <code>this</code>
     * {@link SceneManager manager}.
     *
     * @return The number of {@link Camera cameras}.
     */
    int getCameraCount();

    /**
     * Destroys the {@link Camera camera} by the specified name, if it exists.
     * <p>
     * The {@link SceneManager manager} destroys the instance by removing it
     * from its internal map and invoking its {@link Disposable#notifyDispose()}
     * method.
     *
     * @param name
     *            The name of the {@link Camera camera} to be destroyed.
     * @throws RuntimeException
     *             If no {@link Camera camera} by the specified name exists.
     */
    void destroyCamera(String name);

    /**
     * Destroys the {@link Camera camera} by the specified name, if it exists.
     * <p>
     * The {@link SceneManager manager} destroys the instance by removing it
     * from its internal map and invoking its {@link Disposable#notifyDispose()}
     * method.
     *
     * @param c
     *            The {@link Camera camera} to be destroyed.
     * @throws RuntimeException
     *             If no {@link Camera camera} by the specified name exists.
     * @throws NullPointerException
     *             If the {@link Camera camera} is <code>null</code>.
     */
    void destroyCamera(Camera c);

    // ------------------------------------------------------------------------
    // Entity region
    // ------------------------------------------------------------------------

    /**
     * Creates a new {@link Entity entity} with the specified name based on the
     * {@link Mesh mesh} at the specified file system path.
     *
     * @param name
     *            The name that identifies the new {@link Entity entity}.
     * @param meshPath
     *            The string {@link Path path} to the {@link Mesh mesh} data
     *            file.
     * @throws RuntimeException
     *             If the name already exists.
     * @throws IllegalArgumentException
     *             If the name or path are empty.
     * @throws NullPointerException
     *             If any of the arguments is <code>null</code>.
     * @throws IOException
     *             If the {@link Mesh mesh} file cannot be found or read.
     * @return A new {@link Entity entity}.
     */
    Entity createEntity(String name, String meshPath) throws IOException;

    /**
     * Checks whether <code>this</code> {@link SceneManager manager} owns an
     * {@link Entity entity} with the specified name.
     *
     * @param name
     *            The name of the {@link Entity entity}.
     * @return True if the {@link Entity entity} is owned by <code>this</code>
     *         {@link SceneManager manager}. Otherwise false.
     */
    boolean hasEntity(String name);

    /**
     * Gets the {@link Entity entity} with the specified name, if it exists.
     *
     * @param name
     *            The name of the {@link Entity entity}.
     * @return The {@link Entity entity}, if it exists. Otherwise
     *         <code>null</code>.
     * @throws RuntimeException
     *             If the {@link SceneManager manager} does not own an
     *             {@link Entity entity} with the specified name.
     */
    Entity getEntity(String name);

    /**
     * Lets the caller iterate over all the {@link Entity entities} owned by
     * <code>this</code> {@link SceneManager manager}.
     *
     * @return An {@link Iterable} of {@link Entity entities}.
     */
    Iterable<Entity> getEntities();

    /**
     * Gets the number of {@link Entity entities} owned by <code>this</code>
     * {@link SceneManager manager}.
     *
     * @return The number of {@link Entity entities}.
     */
    int getEntityCount();

    /**
     * Destroys the {@link Entity entity} by the specified name, if it exists.
     * <p>
     * The {@link SceneManager manager} destroys the instance by removing it
     * from its internal map and invoking its {@link Disposable#notifyDispose()}
     * method.
     *
     * @param name
     *            The name of the {@link Entity entity} to be destroyed.
     * @throws RuntimeException
     *             If no {@link Entity entity} by the specified name exists.
     */
    void destroyEntity(String name);

    /**
     * Destroys the {@link Entity entity} by the specified name, if it exists.
     * <p>
     * The {@link SceneManager manager} destroys the instance by removing it
     * from its internal map and invoking its {@link Disposable#notifyDispose()}
     * method.
     *
     * @param e
     *            The {@link Entity entity} to be destroyed.
     * @throws RuntimeException
     *             If no {@link Entity entity} by the specified name exists.
     * @throws NullPointerException
     *             If the {@link Entity entity} is <code>null</code>.
     */
    void destroyEntity(Entity e);

    // ------------------------------------------------------------------------
    // ManualObject region
    // ------------------------------------------------------------------------

    /**
     * Creates a new {@link SkeletalEntity skeletal-entity} with the specified
     * name based on the {@link Mesh mesh} and {@link Skeleton skeleton} at
     * the specified file system path.
     *
     * @param name
     *            The name that identifies the new {@link Entity skeletalEntity}.
     * @param meshPath
     *            The string {@link Path path} to the {@link Mesh skeletalMesh} data
     *            file.
     * @param skeletonPath
     * 			  The string {@link Path path} to the {@link Skeleton skleleton} data file.
     * @throws RuntimeException
     *             If the name already exists.
     * @throws IllegalArgumentException
     *             If the name or paths are empty.
     * @throws NullPointerException
     *             If any of the arguments is <code>null</code>.
     * @throws IOException
     *             If the {@link Mesh skeletalMesh} file cannot be found or read.
     * @return A new {@link Entity entity}.
     */
    SkeletalEntity createSkeletalEntity(String name, String meshPath, String skeletonPath) throws IOException;

    // ------------------------------------------------------------------------
    // ManualObject region
    // ------------------------------------------------------------------------

    /**
     * Creates a new {@link ManualObject manual-object} with the specified name.
     * <p>
     * The {@link MeshManager mesh-manager} used by <code>this</code>
     * {@link SceneManager scene-manager} must be used to create a manual
     * {@link Mesh mesh}, with {@link MeshManager#createManualAsset(String)}.
     * <p>
     * It is the <i>client's responsibility</i> to explicitly set the
     * {@link GpuShaderProgram program} for the {@link ManualObject object}
     * <i>after</i> the {@link ManualObjectSection sections} have been created.
     *
     * @param name
     *            The name that will identify the new {@link ManualObject
     *            manual-object}.
     * @throws RuntimeException
     *             If the name already exists.
     * @throws IllegalArgumentException
     *             If the name is empty.
     * @throws NullPointerException
     *             If the name is <code>null</code>.
     * @return A new {@link ManualObject manual-object}.
     * @see ManualObject#setGpuShaderProgram(GpuShaderProgram)
     * @see ManualObjectSection#setGpuShaderProgram(GpuShaderProgram)
     */
    ManualObject createManualObject(String name);

    /**
     * Checks whether <code>this</code> {@link SceneManager manager} owns a
     * {@link ManualObject manual-object} with the specified name.
     *
     * @param name
     *            The name of the {@link ManualObject manual-object}.
     * @return True if the {@link ManualObject manual-object} is owned by
     *         <code>this</code> {@link SceneManager manager}. Otherwise false.
     */
    boolean hasManualObject(String name);

    /**
     * Gets the {@link ManualObject manual-object} with the specified name, if
     * it exists.
     *
     * @param name
     *            The name of the {@link ManualObject manual-object}.
     * @return The {@link ManualObject manual-object}, if it exists. Otherwise
     *         <code>null</code>.
     * @throws RuntimeException
     *             If the {@link SceneManager manager} does not own a
     *             {@link ManualObject manual-object} with the specified name.
     */
    ManualObject getManualObject(String name);

    /**
     * Lets the caller iterate over all the {@link ManualObject manual-objects}
     * owned by <code>this</code> {@link SceneManager manager}.
     *
     * @return An {@link Iterable} of {@link ManualObject manual-objects}.
     */
    Iterable<ManualObject> getManualObjects();

    /**
     * Gets the number of {@link ManualObject manual-objects} owned by
     * <code>this</code> {@link SceneManager manager}.
     *
     * @return The number of {@link ManualObject manual-objects}.
     */
    int getManualObjectCount();

    /**
     * Destroys the {@link ManualObject manual-object} by the specified name, if
     * it exists.
     * <p>
     * The {@link SceneManager manager} destroys the instance by removing it
     * from its internal map and invoking its {@link Disposable#notifyDispose()}
     * method.
     *
     * @param name
     *            The name of the {@link ManualObject manual-object} to be
     *            destroyed.
     * @throws RuntimeException
     *             If no {@link ManualObject manual-object} by the specified
     *             name exists.
     */
    void destroyManualObject(String name);

    /**
     * Destroys the {@link ManualObject manual-object} by the specified name, if
     * it exists.
     * <p>
     * The {@link SceneManager manager} destroys the instance by removing it
     * from its internal map and invoking its {@link Disposable#notifyDispose()}
     * method.
     *
     * @param mo
     *            The {@link ManualObject manual-object} to be destroyed.
     * @throws RuntimeException
     *             If no {@link ManualObject manual-object} by the specified
     *             name exists.
     * @throws NullPointerException
     *             If the {@link ManualObject manual-object} is
     *             <code>null</code>.
     */
    void destroyManualObject(ManualObject mo);
    
    // ------------------------------------------------------------------------
    // Tessellation region
    // ------------------------------------------------------------------------

    /**
     * Creates a new {@link Tessellation tessellation} with the specified name.
     *
     * @param name
     *            The name that will identify the new {@link Tessellation tessellation}.
     * @throws RuntimeException
     *             If the name already exists.
     * @throws IllegalArgumentException
     *             If the name is empty.
     * @throws NullPointerException
     *             If the name is <code>null</code>.
     * @return A new {@link Tessellation tessellation}.
     */
    Tessellation createTessellation(String name);
    
    /**
     * Creates a new {@link Tessellation tessellation} with the specified name.
     * <p>
     * A quality level can be provided to specify the amount of vertices to be 
     * created for each patch during tessellation. A quality level 5 is the minimum;
     * there is no maximum limit (thought it is recommended to not use values higher 
     * than 10).
     * <p>
     * The number of primitives is determined by Math.Pow(2, quality), and the call to
     * gl.glDrawArraysInstanced in {@link GL4RenderSystem GL4RenderSystem} uses
     * Math.Pow(2, quality) * Math.Pow(2, quality).
     *
     * @param name
     *            The name that will identify the new {@link Tessellation tessellation}.
     * @param quality
     *            The degree of vertices to provide to each patch. A value of 6 to 10 is recommended.
     * @throws RuntimeException
     *             If the name already exists.
     * @throws IllegalArgumentException
     *             If the name is empty.
     * @throws NullPointerException
     *             If the name is <code>null</code>.
     * @return A new {@link Tessellation tessellation}.
     */
    Tessellation createTessellation(String name, int quality);

    /**
     * Checks whether <code>this</code> {@link SceneManager manager} owns a
     * {@link Tessellation tessellation} with the specified name.
     *
     * @param name
     *            The name of the {@link Tessellation tessellation}.
     * @return True if the {@link Tessellation tessellation} is owned by <code>this</code>
     *         {@link SceneManager manager}. Otherwise false.
     */
    boolean hasTessellation(String name);

    /**
     * Gets the {@link Tessellation tessellation} with the specified name, if it exists.
     *
     * @param name
     *            The name of the {@link Tessellation tessellation}.
     * @return The {@link Tessellation tessellation}, if it exists. Otherwise
     *         <code>null</code>.
     * @throws RuntimeException
     *             If the {@link SceneManager manager} does not own a
     *             {@link Tessellation tessellation} with the specified name.
     */
    Tessellation getTessellation(String name);

    /**
     * Lets the caller iterate over all the {@link Tessellation tessellations} owned by
     * <code>this</code> {@link SceneManager manager}.
     *
     * @return An {@link Iterable} of {@link Tessellation tessellations}.
     */
    Iterable<Tessellation> getTessellations();

    /**
     * Gets the number of {@link Tessellation tessellations} owned by <code>this</code>
     * {@link SceneManager manager}.
     *
     * @return The number of {@link Tessellation tessellations}.
     */
    int getTessellationCount();

    /**
     * Destroys the {@link Tessellation tessellation} by the specified name, 
     * if it exists.
     * <p>
     * The {@link SceneManager manager} destroys the instance by removing it
     * from its internal map and invoking its {@link Disposable#notifyDispose()}
     * method.
     *
     * @param name
     *            The name of the {@link Tessellation tessellation} to be destroyed.
     * @throws RuntimeException
     *             If no {@link Tessellation tessellation} by the specified name exists.
     */
    void destroyTessellation(String name);

    /**
     * Destroys the {@link Tessellation tessellation} by the specified name, 
     * if it exists.
     * <p>
     * The {@link SceneManager manager} destroys the instance by removing it
     * from its internal map and invoking its {@link Disposable#notifyDispose()}
     * method.
     *
     * @param tessellation
     *            The {@link Tessellation tessellation} to be destroyed.
     * @throws RuntimeException
     *             If no {@link Tessellation tessellation} by the specified name exists.
     * @throws NullPointerException
     *             If the {@link Tessellation tessellation} is <code>null</code>.
     */
    void destroyTessellation(Tessellation to);

    // ------------------------------------------------------------------------
    // SkyBox region
    // ------------------------------------------------------------------------

    /**
     * Creates a new {@link SkyBox sky-box} with the specified name.
     *
     * @param name
     *            The name that will identify the new {@link SkyBox sky-box}.
     * @throws RuntimeException
     *             If the name already exists.
     * @throws IllegalArgumentException
     *             If the name is empty.
     * @throws NullPointerException
     *             If the name is <code>null</code>.
     * @return A new {@link SkyBox sky-box}.
     */
    SkyBox createSkyBox(String name);

    /**
     * Checks whether <code>this</code> {@link SceneManager manager} owns a
     * {@link SkyBox sky-box} with the specified name.
     *
     * @param name
     *            The name of the {@link SkyBox sky-box}.
     * @return True if the {@link SkyBox sky-box} is owned by <code>this</code>
     *         {@link SceneManager manager}. Otherwise false.
     */
    boolean hasSkyBox(String name);

    /**
     * Gets the {@link SkyBox sky-box} with the specified name, if it exists.
     *
     * @param name
     *            The name of the {@link SkyBox sky-box}.
     * @return The {@link SkyBox sky-box}, if it exists. Otherwise
     *         <code>null</code>.
     * @throws RuntimeException
     *             If the {@link SceneManager manager} does not own a
     *             {@link SkyBox sky-box} with the specified name.
     */
    SkyBox getSkyBox(String name);

    /**
     * Lets the caller iterate over all the {@link SkyBox sky-boxes} owned by
     * <code>this</code> {@link SceneManager manager}.
     *
     * @return An {@link Iterable} of {@link SkyBox sky-boxes}.
     */
    Iterable<SkyBox> getSkyBoxes();

    /**
     * Gets the number of {@link SkyBox sky-boxes} owned by <code>this</code>
     * {@link SceneManager manager}.
     *
     * @return The number of {@link SkyBox sky-boxes}.
     */
    int getSkyBoxCount();

    /**
     * Sets the specified {@link SkyBox sky-box} as the one that should be
     * actively rendered.
     *
     * @param skyBox
     *            The {@link SkyBox sky-box} to use for a scene.
     * @throws IllegalArgumentException
     *             If the name is empty.
     * @throws NullPointerException
     *             If the name is <code>null</code>.
     * @throws RuntimeException
     *             If <code>this</code> {@link SceneManager manager} does not
     *             own the specified {@link SkyBox sky-box}.
     */
    void setActiveSkyBox(SkyBox skyBox);

    /**
     * Specifies the name of the {@link SkyBox sky-box} that should be actively
     * rendered.
     *
     * @param name
     *            The name of the {@link SkyBox sky-box} to use for a scene.
     * @throws IllegalArgumentException
     *             If the name is empty.
     * @throws NullPointerException
     *             If the name is <code>null</code>.
     * @throws RuntimeException
     *             If <code>this</code> {@link SceneManager manager} does not
     *             own the specified {@link SkyBox sky-box}.
     */
    void setActiveSkyBox(String name);

    /**
     * Destroys the {@link SkyBox sky-box} by the specified name, if it exists.
     * <p>
     * The {@link SceneManager manager} destroys the instance by removing it
     * from its internal map and invoking its {@link Disposable#notifyDispose()}
     * method.
     *
     * @param name
     *            The name of the {@link SkyBox sky-box} to be destroyed.
     * @throws RuntimeException
     *             If no {@link SkyBox sky-box} by the specified name exists.
     */
    void destroySkyBox(String name);

    /**
     * Destroys the {@link SkyBox sky-box} by the specified name, if it exists.
     * <p>
     * The {@link SceneManager manager} destroys the instance by removing it
     * from its internal map and invoking its {@link Disposable#notifyDispose()}
     * method.
     *
     * @param skyBox
     *            The {@link SkyBox sky-box} to be destroyed.
     * @throws RuntimeException
     *             If no {@link SkyBox sky-box} by the specified name exists.
     * @throws NullPointerException
     *             If the {@link SkyBox sky-box} is <code>null</code>.
     */
    void destroySkyBox(SkyBox skyBox);

    // ------------------------------------------------------------------------
    // SceneNode region
    // ------------------------------------------------------------------------

    /**
     * Gets the {@link SceneNode scene-node} at the root of the scene graph.
     * <p>
     * This method must <i>always</i> return the root {@link SceneNode
     * scene-node}. It's not allowed to throw exceptions or return a
     * <code>null</code> value.
     *
     * @return The {@link SceneNode scene-node} at the root of the scene graph.
     */
    SceneNode getRootSceneNode();

    /**
     * Creates a new {@link SceneNode scene-node} <i>without adding it to the
     * scene graph</i>.
     * <p>
     * A {@link SceneNode scene-node} is said to be in the scene graph when it
     * can trace its unbroken ancestry all the way up to the root
     * {@link SceneNode scene-node}. The {@link SceneManager scene-manager} is
     * still responsible for tracking and managing the new {@link SceneNode
     * scene-node}. However, the new {@link SceneNode scene-node} will not have
     * a parent; it must be explicitly assigned by the client.
     * <p>
     * To add the {@link SceneNode scene-node} to the graph after the fact,
     * invoke {@link Node#attachChild(Node)} on the {@link SceneNode scene-node}
     * that should become the parent. For example:
     *
     * <pre>
     * parentNode.attachChild(childNode);
     * </pre>
     *
     * @param name
     *            The name for the new {@link SceneNode scene-node}.
     * @return A new {@link SceneNode scene-node} that is not part of the graph.
     */
    SceneNode createSceneNode(String name);

    /**
     * Checks whether a {@link SceneNode scene-node} with the given name is
     * owned by <code>this</code> {@link SceneManager scene-manager}.
     *
     * @param name
     *            The name of the {@link SceneNode scene-node} being checked.
     * @return True if the {@link SceneNode scene-node} exists. Otherwise false.
     */
    boolean hasSceneNode(String name);

    /**
     * Gets the {@link SceneNode scene-node} with the specified name, if it
     * exists.
     *
     * @param name
     *            The name of the {@link SceneNode scene-node}.
     * @return The {@link SceneNode scene-node}, if it exists. Otherwise
     *         <code>null</code>.
     * @throws RuntimeException
     *             If the {@link SceneManager manager} does not own a
     *             {@link SceneNode scene-node} with the specified name.
     */
    SceneNode getSceneNode(String name);

    /**
     * Lets the caller iterate over all the {@link SceneNode scene-nodes} owned
     * by <code>this</code> {@link SceneManager manager}.
     *
     * @return An {@link Iterable} of {@link SceneNode scene-nodes}.
     */
    Iterable<SceneNode> getSceneNodes();

    /**
     * Gets the number of {@link SceneNode scene-nodes} owned by
     * <code>this</code> {@link SceneManager manager}.
     *
     * @return The number of {@link SceneNode scene-nodes}.
     */
    int getSceneNodeCount();

    /**
     * Destroys the {@link SceneNode scene-node} by the specified name, if it
     * exists.
     * <p>
     * It's the {@link SceneManager scene-manager's} responsibility to make sure
     * the {@link SceneNode scene-node} is detached from its direct parent, if
     * any, and has all its {@link SceneObject scene-objects} and child
     * {@link SceneNode nodes} removed.
     *
     * @param name
     *            The name of the {@link SceneNode scene-node} to be destroyed.
     * @throws RuntimeException
     *             If no {@link SceneNode scene-node} by the specified name
     *             exists.
     * @throws IllegalArgumentException
     *             If the root {@link SceneNode scene-node} is specified.
     */
    void destroySceneNode(String name);

    /**
     * Destroys the {@link SceneNode scene-node} by the specified name, if it
     * exists.
     * <p>
     * It's the {@link SceneManager scene-manager's} responsibility to make sure
     * the {@link SceneNode scene-node} is detached from its direct parent, if
     * any, and has all its {@link SceneObject scene-objects} and child
     * {@link SceneNode nodes} removed.
     *
     * @param sn
     *            The {@link SceneNode scene-node} to be destroyed.
     * @throws RuntimeException
     *             If no {@link SceneNode scene-node} by the specified name
     *             exists.
     * @throws NullPointerException
     *             If the {@link SceneNode scene-node} is <code>null</code>.
     * @throws IllegalArgumentException
     *             If the root {@link SceneNode scene-node} is specified.
     */
    void destroySceneNode(SceneNode sn);

    // ------------------------------------------------------------------------
    // Lights region
    // ------------------------------------------------------------------------

    /**
     * Gets the {@link AmbientLight ambient-light} that provides global scene
     * illumination.
     * <p>
     * The {@link AmbientLight ambient-light} is a singleton instance created by
     * the {@link SceneManager scene-manager} during initialization and is
     * always available.
     * <p>
     * Note that, due to the {@link AmbientLight ambient-light's} role in
     * providing global illumination for a scene, it's not a {@link SceneObject
     * scene-object}.
     *
     * @return The {@link AmbientLight ambient-light}.
     */
    AmbientLight getAmbientLight();

    /**
     * Creates a new {@link Light light} of the specified {@link Light.Type
     * type}.
     * <p>
     * <b>NOTE:</b> While the manager can, and does, support creating multiple
     * {@link Light lights}, their number is currently limited to one because
     * the underlying shader programs are not yet capable of supporting multiple
     * sources simultaneously. This is expected to be a temporary limitation.
     *
     * @param name
     *            The name for the new {@link Light light}.
     * @param type
     *            The {@link Light.Type type} of the new {@link Light light}.
     * @return A new {@link Light light}.
     * @throws IllegalArgumentException
     *             If the name is empty.
     * @throws RuntimeException
     *             If there's already another {@link Light light} by the same
     *             name.
     * @throws NullPointerException
     *             If any of the arguments is <code>null</code>.
     */
    Light createLight(String name, Light.Type type);

    /**
     * Checks whether a {@link Light light} with the given name is owned by
     * <code>this</code> {@link SceneManager scene-manager}.
     *
     * @param name
     *            The name of the {@link Light light} being checked.
     * @return True if the {@link Light light} exists. Otherwise false.
     */
    boolean hasLight(String name);

    /**
     * Gets the {@link Light light} with the specified name, if it exists.
     *
     * @param name
     *            The name of the {@link Light light}.
     * @return The {@link Light light}, if it exists. Otherwise
     *         <code>null</code>.
     * @throws RuntimeException
     *             If the {@link SceneManager manager} does not own a
     *             {@link Light light} with the specified name.
     */
    Light getLight(String name);

    /**
     * Lets the caller iterate over all the {@link Light lights} owned by
     * <code>this</code> {@link SceneManager manager}.
     *
     * @return An {@link Iterable} of {@link Light lights}.
     */
    Iterable<Light> getLights();

    /**
     * Gets the number of {@link Light lights} owned by <code>this</code>
     * {@link SceneManager manager}.
     *
     * @return The number of {@link Light lights}.
     */
    int getLightCount();

    /**
     * Destroys the {@link Light light} by the specified name, if it exists.
     * <p>
     * The {@link SceneManager manager} destroys the instance by removing it
     * from its internal map and invoking its {@link Disposable#notifyDispose()}
     * method.
     *
     * @param name
     *            The name of the {@link Light light} to be destroyed.
     * @throws RuntimeException
     *             If no {@link Light light} by the specified name exists.
     * @throws IllegalArgumentException
     *             If the root {@link Light light} is specified.
     */
    void destroyLight(String name);

    /**
     * Destroys the {@link Light light} by the specified name, if it exists.
     * <p>
     * The {@link SceneManager manager} destroys the instance by removing it
     * from its internal map and invoking its {@link Disposable#notifyDispose()}
     * method.
     *
     * @param l
     *            The {@link Light light} to be destroyed.
     * @throws RuntimeException
     *             If no {@link Light light} by the specified name exists.
     * @throws NullPointerException
     *             If the {@link Light light} is <code>null</code>.
     */
    void destroyLight(Light l);

    /**
     * Destroys all the {@link SceneObject scene-objects} created by
     * <code>this</code> {@link SceneManager scene-manager}, except the root
     * {@link SceneNode scene-node}.
     * <p>
     * All {@link SceneObject scene-objects} destroyed during this process will
     * need to be re-created by the client.
     */
    void destroyAllSceneObjects();

    // ------------------------------------------------------------------------
    // Controllers
    // ------------------------------------------------------------------------

    /**
     * Adds the specified {@link Node.Controller controller} to
     * <code>this</code> {@link SceneManager scene-manager}.
     *
     * @param ctrl
     *            The {@link Node.Controller controller}.
     * @throws NullPointerException
     *             If the {@link Node.Controller controller} is
     *             <code>null</code>.
     */
    void addController(Node.Controller ctrl);

    /**
     * Gets the {@link Node.Controller controller} at the specified index, based
     * on the order in which they were added.
     *
     * @param index
     *            The zero-based index of the {@link Node.Controller
     *            controller's} position.
     * @return The {@link Node.Controller controller} at the specified index.
     * @throws IndexOutOfBoundsException
     *             If the index is out of the <code>[0, size())</code> range.
     */
    Node.Controller getController(int index);

    /**
     * Gets the number of {@link Node.Controller controllers} <code>this</code>
     * {@link SceneManager scene-manager} has.
     *
     * @return The number of {@link Node.Controller controllers}.
     */
    int getControllerCount();

    /**
     * Invokes {@link Node.Controller#update(float)} on every
     * {@link Node.Controller controller} <code>this</code> {@link SceneManager
     * scene-manager} has.
     *
     * @param elapsedTimeMillis
     *            The amount of time since the last update, in milliseconds.
     */
    void updateControllers(float elapsedTimeMillis);

    /**
     * Lets the caller iterate over all the {@link Node.Controller controllers}
     * owned by <code>this</code> {@link SceneManager manager}.
     *
     * @return An {@link Iterable} of {@link Node.Controller controllers}.
     */
    Iterable<Node.Controller> getControllers();

    /**
     * Removes all the {@link Node.Controller controllers} from
     * <code>this</code> {@link SceneManager manager's} control.
     * <p>
     * The {@link Node.Controller controllers} are not destroyed; only removed.
     * Updates must be done explicitly by the clients after this call.
     */
    void removeAllControllers();

    // ------------------------------------------------------------------------
    // Scene rendering, etc
    // ------------------------------------------------------------------------

    /**
     * Causes the scene graph to be updated and relevant
     * {@link SceneManager.Listener listeners} to be invoked.
     * <p>
     * Specific {@link SceneManager scene-manager} implementations should decide
     * how to best do this.
     */
    void updateSceneGraph();

    /**
     * Causes each {@link Camera camera} known to <code>this</code>
     * {@link SceneManager scene-manager} to render the scene from its own point
     * of view through its assigned {@link Viewport viewport} <i>before</i>
     * asking the {@link RenderSystem render-system} to swap the back-buffer.
     */
    void renderScene();

    /**
     * Renders the scene from the viewpoint of the specified {@link Camera
     * camera} through the specified {@link Viewport viewport}.
     * <p>
     * This method is internally invoked by each {@link Camera camera} directly.
     * This method is also responsible for emitting events for any registered
     * {@link RenderQueue.Listener render-queue-listeners} and
     * {@link SceneManager.Listener scene-manager-listeners}.
     * <p>
     * <b>WARNING:</b> <i>This method is meant to be used internally by the
     * framework and not by game clients. Use directly only if you really know
     * what you're doing.</i>
     *
     * @param camera
     *            The {@link Camera camera} using this {@link SceneManager
     *            scene-manager} to render the scene from its viewpoint.
     * @param viewport
     *            The {@link Viewport viewport} by which the specified
     *            {@link Camera camera} is allowed to "look" at the scene.
     * @see #renderScene()
     * @see Camera#renderScene()
     */
    void notifyRenderScene(Camera camera, Viewport viewport);

    /**
     * Sets the {@link RenderSystem render-system} <code>this</code>
     * {@link SceneManager scene-manager} should submit its {@link Renderable
     * renderables} to for processing.
     *
     * @param rs
     *            The {@link RenderSystem render-system} that will render the
     *            contents of the scene.
     * @throws NullPointerException
     *             If the {@link RenderSystem render-system} is
     *             <code>null</code>.
     */
    void setRenderSystem(RenderSystem rs);

    /**
     * Gets the {@link RenderSystem render-system} currently in use by
     * <code>this</code> {@link SceneManager scene-manager}, if one has been
     * set.
     *
     * @return The {@link RenderSystem render-system}, if any. Otherwise
     *         <code>null</code>.
     */
    RenderSystem getRenderSystem();

    /**
     * Sets the {@link MeshManager mesh-manager}.
     *
     * @param mm
     *            The {@link MeshManager mesh-manager}.
     * @throws NullPointerException
     *             If the {@link MeshManager mesh-manager} is <code>null</code>.
     */
    void setMeshManager(MeshManager mm);

    /**
     * Gets the {@link MeshManager mesh-manager}, if one has been set.
     *
     * @return The {@link MeshManager mesh-manager}, if any. Otherwise
     *         <code>null</code>.
     */
    MeshManager getMeshManager();

    /**
     * Sets the {@link SkeletonManager skeleton-manager}.
     *
     * @param sm
     *            The {@link SkeletonManager skeleton-manager}.
     * @throws NullPointerException
     *             If the {@link SkeletonManager skeleton-manager} is <code>null</code>.
     */
    void setSkeletonManager(SkeletonManager sm);

    /**
     * Gets the {@link SkeletonManager skeleton-manager}, if one has been set.
     *
     * @return The {@link SkeletonManager skeleton-manager}, if any. Otherwise
     *         <code>null</code>.
     */
    SkeletonManager getSkeletonManager();

    /**
     * Sets the {@link AnimationManager animation-manager}.
     *
     * @param am
     *            The {@link AnimationManager animation-manager}.
     * @throws NullPointerException
     *             If the {@link AnimationManager animation-manager} is <code>null</code>.
     */
    void setAnimationManager(AnimationManager am);

    /**
     * Gets the {@link AnimationManager animation-manager}, if one has been set.
     *
     * @return The {@link AnimationManager animation-manager}, if any. Otherwise
     *         <code>null</code>.
     */
    AnimationManager getAnimationManager();

    /**
     * Sets the {@link TextureManager texture-manager}.
     *
     * @param tm
     *            The {@link TextureManager texture-manager}.
     * @throws NullPointerException
     *             If the {@link TextureManager texture-manager} is
     *             <code>null</code>.
     */
    void setTextureManager(TextureManager tm);

    /**
     * Gets the {@link TextureManager texture-manager}, if one has been set.
     *
     * @return The {@link TextureManager texture-manager}, if any. Otherwise
     *         <code>null</code>.
     */
    TextureManager getTextureManager();

    /**
     * Sets the {@link MaterialManager material-manager}.
     *
     * @param mm
     *            The {@link MaterialManager material-manager}.
     * @throws NullPointerException
     *             If the {@link MaterialManager material-manager} is
     *             <code>null</code>.
     */
    void setMaterialManager(MaterialManager mm);

    /**
     * Gets the {@link MaterialManager material-manager}, if one has been set.
     *
     * @return The {@link MaterialManager material-manager}, if any. Otherwise
     *         <code>null</code>.
     */
    MaterialManager getMaterialManager();

    /**
     * Registers a {@link SceneManager.Listener listener} for
     * {@link SceneManager scene-manager} events.
     *
     * @param sml
     *            The {@link SceneManager.Listener listener}.
     * @throws NullPointerException
     *             If the {@link SceneManager.Listener listener} is
     *             <code>null</code>.
     */
    void addSceneManagerListener(SceneManager.Listener sml);

    /**
     * Removes the specified {@link SceneManager.Listener listener}.
     *
     * @param sml
     *            The {@link SceneManager.Listener listener}.
     * @throws NullPointerException
     *             If the {@link SceneManager.Listener listener} is
     *             <code>null</code>.
     */
    void removeSceneManagerListener(SceneManager.Listener sml);

    /**
     * Registers a {@link RenderQueue.Listener listener} for {@link RenderQueue
     * render-queue} events.
     *
     * @param rql
     *            The {@link RenderQueue.Listener listener}.
     * @throws NullPointerException
     *             If the {@link RenderQueue.Listener listener} is
     *             <code>null</code>.
     */
    void addRenderQueueListener(RenderQueue.Listener rql);

    /**
     * Removes the specified {@link RenderQueue.Listener listener}.
     *
     * @param rql
     *            The {@link RenderQueue.Listener listener}.
     * @throws NullPointerException
     *             If the {@link RenderQueue.Listener listener} is
     *             <code>null</code>.
     */
    void removeRenderQueueListener(RenderQueue.Listener rql);

    /**
     * Sets the {@link Configuration configuration}.
     *
     * @param conf
     *            The {@link Configuration configuration}.
     * @throws NullPointerException
     *             If the {@link Configuration configuration} is
     *             <code>null</code>.
     */
    void setConfiguration(Configuration conf);

    /**
     * Gets the {@link Configuration configuration}, if one has been set.
     *
     * @return The {@link Configuration configuration}, if any. Otherwise
     *         <code>null</code>.
     */
    Configuration getConfiguration();

}
