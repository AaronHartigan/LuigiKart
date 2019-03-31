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

import ray.rage.Engine;
import ray.rage.asset.texture.Texture;
import ray.rage.rendersystem.Renderable;
import ray.rage.rendersystem.shader.*;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.scene.SceneNode;
import ray.rage.scene.SceneObject;
import ray.rage.scene.TessellationBody;

/**
 * A <i>tessellation</i> is a {@link Texture textured} plane that is primarily
 * used to simulate terrain in a scene.
 * <p>
 * The {@link TessellationBody tessellation body} holds the necessary functions
 * required of a {@link Renderable renderable} object. The {@link TessellationBody 
 * tessellation body} holds quality data to define how many vertices are to be generated, 
 * but it does not hold any vertex information (because vertices are all generated in
 * the <code>tessellation shaders</code>).
 * <p>
 * <i>Tessellations</i> do not explicitly require a texture. <i>Tessellations</i> will use
 * <code> default.png </code> by default.
 * <p>
 * <i>Tessellations</i> do not explicitly require a height map. <i>Tessellations</i> will use
 * <code> default.png </code> as a place-holder, and height calculations will be
 * ignored (the height multiplying variable will be set to zero).
 * <p>
 * <i>Tessellations</i> do not explicitly require a normal map. <i>Tessellations</i> will use
 * <code> default.png </code> as a place-holder, and normals will be <i>estimated</i>
 * in the default <code>tessellation shaders</code>. Generating normals without a normal map 
 * is sightly more computationally expensive than deriving them from a normal map.
 * <p>
 * If a <i>tessellation</i> is queried for a height value, but the X-Z coordinate do
 * not fall within the bounds of the plane, the <i>tessellation</i> will normalize the
 * given X-Z coordinates to the closest point on the edge of the plane.
 * <p>
 * It is advised to avoid <i>pitching</i> or <i>rolling</i> the {@link SceneNode scene node} 
 * the <i>tessellation</i> is attached to. Doing so will break any height queries made 
 * to the <i>tessellation</i>. Performing a <i>yaw</i> about the global Y-Axis is acceptable,
 * because <i>tessellations</i> assume that the Y-Axis will be used to signify <i>"up"</i> and
 * <i>"down"</i>.
 * <p>
 * It is advised to avoid Scaling the {@link SceneNode scene node} in a negative
 * direction on the X, Y, or Z axes. <i>Tessellations do</i> account for scaling when
 * height queries are made, but caution should be taken when Scaling the X and Z axes
 * negatively. Clients must be aware that the X and Z axes must <i>both</i> be positive
 * <i>or both</i> be negative. If only one or the other is negative, and the other is positive, the
 * <i>tessellation</i> will not render as intended.
 *
 * @author Kevin S. Cheong
 *
 */
public interface Tessellation extends SceneObject {
	
	/**
	 * Sets a {@link TextureState TextureState} on this <i>tessellation</i>.
	 * 
	 * @param ts
     *            The {@link TextureState TextureState} to set.
	 */
	void setTextureState(TextureState ts);
	
	/**
	 * Retrieves the {@link TextureState TextureState} object bound to this <i>tessellation</i>.
	 * 
	 * @return The {@link TextureState TextureState}.
	 */
	TextureState getTextureState();
	
	/**
     * Tiles the texture across the <i>tessellation</i>.
     *
     * @param dimension
     *            The amount of times to tile the texture across the <i>tessellation</i> in both the X and Z directions.
     */
	void setTextureTiling(int dimension);
	
	/**
     * Tiles the texture across the <i>tessellation</i>.
     *
     * @param dimensionX
     *            The amount of times to tile the texture across the <i>tessellation</i> in the X direction.
     *            
     * @param dimensionZ
     *            The amount of times to tile the texture across the <i>tessellation</i> in the Z direction.
     */
	void setTextureTiling(int dimensionX, int dimensionZ);
	
	/**
     * Tiles the texture across the <i>tessellation</i>.
     *
     * @param dimension
     *            The amount of times to tile the texture across the <i>tessellation</i> in the X direction.
     */
	void setTextureTilingX(int dimension);
	
	/**
     * Tiles the texture across the <i>tessellation</i>.
     * 
     * @param dimension
     *            The amount of times to tile the texture across the <i>tessellation</i> in the Z direction.
     */
	void setTextureTilingZ(int dimention);
	
