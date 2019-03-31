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

package ray.rage.rendersystem.states;

import ray.rage.asset.texture.*;
import ray.rage.rendersystem.*;

/**
 * A <i>texture state</i> determines which {@link Texture textures} will be
 * used, how they'll be applied, filtered, stored in GPU memory, and other
 * properties.
 * <p>
 * Texture states are decoupled from {@link Texture textures} because, in this
 * way, the same state can be applied to multiple {@link Texture textures} at
 * once without duplication.
 *
 * @author Raymond L. Rivera
 *
 */
public interface TextureState extends RenderState {

    /**
     * A <i>texture target</i> is specifies the type of {@link Texture textures}
     * that are supported.
     * <p>
     * A <i>target</i> is a "place" within a texture unit where a texture object
     * can be stored. There're several targets per unit, allowing different
     * objects to be simultaneously stored in the same unit as long as they're
     * assigned to different targets.
     *
     * @author Raymond L. Rivera
     *
     */
    enum Target {
        /**
         * The {@link Target target} for 2-dimensional {@link Texture textures},
         * equivalent to <code>GL_TEXTURE_2D</code>. <i>This is the default</i>.
         */
        TWO_DIMENSIONAL,

        /**
         * The {@link Target target} for cube-map {@link Texture textures},
         * equivalent to <code>GL_CUBE_MAP</code>.
         */
        CUBE_MAP
    }

    /**
     * The <i>color components</i> specify which basic colors are represented
     * and whether alpha transparency is included.
     *
     * @author Raymond L. Rivera
     *
     */
    enum ColorComponents {
        /**
         * Specifies the Red, Green, and Blue components, including Alpha
         * transparency, equivalent to <code>GL_RGBA</code>. <i>This is the
         * default</i>.
         */
        RGBA
    }

    /**
     * The <i>pixel format</i> is describes the internal image data available
     * for each pixel.
     *
     * @author Raymond L. Rivera
     *
     */
    enum PixelFormat {
        /**
         * Specifies there're 8 bits of data per component, equivalent to
         * <code>GL_RGBA</code>. <i>This is the default</i>.
         */
        R8G8B8A8
    }

    /**
     * The <i>memory format</i> describes the space storage requirement for each
     * data "value".
     * <p>
     * For example, if a client stores a group of values, it needs to specify
     * how the generic memory contents should be interpreted by the underlying
     * implementation so that the the intended results are produced.
     *
     * @author Raymond L. Rivera
     *
     */
    enum MemoryFormat {
        /**
         * Specifies generic memory data is to be interpreted as groups of
         * bytes, equivalent to <code>GL_UNSIGNED_BYTE</code>. <i>This is the
         * default</i>.
         */
        BYTES
    }

    /**
     * A <i>minification filter</i> is a <a
     * href="https://en.wikipedia.org/wiki/Texture_filtering"{@link Texture
     * texture} filtering</a> function. It's used whenever the pixel being
     * textured maps to an area greater than one texel.
     *
     * @author Raymond L. Rivera
     *
     */
    enum MinificationFilter {
        /**
         * Chooses the texel closest to the center of the pixel being textured,
         * without using <a href=
         * "https://en.wikipedia.org/wiki/Texture_filtering#Mipmapping">mipmaps</a>.
         * <p>
         * This is equivalent to <code>GL_NEAREST</code>.
         */
        NEAREST_NEIGHBOR_NO_MIPMAPS(false),

        /**
         * Chooses the weighted average of the 4 texels closest to the center of
         * the pixel being textured, without using <a href=
         * "https://en.wikipedia.org/wiki/Texture_filtering#Mipmapping">mipmaps</a>.
         * <p>
         * Note that {@link WrapMode wrap-modes} can affect whether border
         * texels can be included or not. This is equivalent to
         * <code>GL_LINEAR</code>.
         */
        BILINEAR_NO_MIPMAPS(false),

        /**
         * Chooses the <a href=
         * "https://en.wikipedia.org/wiki/Texture_filtering#Mipmapping">mipmap</a>
         * that most closely matches the size of the pixel being textured, while
         * using {@link #NEAREST_NEIGHBOR_NO_MIPMAPS} as the criterion to
         * produce a texture value.
         * <p>
         * This is equivalent to <code>GL_NEAREST_MIPMAP_NEAREST</code>.
         */
        NEAREST_NEIGHBOR_NEAREST_MIPMAP(true),

        /**
         * Chooses the <a href=
         * "https://en.wikipedia.org/wiki/Texture_filtering#Mipmapping">mipmap</a>
         * that most closely matches the size of the pixel being textured, while
         * using {@link #BILINEAR_NO_MIPMAPS} as the criterion to produce a
         * texture value.
         * <p>
         * This is equivalent to <code>GL_LINEAR_MIPMAP_NEAREST</code>.
         */
        BILINEAR_NEAREST_MIPMAP(true),

