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

package ray.rage.asset.shader;

import ray.rage.asset.*;
import ray.rage.asset.shader.Shader;
import ray.rage.asset.shader.ShaderManager;

/**
 * A <i>shader</i> contains the source code that gets submitted to specific
 * stages of the graphics pipeline for rendering.
 *
 * @author Raymond L. Rivera
 *
 * @see ShaderManager
 *
 */
public final class Shader extends AbstractAsset {

    private String source;

    Shader(ShaderManager sm, String name) {
        super(sm, name);
    }

    /**
     * Sets the source code to be contained by <code>this</code> {@link Shader
     * shader}.
     *
     * @param src
     *            The source code.
     * @throws IllegalArgumentException
     *             If the source code string is empty.
     */
    public void setSource(String src) throws IllegalArgumentException {
        if (src.isEmpty())
            throw new IllegalArgumentException("Source is empty");

        source = src;
    }

    /**
     * Returns the source code in <code>this</code> {@link Shader shader}.
     *
     * @return The source code, if it has been set. Otherwise <code>null</code>.
     */
    public String getSource() {
        return source;
    }

    @Override
    public void notifyDispose() {
        source = null;
        super.notifyDispose();
    }

}
