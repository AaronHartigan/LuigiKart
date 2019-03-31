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

package ray.rage.scene.generic;

import java.awt.*;

import ray.rage.scene.*;

/**
 * A generic {@link AmbientLight} for global scene illumination.
 *
 * @author Raymond L. Rivera
 *
 */
final class GenericAmbientLight implements AmbientLight {

    private SceneManager manager;
    private Color        intensity = Color.BLACK;

    /**
     * Constructs a new instance with the given parent {@link SceneManger
     * manager}.
     *
     * @param sm
     *            The {@link SceneManger manager} that created
     *            <code>this</code>.
     * @throws NullPointerException
     *             If the {@link SceneManger manager} is <code>null</code>
     */
    GenericAmbientLight(SceneManager sm) {
        if (sm == null)
            throw new NullPointerException("Null " + SceneManager.class.getSimpleName());

        manager = sm;
    }

    @Override
    public SceneManager getManager() {
        return manager;
    }

    @Override
    public void setIntensity(Color colorIntensity) {
        if (colorIntensity == null)
            throw new NullPointerException("Null " + Color.class.getSimpleName());

        intensity = colorIntensity;
    }

    @Override
    public Color getIntensity() {
        return intensity;
    }

    @Override
    public void notifyDispose() {
        manager = null;
        intensity = null;
    }

}
