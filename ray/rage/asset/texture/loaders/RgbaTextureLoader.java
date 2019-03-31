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

package ray.rage.asset.texture.loaders;

import java.awt.*;
import java.awt.color.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;

import javax.imageio.*;

import ray.rage.asset.texture.*;

/**
 * A {@link TextureLoader texture-loader} capable of processing raw image data
 * stored using the {@link ColorSpace#CS_sRGB RGB color-space}.
 * <p>
 * Some common file extensions generally include the following:
 * <ul>
 * <li><code>.jpg</code></li>
 * <li><code>.jpeg</code></li>
 * <li><code>.png</code></li>
 * </ul>
 *
 * @author Raymond L. Rivera
 */
public final class RgbaTextureLoader implements TextureLoader {

    @Override
    public void loadAsset(Texture tex, Path path) throws IOException, NullPointerException {
        if (tex == null)
            throw new NullPointerException("Null texture");

        BufferedImage img = ImageIO.read(Files.newInputStream(path));
        img = toFlippedRGBA(img);
        tex.setImage(img);
    }

    private static BufferedImage toFlippedRGBA(BufferedImage orig) {
        // makes some relationships more obvious
        final int dataBufferType = DataBuffer.TYPE_BYTE;
        final int[] rgbaBits = new int[] { 8, 8, 8, 8 };

        // @formatter:off
        WritableRaster raster = Raster.createInterleavedRaster(
            dataBufferType,
            orig.getWidth(),
            orig.getHeight(),
            rgbaBits.length,
            null
        );
        ColorModel colorModel = new ComponentColorModel(
            ColorSpace.getInstance(ColorSpace.CS_sRGB),
            rgbaBits,
            true,   // has alpha?
            false,  // pre-multiplied alpha?
            ComponentColorModel.TRANSLUCENT,
            dataBufferType
        );
        BufferedImage xformedImage = new BufferedImage(
            colorModel,
            raster,
            colorModel.isAlphaPremultiplied(),
            null
        );
        // @formatter:on

        // Java expects images to have their origin at the upper left
        // but the render systems expect the origin at the lower
        // left, so we must flip the image upside down.
        //
        // An AffineTransform performs the flipping and a Graphics2D
        // object from the new/flipped image draws the original image into
        // itself, applying the AffineTransform as it draws (i.e. "upside down"
        // in the Java sense, but right-side up in the render system's sense).
        //
        // FIXME: This flip transform leaks an OpenGL render system coordinate
        // system dependency. Perhaps, practically, it will be a non-issue in
        // the long-term, since there's no Direct3D render system support for
        // Java, but might become an issue due to coordinate system changes when
        // moving to Vulkan from OpenGL. Perhaps making render systems
        // responsible for creating the actual transform scaling might be a good
        // solution? It's appropriate for them to be coordinate system-aware.
        //
        AffineTransform xform = new AffineTransform();
        xform.translate(0, orig.getHeight());
        xform.scale(1d, -1d);

        Graphics2D gfx = xformedImage.createGraphics();
        gfx.transform(xform);
        gfx.drawImage(orig, null, null);
        gfx.dispose();

        return xformedImage;
    }

    @Override
    public void notifyDispose() {}

}
