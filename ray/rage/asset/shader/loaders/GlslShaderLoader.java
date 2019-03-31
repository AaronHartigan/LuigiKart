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

package ray.rage.asset.shader.loaders;

import java.io.*;
import java.nio.file.*;

import ray.rage.asset.shader.*;

/**
 * A {@link ShaderLoader shader-loader} capable of processing GLSL source code
 * from text files.
 * <p>
 * The common file extensions generally include the following:
 * <ul>
 * <li><code>.vert</code></li>
 * <li><code>.frag</code></li>
 * <li><code>.tesc</code></li>
 * <li><code>.tese</code></li>
 * <li><code>.geom</code></li>
 * <li><code>.glsl</code></li>
 * </ul>
 * Although these can be generic, filename extensions generally correlate to the
 * pipeline stage they're meant to implement.
 *
 * @author Raymond L. Rivera
 *
 */
public final class GlslShaderLoader implements ShaderLoader {

    @Override
    public void loadAsset(Shader shader, Path path) throws IOException, NullPointerException {
        if (shader == null)
            throw new NullPointerException("Null shader");

        BufferedReader br = Files.newBufferedReader(path);
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null)
            sb.append(line + System.lineSeparator());

        if (br != null)
            br.close();

        shader.setSource(sb.toString());
    }

    @Override
    public void notifyDispose() {}

}
