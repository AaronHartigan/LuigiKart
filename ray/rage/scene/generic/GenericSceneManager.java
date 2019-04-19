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

import java.io.*;
import java.nio.file.*;
import java.util.*;

import ray.rage.asset.animation.*;
import ray.rage.asset.material.*;
import ray.rage.asset.mesh.*;
import ray.rage.asset.skeleton.*;
import ray.rage.asset.texture.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.shader.*;
import ray.rage.scene.*;
import ray.rage.scene.generic.GenericAmbientLight;
import ray.rage.scene.generic.GenericCamera;
import ray.rage.scene.generic.GenericEntity;
import ray.rage.scene.generic.GenericLight;
import ray.rage.scene.generic.GenericManualObject;
import ray.rage.scene.generic.GenericPerspectiveFrustum;
import ray.rage.scene.generic.GenericSceneNode;
import ray.rage.scene.generic.GenericSkeletalEntity;
import ray.rage.scene.generic.GenericSkyBox;
import ray.rage.scene.generic.GenericTessellation;
import ray.rage.scene.visitors.*;
import ray.rage.util.*;
import ray.rml.*;

/**
 * Generic {@link SceneManager} implementation.
 * <p>
 * This implementation does <i>not</i>, and is <i>not</i>, intended to apply
 * space partitioning schemes to the scene, and its {@link Camera cameras},
 * {@link SceneNode scene-nodes}, and other concrete types are, likewise,
 * unaware of any information that might allow them to help at optimizing scene
 * management.
 * <p>
 * A generic implementation like this one is meant to perform the most basic
 * operations and optimizations, such as only submitting {@link Renderable
 * renderables} that are inside the {@link Camera camera's} viewing
 * {@link Camera.Frustum frustum} to the {@link RenderSystem rendering-system}
 * and handle different levels of detail.
 * <p>
 * While it does not yet do such things, improvements to this implementation
 * should remain within those bounds, letting other more specialized packages
 * deal with space partition management schemes, etc.
 *
 * @author Raymond L. Rivera
 *
 */
final class GenericSceneManager implements SceneManager {

    private final static String         ROOT_NODE_NAME        = "Generic/SceneRoot";

    private Map<String, Camera>         cameraMap             = new HashMap<>();
    private Map<String, Entity>         entityMap             = new HashMap<>();
    private Map<String, ManualObject>   manualObjMap          = new HashMap<>();
    private Map<String, SceneNode>      sceneNodeMap          = new HashMap<>();
    private Map<String, SkyBox>         skyBoxMap             = new HashMap<>();
    private Map<String, Tessellation>   tessellationMap       = new HashMap<>(); 
    private Map<String, Light>          lightMap              = new HashMap<>();

    private SkyBox                      activeSkyBox;
    private SceneNode                   rootSceneNode;

    private MeshManager                 meshManager;
    private SkeletonManager             skeletonManager;
    private AnimationManager            animationManager;
    private MaterialManager             materialManager;
    private TextureManager              textureManager;
    private Configuration               configuration;

    private RenderSystem                renderSystem;
    private RenderQueue                 renderQueue;
    private List<RenderQueue.Listener>  renderQueueListeners  = new ArrayList<>();
    private List<SceneManager.Listener> sceneManagerListeners = new ArrayList<>();
    private List<Node.Controller>       nodeControllers       = new ArrayList<>();

    // The ambient light is a singleton object, but I avoid using the Singleton
    // Design (Anti-)Pattern to avoid having the equivalent of global variables,
    // hiding dependencies, and other negative side-effects.
    private AmbientLight                ambientLight          = new GenericAmbientLight(this);

    GenericSceneManager() {
        rootSceneNode = createSceneNode(ROOT_NODE_NAME);
        rootSceneNode.notifyRootNode();
    }

    @Override
    public Environment getEnvironment() {
        return Environment.GENERIC;
    }

