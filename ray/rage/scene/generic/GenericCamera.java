/**
 * Copyright (C) 2016 Raymond L. Rivera
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

import ray.rage.rendersystem.*;
import ray.rage.scene.*;
import ray.rage.scene.generic.AbstractGenericSceneObject;
import ray.rage.scene.generic.GenericCamera;
import ray.rml.*;

/**
 * A generic {@link Camera camera} implementation.
 *
 * @author Raymond L. Rivera
 *
 */
final class GenericCamera extends AbstractGenericSceneObject implements Camera {

    private Viewport              viewport;
    private Camera.Frustum        frustum;
    private List<Camera.Listener> camListeners = new ArrayList<>();
    
    private Vector3f rt;
    private Vector3f fd;
    private Vector3f up;
    private Vector3f po;
    private char mode = 'c';

    /**
     * Creates a new {@link Camera camera} with the specified parent
     * {@link SceneManager manager}, name, and {@link Camera.Frustum
     * viewing-frustum}.
     *
     * @param manager
     *            The parent {@link SceneManager manager}.
     * @param camName
     *            The name that identifies the {@link Camera camera}.
     * @param viewFrustum
     *            The viewing {@link Camera.Frustum frustum}.
     * @throws NullPointerException
     *             If any of the arguments is <code>null</code>.
     * @throws IllegalArgumentException
     *             If the name is empty.
     */
    GenericCamera(SceneManager manager, String camName, Camera.Frustum viewFrustum) {
        super(manager, camName);
        if (viewFrustum == null)
            throw new NullPointerException("Null " + Frustum.class.getSimpleName());

        frustum = viewFrustum;
        frustum.notifyCamera(this);
        setVisible(false);
    }

    @Override
    public Camera.Frustum getFrustum() {
        return frustum;
    }

    @Override
    public void renderScene() {
        if (viewport == null)
            throw new IllegalStateException(Viewport.class.getSimpleName() + " not set");

        for (Camera.Listener cl : camListeners)
            cl.onCameraPreRenderScene(this);

        getManager().notifyRenderScene(this, viewport);

        for (Camera.Listener cl : camListeners)
            cl.onCameraPostRenderScene(this);
    }

    @Override
    public void notifyViewport(Viewport vp) {
        viewport = vp;
    }

    @Override
    public Viewport getViewport() {
        return viewport;
    }

    @Override
    public void addListener(Camera.Listener listener) {
        if (listener == null)
            throw new NullPointerException("Null " + Camera.Listener.class.getSimpleName());

        if (camListeners.contains(listener))
            throw new RuntimeException(Camera.Listener.class.getSimpleName() + " already added");

        camListeners.add(listener);
    }

    @Override
    public void removeListener(Camera.Listener listener) {
        camListeners.remove(listener);
    }

    @Override
    public void notifyDispose() {
        // do not ask the scene manager to destroy this camera from here;
        // the manager is expected to invoke this method and it'll cause an
        // infinite callback loop
        if (viewport != null)
            viewport.setCamera(null);

        camListeners.clear();
        super.notifyDispose();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        Node parentNode = getParentNode();

        sb.append(GenericCamera.class.getSimpleName() + "=" + getName());
        sb.append(": Projection=" + frustum.getProjection());
        
        if (parentNode != null) {
          // sb.append(", Position=" + parentNode.getWorldPosition());
          // sb.append(", ViewDirection=" + parentNode.getWorldForwardAxis());
          // sb.append(", UpDirection=" + parentNode.getWorldUpAxis());
            
            sb.append("\n Position= " + po.x() + " " + po.y() + " " + po.z());
            sb.append("\n ViewDirection= " + fd.x() + " " + fd.y() + " " + fd.z());
            sb.append("\n UpDirection= " + up.x() + " " + up.y() + " " + up.z());
            sb.append("\n RtDirection= " + rt.x() + " " + rt.y() + " " + rt.z());   
        }
        sb.append("\n FoV-Y=" + frustum.getFieldOfViewY());
        sb.append(", Ratio=" + frustum.getAspectRatio());
        sb.append(", NearClip=" + frustum.getNearClipDistance());
        sb.append(", FarClip=" + frustum.getFarClipDistance());

        return sb.toString();
    }
    
    
    public void setFd(Vector3f v)
    {  	fd = v;
    }
    public void setRt(Vector3f v)
    {  	rt = v;
    }
    public void setUp(Vector3f v)
    {  	up = v;
    }
    public void setPo(Vector3f v)
    {	po = v;
    }
    public void setMode(char m)
    {	mode = m;
    }
    public Vector3f getFd()
    {  	return fd;
    }
    public Vector3f getRt()
    {  	return rt;
    }
    public Vector3f getUp()
    {  	return up;
    }
    public Vector3f getPo()
    {	return po;
    }
    public char getMode()
    {	return mode;
    }
}
