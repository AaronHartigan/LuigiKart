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

package ray.rage.rendersystem.gl4;

import java.util.*;
import java.util.logging.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;

import ray.rage.asset.texture.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.gl4.GL4AbstractRenderState;
import ray.rage.rendersystem.gl4.GL4TextureState;
import ray.rage.rendersystem.states.*;

/**
 * A concrete implementation of the {@link TextureState texture-state} interface
 * for a {@link GL4RenderSystem}.
 *
 * @author Raymond L. Rivera
 *
 */
public final class GL4TextureState extends GL4AbstractRenderState implements TextureState {

    private static final Logger             logger              = Logger.getLogger(GL4TextureState.class.getName());

    private static final int                INVALID_ID          = -1;

    private static final int                TEXTURE_UNIT_MIN    = 0;

    private final RenderSystem.Capabilities caps;

    private List<Texture>                   textures            = new ArrayList<>();
    private List<Integer>                   textureBuffers      = new ArrayList<>();

    private boolean                         needsUpdate         = true;

    private Target                          textureTarget       = Target.TWO_DIMENSIONAL;
    private MinificationFilter              minificationFilter  = MinificationFilter.TRILINEAR;
    private MagnificationFilter             magnificationFilter = MagnificationFilter.BILINEAR;
    private ColorComponents                 colorComponents     = ColorComponents.RGBA;
    private PixelFormat                     pixelFormat         = PixelFormat.R8G8B8A8;
    private MemoryFormat                    memoryFormat        = MemoryFormat.BYTES;
    private WrapMode                        wrapMode            = WrapMode.CLAMP_TO_EDGE;

    public GL4TextureState(RenderSystem.Capabilities rsCaps, GLCanvas canvas) {
        super(canvas);

        if (rsCaps == null)
            throw new NullPointerException("Null " + RenderSystem.Capabilities.class.getSimpleName());

        caps = rsCaps;
    }

    @Override
    public Type getType() {
        return Type.TEXTURE;
    }

    @Override
    public void setTexture(Texture t) {
        setTexture(t, TEXTURE_UNIT_MIN);
    }

    @Override
    public void setTexture(Texture t, int textureUnit) {
        if (t == null)
            throw new NullPointerException("Null " + Texture.class.getSimpleName());

        checkTextureUnitBounds(textureUnit);

        // make sure there's enough space in the list so that we can store the
        // specified texture at the specified index/unit
        while (textures.size() <= textureUnit)
            textures.add(null);

        textures.set(textureUnit, t);
        needsUpdate = true;
    }

    @Override
    public Texture getTexture() {
        return getTexture(TEXTURE_UNIT_MIN);
    }

    @Override
    public Texture getTexture(int textureUnit) {
        checkTextureUnitBounds(textureUnit);

        Texture t = textures.get(textureUnit);
        if (t == null)
            throw new RuntimeException("No " + Texture.class.getSimpleName() + " at unit " + textureUnit);

        return t;
    }

    @Override
    protected void applyImpl(GL4 gl) {
        switch (textureTarget) {
            case TWO_DIMENSIONAL:
                processTarget2D(gl);
                break;
            case CUBE_MAP:
                processTargetCubeMap(gl);
                break;
            default:
                logger.severe("Unimplemented target: " + textureTarget);
                break;
        }
    }

    private void processTarget2D(GL4 gl) {
        // XXX: In principle, a single texture state can handle multiple
        // textures simultaneously for the same texture target in different
        // texture units, but for it to work properly, it'll likely need
        // multiple sampler uniform variables in a single fragment shader for
        // all of them to be used at once.
        //
        // If there's only a single sampler, then it'll end up sampling from the
        // last texture object to get bound for its target. In short, if the
        // fragment shader has only one sampler uniform variable, you should
        // make sure the texture state only has 1 texture for the 2D target
        if (needsUpdate) {
            destroyTextureObjs(gl);
            for (int i = 0; i < textures.size(); ++i) {
                addTextureObj(gl);
                commitTexture2D(gl, textures.get(i), i);
            }
            needsUpdate = false;
        } else {
            for (int i = 0; i < textures.size(); ++i) {
                gl.glActiveTexture(GL4.GL_TEXTURE0 + i);
                gl.glBindTexture(GL4.GL_TEXTURE_2D, textureBuffers.get(i));
            }
        }
    }

