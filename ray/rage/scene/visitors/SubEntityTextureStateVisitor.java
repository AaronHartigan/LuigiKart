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

package ray.rage.scene.visitors;

import java.io.*;
import java.util.logging.*;

import ray.rage.asset.material.*;
import ray.rage.asset.mesh.*;
import ray.rage.asset.texture.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.states.*;
import ray.rage.scene.*;
import ray.rage.scene.visitors.SubEntityTextureStateVisitor;
import ray.rage.util.*;

/**
 * A {@link SubEntity.Visitor visitor} that prepares and applies
 * {@link TextureState texture-states} to {@link SubEntity sub-entities}.
 *
 * @author Raymond L. Rivera
 *
 */
public final class SubEntityTextureStateVisitor implements SubEntity.Visitor {

    private static final String TAG    = SubEntityTextureStateVisitor.class.getName();
    private static final Logger logger = Logger.getLogger(TAG);

    private final SceneManager  sceneMgr;

    /**
     * Creates a new {@link SubEntityTextureStateVisitor}.
     *
     * @param sm
     *            The {@link SceneManager scene-manager} that created
     *            <code>this</code> instance.
     * @throws NullPointerException
     *             If the argument is <code>null</code>.
     */
    public SubEntityTextureStateVisitor(SceneManager sm) {
        if (sm == null)
            throw new NullPointerException("Null " + SceneManager.class.getSimpleName());

        sceneMgr = sm;
    }

    @Override
    public void visit(SubEntity subEntity) {
        MaterialManager materialMgr = sceneMgr.getMaterialManager();
        TextureManager textureMgr = sceneMgr.getTextureManager();
        SubMesh subMesh = subEntity.getSubMesh();
        Configuration config = sceneMgr.getConfiguration();

        Material material = getMaterial(materialMgr, subMesh, config);
        Texture texture = getTexture(textureMgr, material, config);

        RenderSystem renderSystem = sceneMgr.getRenderSystem();
        TextureState textureState = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);
        textureState.setTexture(texture);

        subEntity.setMaterial(material);
        subEntity.setRenderState(textureState);
    }

    private Material getMaterial(MaterialManager materialMgr, SubMesh subMesh, Configuration config) {
        Material mat = null;
        try {
            mat = materialMgr.getAssetByPath(subMesh.getMaterialFilename());
        } catch (NullPointerException | IOException e1) {
            logger.log(Level.SEVERE, Material.class.getSimpleName() + " not found: " + subMesh.getMaterialFilename());
            try {
                mat = materialMgr.getAssetByPath(config.valueOf("assets.materials.default"));
            } catch (IOException e2) {
                throw new UncheckedIOException(e2);
            }
        }
        return mat;
    }

    private Texture getTexture(TextureManager textureMgr, Material mat, Configuration config) {
        Texture tex = null;
        try {
            tex = textureMgr.getAssetByPath(mat.getTextureFilename());
        } catch (NullPointerException | IOException e1) {
            logger.log(Level.SEVERE, Texture.class.getSimpleName() + " not found: " + mat.getTextureFilename());
            try {
                tex = textureMgr.getAssetByPath(config.valueOf("assets.textures.default"));
            } catch (IOException e2) {
                throw new UncheckedIOException(e2);
            }
        }
        return tex;
    }

}
