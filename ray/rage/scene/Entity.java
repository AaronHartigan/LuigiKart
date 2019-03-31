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

import ray.rage.asset.material.*;
import ray.rage.asset.mesh.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.rendersystem.shader.*;
import ray.rage.rendersystem.states.*;
import ray.rage.scene.Entity;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneObject;
import ray.rage.scene.SubEntity;

/**
 * An <i>entity</i> defines a discrete {@link SceneObject scene-object} based on
 * a {@link Mesh mesh}.
 * <p>
 * An entity is <i>based</i> on a single, potentially shared, {@link Mesh mesh},
 * but is <i>made of</i> one or more {@link SubEntity sub-entities}. In other
 * words, there's a <code>1:M</code> relation between entities and {@link Mesh
 * meshes} and also between entities and {@link SubEntity sub-entities}.
 * <p>
 * The {@link Mesh mesh} and {@link SubMesh sub-mesh} hold the geometric data
 * used by discrete objects. As such, entities are the real-world objects
 * defined by this geometry. This means there's usually a single {@link Mesh
 * mesh} for car, but there may be multiple car entities in the world made of
 * the same geometry.
 * <p>
 * Even though entities represent discrete objects in the world, entities are
 * <i>not</i> themselves {@link Renderable renderable} objects. This is what the
 * {@link SubEntity sub-entitiesy} are for and it's this conceptual distinction
 * that actually decouples scene <i>management</i> from scene <i>rendering</i>.
 * <p>
 * Entities are not meant to be created directly. Instead, you should use the
 * {@link SceneManager#createEntity(String, String)} factory method.
 *
 * @author Raymond L. Rivera
 *
 * @see SubEntity
 * @see Mesh
 *
 */
public interface Entity extends SceneObject {

    /**
     * Gets the {@link Mesh mesh} <code>this</code> {@link Entity entity} is
     * based on.
     *
     * @return The {@link Mesh mesh}.
     */
    Mesh getMesh();

    /**
     * Gets an {@link Iterable} for the {@link SubEntity sub-entities} that
     * <code>this</code> {@link Entity entity} is made of.
     *
     * @return The {@link SubEntity sub-entities} <code>this</code>
     *         {@link Entity entity} is made of.
     */
    Iterable<SubEntity> getSubEntities();

    /**
     * Gets the specified {@link SubEntity sub-entity}.
     * 
     * @param idx
     *            The index specifying which {@link SubEntity sub-entity} to
     *            return.
     * @return The {@link SubEntity sub-entity} at the specified index.
     * @throws ArrayIndexOutOfBoundsException
     *             If the argument is <code>null</code>.
     */
    SubEntity getSubEntity(int idx);

    /**
     * Gets the number of {@link SubEntity sub-entities} <code>this</code>
     * {@link Entity entity} is made of.
     *
     * @return The number of {@link SubEntity sub-entities}.
     */
    int getSubEntityCount();

    /**
     * A convenience method that sets the same {@link Material material} to
     * <i>all</i> its {@link SubEntity sub-entities}.
     * <p>
     * Only use this method if you want <i>all</i> the {@link SubEntity
     * sub-entities} of <code>this</code> {@link Entity entity} to use the same
     * {@link Material material} or if you're certain there's only one
     * {@link Material material} to be used.
     * <p>
     * If more granularity is needed, then use {@link #getSubEntity(int)} to
     * retrieve and update individual {@link SubEntity sub-entities}.
     *
     * @param mat
     *            The {@link Material material} to be set for each
     *            {@link SubEntity sub-entity}.
     * @throws NullPointerException
     *             If the argument is <code>null</code>.
     */
    void setMaterial(Material mat);

    /**
     * A convenience method that sets the {@link GpuShaderProgram
     * shader-program} that should be used to render the {@link SubEntity
     * sub-entities} of <code>this</code> {@link Entity entity}.
     * <p>
     * Only use this method if you want <i>all</i> the {@link SubEntity
     * sub-entities} of <code>this</code> {@link Entity entity} to use the same
     * {@link GpuShaderProgram shader-program}.
     * <p>
     * If more granularity is needed, then use {@link #getSubEntity(int)} to
     * retrieve and update individual {@link SubEntity sub-entities}.
     *
     * @param prog
     *            The {@link GpuShaderProgram shader-program} to use.
     * @throws NullPointerException
     *             If the argument is <code>null</code>.
     */
    void setGpuShaderProgram(GpuShaderProgram prog);

    /**
     * A convenience method that sets the same {@link Primitive primitive} for
     * <i>all</i> its {@link SubEntity sub-entities}.
     * <p>
     * Only use this method if you want <i>all</i> the {@link SubEntity
     * sub-entities} of <code>this</code> {@link Entity entity} to use the same
     * {@link Primitive primitive}.
     * <p>
     * If more granularity is needed, then use {@link #getSubEntity(int)} to
     * retrieve and update individual {@link SubEntity sub-entities}.
     *
     * @param prim
     *            The {@link Primitive primitive} to be set for each
     *            {@link SubEntity sub-entity}.
     * @throws NullPointerException
     *             If the argument is <code>null</code>.
     */
    void setPrimitive(Primitive prim);

    /**
     * A convenience method that allows a {@link SubEntity.Visitor} to visit all
     * the {@link SubEntity sub-entities} of <code>this</code> {@link Entity
     * entity}.
     *
     * @param visitor
     *            The {@link SubEntity.Visitor}.
     * @throws NullPointerException
     *             If the argument is <code>null</code>.
     */
    void visitSubEntities(SubEntity.Visitor visitor);

    /**
     * A convenience method that sets the same {@link RenderState render-state}
     * for <i>all</i> its {@link SubEntity sub-entities}.
     * <p>
     * Only use this method if you want <i>all</i> the {@link SubEntity
     * sub-entities} of <code>this</code> {@link Entity entity} to use the same
     * {@link RenderState render-state}.
     * <p>
     * If more granularity is needed, then use {@link #getSubEntity(int)} to
     * retrieve and update individual {@link SubEntity sub-entities}.
     *
     * @param rs
     *            The {@link RenderState render-state} to be set for each
     *            {@link SubEntity sub-entity}.
     * @throws NullPointerException
     *             If the argument is <code>null</code>.
     */
    void setRenderState(RenderState rs);

	void setDepthShaderProgram(GpuShaderProgram gpuShaderProgram);

}
