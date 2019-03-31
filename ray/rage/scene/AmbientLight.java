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

import java.awt.*;

import ray.rage.common.*;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneObject;

/**
 * An <i>ambient light</i> provides global, and evenly-distributed, illumination
 * for an entire scene.
 * <p>
 * An <i>ambient light</i> is not a {@link SceneObject scene-object}, has no
 * position, direction, or attenuation/falloff. Instead, it simulates the
 * indirect light that has already bounced around an entire scene and
 * illuminated everything in it evenly.
 *
 * @author Raymond L. Rivera
 *
 */
public interface AmbientLight extends Managed<SceneManager>, Disposable {

    /**
     * Sets the intensity of global illumination in a scene.
     *
     * @param intensity
     *            The intensity for global scene illumination.
     * @throws NullPointerException
     *             If the intensity is <code>null</code>.
     */
    void setIntensity(Color intensity);

    /**
     * Gets the intensity of global illumination in a scene.
     *
     * @return The intensity of global illumination.
     */
    Color getIntensity();

}
