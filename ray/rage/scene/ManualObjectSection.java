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

import java.nio.*;

import ray.rage.rendersystem.*;
import ray.rage.scene.ManualObject;
import ray.rage.scene.ManualObjectSection;
import ray.rage.scene.SceneManager;

/**
 * A <i>manual object section</i> is a {@link Renderable renderable} part of a
 * {@link ManualObject manual-object}.
 * <p>
 * Only a {@link Renderable renderable} section like <code>this</code> one gets
 * submitted by the {@link SceneManager scene-manager} to the
 * {@link RenderSystem render-system} for processing.
 * <p>
 * There's a <code>1:M</code> relationship between {@link ManualObject
 * manual-object} and sections. A section must be owned by only one
 * {@link ManualObject manual-object}. It's the {@link ManualObject
 * manual-object's} responsibility to {@link #notifyDispose() dispose} of its
 * sections.
 *
 * @author Raymond L. Rivera
 *
 * @see ManualObject
 *
 */
public interface ManualObjectSection extends Renderable {

    /**
     * Gets the {@link ManualObject manual-object} that created and owns
     * <code>this</code> {@link ManualObjectSection section}.
     *
     * @return The {@link ManualObject manual-object}.
     */
    ManualObject getParent();

    /**
     * Sets the vertex <i>positions</i> buffer for <code>this</code>
     * {@link ManualObjectSection section}.
     *
     * @param vertices
     *            The vertex <i>positions</i> buffer.
     * @throws IllegalArgumentException
     *             If the buffer is not direct.
     * @throws NullPointerException
     *             If the buffer is <code>null</code>.
     */
    void setVertexBuffer(FloatBuffer vertices);

    /**
     * Sets the vertex <i>texture coordinates</i> buffer for <code>this</code>
     * {@link ManualObjectSection section}.
     *
     * @param texcoords
     *            The vertex <i>texture coordinates</i> buffer.
     * @throws IllegalArgumentException
     *             If the buffer is not direct.
     * @throws NullPointerException
     *             If the buffer is <code>null</code>.
     */
    void setTextureCoordsBuffer(FloatBuffer texcoords);

    /**
     * Sets the vertex <i>normals</i> buffer for <code>this</code>
     * {@link ManualObjectSection section}.
     *
     * @param normals
     *            The vertex <i>normals</i> buffer.
     * @throws IllegalArgumentException
     *             If the buffer is not direct.
     * @throws NullPointerException
     *             If the buffer is <code>null</code>.
     */
    void setNormalsBuffer(FloatBuffer normals);

    /**
     * Sets the vertex <i>index</i> buffer for <code>this</code>
     * {@link ManualObjectSection section}.
     *
     * @param indices
     *            The vertex <i>index</i> buffer.
     * @throws IllegalArgumentException
     *             If the buffer is not direct.
     * @throws NullPointerException
     *             If the buffer is <code>null</code>.
     */
    void setIndexBuffer(IntBuffer indices);

}
