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
import ray.rage.scene.Entity;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SubEntity;

/**
 * A <i>sub entity</i> is a {@link Renderable renderable} part of an
 * {@link Entity entity}, much like the independent pieces of a jigsaw puzzle
 * forms a whole picture.
 * <p>
 * A Sub-Entity is based on a single {@link SubMesh sub-mesh} and a single
 * {@link Material material}, i.e. there's a 1:1 relationship between them. This
 * interface specifies the {@link Material material} to be applied separately
 * from the {@link SubMesh sub-mesh} to keep its geometric shape decoupled from
 * its cosmetic appearance.
 * <p>
 * Unlike their parent {@link Entity entities}, sub-entities are
 * {@link Renderable renderable}. Only {@link Renderable renderable} parts get
 * submitted by the {@link SceneManager scene-manager} to the
 * {@link RenderSystem render-system} for processing.
 * <p>
 * Sub-Entities are never created directly. Instead, they're created
 * automatically when the {@link SceneManager#createEntity(String, String)
 * method is used to create an {@link Entity entity}.
 *
 * @see Entity
 * @see SubMesh
 *
 * @author Raymond L. Rivera
 *
 */
public interface SubEntity extends Renderable {

    /**
     * Visitor interface for {@link SubEntity sub-entities}.
     *
     * @author Raymond L. Rivera
     *
     */
    public interface Visitor {

        /**
         * Visitor method to visit {@link SubEntity sub-entities}.
         *
         * @param se
         *            The {@link SubEntity sub-entity} to visit.
         */
        void visit(SubEntity se);

    }

    /**
     * Gets the parent {@link Entity entity} of <code>this</code>
     * {@link SubEntity sub-entity}
     *
     * @return The parent {@link Entity entity}.
     */
    Entity getParent();

    /**
     * Gets the {@link SubMesh sub-mesh} <code>this</code> {@link SubEntity
     * sub-entity} is based on.
     *
     * @return The {@link SubMesh sub-mesh}.
     */
    public SubMesh getSubMesh();

}