	/**
     * Tiles the height map across the <i>tessellation</i>.
     *
     * @param dimension
     *            The amount of times to tile the height map across the <i>tessellation</i> in both the X and Z directions.
     */
	void setHeightMapTiling(int dimension);
	
	/**
     * Tiles the height map across the <i>tessellation</i>.
     *
     * @param dimensionX
     *            The amount of times to tile the height map across the <i>tessellation</i> in the X direction.
     *            
     * @param dimensionZ
     *            The amount of times to tile the height map across the <i>tessellation</i> in the Z direction.
     */
	void setHeightMapTiling(int dimensionX, int dimensionZ);
	
	/**
     * Tiles the height map across the <i>tessellation</i>.
     *
     * @param dimension
     *            The amount of times to tile the height map across the <i>tessellation</i> in the X direction.
     */
	void setHeightMapTilingX(int dimension);
	
	/**
     * Tiles the height map across the <i>tessellation</i>.
     *
     * @param dimension
     *            The amount of times to tile the height map across the <i>tessellation</i> in the Z direction.
     */
	void setHeightMapTilingZ(int dimention);
	
	/**
     * Tiles the normal map across the <i>tessellation</i>.
     *
     * @param dimension
     *            The amount of times to tile the normal map across the <i>tessellation</i> in both the X and Z directions.
     */
	void setNormalMapTiling(int dimension);
	
	/**
     * Tiles the normal map across the <i>tessellation</i>.
     *
     * @param dimensionX
     *            The amount of times to tile the normal map across the <i>tessellation</i> in the X direction.
     *            
     * @param dimensionZ
     *            The amount of times to tile the normal map across the <i>tessellation</i> in the Z direction.
     */
	void setNormalMapTiling(int dimensionX, int dimensionZ);
	
	/**
     * Tiles the normal map across the <i>tessellation</i>.
     *
     * @param dimension
     *            The amount of times to tile the normal map across the <i>tessellation</i> in the X direction.
     */
	void setNormalMapTilingX(int dimension);
	
	/**
     * Tiles the normal map across the <i>tessellation</i>.
     *
     * @param dimension
     *            The amount of times to tile the normal map across the <i>tessellation</i> in the Z direction.
     */
	void setNormalMapTilingZ(int dimention);
	
	/**
	 * Retrieves the current amount of times the texture is tiled in the X dimension.
	 * 
	 * @return The number of times the texture tiled in the X direction.
	 */
	int getTextureTilingX();
	
	/**
	 * Retrieves the current amount of times the texture is tiled in the Z dimension.
	 * 
	 * @return The number of times the texture tiled in the Z direction.
	 */
	int getTextureTilingZ();
	
	/**
	 * Retrieves the current amount of times the height map is tiled in the X dimension.
	 * 
	 * @return The number of times the height map tiled in the X direction.
	 */
	int getHeightMapTilingX();
	
	/**
	 * Retrieves the current amount of times the height map is tiled in the Z dimension.
	 * 
	 * @return The number of times the height map tiled in the Z direction.
	 */
	int getHeightMapTilingZ();
	
	/**
	 * Retrieves the current amount of times the height map is tiled in the X dimension.
	 * 
	 * @return The number of times the height map tiled in the X direction.
	 */
	int getNormalMapTilingX();
	
	/**
	 * Retrieves the current amount of times the normal map is tiled in the Z dimension.
	 * 
	 * @return The number of times the normal map tiled in the Z direction.
	 */
	int getNormalMapTilingZ();
	
	/**
     * Sets the specified {@link Texture texture} onto the <i>tessellation</i>.
     *
     * @param eng
     *            The {@link Engine engine}.
     * @param texPath
     *            The filename of the texture (e.g. <i>"default.png"</i>).
     * @throws NullPointerException
     *             If any of the arguments is <code>null</code>.
     */
	void setTexture(Engine eng, String texPath);
	
	/**
     * Sets the specified {@link Texture texture} onto the <i>tessellation</i>
     * as a height map.
     * <p>
     * The <i>red</i> pixel data values are used to determine height.
     * <p>
     * modifying the <i>tessellation</i> <code>multiplier</code> variable may
     * be used to adjust the extremeties of the heights.
     *
     * @param eng
     *            The {@link Engine engine}.
     * @param texPath
     *            The filename of the texture (e.g. <i>"default.png"</i>).
     * @throws NullPointerException
     *             If any of the arguments is <code>null</code>.
     */
	void setHeightMap(Engine eng, String texPath);
	
