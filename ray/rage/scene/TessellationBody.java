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

import ray.rage.rendersystem.*;
import ray.rage.scene.SceneManager;
import ray.rage.scene.Tessellation;

/**
 * A <i>tessellation body</i> is the {@link Renderable renderable} part of a
 * {@link Tessellation tessellation}.
 * <p>
 * Only a {@link Renderable renderable} section like <code>this</code> one gets
 * submitted by the {@link SceneManager scene-manager} to the
 * {@link RenderSystem render-system} for processing.
 * <p>
 * There is one <i>tessellation body</i> per {@link Tessellation tessellation}.
 *
 * @author Kevin S. Cheong
 *
 * @see Tessellation
 *
 */
public interface TessellationBody extends Renderable {

	/**
     * Gets the {@link Tessellation tessellation} that created and owns
     * <code>this</code> <i>tessellation body</i>.
     *
     * @return The {@link Tessellation tessellation}.
     */
    Tessellation getParent();
    
    /**
     * Sets the amount of times to repeat the texture in the X dimension when tiling.
     * Remember to set the {@link TextureState TextureState's} WrapMode to GL_REPEAT 
     * or GL_MIRRORED_REPEAT.
     * 
     * @param The amount of times to repeat in the X direction
     */
    void setTextureTilingX(int amount);
    
    /**
     * Sets the amount of times to repeat the texture in the Z dimension when tiling.
     * Remember to set the {@link TextureState TextureState's} WrapMode to GL_REPEAT 
     * or GL_MIRRORED_REPEAT.
     * 
     * @param The amount of times to repeat in the Z direction
     */
    void setTextureTilingZ(int amount);
    
    /**
     * Sets the amount of times to repeat the height map in the X dimension when tiling.
     * Remember to set the {@link TextureState TextureState's} WrapMode to GL_REPEAT 
     * or GL_MIRRORED_REPEAT.
     * 
     * @param The amount of times to repeat in the X direction
     */
    void setHeightMapTilingX(int amount);
    
    /**
     * Sets the amount of times to repeat the height map in the Z dimension when tiling.
     * Remember to set the {@link TextureState TextureState's} WrapMode to GL_REPEAT 
     * or GL_MIRRORED_REPEAT.
     * 
     * @param The amount of times to repeat in the Z direction
     */
    void setHeightMapTilingZ(int amount);
    
    /**
     * Sets the amount of times to repeat the normal map in the X dimension when tiling.
     * Remember to set the {@link TextureState TextureState's} WrapMode to GL_REPEAT 
     * or GL_MIRRORED_REPEAT.
     * 
     * @param The amount of times to repeat in the X direction
     */
    void setNormalMapTilingX(int amount);
    
    /**
     * Sets the amount of times to repeat the normal map in the Z dimension when tiling.
     * Remember to set the {@link TextureState TextureState's} WrapMode to GL_REPEAT 
     * or GL_MIRRORED_REPEAT.
     * 
     * @param The amount of times to repeat in the Z direction
     */
    void setNormalMapTilingZ(int amount);
    
    /**
     * The amount of times the texture is repeated in the X dimension. Applies when
     * {@link TextureState TextureState's} WrapMode is set to GL_REPEAT or GL_MIRRORED_REPEAT.
     * 
     * @return The amount of times repeated in the X direction.
     */
    int getTextureTilingX();
    
    /**
     * The amount of times the texture is repeated in the Z dimension. Applies when
     * {@link TextureState TextureState's} WrapMode is set to GL_REPEAT or GL_MIRRORED_REPEAT.
     * 
     * @return The amount of times repeated in the Z direction.
     */
    int getTextureTilingZ();
    
    /**
     * The amount of times the height map is repeated in the X dimension. Applies when
     * {@link TextureState TextureState's} WrapMode is set to GL_REPEAT or GL_MIRRORED_REPEAT.
     * 
     * @return The amount of times repeated in the X direction.
     */
    int getHeightMapTilingX();
    
    /**
     * The amount of times the height map is repeated in the Z dimension. Applies when
     * {@link TextureState TextureState's} WrapMode is set to GL_REPEAT or GL_MIRRORED_REPEAT.
     * 
     * @return The amount of times repeated in the Z direction.
     */
    int getHeightMapTilingZ();
    
    /**
     * The amount of times the normal map is repeated in the X dimension. Applies when
     * {@link TextureState TextureState's} WrapMode is set to GL_REPEAT or GL_MIRRORED_REPEAT.
     * 
     * @return The amount of times repeated in the X direction.
     */
    int getNormalMapTilingX();
    
    /**
     * The amount of times the normal map is repeated in the Z dimension. Applies when
     * {@link TextureState TextureState's} WrapMode is set to GL_REPEAT or GL_MIRRORED_REPEAT.
     * 
     * @return The amount of times repeated in the Z direction.
     */
    int getNormalMapTilingZ();
    
    /**
     * Multiplies the <i>red</i> values in the pixel data of provided
     * height maps.
     * <p>
     * The multiplier is automatically set to zero when the <i>tessellation</i>
     * is first created, or if the client calls <code>removeHeightMap</code>.
     * <p>
     * The multiplier is automatically set to one when the client calls
     * <code>setTexture</code>.
     *
     * @param amount
     *            The amount to multiply reds. Negative values are permitted.
     */
    void setMultiplier(float amount);
    
    /**
     * Sets the amount of subdivisions during the <i>tessellation</i> process done
     * in the <code>tessellation shaders</code>. It is set to zero by default.
     * <p>
     * Subdivisions are used to perform Level-of-Detail (LOD) operations in an
     * attempt to make <i>tessellations</i> less expensive to perform and maintain.
     * Making the subdivisions higher improves performance in theory, but may 
     * cause unwanted visible "cracks" in the tessellated plane.
     *
     * @param amount
     *            The amount to subdivide tessellated vertices by.
     */
    void setSubdivisions(float amount);
	