    @Override
    public Camera createCamera(String name, Camera.Frustum.Projection proj) {
        if (name.isEmpty())
            throw new IllegalArgumentException("Name is empty");
        if (cameraMap.containsKey(name))
            throw new RuntimeException(name + " already exists");

        Camera.Frustum frustum;
        switch (proj) {
            case PERSPECTIVE:
                frustum = new GenericPerspectiveFrustum();
                break;
            default:
                throw new UnsupportedOperationException(proj + " is not yet implemented");
        }
        Camera cam = new GenericCamera(this, name, frustum);
        cameraMap.put(name, cam);
        return cam;
    }

    @Override
    public boolean hasCamera(String name) {
        return cameraMap.containsKey(name);
    }

    @Override
    public Camera getCamera(String name) {
        Camera cam = cameraMap.get(name);
        if (cam == null)
            throw new RuntimeException(Camera.class.getSimpleName() + " not found: " + name);

        return cam;
    }

    @Override
    public Iterable<Camera> getCameras() {
        return cameraMap.values();
    }

    @Override
    public int getCameraCount() {
        return cameraMap.size();
    }

    @Override
    public void destroyCamera(String camName) {
        destroySceneObject(camName, cameraMap);
    }

    @Override
    public void destroyCamera(Camera camera) {
        destroySceneObject(camera.getName(), cameraMap);
    }