    private void commitTexture2D(GL4 gl, Texture t, int unit) {
        final int components = getGLColorComponents(colorComponents);
        final int pixFormat = getGLPixelFormat(pixelFormat);
        final int memFormat = getGLMemoryFormat(memoryFormat);
        final int minFilter = getGLMinificationFilter(minificationFilter);
        final int magFilter = getGLMagnificationFilter(magnificationFilter);
        final int wrapParam = getGLWrapMode(wrapMode);

        gl.glActiveTexture(GL4.GL_TEXTURE0 + unit);
        gl.glBindTexture(GL4.GL_TEXTURE_2D, textureBuffers.get(unit));

        // @formatter:off
        gl.glTexImage2D(
            GL4.GL_TEXTURE_2D,
            0,
            components,
            t.getImage().getWidth(),
            t.getImage().getHeight(),
            0,
            pixFormat,
            memFormat,
            t.getBuffer()
        );
        // @formatter:on

        if (minificationFilter.usesMipMaps())
            gl.glGenerateMipmap(GL4.GL_TEXTURE_2D);

        gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, minFilter);
        gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, magFilter);
        gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_S, wrapParam);
        gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_T, wrapParam);
    }

    private void processTargetCubeMap(GL4 gl) {
        if (needsUpdate) {
            destroyTextureObjs(gl);
            addTextureObj(gl);

            // @formatter:off
            // FIXME: entries here must match order in SkyBox.Face enum
            final int[] targets = new int[] {
                GL4.GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
                GL4.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
                GL4.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
                GL4.GL_TEXTURE_CUBE_MAP_POSITIVE_X,
                GL4.GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
                GL4.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
            };
            // @formatter:on

            // TODO?: for skybox cubemaps, validate all textures have the same
            // size/dimensions
            Texture t = textures.get(0);
            final int width = t.getImage().getWidth();
            final int height = t.getImage().getHeight();

            gl.glActiveTexture(GL4.GL_TEXTURE0);
            gl.glBindTexture(GL4.GL_TEXTURE_CUBE_MAP, textureBuffers.get(0));
            gl.glTexStorage2D(GL4.GL_TEXTURE_CUBE_MAP, 1, GL4.GL_RGBA8, width, height);

            for (int i = 0; i < textures.size(); ++i)
                commitTextureCubeMap(gl, textures.get(i), targets[i]);

            needsUpdate = false;
        } else {
            gl.glActiveTexture(GL4.GL_TEXTURE0);
            gl.glBindTexture(GL4.GL_TEXTURE_CUBE_MAP, textureBuffers.get(0));
        }
    }

    private void commitTextureCubeMap(GL4 gl, Texture t, int target) {
        final int pixFormat = getGLPixelFormat(pixelFormat);
        final int memFormat = getGLMemoryFormat(memoryFormat);
        final int minFilter = getGLMinificationFilter(minificationFilter);
        final int magFilter = getGLMagnificationFilter(magnificationFilter);
        final int wrapParam = getGLWrapMode(wrapMode);

        // @formatter:off
        gl.glTexSubImage2D(
            target,
            0,  // MIPmap level
            0,  // x-offset
            0,  // y-offset
            t.getImage().getWidth(),
            t.getImage().getHeight(),
            pixFormat,
            memFormat,
            t.getBuffer()
        );
        // @formatter:on

        if (minificationFilter.usesMipMaps())
            gl.glGenerateMipmap(GL4.GL_TEXTURE_CUBE_MAP);

        gl.glTexParameteri(GL4.GL_TEXTURE_CUBE_MAP, GL4.GL_TEXTURE_MIN_FILTER, minFilter);
        gl.glTexParameteri(GL4.GL_TEXTURE_CUBE_MAP, GL4.GL_TEXTURE_MAG_FILTER, magFilter);
        gl.glTexParameteri(GL4.GL_TEXTURE_CUBE_MAP, GL4.GL_TEXTURE_WRAP_S, wrapParam);
        gl.glTexParameteri(GL4.GL_TEXTURE_CUBE_MAP, GL4.GL_TEXTURE_WRAP_T, wrapParam);
        gl.glTexParameteri(GL4.GL_TEXTURE_CUBE_MAP, GL4.GL_TEXTURE_WRAP_R, wrapParam);
    }

    private void addTextureObj(GL4 gl) {
        final int[] tbos = new int[1];
        gl.glGenTextures(tbos.length, tbos, 0);

        if (tbos[0] == INVALID_ID)
            throw new IllegalStateException("Invalid " + Texture.class.getSimpleName() + " buffer ID");

        textureBuffers.add(tbos[0]);
    }

    private void destroyTextureObjs(GL4 gl) {
        if (textureBuffers.size() == 0)
            return;

        final int[] tbos = toPrimitiveArray(textureBuffers);
        gl.glDeleteTextures(tbos.length, tbos, 0);
        textureBuffers.clear();
    }

    public void setTarget(Target target) {
        if (target == null)
            throw new NullPointerException("Target is null");

        textureTarget = target;
        needsUpdate = true;
    }

    public Target getTarget() {
        return textureTarget;
    }

    public void setMinificationFilter(MinificationFilter filter) {
        if (filter == null)
            throw new NullPointerException("Null " + MinificationFilter.class.getSimpleName());

        minificationFilter = filter;
        needsUpdate = true;
    }

    public MinificationFilter getMinificationFilter() {
        return minificationFilter;
    }

    public void setMagnificationFilter(MagnificationFilter filter) {
        if (filter == null)
            throw new NullPointerException("Null " + MagnificationFilter.class.getSimpleName());

        magnificationFilter = filter;
        needsUpdate = true;
    }

    public MagnificationFilter getMagnificationFilter() {
        return magnificationFilter;
    }

    public void setColorComponents(ColorComponents ccs) {
        if (ccs == null)
            throw new NullPointerException("Null " + ColorComponents.class.getSimpleName());

        colorComponents = ccs;
        needsUpdate = true;
    }

    public ColorComponents getColorComponents() {
        return colorComponents;
    }

    public void setPixelFormat(PixelFormat format) {
        if (format == null)
            throw new NullPointerException("Null " + PixelFormat.class.getSimpleName());

        pixelFormat = format;
        needsUpdate = true;
    }

    public PixelFormat getPixelFormat() {
        return pixelFormat;
    }

    public void setMemoryFormat(MemoryFormat format) {
        if (format == null)
            throw new NullPointerException("Null " + MemoryFormat.class.getSimpleName());

        memoryFormat = format;
        needsUpdate = true;
    }

    public MemoryFormat getMemoryFormat() {
        return memoryFormat;
    }

    @Override
    public void setWrapMode(WrapMode mode) {
        if (mode == null)
            throw new NullPointerException("Null " + WrapMode.class.getSimpleName());

        wrapMode = mode;
    }

    @Override
    public WrapMode getWrapMode() {
        return wrapMode;
    }

    @Override
    protected void disposeImpl(GL4 gl) {
        // we don't own textures, so we don't try to destroy them
        textures.clear();
        destroyTextureObjs(gl);

        textures = null;
        textureBuffers = null;

        needsUpdate = true;
    }

    private void checkTextureUnitBounds(int unit) {
        if (unit < 0 || unit >= caps.getTextureUnitCount())
            throw new IllegalArgumentException("Invalid texture unit: " + unit);
    }

    private int[] toPrimitiveArray(List<Integer> list) {
        int[] values = new int[list.size()];
        for (int i = 0; i < values.length; ++i)
            values[i] = list.get(i);
        return values;
    }

    private static int getGLColorComponents(ColorComponents ccs) {
        switch (ccs) {
            case RGBA:
                return GL4.GL_RGBA;
            default:
                logger.warning("Unimplemented components: " + ccs + ". Using " + ColorComponents.RGBA);
                return GL4.GL_RGBA;
        }
    }

    private static int getGLPixelFormat(PixelFormat format) {
        switch (format) {
            case R8G8B8A8:
                return GL4.GL_RGBA;
            default:
                logger.warning("Unimplemented pixel format: " + format + ". Using " + PixelFormat.R8G8B8A8);
                return GL4.GL_RGBA;
        }
    }

    private static int getGLMinificationFilter(MinificationFilter filter) {
        switch (filter) {
            case NEAREST_NEIGHBOR_NO_MIPMAPS:
                return GL4.GL_NEAREST;
            case BILINEAR_NO_MIPMAPS:
                return GL4.GL_LINEAR;
            case NEAREST_NEIGHBOR_NEAREST_MIPMAP:
                return GL4.GL_NEAREST_MIPMAP_NEAREST;
            case BILINEAR_NEAREST_MIPMAP:
                return GL4.GL_LINEAR_MIPMAP_NEAREST;
            case NEAREST_NEIGHBOR_LINEAR_MIPMAP:
                return GL4.GL_NEAREST_MIPMAP_LINEAR;
            case TRILINEAR:
                return GL4.GL_LINEAR_MIPMAP_LINEAR;
            default:
                logger.warning(
                        "Unimplemented minification filter: " + filter + ". Using " + MinificationFilter.TRILINEAR);
                return GL4.GL_LINEAR_MIPMAP_LINEAR;
        }
    }

    private static int getGLMagnificationFilter(MagnificationFilter filter) {
        switch (filter) {
            case NEAREST_NEIGHBOR:
                return GL4.GL_NEAREST;
            case BILINEAR:
                return GL4.GL_LINEAR;
            default:
                logger.warning(
                        "Unimplemented magnification filter: " + filter + ". Using " + MagnificationFilter.BILINEAR);
                return GL4.GL_LINEAR;
        }
    }

    private static int getGLMemoryFormat(MemoryFormat format) {
        switch (format) {
            case BYTES:
                return GL4.GL_UNSIGNED_BYTE;
            default:
                logger.warning("Unimplemented memory format: " + format + ". Using " + MemoryFormat.BYTES);
                return GL4.GL_UNSIGNED_BYTE;
        }
    }

    private static int getGLWrapMode(WrapMode mode) {
        switch (mode) {
            case REPEAT:
                return GL4.GL_REPEAT;
            case REPEAT_MIRRORED:
                return GL4.GL_MIRRORED_REPEAT;
            case CLAMP_TO_EDGE:
                return GL4.GL_CLAMP_TO_EDGE;
            case CLAMP_TO_EDGE_MIRRORED:
                return GL4.GL_MIRROR_CLAMP_TO_EDGE;
            case CLAMP_TO_BORDER:
                return GL4.GL_CLAMP_TO_BORDER;
            default:
                logger.warning("Unimplemented wrap mode: " + mode + ". Using " + WrapMode.CLAMP_TO_EDGE);
                return GL4.GL_CLAMP_TO_EDGE;
        }
    }

}