	/**
     * Sets the specified {@link Texture texture} onto the <i>tessellation</i>
     * as a normal map.
     *
     * @param eng
     *            The {@link Engine engine}.
     * @param texPath
     *            The filename of the texture (e.g. <i>"default.png"</i>).
     * @throws NullPointerException
     *             If any of the arguments is <code>null</code>.
     */
	void setNormalMap(Engine eng, String texPath);
	
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
     * Gets the {@link Texture texture} currently being used on the tessellation.
     * If a texture has not been supplied, it will return the default texture
     * <code>default.png</code>.
     *
     * @return The {@link Texture texture}
     * @throws NullPointerException
     *             If the {@link Texture texture} is <code>null</code>.
     */
	Texture getTexture();
	
	/**
     * Gets the {@link Texture texture} currently being used on the <i>tessellation</i>
     * as a height map. If a height map has not been supplied, it will return 
     * the default texture <code>default.png</code>.
     *
     * @return The {@link Texture texture}
     * @throws NullPointerException
     *             If the {@link Texture texture} is <code>null</code>.
     */
	Texture getHeighMap();
	
	/**
     * Gets the {@link Texture texture} currently being used on the <i>tessellation</i>
     * as a normal map. If a normal map has not been supplied, it will return 
     * the default texture <code>default.png</code>.
     *
     * @return The {@link Texture texture}
     * @throws NullPointerException
     *             If the {@link Texture texture} is <code>null</code>.
     */
	Texture getNormalMap();
	
	/**
     * Removes the {@link Texture texture} currently being used on the <i>tessellation</i>,
     * and replaces it with the default texture <code>default.png</code>.
     *
     * @param eng
     *            The {@link Engine engine}.
     * @throws NullPointerException
     *             If any of the arguments is <code>null</code>.
     */
	void removeTexture(Engine eng);
	
	/**
     * Removes the {@link Texture texture} currently being used on the <i>tessellation</i>
     * as a height map, and replaces it with the default texture <code>default.png</code>.
     * <p>
     * When this is called, the tessellation's height multiplier is automatically set
     * to zero.
     *
     * @param eng
     *            The {@link Engine engine}.
     * @throws NullPointerException
     *             If any of the arguments is <code>null</code>.
     */
	void removeHeightMap(Engine eng);
	
	/**
     * Removes the {@link Texture texture} currently being used on the <i>tessellation</i>
     * as a normal map, and replaces it with the default texture <code>default.png</code>.
     * <p>
     * When this is called, the <i>tessellation</i> will switch to estimating normals in the
     * <code>tessellation shaders</code>.
     *
     * @param eng
     *            The {@link Engine engine}.
     * @throws NullPointerException
     *             If any of the arguments is <code>null</code>.
     */
	void removeNormalMap(Engine eng);
	
	/**
     * Sets the {@link GpuShaderProgram gpu-shader-program} responsible for
     * rendering the {@link TessellationBody tessellation body}.
     *
     * @param program
     *            The {@link GpuShaderProgram program}.
     * @throws NullPointerException
     *             If the {@link GpuShaderProgram program} is <code>null</code>.
     */
	void setGpuShaderProgram(GpuShaderProgram program);
	
	void setDepthShaderProgram(GpuShaderProgram program);

