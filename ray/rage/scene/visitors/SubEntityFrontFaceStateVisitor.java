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

package ray.rage.scene.visitors;

import ray.rage.rendersystem.*;
import ray.rage.rendersystem.states.*;
import ray.rage.scene.*;
import ray.rage.scene.visitors.SubEntityFrontFaceStateVisitor;

/**
 * A {@link SubEntity.Visitor visitor} that prepares and applies
 * {@link ZBufferState depth-states} to {@link SubEntity sub-entities}.
 *
 * @author Raymond L. Rivera
 *
 */
public final class SubEntityFrontFaceStateVisitor implements SubEntity.Visitor {

    private final SceneManager manager;

    /**
     * Creates a new {@link SubEntityFrontFaceStateVisitor}.
     *
     * @param sm
     *            The {@link SceneManager scene-manager} that created
     *            <code>this</code> instance.
     * @throws NullPointerException
     *             If the argument is <code>null</code>.
     */
    public SubEntityFrontFaceStateVisitor(SceneManager sm) {
        if (sm == null)
            throw new NullPointerException("Null " + SceneManager.class.getSimpleName());

        manager = sm;
    }

    @Override
    public void visit(SubEntity subEntity) {
        RenderSystem system = manager.getRenderSystem();
        RenderState state = system.createRenderState(RenderState.Type.FRONT_FACE);
        subEntity.setRenderState(state);
    }

}
