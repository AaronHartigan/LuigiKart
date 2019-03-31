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

package ray.rage.rendersystem.gl4;

import ray.rage.rendersystem.*;

/**
 * A concrete implementation of the {@link RenderSystemFactory
 * render-system-factory} interface for a {@link GL4RenderSystem}.
 *
 * @author Raymond L. Rivera
 *
 */
public final class GL4RenderSystemFactory implements RenderSystemFactory {

    @Override
    public RenderSystem createInstance() {
        return new GL4RenderSystem();
    }

}
