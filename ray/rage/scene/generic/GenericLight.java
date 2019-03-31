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
import ray.rage.scene.generic.AbstractGenericSceneObject;
import ray.rml.*;

/**
 * A generic {@link Light light}.
 *
 * @author Raymond L. Rivera
 *
 */
final class GenericLight extends AbstractGenericSceneObject implements Light {

    // 180 degrees is a special case
    private final static float POINT_LIGHT_CUTOFF   = 180f;

    // anything in [0, 90) degree range; 45 is default
    private final static float SPOT_LIGHT_CUTOFF    = 45f;

    private Color              ambient              = Color.BLACK;
    private Color              diffuse              = Color.GRAY;
    private Color              specular             = Color.WHITE;
    private Type               type                 = Light.Type.POINT;

    // WARN: While attenuation values can be set to zero, they must not all
    // be set to zero at the same time.
    private float              constAttenuation     = 1;
    private float              linearAttenuation    = .2f;
    private float              quadraticAttenuation = .02f;
    private float              range                = 100;

    // valid range is [0, 90) to restrict cone, or 180 to act as point light,
    // which is a special case of the spot light but with a cone wide-enough to
    // illuminate in all directions;
    private Angle              coneCutoffAngle      = Degreef.createFrom(POINT_LIGHT_CUTOFF);

    // The larger the value, the faster light 'decays' as it gets farther away
    // from the cone's center; 0 = constant illumination
    private float              falloffExponent      = 2f;

    /**
     * Creates a new {@link Light light} with the specified parent
     * {@link SceneManager manager}, name, and {@link Light.Type type}.
     *
     * @param manager
     *            The parent {@link SceneManager manager}.
     * @param name
     *            The name that identifies the {@link Light light}.
     * @param type
     *            The {@link Light.Type type}.
     * @throws NullPointerException
     *             If any of the arguments is <code>null</code>.
     * @throws IllegalArgumentException
     *             If the name is empty.
     */
    GenericLight(SceneManager manager, String name, Type type) {
        super(manager, name);
        setType(type);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void setType(Type type) {
        if (type == null)
            throw new NullPointerException("Null " + Light.Type.class.getSimpleName());

        switch (type) {
            case DIRECTIONAL:
                // pass; cutoff angle doesn't matter for directional lights
                break;
            case POINT:
                setConeCutoffAngle(Degreef.createFrom(POINT_LIGHT_CUTOFF));
                break;
            case SPOT:
                setConeCutoffAngle(Degreef.createFrom(SPOT_LIGHT_CUTOFF));
                break;
        }
        this.type = type;
    }

    @Override
    public void setAmbient(Color ambientColor) {
        if (ambientColor == null)
            throw new NullPointerException("Ambient color is null");

        ambient = ambientColor;
    }

    @Override
    public Color getAmbient() {
        return ambient;
    }

    @Override
    public void setDiffuse(Color diffuseColor) {
        if (diffuseColor == null)
            throw new NullPointerException("Diffuse color is null");

        diffuse = diffuseColor;
    }

    @Override
    public Color getDiffuse() {
        return diffuse;
    }

    @Override
    public void setSpecular(Color specularColor) {
        if (specularColor == null)
            throw new NullPointerException("Specular color is null");

        specular = specularColor;
    }

    @Override
    public Color getSpecular() {
        return specular;
    }

    @Override
    public void setRange(float value) {
        if (value <= 0)
            throw new IllegalArgumentException("Range <= 0");

        range = value;
    }

    @Override
    public float getRange() {
        return range;
    }

    @Override
    public void setConstantAttenuation(float value) {
        if (value < 0)
            throw new IllegalArgumentException("Constant attenuation < 0");

        constAttenuation = value;
    }

    @Override
    public float getConstantAttenuation() {
        return constAttenuation;
    }

    @Override
    public void setLinearAttenuation(float value) {
        if (value < 0)
            throw new IllegalArgumentException("Linear attenuation < 0");

        linearAttenuation = value;
    }

    @Override
    public float getLinearAttenuation() {
        return linearAttenuation;
    }

    @Override
    public void setQuadraticAttenuation(float value) {
        if (value < 0)
            throw new IllegalArgumentException("Quadratic attenuation < 0");

        quadraticAttenuation = value;
    }

    @Override
    public float getQuadraticAttenuation() {
        return quadraticAttenuation;
    }

    @Override
    public void setConeCutoffAngle(Angle angle) {
        if (angle == null)
            throw new NullPointerException("Null cutoff " + Angle.class.getSimpleName());

        // valid range is [0, 90) or 180
        final float value = angle.valueDegrees();
        if (value < 0 || value >= 90.0f && value != 180.0f)
            throw new IllegalArgumentException(
                    "Cutoff " + Angle.class.getSimpleName() + " outside [0, 90), 180 range: " + value);

        coneCutoffAngle = angle;
    }

    @Override
    public Angle getConeCutoffAngle() {
        return coneCutoffAngle;
    }

    @Override
    public void setFalloffExponent(float falloff) {
        if (falloff < 0)
            throw new IllegalArgumentException("Falloff < 0");

        falloffExponent = falloff;
    }

    @Override
    public float getFalloffExponent() {
        return falloffExponent;
    }

}
