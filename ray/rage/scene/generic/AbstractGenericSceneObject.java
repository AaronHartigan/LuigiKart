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

import ray.rage.scene.*;

/**
 * An abstract base implementation of a {@link SceneObject}.
 *
 * @author Raymond L. Rivera
 *
 */
abstract class AbstractGenericSceneObject implements SceneObject {

    private SceneManager         sceneManager;
    private String               name;

    private boolean              isVisible       = true;

    private SceneNode            parentSceneNode = null;

    private SceneObject.Listener sceneListener   = null;

    /**
     * Constructs a new instance with the given parent {@link SceneManger
     * manager} and name.
     *
     * @param manager
     *            The {@link SceneManger manager} that created
     *            <code>this</code>.
     * @param objectName
     *            The name for <code>this</code>.
     * @throws NullPointerException
     *             If the {@link SceneManger manager} is <code>null</code>
     * @throws IllegalArgumentException
     *             If the name is empty.
     */
    AbstractGenericSceneObject(SceneManager manager, String objectName) {
        if (manager == null)
            throw new NullPointerException("Null " + SceneManager.class.getSimpleName());
        if (objectName.isEmpty())
            throw new IllegalArgumentException("Name is empty");

        name = objectName;
        sceneManager = manager;
    }

    @Override
    public SceneManager getManager() {
        return sceneManager;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Node getParentNode() {
        return parentSceneNode;
    }

    @Override
    public SceneNode getParentSceneNode() {
        return parentSceneNode;
    }

    @Override
    public void notifyAttached(SceneNode newParent) {
        if (newParent == null)
            throw new NullPointerException("Null parent " + SceneNode.class.getSimpleName());

        if (parentSceneNode != newParent) {
            // do not call notifyDetached() here; parent scene node is
            // responsible for notifying object
            detachFromParent();
            parentSceneNode = newParent;

            if (sceneListener != null)
                sceneListener.onObjectAttached(this);
        }
    }

    @Override
    public void notifyDetached() {
        if (isAttached()) {
            if (sceneListener != null)
                sceneListener.onObjectDetached(this);

            parentSceneNode = null;
        }
    }

    @Override
    public boolean isAttached() {
        return parentSceneNode != null;
    }

    @Override
    public void detachFromParent() {
        // the parent node is the one responsible for calling notifyDetached()
        // on this object, so we avoid invoking/repeating the logic here; see
        // javadocs
        if (isAttached())
            parentSceneNode.detachObject(this);
    }

    @Override
    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    @Override
    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public boolean isInScene() {
        return isAttached() && parentSceneNode.isInSceneGraph();
    }

    @Override
    public void setListener(SceneObject.Listener listener) {
        sceneListener = listener;
    }

    @Override
    public SceneObject.Listener getListener() {
        return sceneListener;
    }

    @Override
    public void notifyDispose() {
        detachFromParent();
        setListener(null);
        sceneManager = null;
        name = null;
    }

}
