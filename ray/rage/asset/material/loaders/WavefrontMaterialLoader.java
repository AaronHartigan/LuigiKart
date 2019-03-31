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

package ray.rage.asset.material.loaders;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;

import ray.rage.asset.material.*;
import ray.rage.asset.material.loaders.WavefrontMaterialLoader;

/**
 * A {@link MaterialLoader material-loader} capable of processing the <a href=
 * "https://en.wikipedia.org/wiki/Wavefront_.obj_file#Material_template_library">Material
 * Template Library</a> format specified by
 * <a href= "https://en.wikipedia.org/wiki/Wavefront_.obj_file">Wavefront</a>
 * files.
 * <p>
 * The common file extension is <code>.mtl</code>.
 *
 * @author Raymond L. Rivera
 *
 */
public final class WavefrontMaterialLoader implements MaterialLoader {

    private static final String TAG                  = WavefrontMaterialLoader.class.getName();
    private static final Logger logger               = Logger.getLogger(TAG);

    private static final String SPACE_REGEX          = "\\s+";

    private static final String NEW_MATERIAL         = "newmtl";
    private static final String USE_MATERIAL         = "usemtl";

    private static final String AMBIENT_COMMAND      = "Ka";
    private static final String DIFFUSE_COMMAND      = "Kd";
    private static final String SPECULAR_COMMAND     = "Ks";
    private static final String EMISSIVE_COMMAND     = "Ke";
    private static final String REFRACTION_COMMAND   = "Ni";
    private static final String SHININESS_COMMAND    = "Ns";
    private static final String TRANSPARENCY_COMMAND = "Tr";
    private static final String DISSOLVED_COMMAND    = "d ";
    private static final String TEXTURE_COMMAND      = "map_Kd";

    // FIXME: currently supports only 1 material per file
    private static final int    MAX_MATERIALS_COUNT  = 1;
    private static final int    TUPLE_FIELD_COUNT    = 3;

    private float[]             ambient;
    private float[]             diffuse;
    private float[]             specular;
    private float[]             emissive;
    private float               shininess;

    private float               refractionIndex;
    private float               transparency;

    private String              textureFilename;

    @Override
    public void loadAsset(Material mat, Path path) throws IOException, NullPointerException {
        if (mat == null)
            throw new NullPointerException("Null material");

        BufferedReader br = Files.newBufferedReader(path);

        int matCount = 0;
        String line;
        try {
            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.startsWith(NEW_MATERIAL) && ++matCount > MAX_MATERIALS_COUNT) {
                    logger.log(Level.FINE, "New material found: " + line);
                    break;
                }

                // skip to the next line when the current one is empty, a
                // comment, or has spaces
                if (line.startsWith("#") || line.startsWith(" ") || line.length() == 0)
                    continue;

                if (line.startsWith(AMBIENT_COMMAND)) {
                    ambient = parseAmbient(line);
                } else if (line.startsWith(DIFFUSE_COMMAND)) {
                    diffuse = parseDiffuse(line);
                } else if (line.startsWith(SPECULAR_COMMAND)) {
                    specular = parseSpecular(line);
                } else if (line.startsWith(EMISSIVE_COMMAND)) {
                    emissive = parseEmissive(line);
                } else if (line.startsWith(SHININESS_COMMAND)) {
                    shininess = parseShininess(line);
                } else if (line.startsWith(REFRACTION_COMMAND)) {
                    refractionIndex = parseRefractionIndex(line);
                } else if (line.startsWith(TRANSPARENCY_COMMAND)) {
                    transparency = parseTransparency(line, true);
                } else if (line.startsWith(DISSOLVED_COMMAND)) {
                    transparency = parseTransparency(line, false);
                } else if (line.startsWith(TEXTURE_COMMAND)) {
                    textureFilename = line.split(SPACE_REGEX)[1];
                } else {
                    logger.log(Level.FINE, "Ignored command: " + line);
                }
            }
        } catch (IOException | NumberFormatException | IndexOutOfBoundsException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        } finally {
            if (br != null)
                br.close();
        }

        mat.setAmbient(new Color(ambient[0], ambient[1], ambient[2]));
        mat.setDiffuse(new Color(diffuse[0], diffuse[1], diffuse[2]));
        mat.setSpecular(new Color(specular[0], specular[1], specular[2]));
        mat.setEmissive(new Color(emissive[0], emissive[1], emissive[2]));
        mat.setShininess(shininess);
        mat.setTextureFilename(textureFilename);
    }

    private float[] parseAmbient(String line) {
        return parseTuple(line, "ambient");
    }

    private float[] parseDiffuse(String line) {
        return parseTuple(line, "diffuse");
    }

    private float[] parseSpecular(String line) {
        return parseTuple(line, "specular");
    }

    private float[] parseEmissive(String line) {
        return parseTuple(line, "emissive");
    }

    private float parseShininess(String line) {
        return parseScalar(line, "shininess");
    }

    private float parseRefractionIndex(String line) {
        return 0;
    }

    private float parseTransparency(String line, boolean inverted) {
        float value = parseScalar(line, "transparency");

        // depending on which command was used we may need to invert the value;
        // for example, "d 1.0" == "Tr 0.0" == fully opaque, i.e. inverted "Tr"
        // is the opposite of "d" and is calculated as 1 - d
        return inverted ? 1f - value : value;
    }

    private static float parseScalar(String line, String fieldName) {
        String[] text = line.split(SPACE_REGEX);
        if (text.length != 2)
            throw new RuntimeException(TAG + ": Error parsing " + fieldName + " coefficient: " + line);

        return Float.valueOf(text[1]);
    }

    private static float[] parseTuple(String line, String fieldName) {
        // skip the command part and focus on the values
        String[] fields = line.split(SPACE_REGEX);
        fields = Arrays.copyOfRange(fields, 1, fields.length);

        if (fields.length != TUPLE_FIELD_COUNT)
            throw new RuntimeException(TAG + ": Error parsing " + fieldName + " coefficients: " + line);

        float[] values = new float[fields.length];
        for (int i = 0; i < fields.length; i++)
            values[i] = Float.valueOf(fields[i]);

        return values;
    }

    @Override
    public void notifyDispose() {
        ambient = null;
        diffuse = null;
        specular = null;
        emissive = null;
        textureFilename = null;

        shininess = -1;
        refractionIndex = -1;
        transparency = -1;
    }

}
