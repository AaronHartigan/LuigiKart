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

package ray.rage.asset.material;

import java.awt.*;

import ray.rage.asset.*;
import ray.rage.asset.material.Material;
import ray.rage.asset.material.MaterialManager;
import ray.rage.asset.mesh.*;
import ray.rage.rendersystem.*;
import ray.rage.scene.*;

/**
 * A <i>material</i> describes some aspects of the visual appearance of a
 * {@link Renderable renderable} object, namely how its surface reflects light.
 * <p>
 * Materials and {@link SubEntity sub-entities} have a 1:1 relationship, which
 * is generally preserved by the {@link MaterialManager material-manager}, but
 * only as long as {@link Mesh meshes} specify <i>separate</i> material source
 * files. Otherwise, if different {@link Mesh meshes} specify and share the same
 * material, then it's the client's responsibility to handle this situation by
 * creating materials <i>manually</i>.
 *
 * @author Raymond L. Rivera
 *
 * @see MaterialManager
 *
 */
public final class Material extends AbstractAsset {

    private final static String TAG           = Material.class.getName();

    private final static int    SHININESS_MIN = 1;
    private final static int    SHININESS_MAX = 128;

    private Color               ambient       = Color.GRAY;
    private Color               diffuse       = Color.GRAY;
    private Color               specular      = Color.GRAY;
    private Color               emissive      = Color.BLACK;
    private float               shininess     = SHININESS_MIN;

    private String              textureFilename;

    Material(MaterialManager mm, String name) {
        super(mm, name);
    }

    public void setAmbient(Color newAmbient) {
        if (newAmbient == null)
            throw new NullPointerException(TAG + ": Null ambient color");

        ambient = newAmbient;
    }

    public Color getAmbient() {
        return ambient;
    }

    public void setDiffuse(Color newDiffuse) {
        if (newDiffuse == null)
            throw new NullPointerException(TAG + ": Null diffuse color");

        diffuse = newDiffuse;
    }

    public Color getDiffuse() {
        return diffuse;
    }

    public void setSpecular(Color newSpecular) {
        if (newSpecular == null)
            throw new NullPointerException(TAG + ": Null specular color");

        specular = newSpecular;
    }

    public Color getSpecular() {
        return specular;
    }

    public void setEmissive(Color newEmissive) {
        if (newEmissive == null)
            throw new NullPointerException(TAG + ": Null emissive color");

        emissive = newEmissive;
    }

    public Color getEmissive() {
        return emissive;
    }

    public void setShininess(float newShininess) {
        if (newShininess < SHININESS_MIN || newShininess > SHININESS_MAX)
            throw new IllegalArgumentException(
                    TAG + ": shininess out of [" + SHININESS_MIN + ", " + SHININESS_MAX + "] range");

        shininess = newShininess;
    }

    public float getShininess() {
        return shininess;
    }

    public void setTextureFilename(String name) {
        if (name == null)
            throw new NullPointerException("Null filename");
        if (name.isEmpty())
            throw new IllegalArgumentException("Empty filename");

        textureFilename = name;
    }

    public String getTextureFilename() {
        return textureFilename;
    }

    @Override
    public void notifyDispose() {
        ambient = null;
        diffuse = null;
        specular = null;
        emissive = null;

        textureFilename = null;
        super.notifyDispose();
    }

}
