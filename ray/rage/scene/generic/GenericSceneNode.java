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

import ray.physics.PhysicsObject;
import ray.rage.math.*;
import ray.rage.rendersystem.*;
import ray.rage.scene.*;
import ray.rage.scene.generic.GenericSceneNode;
import ray.rml.*;

/**
 * A generic implementation of a {@link SceneNode scene-node} for use with the
 * {@link SceneManager scene-manager}. This implementation makes no assumptions
 * about how the space in a scene is partitioned, if at all.
 * <p>
 * Note that implementations may cause a subtle degree of (unavoidable) coupling
 * with {@link RenderSystem render-system} implementations, specifically related
 * to the internal details of the coordinate systems they use.
 * <p>
 * For example, +Z/+X axes in OpenGL and Vulkan point in the same direction.
 * However, Vulkan's +Y axis points down the screen, which is the opposite of
 * OpenGL. Details like these should be dealt with in the {@link RenderSystem
 * render-system} implementations and their shaders, <i>not</i> in the
 * {@link Node nodes} or the {@link SceneManager scene-manager}.
 *
 * @author Raymond L. Rivera
 *
 */
final class GenericSceneNode implements SceneNode {

    private String                   name;
    private SceneManager             sceneManager;
    private Node                     parentNode;
    private Node.Listener            nodeListener;
    private PhysicsObject			 physicsObject;
	
    // local transforms are relative to the parent
    // world transforms are derived by accumulating transforms from all parents
    // and are relative to the world's origin
    private Transform                localTransform  = Transform.createDefault();
    private Transform                worldTransform  = Transform.createDefault();

    private boolean                  isInSceneGraph  = false;
    private boolean                  parentOutOfSync = true;

    // The LHM implementations are used to guarantee that keys are always
    // returned in the order in which they were inserted; this makes the
    // getAttachedObject(index) and getChild(index) methods predictable
    private Map<String, SceneObject> sceneObjMap     = new LinkedHashMap<>();
    private Map<String, Node>        childNodesMap   = new LinkedHashMap<>();

