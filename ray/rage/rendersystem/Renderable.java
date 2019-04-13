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

import java.nio.*;

import ray.rage.asset.material.*;
import ray.rage.common.*;
import ray.rage.rendersystem.RenderSystem;
import ray.rage.rendersystem.Renderable;
import ray.rage.rendersystem.shader.*;
import ray.rage.rendersystem.states.*;
import ray.rml.*;

/**
 * A <i>renderable</i> is a <i>discrete</i> object that can be rendered by a
 * {@link RenderSystem render-system}. It's <i>not</i> meant for terrains and
 * other kinds of "static" geometry.
 * <p>
 * Any object meant to be processed and rendered by a {@link RenderSystem
 * render-system} <i>must</i> itself be <i>renderable</i>.
 *
 * @author Raymond L. Rivera
 *
 */
public interface Renderable extends Disposable {

    /**
     * The primitives to be used by the {@link RenderSystem render-system} to
     * render <code>this</code> {@link Renderable renderable}.
     *
     * @author Raymond L. Rivera
     *
     */
    enum Primitive {
        /**
         * Draw <code>this</code> using lines to connect vertex pairs.
         */
        LINES,

        /**
         * Draw <code>this</code> using unconnected points.
         */
        POINTS,

        /**
         * Draw <code>this</code> using triangles (default).
         */
        TRIANGLES,

        /**
         * Draw <code>this</code> using
         * <a href="https://en.wikipedia.org/wiki/Triangle_strip">triangle
         * strips</a>.
         */
        TRIANGLE_STRIP
    }

    /**
     * Specifies whether the {@link RenderSystem render-system} should use data
     * from a vertex buffer or an index buffer for drawing operations. The order
     * in which the data has been specified makes a difference on how a
     * {@link Renderable renderable} is rendered, and might even cause an object
     * to be drawn backwards/"inside out" or not at all (i.e. due to back-face
     * culling), depending on what ordering was used to determine its
     * "front-face", as specified by the {@link FrontFaceState
     * front-face-state}.
     * <p>
     * Note that changing the source implies changing the underlying data.
     * Generally, when using {@link #VERTEX_BUFFER vertex-buffers} only, the
     * number of vertices might need to be greater because some data may need to
     * be duplicated. Using {@link #INDEX_BUFFER index-buffers} can allows the
     * number of vertices in the vertex buffer to be smaller because indexing
     * into the vertex buffer allows vertices to be re-used if they're in the
     * required position, avoiding duplication of geometry.
     *
     * @author Raymond L. Rivera
     *
     */
    enum DataSource {
        /**
         * The {@link RenderSystem render-system} will rely on data directly
         * from a <i>vertex buffer</i> to draw the object. Vertex buffers
         * contain geometry and are <i>always</i> required for every
         * {@link Renderable renderable}.
         */
        VERTEX_BUFFER,

        /**
         * The {@link RenderSystem render-system} will rely on data directly
         * from an <i>index buffer</i> to draw the object. Index buffers contain
         * values that are used to <i>index into a vertex buffer</i>, much like
         * loop variables are used to index into arrays.
         */
        INDEX_BUFFER,
        
        /**
         * The {@link RenderSystem render-system} will rely on data generated by
         * <code>tessellation shaders</code>. Clients specify certain parameters
         * in {@Link Tessellation tessellations} such as quality, textures and
         * maps, height multipliers, etc. These parameters are passed as uniform
         * variables in {@Link GlslTessProgram}.
         */
        TESS_VERT_BUFFER
    }

    /**
     * Sets the {@link Primitive primitive} to use when drawing
     * <code>this</code> {@link Renderable renderable}.
     *
     * @param prim
     *            The {@link Primitive primitive}.
     * @throws NullPointerException
     *             If the {@link Primitive primitive} is null.
     */
    void setPrimitive(Primitive prim);

    /**
     * Gets the {@link Primitive primitive} used to draw <code>this</code>
     * {@link Renderable renderable}.
     *
     * @return The {@link Primitive primitive}.
     */
    Primitive getPrimitive();