    @Override
    public Entity createEntity(String name, String path) throws IOException {
        if (name.isEmpty())
            throw new IllegalArgumentException("Name is empty");
        if (entityMap.containsKey(name))
            throw new RuntimeException(name + " already exists");
        if (path.isEmpty())
            throw new IllegalArgumentException("Path is empty");

        Mesh mesh = meshManager.getAsset(Paths.get(path));
        Entity entity = new GenericEntity(this, name, mesh);

        entity.setGpuShaderProgram(renderSystem.getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
        entity.setDepthShaderProgram(renderSystem.getGpuShaderProgram(GpuShaderProgram.Type.DEPTH));

        entityMap.put(name, entity);

        SubEntity.Visitor depthVisitor = new SubEntityZBufferStateVisitor(this);
        SubEntity.Visitor textureVisitor = new SubEntityTextureStateVisitor(this);
        SubEntity.Visitor faceVisitor = new SubEntityFrontFaceStateVisitor(this);

        entity.visitSubEntities(depthVisitor);
        entity.visitSubEntities(textureVisitor);
        entity.visitSubEntities(faceVisitor);

        return entity;
    }

    public boolean hasEntity(String name) {
        return entityMap.containsKey(name);
    };

    @Override
    public Entity getEntity(String name) {
        Entity ent = entityMap.get(name);
        if (ent == null)
            throw new RuntimeException(Entity.class.getSimpleName() + " not found: " + name);

        return ent;
    }

    @Override
    public Iterable<Entity> getEntities() {
        return entityMap.values();
    }

    @Override
    public int getEntityCount() {
        return entityMap.size();
    }

    @Override
    public void destroyEntity(String name) {
        destroySceneObject(name, entityMap);
    }

    @Override
    public void destroyEntity(Entity e) {
        destroySceneObject(e.getName(), entityMap);
    }

    @Override
    public SkeletalEntity createSkeletalEntity(String name, String meshPath, String skeletonPath) throws IOException
    {
        if (name.isEmpty())
            throw new IllegalArgumentException("Name is empty");
        if (entityMap.containsKey(name))
            throw new RuntimeException(name + " already exists");
        if (meshPath.isEmpty())
            throw new IllegalArgumentException("meshPath is empty");
        if (skeletonPath.isEmpty())
            throw new IllegalArgumentException("skeletonPath is empty");

        Mesh mesh = meshManager.getAsset(Paths.get(meshPath));
        Skeleton skeleton = skeletonManager.getAsset(Paths.get(skeletonPath));
        SkeletalEntity skeletalEntity = new GenericSkeletalEntity(this, name, mesh, skeleton);
        skeletalEntity.setGpuShaderProgram(renderSystem.getGpuShaderProgram(GpuShaderProgram.Type.SKELETAL_RENDERING));
        skeletalEntity.setDepthShaderProgram(renderSystem.getGpuShaderProgram(GpuShaderProgram.Type.DEPTH));
        
        // Giving the skeletal entity a generic material
        // Assigning the material of all submeshes as the default material (as specified in the user's config file)
        for(SubMesh sm : mesh.getSubMeshes())
            sm.setMaterialFilename(getConfiguration().valueOf("assets.materials.default"));

        entityMap.put(name, skeletalEntity);

        SubEntity.Visitor depthVisitor = new SubEntityZBufferStateVisitor(this);
        SubEntity.Visitor textureVisitor = new SubEntityTextureStateVisitor(this);
        SubEntity.Visitor faceVisitor = new SubEntityFrontFaceStateVisitor(this);

        skeletalEntity.visitSubEntities(depthVisitor);
        skeletalEntity.visitSubEntities(textureVisitor);
        skeletalEntity.visitSubEntities(faceVisitor);

        return skeletalEntity;
    }

    @Override
    public ManualObject createManualObject(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("Name is empty");
        if (manualObjMap.containsKey(name))
            throw new RuntimeException(name + " already exists");

        Mesh mesh = meshManager.createManualAsset(name + Mesh.class.getSimpleName());
        ManualObject obj = new GenericManualObject(this, name, mesh);

        manualObjMap.put(name, obj);
        return obj;
    }

    @Override
    public boolean hasManualObject(String name) {
        return manualObjMap.containsKey(name);
    };

    @Override
    public ManualObject getManualObject(String name) {
        ManualObject mo = manualObjMap.get(name);
        if (mo == null)
            throw new RuntimeException(ManualObject.class.getSimpleName() + " not found: " + name);

        return mo;
    }

    @Override
    public Iterable<ManualObject> getManualObjects() {
        return manualObjMap.values();
    }

    @Override
    public int getManualObjectCount() {
        return manualObjMap.size();
    }

    @Override
    public void destroyManualObject(String name) {
        destroySceneObject(name, manualObjMap);
    }

    @Override
    public void destroyManualObject(ManualObject obj) {
        destroySceneObject(obj.getName(), manualObjMap);
    }

    @Override
    public SkyBox createSkyBox(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("Name is empty");
        if (skyBoxMap.containsKey(name))
            throw new RuntimeException(SkyBox.class.getSimpleName() + " already exists: " + name);

        SkyBox sb = new GenericSkyBox(this, name);
        sb.setGpuShaderProgram(renderSystem.getGpuShaderProgram(GpuShaderProgram.Type.SKYBOX));

        skyBoxMap.put(name, sb);
        return sb;
    }

    @Override
    public void setActiveSkyBox(SkyBox skyBox) {
        setActiveSkyBox(skyBox.getName());
    }

    @Override
    public void setActiveSkyBox(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("Name is empty");

        SkyBox sb = skyBoxMap.get(name);
        if (sb == null)
            throw new RuntimeException(SkyBox.class.getSimpleName() + " not owned by this manager: " + name);

        activeSkyBox = sb;
    }

    @Override
    public SkyBox getSkyBox(String name) {
        SkyBox sb = skyBoxMap.get(name);
        if (sb == null)
            throw new RuntimeException(SkyBox.class.getSimpleName() + " not found: " + name);

        return sb;
    }

    @Override
    public Iterable<SkyBox> getSkyBoxes() {
        return skyBoxMap.values();
    }

    @Override
    public int getSkyBoxCount() {
        return skyBoxMap.size();
    }

    @Override
    public boolean hasSkyBox(String name) {
        return skyBoxMap.containsKey(name);
    }

    @Override
    public void destroySkyBox(SkyBox skyBox) {
        destroySkyBox(skyBox.getName());
    }

    @Override
    public void destroySkyBox(String name) {
        SkyBox sb = skyBoxMap.get(name);
        if (sb == activeSkyBox)
            activeSkyBox = null;

        destroySceneObject(name, skyBoxMap);
    }
    
    @Override
    public Tessellation createTessellation(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("Name is empty");
        if (tessellationMap.containsKey(name))
            throw new RuntimeException(name + " already exists");

        Tessellation obj = new GenericTessellation(this, name);

        tessellationMap.put(name, obj);
        return obj;
    }

    @Override
    public Tessellation createTessellation(String name, int quality) {
        if (name.isEmpty())
            throw new IllegalArgumentException("Name is empty");
        if (tessellationMap.containsKey(name))
            throw new RuntimeException(name + " already exists");

        Tessellation obj = new GenericTessellation(this, name, quality);

        tessellationMap.put(name, obj);
        return obj;
    }
    
    @Override
    public boolean hasTessellation(String name) {
        return tessellationMap.containsKey(name);
    }

    @Override
    public Tessellation getTessellation(String name) {
    	Tessellation to = tessellationMap.get(name);
        if (to == null)
            throw new RuntimeException(Tessellation.class.getSimpleName() + " not found: " + name);

        return to;
    }

    @Override
    public Iterable<Tessellation> getTessellations() {
        return tessellationMap.values();
    }

    @Override
    public int getTessellationCount() {
        return tessellationMap.size();
    }

    @Override
    public void destroyTessellation(String name) {
        destroySceneObject(name, tessellationMap);
    }

    @Override
    public void destroyTessellation(Tessellation obj) {
        destroySceneObject(obj.getName(), tessellationMap);
    }

    @Override
    public SceneNode getRootSceneNode() {
        return rootSceneNode;
    }

    @Override
    public SceneNode createSceneNode(String name) {
        if (sceneNodeMap.containsKey(name))
            throw new RuntimeException(SceneNode.class.getSimpleName() + " already exists: " + name);

        SceneNode sn = new GenericSceneNode(this, name);
        sceneNodeMap.put(name, sn);
        return sn;
    }

    @Override
    public boolean hasSceneNode(String name) {
        return sceneNodeMap.containsKey(name);
    }

    @Override
    public SceneNode getSceneNode(String name) {
        SceneNode sn = sceneNodeMap.get(name);
        if (sn == null)
            throw new RuntimeException(SceneNode.class.getSimpleName() + " not found: " + name);

        return sn;
    }

    @Override
    public Iterable<SceneNode> getSceneNodes() {
        return sceneNodeMap.values();
    }

    @Override
    public int getSceneNodeCount() {
        return sceneNodeMap.size();
    }

    @Override
    public void destroySceneNode(String name) {
        if (name.equals(ROOT_NODE_NAME))
            throw new IllegalArgumentException("Cannot destroy the root " + SceneNode.class.getSimpleName());

        SceneNode sn = sceneNodeMap.remove(name);
        if (sn == null)
            throw new RuntimeException(SceneNode.class.getSimpleName() + " does not exist: " + name);

        Node parent = sn.getParent();
        if (parent != null)
            parent.detachChild(sn);

        sn.detachAllChildren();
        sn.detachAllObjects();
    }

    @Override
    public void destroySceneNode(SceneNode sn) {
        destroySceneNode(sn.getName());
    }

    @Override
    public AmbientLight getAmbientLight() {
        return ambientLight;
    }

    @Override
    public Light createLight(String name, Light.Type type) {
        if (name.isEmpty())
            throw new IllegalArgumentException("Name is empty");
        if (lightMap.containsKey(name))
            throw new RuntimeException(name + " already exists");

        Light light = new GenericLight(this, name, type);
        lightMap.put(name, light);

        return light;
    }

    @Override
    public boolean hasLight(String name) {
        return lightMap.containsKey(name);
    }

    @Override
    public Light getLight(String name) {
        Light light = lightMap.get(name);
        if (light == null)
            throw new RuntimeException(Light.class.getSimpleName() + " does not exist: " + name);

        return light;
    }

    @Override
    public Iterable<Light> getLights() {
        return lightMap.values();
    }

    @Override
    public int getLightCount() {
        return lightMap.size();
    }

    @Override
    public void destroyLight(String name) {
        Light light = lightMap.remove(name);

        if (light == null)
            throw new RuntimeException(Light.class.getSimpleName() + " does not exist: " + name);

        light.notifyDispose();
    }

    @Override
    public void destroyLight(Light l) {
        destroyLight(l.getName());
    }

    @Override
    public void destroyAllSceneObjects() {
        destroySceneObjectsMap(cameraMap);
        destroySceneObjectsMap(entityMap);
        destroySceneObjectsMap(manualObjMap);
        destroySceneObjectsMap(skyBoxMap);
        destroySceneObjectsMap(tessellationMap);
        destroySceneObjectsMap(lightMap);

        // detach everything from everything, but always keep the root node
        destroySceneNodesMap(sceneNodeMap);
        sceneNodeMap.put(rootSceneNode.getName(), rootSceneNode);

        // at this point, controllers have nodes that the manager does not
        // consider valid, so the controllers must discard them, too
        removeNodesFromControllers();
        removeAllControllers();

        ambientLight.notifyDispose();
    }

    @Override
    public void addController(Node.Controller ctrl) {
        if (ctrl == null)
            throw new NullPointerException("Null " + Node.Controller.class.getSimpleName());

        nodeControllers.add(ctrl);
    }

    @Override
    public Node.Controller getController(int index) {
        return nodeControllers.get(index);
    }

    @Override
    public int getControllerCount() {
        return nodeControllers.size();
    }

    @Override
    public void updateControllers(float time) {
    	Iterator<Node.Controller> iter = nodeControllers.iterator();
    	while (iter.hasNext()) {
    		Node.Controller nc = iter.next();
    		if (nc.isShouldDelete()) {
    			iter.remove();
        	}
    		else if (nc.isEnabled()) {
                nc.update(time);
            }
    	}
    }

    @Override
    public Iterable<Node.Controller> getControllers() {
        return nodeControllers;
    }

    @Override
    public void removeAllControllers() {
        nodeControllers.clear();
    }

    private void removeNodesFromControllers() {
        for (Node.Controller c : nodeControllers)
            c.removeAllNodes();
    }

    @Override
    public void updateSceneGraph() {
        emitPreUpdateSceneGraph();

        // For this implementation, we update the entire graph starting at the
        // root. A smarter manager might want to focus on updating specific
        // branches of the graph.
        rootSceneNode.update();

        emitPostUpdateSceneGraph();
    }

    @Override
    public void renderScene() {
        setRenderSystemLights();

        // TODO: use separate queues
        prepareSkyBoxRenderQueue();
        prepareManualObjectsQueue();
        prepareEntityRenderQueue();
        prepareTessellationQueue();

        emitPreRenderQueues();

        // cameras render the scenes from their own perspectives, invoke
        // registered listeners, and notify the scene manager
        
        for (Camera c : cameraMap.values())
            c.renderScene();

        emitPostRenderQueues();
        renderQueue.clear();

        // the render system is expected to have automatic buffer swapping
        // disabled by default to avoid swapping buffers repeatedly if there're
        // multiple cameras rendering to different sections of the same window;
        // it's the scene manager's responsibility to decide when the render
        // system should swap the back buffers, which is generally after all the
        // cameras have had a chance to render the scene into their viewports
        // from their own positions
        
        renderSystem.swapBuffers();
    }

    @Override
    public void notifyRenderScene(Camera cam, Viewport vp) {
        if (renderSystem == null)
            throw new RuntimeException(RenderSystem.class.getSimpleName() + " not set");
        if (cam == null)
            throw new RuntimeException("Null " + Camera.class.getSimpleName());
        if (vp == null)
            throw new RuntimeException("Null " + Viewport.class.getSimpleName());

        renderSystem.clearViewport(vp);
        Camera.Frustum frustum = cam.getFrustum();
        processRenderQueue(renderQueue, vp, cam.getParentNode().getWorldPosition(), frustum.getViewMatrix(), frustum.getProjectionMatrix());
    }

    @Override
    public void setRenderSystem(RenderSystem rs) {
        if (rs == null)
            throw new NullPointerException("Null " + RenderSystem.class.getSimpleName());

        // create the render queue if this is the first time a
        // render system is set
        if (renderSystem == null)
            renderQueue = rs.createRenderQueue();

        renderSystem = rs;
    }

    @Override
    public RenderSystem getRenderSystem() {
        return renderSystem;
    }

    @Override
    public void setMeshManager(MeshManager mm) {
        if (mm == null)
            throw new NullPointerException("Null " + MeshManager.class.getSimpleName());

        meshManager = mm;
    }

    @Override
    public MeshManager getMeshManager() {
        return meshManager;
    }

    @Override
    public void setSkeletonManager(SkeletonManager sm) {
        if (sm == null)
            throw new NullPointerException("Null " + SkeletonManager.class.getSimpleName());

        skeletonManager = sm;
    }

    @Override
    public SkeletonManager getSkeletonManager() {
        return skeletonManager;
    }

    @Override
    public void setAnimationManager(AnimationManager am) {
        if (am == null)
            throw new NullPointerException("Null " + AnimationManager.class.getSimpleName());

        animationManager = am;
    }

    @Override
    public AnimationManager getAnimationManager() {
        return animationManager;
    }

    @Override
    public void setTextureManager(TextureManager tm) {
        if (tm == null)
            throw new NullPointerException("Null " + TextureManager.class.getSimpleName());

        textureManager = tm;
    }

    @Override
    public TextureManager getTextureManager() {
        return textureManager;
    }

    @Override
    public void setMaterialManager(MaterialManager mm) {
        if (mm == null)
            throw new NullPointerException("Null " + MaterialManager.class.getSimpleName());

        materialManager = mm;
    }

    @Override
    public MaterialManager getMaterialManager() {
        return materialManager;
    }

    @Override
    public void setConfiguration(Configuration conf) {
        if (conf == null)
            throw new NullPointerException("Null " + Configuration.class.getSimpleName());

        configuration = conf;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void addSceneManagerListener(SceneManager.Listener sml) {
        if (sml == null)
            throw new NullPointerException("Null " + SceneManager.Listener.class.getSimpleName());

        sceneManagerListeners.add(sml);
    }

    @Override
    public void removeSceneManagerListener(SceneManager.Listener sml) {
        if (sml == null)
            throw new NullPointerException("Null " + SceneManager.Listener.class.getSimpleName());

        sceneManagerListeners.remove(sml);
    }

    @Override
    public void addRenderQueueListener(RenderQueue.Listener rql) {
        if (rql == null)
            throw new NullPointerException("Null " + RenderQueue.Listener.class.getSimpleName());

        renderQueueListeners.add(rql);
    }

    @Override
    public void removeRenderQueueListener(RenderQueue.Listener rql) {
        if (rql == null)
            throw new NullPointerException("Null " + RenderQueue.Listener.class.getSimpleName());

        renderQueueListeners.remove(rql);
    }

    @Override
    public void notifyDispose() {
        destroyAllSceneObjects();

        renderQueue.clear();
        renderQueueListeners.clear();

        rootSceneNode = null;
        activeSkyBox = null;
        skyBoxMap = null;
        cameraMap = null;
        entityMap = null;
        manualObjMap = null;
        lightMap = null;
        renderQueue = null;
        renderQueueListeners = null;
        meshManager = null;
        textureManager = null;
        skeletonManager = null;
        animationManager = null;
        materialManager = null;
        configuration = null;
        ambientLight = null;
    }

    private void prepareEntityRenderQueue() {
        for (Entity e : entityMap.values())
            if (e.isVisible() && e.isInScene())
                for (Renderable se : e.getSubEntities())
                    renderQueue.add(se);
    }

    private void prepareSkyBoxRenderQueue() {
        if (activeSkyBox != null && activeSkyBox.isVisible())
            for (Renderable face : activeSkyBox.getFaces())
                renderQueue.add(face);
    }

    private void prepareManualObjectsQueue() {
        for (ManualObject mo : manualObjMap.values())
            if (mo.isVisible() && mo.isInScene())
                for (Renderable sec : mo.getManualSections())
                    renderQueue.add(sec);
    }
    
    private void prepareTessellationQueue() {
        for (Tessellation t : tessellationMap.values())
            if (t.isVisible() && t.isInScene())
            	renderQueue.add(t.getTessellationBody());
    }

    private void setRenderSystemLights() {
        renderSystem.setAmbientLight(ambientLight);

        List<Light> lights = new ArrayList<>(lightMap.values().size());
        for (Light l : lightMap.values())
            if (l.isVisible() && l.isAttached() && l.isInScene())
                lights.add(l);

        renderSystem.setActiveLights(lights);
    }

    private void processRenderQueue(RenderQueue rq, Viewport vp, Vector3 posVector, Matrix4 viewMatrix, Matrix4 projMatrix) {
        boolean processQueue = emitRenderQueueStarted(rq);
        boolean repeatQueue = false;

        while (processQueue || repeatQueue) {
            if (!rq.isEmpty())
                renderSystem.processRenderQueue(rq, vp, posVector, viewMatrix, projMatrix);

            processQueue = repeatQueue = emitRenderQueueEnded(rq);
            if (repeatQueue)
                processQueue = emitRenderQueueStarted(rq);
        }
    }

    private void emitPreRenderQueues() {
        for (RenderQueue.Listener rql : renderQueueListeners)
            rql.onPreRenderQueues();
    }

    private boolean emitRenderQueueStarted(RenderQueue rq) {
        boolean processQueue = true;

        for (RenderQueue.Listener rql : renderQueueListeners)
            processQueue = rql.onRenderQueueStarted(rq);

        return processQueue;
    }

    private boolean emitRenderQueueEnded(RenderQueue rq) {
        boolean repeatQueue = false;

        for (RenderQueue.Listener rql : renderQueueListeners)
            repeatQueue = rql.onRenderQueueEnded(rq);

        return repeatQueue;
    }

    private void emitPostRenderQueues() {
        for (RenderQueue.Listener rql : renderQueueListeners)
            rql.onPostRenderQueues();
    }

    private void emitPreUpdateSceneGraph() {
        if (sceneManagerListeners.size() > 0)
            for (Camera c : cameraMap.values())
                for (SceneManager.Listener sml : sceneManagerListeners)
                    sml.onPreUpdateSceneGraph(this, c);
    }

    private void emitPostUpdateSceneGraph() {
        if (sceneManagerListeners.size() > 0)
            for (Camera c : cameraMap.values())
                for (SceneManager.Listener sml : sceneManagerListeners)
                    sml.onPostUpdateSceneGraph(this, c);
    }

    private static void destroySceneObject(String name, Map<String, ? extends SceneObject> map) {
        SceneObject so = map.remove(name);
        if (so == null)
            throw new RuntimeException("This manager does not own: " + name);

        so.notifyDispose();
    }

    private static void destroySceneObjectsMap(Map<String, ? extends SceneObject> map) {
        for (SceneObject so : map.values())
            so.notifyDispose();
        map.clear();
    }

    private static void destroySceneNodesMap(Map<String, ? extends SceneNode> map) {
        for (SceneNode sn : map.values()) {
            sn.detachAllChildren();
            sn.detachAllObjects();
        }
        map.clear();
    }

}
