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

import ray.rage.scene.Light;
import ray.rage.scene.SceneObject;
import ray.rml.*;

/**
 * A <i>light</i> is a {@link SceneObject scene-object} that can illuminate
 * other {@link SceneObject scene-objects} in a scene.
 * <p>
 * There're several {@link Type types} of light and the relevance of different
 * attributes will depend on the specific {@link Type type}.
 *
 * @author Raymond L. Rivera
 *
 */
public interface Light extends SceneObject {

    enum Type {
        /**
         * A {@link Light light} with a position, but no direction. It's emitted
         * in <i>all</i> directions from a specific location.
         * <p>
         * This {@link Type type} <i>is</i> affected by attenuation and range.
         */
        POINT,
        /**
         * A {@link Light light} with a direction, but no position. It's emitted
         * in a specific direction, but from <i>infinitely</i> far away.
         * <p>
         * This {@link Type type} is <i>not</i> affected by attenuation or
         * range.
         */
        DIRECTIONAL,
        /**
         * A {@link Light light} with both position and direction, in the shape
         * of a cone (e.g. a flashlight).
         * <p>
         * This {@link Type type} <i>is</i> affected by attenuation and range.
         */
        SPOT
    }

    /**
     * Sets the {@link Type type} of <code>this</code> {@link Light light}.
     * <p>
     * This call has the following side-effects on the {@link Light light's}
     * cone cutoff {@link Angle angle}, based on the chosen {@link Type type}:
     * <ul>
     * <li>{@link Type#DIRECTIONAL}: No side-effects. Cutoff {@link Angle angle}
     * not used.</li>
     * <li>{@link Type#POINT}: Sets {@link #setConeCutoffAngle(Angle)} to
     * <code>180</code> {@link Degreef degrees}, which is a special case.</li>
     * <li>{@link Type#SPOT}: Sets {@link #setConeCutoffAngle(Angle)} to a
     * default value of <code>45</code> {@link Degreef degrees}, which is in the
     * <code>[0, 90)</code> {@link Degreef degree} range.</li>
     * </ul>
     *
     * @param type
     *            The {@link Type type}.
     * @throws NullPointerException
     *             If the {@link Type type} is <code>null</code>.
     * @see #setConeCutoffAngle(Angle)
     */
    void setType(Type type);

    /**
     * Gets the {@link Type type} of <code>this</code> {@link Light light}.
     *
     * @return The {@link Type type}.
     */
    Type getType();

    /**
     * Sets the <i>ambient</i> component of <code>this</code> {@link Light
     * light}.
     *
     * @param ambientColor
     *            The ambient intensity.
     * @throws NullPointerException
     *             If the {@link Color color} is <code>null</code>.
     */
    void setAmbient(Color ambientColor);

    /**
     * Gets the <i>ambient</i> component of <code>this</code> {@link Light
     * light}.
     *
     * @return The ambient intensity.
     */
    Color getAmbient();

    /**
     * Sets the <i>diffuse</i> component of <code>this</code> {@link Light
     * light}.
     *
     * @param diffuseColor
     *            The diffuse intensity.
     * @throws NullPointerException
     *             If the {@link Color color} is <code>null</code>.
     */
    void setDiffuse(Color diffuseColor);

    /**
     * Gets the <i>diffuse</i> component of <code>this</code> {@link Light
     * light}.
     *
     * @return The ambient diffuse.
     */
    Color getDiffuse();

    /**
     * Sets the <i>specular</i> component of <code>this</code> {@link Light
     * light}.
     *
     * @param specularColor
     *            The specular intensity.
     * @throws NullPointerException
     *             If the {@link Color color} is <code>null</code>.
     */
    void setSpecular(Color specularColor);

    /**
     * Gets the <i>specular</i> component of <code>this</code> {@link Light
     * light}.
     *
     * @return The specular intensity.
     */
    Color getSpecular();

    /**
     * Sets how "far" rays emitted by <code>this</code> source of {@link Light
     * light} can reach.
     * <p>
     * The range is the maximum distance (i.e. number of world-units) a ray can
     * "travel" before it can no longer illuminate other {@link SceneObject
     * scene-objects}. {@link SceneObject}s beyond the range of
     * <code>this</code> {@link Light light} do not get lit.
     * <p>
     * <i>This value only affects {@link Light.Type#POINT point} and
     * {@link Light.Type#SPOT spot} {@link Light lights}</i>.
     *
     * @param range
     *            The range.
     * @throws IllegalArgumentException
     *             If <code>range <= 0</code>
     */
    void setRange(float range);

    /**
     * Gets the range of <code>this</code> {@link Light light}.
     * <p>
     * <i>This value only affects {@link Light.Type#POINT point} and
     * {@link Light.Type#SPOT spot} {@link Light lights}</i>.
     *
     * @return The range.
     */
    float getRange();