    /**
     * Sets the {@link DataSource data-source} of <code>this</code>
     * {@link Renderable renderable}.
     *
     * @param ds
     *            The {@link DataSource data-source}.
     * @throws NullPointerException
     *             If the {@link DataSource data-source} is null.
     */
    void setDataSource(DataSource ds);

    /**
     * Gets the {@link DataSource data-source} <code>this</code>
     * {@link Renderable renderable}.
     *
     * @return The {@link DataSource data-source}.
     */
    DataSource getDataSource();

    /**
     * Gets <code>this</code> {@link Renderable renderable's} vertex
     * <i>positions</i> buffer.
     * <p>
     * Vertex positions define the {@link Renderable renderable's} actual
     * geometric shape relative to an arbitrary origin in local/object-space.
     *
     * @return The vertex positions buffer.
     */
    FloatBuffer getVertexBuffer();

    /**
     * Gets <code>this</code> {@link Renderable renderable's} vertex <i>texture
     * coordinates</i> buffer.
     * <p>
     * Texture coordinates specify which section of a texture should be sampled
     * to determine a fragment's color.
     *
     * @return The vertex texture coordinates buffer.
     */
    FloatBuffer getTextureCoordsBuffer();

    /**
     * Gets <code>this</code> {@link Renderable renderable's} vertex
     * <i>normals</i> buffer.
     * <p>
     * Vertex normals are perpendicular to a primitive's surface and are
     * necessary for lighting effects.
     *
     * @return The vertex normals buffer.
     */
    FloatBuffer getNormalsBuffer();

    /**
     * Gets <code>this</code> {@link Renderable renderable's} vertex
     * <i>index</i> buffer.
     * <p>
     * Vertex indices are used to "point to" specific vertex positions when
     * processing geometry that has been specified with index data.
     *
     * @return The vertex index buffer.
     */
    IntBuffer getIndexBuffer();

    /**
     * Gets <code>this</code> {@link Renderable renderable's} bone <i>weight</i>
     * buffer.
     * <p>
     * Bone weights are used to specify how much a vertex's position is affected
     * by a particular bone.
     *
     * @return The bone weight buffer.
     */
    FloatBuffer getBoneWeightBuffer();

    /**
     * Gets <code>this</code> {@link Renderable renderable's} bone
     * <i>index</i> buffer.
     * <p>
     * Bone indices are used to "point to" specific bones in a skeleton
     * for the vertices to be modified by that specific bone.
     * This also utilizes the boneWeightBuffer for specifying how much
     * the vertex's position is modified by said bone.
     *
     * @return The bone index buffer.
     */
    FloatBuffer getBoneIndexBuffer();

    /**
     * Sets the pose Skinning Matrices that should be used
     * for <code>this</code> {@link Renderable renderable}.
     *
     * @param psm
     *            Array of Matrix4 to use.
     * @throws NullPointerException
     *             If the array is <code>null</code>.
     */
    void setPoseSkinMatrices(Matrix4[] psm);

    /**
     * Gets the pose Skin Matrices Array specified for
     * <code>this</code> {@link Renderable renderable}, if any.
     *
     * @return The array of skinning matrices, if
     *         set. Otherwise <code>null</code>.
     */
    Matrix4[] getPoseSkinMatrices();

    /**
     * Sets the inverse-transpose pose Skinning Matrices that should be used
     * for <code>this</code> {@link Renderable renderable}.
     *
     * @param psmIT
     *            Array of Matrix3 to use.
     * @throws NullPointerException
     *             If the array is <code>null</code>.
     */
    void setPoseSkinMatricesIT(Matrix3[] psmIT);

    /**
     * Gets the pose inverse-transpose Skin Matrices Array specified for
     * <code>this</code> {@link Renderable renderable}, if any.
     *
     * @return The array of IT normal vector skinning matrices, if
     *         set. Otherwise <code>null</code>.
     */
    Matrix3[] getPoseSkinMatricesIT();

