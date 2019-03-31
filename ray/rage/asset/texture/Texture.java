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

package ray.rage.asset.texture;

import java.awt.*;
import java.awt.color.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.nio.*;

import ray.rage.asset.*;
import ray.rage.asset.mesh.*;
import ray.rage.asset.texture.Texture;
import ray.rage.asset.texture.TextureLoader;
import ray.rage.asset.texture.TextureManager;
import ray.rage.rendersystem.states.*;
import ray.rage.util.*;

/**
 * A <i>texture</i> is an {@link Asset asset} that contains a
 * {@link BufferedImage buffered-image} in the {@link ColorSpace#CS_sRGB sRGB
 * color-space} and can be mapped to a {@link Mesh mesh's} geometry for
 * rendering, among other things.
 * <p>
 * A texture contains data as a {@link BufferedImage buffered-image}. Data about
 * how this texture is to be used is specified separately by a
 * {@link TextureState texture-state}, allowing the same texture to be re-used
 * for multiple different things without having to duplicate image or state
 * data.
 *
 * @author Raymond L. Rivera
 *
 * @see TextureLoader
 * @see TextureManager
 * @see TextureState
 */
public final class Texture extends AbstractAsset {

    private static final ColorModel requiredColorModel = createColorModel();

    private ByteBuffer              rgbaBuffer;
    private BufferedImage           image;

    Texture(TextureManager tm, String name) {
        super(tm, name);
    }

    /**
     * Sets the {@link BufferedImage buffered-image} <code>this</code>
     * {@link Texture texture} represents.
     * <p>
     * The {@link BufferedImage buffered-image's} {@link ColorModel color-model}
     * must match the following attributes:
     *
     * <pre>
     * <code>
     * ComponentColorModel(
     *      ColorSpace.getInstance(ColorSpace.CS_sRGB),
     *      new int[] { 8, 8, 8, 8 },        // bits per RGBA component
     *      true,                            // has alpha
     *      false,                           // has pre-multiplied alpha
     *      ComponentColorModel.TRANSLUCENT,
     *      DataBuffer.TYPE_BYTE
     * );
     * </code>
     * </pre>
     *
     * It's the {@link TextureLoader texture-loader's} responsibility to
     * transform raw {@link Texture texture} data into the appropriate
     * {@link ColorModel color-model} before assignment.
     *
     * @param img
     *            The {@link BufferedImage buffered-image}.
     * @throws IllegalArgumentException
     *             If the {@link BufferedImage buffered-image's}
     *             {@link ColorModel color-model} is not what's expected.
     */
    public void setImage(BufferedImage img) throws IllegalArgumentException {
        ColorModel cm = img.getColorModel();

        if (!cm.equals(requiredColorModel))
            throw new IllegalArgumentException("Invalid image color model: " + cm);

        // successful color model validation guarantees that this cast will
        // work due to the specified transfer type of DataBuffer.TYPE_BYTE
        DataBufferByte bytes = (DataBufferByte) img.getRaster().getDataBuffer();
        rgbaBuffer = BufferUtil.directByteBuffer(bytes.getData());
        image = img;
    }

    /**
     * Gets <code>this</code> {@link Texture texture's} data as a
     * {@link BufferedImage buffered-image}.
     *
     * @return The data as a {@link BufferedImage buffered-image}.
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Gets <code>this</code> {@link Texture texture's} data as a
     * {@link ByteBuffer byte-buffer}.
     *
     * @return The data as a {@link ByteBuffer byte-buffer}.
     */
    public ByteBuffer getBuffer() {
        return rgbaBuffer;
    }

    /**
     * Allows the client to apply an {@link AffineTransform affine-transform} to
     * <code>this</code> {@link Texture texture}.
     * <p>
     * Note that this requires the internal {@link BufferedImage buffered-image}
     * to be re-drawn, so you should avoid doing this frequently or in
     * performance-critical sections.
     *
     * @param xform
     *            The {@link AffineTransform affine-transform} to be applied.
     */
    public void transform(AffineTransform xform) {
        ColorModel model = image.getColorModel();
        BufferedImage xformedImg = new BufferedImage(model, image.getRaster(), model.isAlphaPremultiplied(), null);

        Graphics2D gfx = xformedImg.createGraphics();
        gfx.transform(xform);
        gfx.drawImage(image, null, null);
        gfx.dispose();

        setImage(xformedImg);
    }

    @Override
    public void notifyDispose() {
        rgbaBuffer.clear();
        rgbaBuffer = null;
        image = null;
        super.notifyDispose();
    }

    private static ColorModel createColorModel() {
        // writing it like this should make the relationships between the fields
        // very clear
        final int[] bitsPerComponent = new int[] { 8, 8, 8, 8 };
        final boolean hasAlpha = bitsPerComponent.length == 4;
        final boolean hasPreMultipliedAlpha = false;

        // FIXME: This requirement leaks render system dependencies. For
        // example:
        //
        // 1. expected image color components is GL_RGBA,
        // 2. expected in-memory pixel format is GL_RGBA, and
        // 3. expected in-memory pixel data type is GL_UNSIGNED_BYTE
        //
        // Perhaps, practically, it will be a non-issue in the long-term, but I
        // don't like it.
        //
        // @formatter:off
        return new ComponentColorModel(
            ColorSpace.getInstance(ColorSpace.CS_sRGB),
            bitsPerComponent,
            hasAlpha,
            hasPreMultipliedAlpha,
            ComponentColorModel.TRANSLUCENT,
            DataBuffer.TYPE_BYTE
        );
        // @formatter:on
    }

}