	/**
     * Gets the {@link TessellationBody tessellation body} associated with this
     * <i>tessellation</i>.
     *
     * @return {@link TessellationBody tessellation body}
     *            The {@link Renderable renderable} {@link TessellationBody tessellation body}.
     * @throws NullPointerException
     *             If the {@link TessellationBody tessellation body} is <code>null</code>.
     */
	Renderable getTessellationBody();
	
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
     * Returns the height of a particular point on the <i>tessellation</i> in world space.
     * <p>
     * Because <i>tessellations</i> are attached to {@Link SceneNode scene nodes} just like 
     * other {@Link Renderable renderable} objects, they can be transformed arbitrarily within 
     * certain limitations. This method will convert global X and Z coordinates into local X 
     * and Z coordinates relative to the <i>tessellation</i>.
     * <p>
     * It is permitted to supply coordinates that do not land on the tessellated plane.
     * If clients supply a coordinates that do not intersect the plane on the global Y-Axis,
     * the closest points on the <i>tessellation</i> will be used.
     * <p>
     * The height is determined by identifying the pixel (from the height map) associated with
     * the supplied coordinates. It is then averaged (with weights) with the 3 closest pixels.
     * <p>
     * For some examples: If the supplied coordinates happen to perfectly land in the center of a 
     * pixel from the height map, 100% of the returned amount will come from that one pixel. If the
     * supplied coordinate lands perfectly between 2 pixels, each pixel will contribute 50% of its
     * height. If the supplied coordinates land mostly on one pixel, but is not perfectly centered,
     * it will take weighted averages from the closest lateral pixel, the closest longitudinal pixel,
     * and the closest diagonal pixel. If a neighboring pixel does not exist (such as on the edges of
     * a tessellated plane), the closest pixel on the edge is used.
     * <p>
     * It is possible for 1 pixel to be used for several points on the <i>tessellation</i>.
     * <p>
     * Clients should take care to avoid making height queries along the edges of a tessellated plane.
     * <p>
     * Clients should take care to avoid pitching or rolling the {@Link SceneNode scene node} the 
     * <i>tessellation</i> is attached to, as this will disturb the accuracy of the (X, Z) coordinate conversions.
     * Yawing is allowed, and negative scaling is allowed, but clients should keep in mind that if any
     * negative scaling is done on the X-Axis, it must <i>also</i> be done on the Z-Axis (and vice versa).
     * Negative scaling on the Y-Axis can be done independently). Any arbitrary translation is permitted.
     * <p>
     * The amount returned is calculated as follows: <br> 
     * The weighted averages of the <i>red</i> values from the height map... <br>
     * ...multiplied by the multiplier... <br>
     * ...multiplied by the tessellation's {@Link SceneNode scene node's} scaling in the Y direction... <br>
     * ...with all of the above added to the tessellation's {@Link SceneNode scene node's} global Y position.
     *
     * @param globalX
     * 			Any arbitrary X coordinate that exists in the game's world, preferably one located near the <i>tessellation</i>.
     * @param globalZ
     * 			Any arbitrary Z coordinate that exists in the game's world, preferably one located near the <i>tessellation</i>.
     * @return height
     * 			The Y coordinate in global space.
     */
	float getWorldHeight(float globalX, float globalZ);
	
	/**
     * Returns the height of a particular point on the <i>tessellation</i>.
     * <p>
     * If the bottom left of a <i>tessellation</i> is considered to be (0, 0) and the top right is
     * considered to be (1, 1), (X, Z) coordinates can be supplied to query a particular point
     * on the tessellated terrain to determine the Y coordinate.
     * <p>
     * It is permitted to supply coordinates that do not land on the tessellated plane.
     * If clients supply a coordinate value less than zero, it will be set to zero.
     * Likewise, if a supplied coordinate is greater than one, it will be set to one.
     * <p>
     * The height is determined by identifying the pixel (from the height map) associated with
     * the supplied coordinates. It is then averaged (with weights) with the 3 closest pixels.
     * <p>
     * For some examples: If the supplied coordinates happen to perfectly land in the center of a 
     * pixel from the height map, 100% of the returned amount will come from that one pixel. If the
     * supplied coordinate lands perfectly between 2 pixels, each pixel will contribute 50% of its
     * height. If the supplied coordinates land mostly on one pixel, but is not perfectly centered,
     * it will take weighted averages from the closest lateral pixel, the closest longitudinal pixel,
     * and the closest diagonal pixel. If a neighboring pixel does not exist (such as on the edges of
     * a tessellated plane), the closest pixel on the edge is used.
     * <p>
     * It is possible for 1 pixel to be used for several points on the <i>tessellation</i>.
     * <p>
     * Clients should take care to avoid making height queries along the edges of a tessellated plane.
     * <p>
     * The amount returned is calculated as follows: <br> 
     * The weighted averages of the <i>red</i> values from the height map... <br>
     * ...multiplied by the multiplier... <br>
     * ...multiplied by the tessellation's {@Link SceneNode scene node's} scaling in the Y direction... <br>
     * ...with all of the above added to the tessellation's {@Link SceneNode scene node's} global Y position.
     *
     * @param xPercent
     * 			A value between 0.0 to 1.0, 0.5 being the center of the <i>tessellation</i> along the X-Axis.
     * @param zPercent
     * 			A value between 0.0 to 1.0, 0.5 being the center of the <i>tessellation</i> along the Z-Axis.
     * @return height
     * 			The Y coordinate in the tessellation's local space.
     */
	float getAverageHeight(float xPercent, float zPercent);

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