        /**
         * Chooses a weighted average of the 2 <a href=
         * "https://en.wikipedia.org/wiki/Texture_filtering#Mipmapping">mipmaps</a>
         * that most closely match the size of the pixel being textured, while
         * using {@link #NEAREST_NEIGHBOR_NO_MIPMAPS} as the criterion to
         * produce a texture value from each mipmap.
         * <p>
         * This is equivalent to <code>GL_NEAREST_MIPMAP_LINEAR</code>.
         */
        NEAREST_NEIGHBOR_LINEAR_MIPMAP(true),

        /**
         * Chooses a weighted average of the 2 <a href=
         * "https://en.wikipedia.org/wiki/Texture_filtering#Mipmapping">mipmaps</a>
         * that most closely match the size of the pixel being textured, while
         * using {@link #BILINEAR_NO_MIPMAPS} as the criterion to produce a
         * texture value from each mipmap.
         * <p>
         * This is equivalent to <code>GL_LINEAR_MIPMAP_LINEAR</code>. <i>This
         * is the default.</i>
         */
        TRILINEAR(true);

        private boolean usesMipMaps;

        private MinificationFilter(boolean wantsMipMaps) {
            usesMipMaps = wantsMipMaps;
        }

        public boolean usesMipMaps() {
            return usesMipMaps;
        }
    }

    /**
     * A <i>magnification filter</i> is a <a
     * href="https://en.wikipedia.org/wiki/Texture_filtering"{@link Texture
     * texture} filtering</a> function, used whenever the pixel being textured
     * maps to an area less than or equal to that of one texel.
     *
     * @author Raymond L. Rivera
     *
     */
    enum MagnificationFilter {
        /**
         * Chooses the texel closest to the center of the pixel being textured.
         * <p>
         * This is equivalent to <code>GL_NEAREST</code>.
         */
        NEAREST_NEIGHBOR,

        /**
         * Chooses the weighted average of the 4 texels closest to the center of
         * the pixel being textured.
         * <p>
         * This is equivalent to <code>GL_LINEAR</code>. Note that
         * {@link WrapMode wrap-modes} can affect whether border texels can be
         * included or not. <i>This is the default.</i>
         */
        BILINEAR;
    }

    /**
     * The <i>wrap mode</i> is a function that determines how {@link Texture
     * texture} coordinates will be used to map texels to pixels.
     *
     * @author Raymond L. Rivera
     *
     */
    enum WrapMode {
        /**
         * Ignores the integer part of the {@link Texture texture} coordinate;
         * uses only the fractional part, thereby creating a repeating pattern.
         */
        REPEAT,

        /**
         * Causes the {@link Texture texture} coordinate to be set to the
         * fractional part if the integer part of coordinate is an even number.
         * If the integer part of coordinate is odd, then the {@link Texture
         * texture} coordinate is set to <code>1 - fractionalPart</code>.
         */
        REPEAT_MIRRORED,

        /**
         * Clamps the {@link Texture texture} coordinate gets to the range
         * <code>[1/2N, 1 - 1/2N]</code>, where <code>N</code> is the size of
         * the {@link Texture texture} in the direction of clamping.
         */
        CLAMP_TO_EDGE,

        /**
         * Causes the the coordinate to be repeated as in
         * {@value #REPEAT_MIRRORED}, but only for one repetition of the
         * {@link Texture texture}, after which point the coordinate is to be
         * clamped as in {@link #CLAMP_TO_EDGE}.
         */
        CLAMP_TO_EDGE_MIRRORED,

        /**
         * Behaves like {@link #CLAMP_TO_EDGE}, except in cases where clamping
         * would have occurred if {@link #CLAMP_TO_EDGE} had been in use. In
         * this case, the fetched texel data is replaced by values specified for
         * the {@link Texture texture's} border.
         * <p>
         * Note that this does not happen automatically. At this time, there's
         * no support for specifying what the {@link RenderSystem} should use as
         * border colors.
         */
        CLAMP_TO_BORDER
    }

    /**
     * Sets the {@link Texture texture} to be used when {@link #apply()
     * applying} <code>this</code> {@link TextureState state}.
     * <p>
     * By default, it gets assigned to {@link Texture texture} unit zero.
     *
     * @param t
     *            The {@link Texture texture}.
     * @throws NullPointerException
     *             If the {@link Texture texture} is <code>null</code>.
     */
    void setTexture(Texture t);

    /**
     * Sets the {@link Texture texture} to be used when {@link #apply()
     * applying} <code>this</code> {@link TextureState state}.
     *
     * @param t
     *            The {@link Texture texture}.
     * @param textureUnit
     *            The unit where the {@link Texture texture} will go.
     * @throws NullPointerException
     *             If the {@link Texture texture} is <code>null</code>.
     * @throws IllegalArgumentException
     *             If the unit is negative or greater than the number of
     *             available units, which is hardware-dependent.
     */
    void setTexture(Texture t, int textureUnit);