    /**
     * Setting this to <code>true</code> informs the <code>tessellation shaders</code> to use
     * a provided texture (if false, <code>default.png</code> will be used instead).
     * <p>
     * The {@Link Tessellation tessellation} that owns <code>this</code> <i>tessellation
     * body</i> will <i>automatically</i> set the boolean to <code>true</code> when a texture
     * is provided, or <code>false</code> if removed.
     *
     * @param boolean
     *            Turns the use of a provided texture on and off.
     */
	void setHasTexture(boolean bool);

	/**
     * Setting this to <code>true</code> informs the <code>tessellation shaders</code> to use
     * a provided height map (if false, <code>default.png</code> will be used instead, and its
     * height will be ignored unless the user specifies a multiplier other than zero).
     * <p>
     * The {@Link Tessellation tessellation} that owns <code>this</code> <i>tessellation
     * body</i> will <i>automatically</i> set the boolean to <code>true</code> when a height map
     * is provided, or <code>false</code> if removed.
     *
     * @param boolean
     *            Turns the use of a provided texture on and off.
     */
	void setHasHeightMap(boolean bool);

	/**
     * Setting this to <code>true</code> informs the <code>tessellation shaders</code> to use
     * a provided normal map (if false, <code>default.png</code> will be used instead, and the
     * normals will be estimated in the <code>tessellation shaders</code>).
     * <p>
     * The {@Link Tessellation tessellation} that owns <code>this</code> <i>tessellation
     * body</i> will <i>automatically</i> set the boolean to <code>true</code> when a normal map
     * is provided, or <code>false</code> if removed.
     *
     * @param boolean
     *            Turns the use of a provided texture on and off.
     */
	void setHasNormalMap(boolean bool);
	
	/**
     * Sets the amount of vertices created during the <i>tessellation</i> process done
     * in the <code>tessellation shaders</code>. It is set to a level of seven 
     * by default.
     * <p>
     * Quality levels are used to perform geometry (primitive) generating operations in
     * an attempt to make <i>tessellations</i> less expensive to perform and maintain.
     * Making the quality lower improves performance in theory, but may 
     * cause inaccurate visual height representations and poor height query results.
     * <p>
     * The minimum quality level is 5 (32x32 vertex patches). There is no
     * maximum quality, but it is recommended to not use anything higher than
     * a quality level of 10 (1024x1024 vertex patches). If the client attempts
     * to assign a quality level of 4 or lower, it will be set to 5.
     *
     * @param amount
     *            The level of quality to set.
     */
	void setQuality(int amount);
	
	/**
     * Returns the current multiplier. The multiplier adjusts the height map values.
     * <p>
     * If the <i>tessellation</i> is first created, or has its height map removed, the multiplier
     * will automatically be set to zero.
     * <p>
     * If the <i>tessellation</i> is provided a new height map, the multiplier will automatically
     * be set to one.
     *
     * @return multiplier
     */
	float getMultiplier();
	
	/**
     * Returns the current subdivision amount. Subdivisions adjust the spacing of vertices
     * generated in the <code>tessellation shaders</code>, causing vertices to be less
     * densely generated the farther away they are from the client's {@Link Camera camera}. 
     * Level-of-Detail (LOD) operations require subdivisons to be some positive number.
     * <p>
     * If the <i>tessellation</i> is first created, the subdivisions are set to zero,
     * essentially disabling LOD.
     * <p>
     * Having subdivisions less than zero are permitted, but vertices near the client's
     * {@Link Camera camera} will not be generated; may cause unwanted visual results.
     * <p>
     * Having subdivisions set to zero is essentially disabling LOD.
     * <p>
     * Having a positive value in subdivisions may improve performance, but could also cause
     * unwanted visual artifacts (such as cracks appearing on the plane).
     *
     * @return subdivisions
     */
	float getSubdivisions();
	
	/**
     * Returns true if the <i>tessellation</i> has a user-defined texture associated with it.
     *
     * @return boolean
     */
	boolean hasTexture();
	
	/**
     * Returns true if the <i>tessellation</i> has a user-defined height map associated with it.
     *
     * @return boolean
     */
	boolean hasHeightMap();
	
	/**
     * Returns true if the <i>tessellation</i> has a user-defined normal map associated with it.
     *
     * @return boolean
     */
	boolean hasNormalMap();
	
	/**
     * Returns the current quality level. The quality adjusts how many vertices are generated
     * per patch during <i>tessellation</i> in the default <code>tessellation shaders</code>.
     * <p>
     * Upon creation of a <i>tessellation</i>, the quality is set to seven by default.
     * The quality level must be a minimum of five. No maximum is enforced, though it is advised to
     * use a value of ten or less.
     * <p>
     * Higher quality levels typically improve the accuracy of height queries, but may impact performance.
     *
     * @return quality level
     */
	int getQualityLevel();
	
	/**
     * Returns the amount: 2^(quality level). The quality total squared is the amount of primitives
     * generated during <i>tessellation</i> in the default <code>tessellation shaders</code>.
     * <p>
     * Upon creation of a <i>tessellation</i>, the quality is set to seven by default (creating
     * a quality total of 128). The quality level must be a minimum of five (a total of 32). No maximum 
     * is enforced, though it is advised to use a value of ten or less (2^10 is 1024).
     * <p>
     * Higher quality levels typically improve the accuracy of height queries, but may impact performance.
     * <p>
     * This call is mostly needed by {@link GL4RenderSystem GL4RenderSystem}
	 * and {@link GlslTessProgram GlslTessProgram}.
     *
     * @return quality total
     */
	int getQualityTotal();
}
