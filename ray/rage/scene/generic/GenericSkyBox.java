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

package ray.rage.scene.generic;

import java.util.*;

import ray.rage.asset.mesh.*;
import ray.rage.asset.texture.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.rendersystem.shader.*;
import ray.rage.rendersystem.states.*;
import ray.rage.scene.*;
import ray.rage.scene.generic.AbstractGenericSceneObject;
import ray.rage.scene.generic.GenericManualObject;
import ray.rage.util.*;

/**
 * A generic {@link SkyBox} implementation.
 *
 * @author Raymond L. Rivera
 *
 */
// Keep these outside javadocs.
//
// TODO: Refactor so that clients can specify sky-box size, are required
// to attach them to the same scene node as the camera, and not have to
// transform textures.
//
// NOTE: This implementation currently needs more work, specifically
// because the textures need to be transformed/modified by the clients, which is
// not desirable. There's also an inconsistency in that, due to how the shader
// is written, the box as a scene object does not really need to be attached to
// a scene node at this time before being part of the scene.
final class GenericSkyBox extends AbstractGenericSceneObject implements SkyBox {

    private ManualObject manualObj;

    /**
     * Creates a new {@link SkyBox sky-box} with the given {@link SceneManager
     * manager} and name.
     *
     * @param sm
     *            The parent {@link SceneManager manager}
     * @param name
     *            The name to identify <code>this</code> {@link SkyBox sky-box}.
     * @throws NullPointerException
     *             If either argument is <code>null</code>.
     * @throws IllegalArgumentException
     *             If the name is empty.
     */
    GenericSkyBox(SceneManager sm, String name) {
        super(sm, name);
        if (sm == null)
            throw new NullPointerException("Null " + SceneManager.class.getSimpleName());

        RenderSystem rs = sm.getRenderSystem();
        ZBufferState zstate = (ZBufferState) rs.createRenderState(RenderState.Type.ZBUFFER);
        TextureState tstate = (TextureState) rs.createRenderState(RenderState.Type.TEXTURE);
        zstate.setTestEnabled(false);
        tstate.setTarget(TextureState.Target.CUBE_MAP);

        // Do NOT use the SceneManager to directly create this ManualObject;
        // it's an implementation detail of the SkyBox the Manager must not
        // know about. Otherwise, the SM will place the ManualObject into its
        // own map and cause the destruction of internal SkyBox components
        // from the outside, violating the protections that encapsulation
        // provides and introducing a bug. In addition, this prevents the SkyBox
        // from being added twice to the RenderQueue, b/c the SM adds the SkyBox
        // and all the ManualObjects it knows about separately to the
        // queues, which would add this twice.
        Mesh mesh = sm.getMeshManager().createManualAsset(name + Mesh.class.getSimpleName());
        mesh.createSubMesh(name + SubMesh.class.getSimpleName());
        manualObj = new GenericManualObject(sm, "Manual" + name, mesh);
        manualObj.createManualSection("Manual" + name + "Section");

        // INFO: The render system needs to know exactly how many vertices it
        // needs to render for glDrawArrays, but the vertices are hard-coded in
        // the skybox.vert shader for simplicity, so we simply make sure the
        // size of the vertex buffer matches the number of hard-coded vertices
        // without caring for the actual values b/c don't get used.
        manualObj.setVertexBuffer(BufferUtil.directFloatBuffer(4));
        manualObj.setPrimitive(Primitive.TRIANGLE_STRIP);
        manualObj.setDataSource(DataSource.VERTEX_BUFFER);
        manualObj.setRenderState(zstate);
        manualObj.setRenderState(tstate);
    }

    @Override
    public void setTexture(Texture tex, Face face) {
        if (tex == null)
            throw new NullPointerException("Null " + Texture.class.getSimpleName());
        if (face == null)
            throw new NullPointerException("Null " + Face.class.getSimpleName());

        ManualObjectSection sec = manualObj.getManualSection(0);
        TextureState tstate = (TextureState) sec.getRenderState(RenderState.Type.TEXTURE);
        tstate.setTexture(tex, face.value());
    }

    @Override
    public Texture getTexture(Face face) {
        if (face == null)
            throw new NullPointerException("Null " + Face.class.getSimpleName());

        ManualObjectSection sec = manualObj.getManualSection(0);
        TextureState tstate = (TextureState) sec.getRenderState(RenderState.Type.TEXTURE);
        return tstate.getTexture(face.value());
    }

    @Override
    public Iterable<Renderable> getFaces() {
        List<Renderable> list = new ArrayList<>(manualObj.getManualSectionCount());
        for (ManualObjectSection mos : manualObj.getManualSections())
            list.add(mos);

        return list;
    }

    @Override
    public void setGpuShaderProgram(GpuShaderProgram program) {
        ManualObjectSection sec = manualObj.getManualSection(0);
        sec.setGpuShaderProgram(program);
    }
    
    @Override
    public void setDepthShaderProgram(GpuShaderProgram program) {
        ManualObjectSection sec = manualObj.getManualSection(0);
        sec.setDepthShaderProgram(program);
    }

    @Override
    public void notifyDispose() {
        manualObj.notifyDispose();
        manualObj = null;
        super.notifyDispose();
    }

}
