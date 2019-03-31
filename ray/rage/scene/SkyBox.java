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

import ray.rage.asset.texture.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.shader.*;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rage.scene.SceneObject;
import ray.rage.scene.SkyBox;

/**
 * A <i>sky box</i> is a {@link Texture textured} cube that simulates the
 * presence of a sky, ground, and horizon in a scene.
 * <p>
 * Sky boxes are meant to be unreachable by players, so it's the client's
 * responsibility to make sure this is the case. This can be done in many
 * different ways, such as making sure the sky box and {@link Camera camera}
 * {@link SceneNode scene-nodes} are always at the same location in world-space.
 *
 * @author Raymond L. Rivera
 *
 */
public interface SkyBox extends SceneObject {

    // FIXME: entries here must match order in GL4TextureState for
    // GL_TEXTURE_CUBE_MAP targets (e.g. GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
    // GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, etc)
    /**
     * The <i>faces</i> of a {@link SkyBox sky-box} are the "sides" onto which
     * {@link Texture textures} get mapped. There's one for each direction to
     * ensure the entire scene is covered.
     *
     * @author Raymond L. Rivera
     *
     */
    enum Face {
        /**
         * The +Y side.
         */
        TOP(0),

        /**
         * The -Y side.
         */
        BOTTOM(1),

        /**
         * The -X side.
         */
        LEFT(2),

        /**
         * The +X side.
         */
        RIGHT(3),

        /**
         * The -Z side.
         */
        FRONT(4),

        /**
         * The +Z side.
         */
        BACK(5);

        // See Item #31, Effective Java 2nd Edition
        private final int value;

        private Face(int v) {
            value = v;
        }

        public int value() {
            return value;
        }
    }

    /**
     * Sets the specified {@link Texture texture} onto the specified {@link Face
     * face}.
     *
     * @param tex
     *            The {@link Texture texture}.
     * @param face
     *            The {@link Face face}.
     * @throws NullPointerException
     *             If any of the arguments is <code>null</code>.
     */
    void setTexture(Texture tex, Face face);

    /**
     * Gets the {@link Texture texture} currently on the specified {@link Face
     * face}, if any.
     *
     * @param face
     *            The {@link Face face} to get the {@link Texture texture} from.
     * @return The {@link Texture texture}, if any. Otherwise <code>null</code>.
     * @throws NullPointerException
     *             If the {@link Face face} is <code>null</code>.
     */
    Texture getTexture(Face face);

    /**
     * Gets an {@link Iterable} for the {@link Renderable renderables} that
     * <code>this</code> {@link SkyBox sky-box} is made of.
     *
     * @return An {@link Iterable} of {@link Renderable renderables}.
     */
    Iterable<Renderable> getFaces();

    /**
     * Sets the {@link GpuShaderProgram gpu-shader-program} responsible for
     * rendering the {@link SkyBox sky-box}.
     *
     * @param program
     *            The {@link GpuShaderProgram program}.
     * @throws NullPointerException
     *             If the {@link GpuShaderProgram program} is <code>null</code>.
     */
    void setGpuShaderProgram(GpuShaderProgram program);

    void setDepthShaderProgram(GpuShaderProgram program);
}