    /**
     * Gets the {@link Texture texture} at unit zero.
     *
     * @return The {@link Texture texture} in unit zero, if one exists.
     *         Otherwise <code>null</code>.
     */
    Texture getTexture();

    /**
     * Gets the {@link Texture texture} at specified unit.
     *
     * @param textureUnit
     *            The unit from which to get the {@link Texture texture}.
     * @return The {@link Texture texture} in the specified unit, if one exists.
     * @throws IllegalArgumentException
     *             If the unit is negative or greater than the number of
     *             available units, which is hardware-dependent.
     */
    Texture getTexture(int textureUnit);

    /**
     * Sets the {@link Target target} of <code>this</code> {@link TextureState
     * state}.
     *
     * @param target
     *            The {@link Target target}
     * @throws NullPointerException
     *             If the {@link Target target} is <code>null</code>.
     */
    void setTarget(Target target);

    /**
     * Gets the {@link Target target} of <code>this</code> {@link TextureState
     * state}.
     *
     * @return The {@link Target target}.
     */
    Target getTarget();

    /**
     * Sets the {@link MinificationFilter minification-filter} of
     * <code>this</code> {@link TextureState state}.
     *
     * @param filter
     *            The {@link MinificationFilter minification-filter}
     * @throws NullPointerException
     *             If the {@link MinificationFilter minification-filter} is
     *             <code>null</code>.
     */
    void setMinificationFilter(MinificationFilter filter);

    /**
     * Gets the {@link MinificationFilter minification-filter} of
     * <code>this</code> {@link TextureState state}.
     *
     * @return The {@link MinificationFilter minification-filter}.
     */
    MinificationFilter getMinificationFilter();

    /**
     * Sets the {@link MagnificationFilter magnification-filter} of
     * <code>this</code> {@link TextureState state}.
     *
     * @param filter
     *            The {@link MagnificationFilter magnification-filter}
     * @throws NullPointerException
     *             If the {@link MagnificationFilter magnification-filter} is
     *             <code>null</code>.
     */
    void setMagnificationFilter(MagnificationFilter filter);

    /**
     * Gets the {@link MagnificationFilter magnification-filter} of
     * <code>this</code> {@link TextureState state}.
     *
     * @return The {@link MagnificationFilter magnification-filter}.
     */
    MagnificationFilter getMagnificationFilter();

    /**
     * Sets the {@link ColorComponents color-components} of <code>this</code>
     * {@link TextureState state}.
     *
     * @param ccs
     *            The {@link ColorComponents color-components}
     * @throws NullPointerException
     *             If the {@link ColorComponents color-components} is
     *             <code>null</code>.
     */
    void setColorComponents(ColorComponents ccs);

    /**
     * Gets the {@link ColorComponents color-components} of <code>this</code>
     * {@link TextureState state}.
     *
     * @return The {@link ColorComponents color-components}.
     */
    ColorComponents getColorComponents();

    /**
     * Sets the {@link PixelFormat pixel-format} of <code>this</code>
     * {@link TextureState state}.
     *
     * @param format
     *            The {@link PixelFormat pixel-format}
     * @throws NullPointerException
     *             If the {@link PixelFormat pixel-format} is <code>null</code>.
     */
    void setPixelFormat(PixelFormat format);

    /**
     * Gets the {@link PixelFormat pixel-format} of <code>this</code>
     * {@link TextureState state}.
     *
     * @return The {@link PixelFormat pixel-format}.
     */
    PixelFormat getPixelFormat();

    /**
     * Sets the {@link MemoryFormat memory-format} of <code>this</code>
     * {@link TextureState state}.
     *
     * @param format
     *            The {@link MemoryFormat memory-format}
     * @throws NullPointerException
     *             If the {@link MemoryFormat memory-format} is
     *             <code>null</code>.
     */
    void setMemoryFormat(MemoryFormat format);

    /**
     * Gets the {@link MemoryFormat memory-format} of <code>this</code>
     * {@link TextureState state}.
     *
     * @return The {@link MemoryFormat memory-format}.
     */
    MemoryFormat getMemoryFormat();

    /**
     * Sets the {@link WrapMode wrap-mode} of <code>this</code>
     * {@link TextureState state}.
     *
     * @param format
     *            The {@link WrapMode wrap-mode}
     * @throws NullPointerException
     *             If the {@link WrapMode wrap-mode} is <code>null</code>.
     */
    void setWrapMode(WrapMode mode);

    /**
     * Gets the {@link WrapMode wrap-mode} of <code>this</code>
     * {@link TextureState state}.
     *
     * @return The {@link WrapMode wrap-mode}.
     */
    WrapMode getWrapMode();

}