    /**
     * Sets the value of the <i>constant</i> coefficient in the attenuation
     * formula.
     * <p>
     * The larger this value is the faster light gets dimmer regardless of
     * distance to the {@link SceneObject object} being illuminated. This value
     * is not scaled by the distance; it's used "as is".
     * <p>
     * Note that, while this coefficient can be independently set to zero, it
     * <b><i>must not</i></b> be set to zero <i>at the same time</i> as the
     * other coefficients.
     * <p>
     * <i>This value only affects {@link Light.Type#POINT point} and
     * {@link Light.Type#SPOT spot} {@link Light lights}</i>.
     *
     * @param value
     *            The constant coefficient.
     * @throws IllegalArgumentException
     *             If the <code>value < 0</code>.
     */
    void setConstantAttenuation(float value);

    /**
     * Gets the constant coefficient.
     *
     * @return The constant coefficient.
     * @see #setConstantAttenuation(float)
     */
    float getConstantAttenuation();

    /**
     * Sets the value of the <i>linear</i> coefficient in the attenuation
     * formula.
     * <p>
     * The larger this value is the faster {@link Light light} gets dimmer. This
     * value is proportional (i.e. <i>linear</i>) to the distance between this
     * {@link Light light} and {@link SceneObject object} being illuminated.
     * <p>
     * Note that, while this coefficient can be independently set to zero, it
     * <b><i>must not</i></b> be set to zero <i>at the same time</i> as the
     * other coefficients.
     * <p>
     * <i>This value only affects {@link Light.Type#POINT point} and
     * {@link Light.Type#SPOT spot} {@link Light lights}</i>.
     *
     * @param value
     *            The linear coefficient.
     * @throws IllegalArgumentException
     *             If the <code>value < 0</code>.
     */
    void setLinearAttenuation(float value);

    /**
     * Gets the linear coefficient.
     *
     * @return The linear coefficient.
     * @see #setLinearAttenuation(float)
     */
    float getLinearAttenuation();

    /**
     * Sets the value of the <i>quadratic</i> coefficient in the attenuation
     * formula.
     * <p>
     * The larger this value is the faster {@link Light light} gets dimmer. This
     * value is scaled by the <i>squared</i> distance to the {@link SceneObject
     * object} being illuminated.
     * <p>
     * Note that, while this coefficient can be independently set to zero, it
     * <b><i>must not</i></b> be set to zero <i>at the same time</i> as the
     * other coefficients.
     * <p>
     * <i>This value only affects {@link Light.Type#POINT point} and
     * {@link Light.Type#SPOT spot} {@link Light lights}</i>.
     *
     * @param value
     *            The quadratic coefficient.
     * @throws IllegalArgumentException
     *             If the <code>value < 0</code>.
     */
    void setQuadraticAttenuation(float value);

    /**
     * Gets the quadratic coefficient.
     *
     * @return The quadratic coefficient.
     * @see #setQuadraticAttenuation(float)
     */
    float getQuadraticAttenuation();

    /**
     * Sets the {@link Angle angle} of <code>this</code> {@link Light light's}
     * cone of illumination, as measured relative to a ray cast from the cone's
     * center in the direction it's pointing.
     * <p>
     * The cutoff {@link Angle angle} must be within the
     * <code>[0, 90), 180</code> {@link Degreef degree} range, where
     * <code>180</code> is a special case for {@link Light.Type#POINT point}
     * {@link Light lights}
     * <p>
     * All {@link SceneObject scene-objects} outside of <code>this</code>
     * {@link Light light's} cone of illumination remain unlit by
     * <code>this</code> source.
     * <p>
     * <i>This value only affects {@link Light.Type#SPOT spot} {@link Light
     * lights}</i>.
     *
     * @param angle
     *            The {@link Angle angle}.
     * @throws NullPointerException
     *             If the {@link Angle angle} is <code>null</code>.
     * @throws IllegalArgumentException
     *             If the cutoff {@link Angle angle} is outside the
     *             <code>[0, 90), 180</code> degree ranges.
     */
    void setConeCutoffAngle(Angle angle);

    /**
     * Gets the {@link Angle angle} of <code>this</code> {@link Light light's}
     * cone of illumination.
     *
     * @return The {@link Angle angle}.
     * @see #setConeCutoffAngle(Angle)
     */
    Angle getConeCutoffAngle();

    /**
     * Sets the value of the <i>falloff</i> coefficient for <code>this</code>
     * {@link Light light}.
     * <p>
     * This value is used to calculate how {@link SceneObject scene-objects} are
     * illuminated as their {@link Angle angle} relative to the cone's center
     * increases, i.e. as they get further away from it and get closer to the
     * "edge" of the cone. The larger this value, the faster <code>this</code>
     * {@link Light light's} rays will attenuate as it gets closer to its
     * cuttoff {@link Angle angle}.
     * <p>
     * <i>This value only affects {@link Light.Type#SPOT spot} {@link Light
     * lights}</i>.
     *
     * @param falloff
     *            The falloff coefficient.
     * @throws IllegalArgumentException
     *             If the <code>value < 0</code>.
     */
    void setFalloffExponent(float falloff);

    /**
     * Gets the falloff coefficient for <code>this</code> {@link Light light}.
     *
     * @return The falloff coefficient.
     * @see #setFalloffExponent(float)
     */
    float getFalloffExponent();

}