    /**
     * Creates a new {@link SceneNode scene-node} with the given
     * {@link SceneManager manager} and name.
     *
     * @param sm
     *            The parent {@link SceneManager manager}
     * @param nodeName
     *            The name to identify <code>this</code> {@link SceneNode
     *            scene-node}.
     * @throws NullPointerException
     *             If either argument is <code>null</code>.
     * @throws IllegalArgumentException
     *             If the name is empty.
     */
    GenericSceneNode(SceneManager sm, String nodeName) {
        if (sm == null)
            throw new NullPointerException("Null " + SceneManager.class.getSimpleName());

        if (nodeName.isEmpty())
            throw new IllegalArgumentException("Empty name");

        sceneManager = sm;
        name = nodeName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SceneManager getManager() {
        return sceneManager;
    }

    @Override
    public Node createChildNode(String name) {
        return createChildSceneNode(name);
    }

    @Override
    public SceneNode createChildSceneNode(String name) {
        SceneNode sn = sceneManager.createSceneNode(name);
        attachChild(sn);
        return sn;
    }

    @Override
    public void setLocalPosition(float x, float y, float z) {
        setLocalPosition(Vector3f.createFrom(x, y, z));
    }

    @Override
    public void setLocalPosition(Vector3 pv) {
        localTransform.setPosition(pv);
        updateWorldPosition();
        parentOutOfSync = true;
    }

    @Override
    public Vector3 getLocalPosition() {
        return localTransform.position();
    }

    @Override
    public void setLocalScale(float x, float y, float z) {
        setLocalScale(Vector3f.createFrom(x, y, z));
    }

    @Override
    public void setLocalScale(Vector3 sv) {
        localTransform.setScale(sv);
        updateWorldScale();
        parentOutOfSync = true;
    }

    @Override
    public Vector3 getLocalScale() {
        return localTransform.scale();
    }

    @Override
    public void setLocalRotation(Matrix3 rm) {
        localTransform.setRotation(rm);
        updateWorldRotation();
        parentOutOfSync = true;
    }

    @Override
    public Matrix3 getLocalRotation() {
        return localTransform.rotation();
    }

    @Override
    public Matrix4 getLocalTransform() {
        return localTransform.toMatrixTRS();
    }

    @Override
    public Vector3 getWorldPosition() {
        return worldTransform.position();
    }

    @Override
    public Matrix3 getWorldRotation() {
        return worldTransform.rotation();
    }

    @Override
    public Vector3 getWorldScale() {
        return worldTransform.scale();
    }

    @Override
    public Matrix4 getWorldTransform() {
        return worldTransform.toMatrixTRS();
    }

    @Override
    public void moveForward(float offset) {
        translate(getWorldForwardAxis().mult(offset));	// switched by scott
    }

    @Override
    public void moveBackward(float offset) {
        translate(getWorldForwardAxis().mult(-offset));  // switched by scott
    }

    @Override
    public void moveLeft(float offset) {
        translate(getWorldRightAxis().mult(-offset));
    }

    @Override
    public void moveRight(float offset) {
        translate(getWorldRightAxis().mult(offset));
    }

    @Override
    public void moveUp(float offset) {
        translate(getWorldUpAxis().mult(offset));
    }

    @Override
    public void moveDown(float offset) {
        translate(getWorldUpAxis().mult(-offset));
    }

    @Override
    public void translate(float x, float y, float z) {
        translate(Vector3f.createFrom(x, y, z));
    }

    @Override
    public void translate(Vector3 tv) {
        localTransform.setPosition(localTransform.position().add(tv));
        updateWorldPosition();
        parentOutOfSync = true;
    }

    @Override
    public void yaw(Angle angle) {
        rotate(angle, Vector3f.createUnitVectorY());
    }

    @Override
    public void roll(Angle angle) {
        rotate(angle, Vector3f.createUnitVectorZ());
    }

    @Override
    public void pitch(Angle angle) {
        rotate(angle, Vector3f.createUnitVectorX());
    }

    @Override
    public void rotate(Angle angle, Vector3 axis) {
        if (angle == null)
            throw new NullPointerException("Null rotation " + Angle.class.getSimpleName());
        if (axis == null)
            throw new NullPointerException("Null rotation axis " + Vector3.class.getSimpleName());
        
        localTransform.setRotation(localTransform.rotation().rotate(angle, axis));
        updateWorldRotation();
        parentOutOfSync = true;
    }

    @Override
    public void scale(float x, float y, float z) {
        scale(Vector3f.createFrom(x, y, z));
    }

    @Override
    public void scale(Vector3 sv) {
        if (sv == null)
            throw new NullPointerException("Null scaling " + Vector3.class.getSimpleName());

        localTransform.setScale(localTransform.scale().mult(sv));
        updateWorldScale();
        parentOutOfSync = true;
    }

    @Override
    public void lookAt(float x, float y, float z) {
        lookAt(Vector3f.createFrom(x, y, z), Vector3f.createUnitVectorY());
    }

    @Override
    public void lookAt(Vector3 target) {
        lookAt(target, Vector3f.createUnitVectorY());
    }

    @Override
    public void lookAt(Vector3 target, Vector3 up) {
        if (target == null)
            throw new NullPointerException("Null look-at position " + Vector3.class.getSimpleName());
        if (up == null)
            throw new NullPointerException("Null up direction " + Vector3.class.getSimpleName());

        localTransform.setRotation(Matrix4f.createLookAtMatrix(worldTransform.position(), target, up).toMatrix3());
        updateWorldRotation();
        parentOutOfSync = true;
    }

    @Override
    public void lookAt(Node target) {
        lookAt(target, Vector3f.createUnitVectorY());
    }

    @Override
    public void lookAt(Node target, Vector3 up) {
        lookAt(target.getWorldPosition(), up);
    }

    @Override
    public Vector3 getLocalRightAxis() {
        return localTransform.rotation().column(0);  // scott
    }

    @Override
    public Vector3 getLocalUpAxis() {
        return localTransform.rotation().column(1);
    }

    @Override
    public Vector3 getLocalForwardAxis() {
        return localTransform.rotation().column(2); // scott
    }

    @Override
    public Vector3 getWorldRightAxis() {
        return worldTransform.rotation().column(0);  // scott
    }

    @Override
    public Vector3 getWorldUpAxis() {
        return worldTransform.rotation().column(1);
    }

    @Override
    public Vector3 getWorldForwardAxis() {
        return worldTransform.rotation().column(2); // scott
    }

    @Override
    public Node getParent() {
        return parentNode;
    }

    @Override
    public void attachChild(Node child) {
        if (child == null)
            throw new NullPointerException("Null child " + Node.class.getSimpleName());

        if (childNodesMap.containsKey(child.getName()))
            throw new RuntimeException(child.getName() + " is already a child of " + getName());

        childNodesMap.put(child.getName(), child);
        child.notifyAttached(this);
    }

    @Override
    public Node getChild(String childName) {
        Node child = childNodesMap.get(childName);

        if (child == null)
            throw new RuntimeException(childName + " is not a child of " + getName());

        return child;
    }

    @Override
    public Node getChild(int index) {
        String name = new ArrayList<>(childNodesMap.keySet()).get(index);
        return childNodesMap.get(name);
    }

    @Override
    public int getChildCount() {
        return childNodesMap.size();
    }

    @Override
    public Iterable<Node> getChildNodes() {
        return childNodesMap.values();
    }

    @Override
    public void detachChild(Node child) {
        if (child == null)
            throw new NullPointerException("Null child " + Node.class.getSimpleName());

        childNodesMap.remove(child.getName());
        child.notifyDetached();
    }

    @Override
    public void detachAllChildren() {
        // avoid detachChild(Node) to avoid ConcurrentModificationException
        for (Node cn : childNodesMap.values())
            cn.notifyDetached();

        childNodesMap.clear();
    }

    @Override
    public void notifyAttached(Node newParent) {
        if (newParent == null)
            throw new NullPointerException("Null parent " + Node.class.getSimpleName());

        if (parentNode != newParent) {
            notifyDetached();
            parentNode = newParent;
            try {
                notifyInSceneGraph(((SceneNode) newParent).isInSceneGraph());
            } catch (ClassCastException e) {
                notifyInSceneGraph(false);
            }
            emitNodeAttached(this, newParent);
            parentOutOfSync = true;
        }
    }

    @Override
    public void notifyDetached() {
        if (parentNode != null) {
            // a tmp ref to the parent is used so that this node's parent can be
            // made null while at the same time allowing us to report who the
            // parent used to be; by the time the listener is notified, this
            // node must already look detached
            Node oldParent = parentNode;
            parentNode = null;
            notifyInSceneGraph(false);
            emitNodeDetached(this, oldParent);
            parentOutOfSync = true;
        }
    }

    @Override
    public void notifyRootNode() {
        if (parentNode != null)
            throw new RuntimeException(name + " is a child of '" + parentNode.getName() + "' and cannot be root");

        isInSceneGraph = true;
        parentOutOfSync = true;
    }

    @Override
    public void update() {
        update(true, parentOutOfSync);
    }

    @Override
    public void update(boolean updateChildren, boolean parentHasChanged) {
        // account for previous changes the caller may not be accounting for
        boolean updateRequired = parentHasChanged || parentOutOfSync;

        if (updateRequired)
            updateFromParent();

        if (updateChildren)
            for (Node n : childNodesMap.values())
                n.update(updateChildren, updateRequired);

        emitNodeUpdated(this);
    }

    @Override
    public void updateFromParent() {
        updateWorldPosition();
        updateWorldRotation();
        updateWorldScale();
        parentOutOfSync = false;
    }

    @Override
    public boolean isInSceneGraph() {
        return isInSceneGraph;
    }

    @Override
    public void notifyInSceneGraph(boolean inGraph) {
        if (isInSceneGraph != inGraph) {
            isInSceneGraph = inGraph;
            for (Node n : childNodesMap.values()) {
                try {
                    SceneNode sn = (SceneNode) n;
                    sn.notifyInSceneGraph(inGraph);
                } catch (ClassCastException e) {
                    // pass
                }
            }
        }
    }

    @Override
    public void attachObject(SceneObject obj) {
        if (obj == null)
            throw new NullPointerException("Null " + SceneObject.class.getSimpleName());

        if (sceneObjMap.containsKey(obj.getName()))
            throw new RuntimeException(obj.getName() + " is already a attached to " + getName());

        sceneObjMap.put(obj.getName(), obj);
        obj.notifyAttached(this);
    }

    @Override
    public SceneObject getAttachedObject(String name) {
        SceneObject obj = sceneObjMap.get(name);

        if (obj == null)
            throw new RuntimeException(name + " is not attached to " + getName());

        return obj;
    }

    @Override
    public SceneObject getAttachedObject(int index) {
        String name = new ArrayList<>(sceneObjMap.keySet()).get(index);
        return sceneObjMap.get(name);
    }

    @Override
    public int getAttachedObjectCount() {
        return sceneObjMap.size();
    }

    @Override
    public Iterable<SceneObject> getAttachedObjects() {
        return sceneObjMap.values();
    }

    @Override
    public void detachObject(SceneObject obj) {
        if (obj == null)
            throw new NullPointerException("Null " + SceneObject.class.getSimpleName());

        detachObject(obj.getName());
    }

    @Override
    public SceneObject detachObject(String name) {
        // although .remove(..) returning null does not necessarily mean that
        // no mapping existed, we can assume this to be the case b/c we don't
        // allow null objects to be added in the first place; see
        // #attachObject(SceneObject)
        SceneObject obj = sceneObjMap.remove(name);

        if (obj == null)
            throw new RuntimeException(name + " is not attached to " + getName());

        obj.notifyDetached();
        return obj;
    }

    @Override
    public void detachAllObjects() {
        // avoid detachObject(SceneObject) to avoid
        // ConcurrentModificationException
        for (SceneObject obj : sceneObjMap.values())
            obj.notifyDetached();

        sceneObjMap.clear();
    }
	
    public PhysicsObject getPhysicsObject() {
    	return this.physicsObject;
    }
    
    public void setPhysicsObject(PhysicsObject physicsObject) {
    	this.physicsObject = physicsObject;
    }
	
    @Override
    public void setListener(Node.Listener listener) {
        // allow nulls so that callers can unregister
        nodeListener = listener;
    }

    @Override
    public Node.Listener getListener() {
        return nodeListener;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(GenericSceneNode.class.getSimpleName() + "=" + name);
        sb.append(": LocalPosition=" + localTransform.position());
        sb.append(", LocalForward=" + getLocalForwardAxis());
        sb.append(", LocalRight=" + getLocalRightAxis());
        sb.append(", LocalUp=" + getLocalUpAxis());
        sb.append(", LocalScale=" + localTransform.scale());
        sb.append(", WorldPosition=" + worldTransform.position());
        sb.append(", WorldForward=" + getWorldForwardAxis());
        sb.append(", WorldRight=" + getWorldRightAxis());
        sb.append(", WorldUp=" + getWorldUpAxis());
        sb.append(", WorldScale=" + worldTransform.scale());
        sb.append(", InGraph=" + (isInSceneGraph ? "True" : "False"));
        sb.append(", Parent=" + (parentNode != null ? parentNode.getName() : "N/A"));

        return sb.toString();
    }

    private void updateWorldPosition() {
        // @formatter:off
        // worldPosition = parent.worldPosition + parent.worldRotation * (parent.worldScale * localPosition)
        // @formatter:on
        if (parentNode != null) {
            Vector3 scaledPos = parentNode.getWorldScale().mult(localTransform.position());
            Vector3 worldPos = parentNode.getWorldPosition().add(parentNode.getWorldRotation().mult(scaledPos));
            worldTransform.setPosition(worldPos);
        } else {
            worldTransform.setPosition(localTransform.position());
        }
    }

    private void updateWorldRotation() {
        // worldRotation = parent.worldRotation * localRotation
        if (parentNode != null)
            worldTransform.setRotation(parentNode.getWorldRotation().mult(localTransform.rotation()));
        else
            worldTransform.setRotation(localTransform.rotation());
    }

    private void updateWorldScale() {
        // worldScale = parent.worldScale * localScale
        if (parentNode != null)
            worldTransform.setScale(parentNode.getWorldScale().mult(localTransform.scale()));
        else
            worldTransform.setScale(localTransform.scale());
    }

    private void emitNodeAttached(Node child, Node newParent) {
        if (nodeListener != null)
            nodeListener.onNodeAttached(child, newParent);
    }

    private void emitNodeUpdated(Node n) {
        if (nodeListener != null)
            nodeListener.onNodeUpdated(n);
    }

    private void emitNodeDetached(Node orphan, Node oldParent) {
        if (nodeListener != null)
            nodeListener.onNodeDetached(orphan, oldParent);
    }

}
