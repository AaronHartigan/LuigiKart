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

import com.jogamp.opengl.*;

import ray.rage.rendersystem.*;

/**
 * A concrete implementation of the {@link RenderSystemCaps
 * render-system-capabilities} interface for a {@link GL4RenderSystem}.
 *
 * @author Raymond L. Rivera
 *
 */
public final class GL4RenderSystemCaps implements RenderSystem.Capabilities {

    private final static int INVALID = -1;

    private final int        textureUnitCount;

    public GL4RenderSystemCaps(GL4 gl) {
        if (gl == null)
            throw new NullPointerException("Null GL4 instance");

        textureUnitCount = getTextureUnitCount(gl);
    }

    @Override
    public int getTextureUnitCount() {
        return textureUnitCount;
    }

    private static int getTextureUnitCount(GL4 gl) {
        int[] unitCount = new int[1];
        gl.glGetIntegerv(GL4.GL_MAX_TEXTURE_IMAGE_UNITS, unitCount, 0);

        if (unitCount[0] == INVALID)
            throw new RuntimeException("Could not get texture unit count");

        return unitCount[0];
    }

}