    /**
     * Sets the {@link Material material} for <code>this</code>
     * {@link Renderable renderable}.
     *
     * @param mat
     *            The {@link Material material}.
     * @throws NullPointerException
     *             If the {@link Material material} is null.
     */
    void setMaterial(Material mat);

    /**
     * Gets the {@link Material material} currently assigned to
     * <code>this</code> {@link Renderable renderable}. Otherwise
     * <code>null</code>.
     *
     * @return The {@link Material material}.
     */
    Material getMaterial();

    /**
     * Sets the {@link GpuShaderProgram shader-program} the {@link RenderSystem
     * render-system} must use to draw <code>this</code> {@link Renderable
     * renderable}.
     * <p>
     * Note that {@link Renderable renderables} <i>cannot be processed</i>
     * unless it has an associated {@link GpuShaderProgram shader-program}.
     *
     * @param prog
     *            The {@link GpuShaderProgram shader-program}.
     * @throws NullPointerException
     *             If the {@link GpuShaderProgram shader-program} is null.
     */
    void setGpuShaderProgram(GpuShaderProgram prog);

    /**
     * Gets the {@link GpuShaderProgram shader-program} currently assigned to
     * <code>this</code> {@link Renderable renderable}. Otherwise
     * <code>null</code>.
     *
     * @return The {@link GpuShaderProgram shader-program}.
     */
    GpuShaderProgram getGpuShaderProgram();
    
    void setDepthShaderProgram(GpuShaderProgram prog);

    /**
     * Gets the {@link GpuShaderProgram shader-program} currently assigned to
     * <code>this</code> {@link Renderable renderable}. Otherwise
     * <code>null</code>.
     *
     * @return The {@link GpuShaderProgram shader-program}.
     */
    GpuShaderProgram getDepthShaderProgram();

    /**
     * Sets a {@link RenderState render-state} that will be applied to the
     * {@link RenderSystem render-system} <i>before</i> attempting to render
     * <code>this</code> {@link Renderable renderable}.
     * <p>
     * A single {@link Renderable renderable} can have multiple
     * {@link RenderState render-states} simultaneously, <i>as long as they're
     * of different {@link RenderState.Type types}</i>. Setting a different
     * instance of the same {@link RenderState.Type type} causes the previous
     * instance to be {@link Disposable#notifyDispose() disposed} and
     * overwritten/replaced.
     * <p>
     * Note that {@link Renderable renderables} may not be rendered correctly
     * unless an appropriate {@link RenderState render-state} has been added.
     *
     * @param state
     *            The {@link RenderState render-state} to be added or replaced.
     * @throws NullPointerException
     *             If the {@link RenderState render-state} is null.
     */
    void setRenderState(RenderState state);

    /**
     * Gets a {@link RenderState render-state} of the specified
     * {@link RenderState.Type type}, if one has already been set. Otherwise
     * <code>null</code>.
     * <p>
     * It's the client's responsibility to cast the returned instance into the
     * proper type, if necessary.
     *
     * @param type
     *            The {@link RenderState.Type type}.
     * @return The {@link RenderState render-state}, if one is found. Otherwise
     *         <code>null</code>.
     * @throws NullPointerException
     *             If the {@link RenderState.Type type} is null.
     */
    RenderState getRenderState(RenderState.Type type);

    /**
     * Allows clients to iterate over all the {@link RenderState render-states}
     * that have been added to <code>this</code>.
     *
     * @return An {@link Iterable iterable} of {@link RenderState
     *         render-states}.
     */
    Iterable<RenderState> getRenderStates();

    /**
     * Removes and disposes of the {@link RenderState render-state} of the
     * specified {@link RenderState.Type type}.
     *
     * @param type
     *            The {@link RenderState.Type type} to be removed.
     * @throws NullPointerException
     *             If the {@link RenderState.Type type} is null.
     */
    void clearRenderState(RenderState.Type type);

    /**
     * Gets the world-space transform {@link Matrix4 matrix} of
     * <code>this</code> {@link Renderable renderable}.
     *
     * @return The world-space transform {@link Matrix4 matrix}.
     */
    Matrix4 getWorldTransformMatrix();

}