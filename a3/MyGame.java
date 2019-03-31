package a3;

import myGameEngine.*;
import myGameEngine.Networking.ProtocolClient;
import myGameEngine.controllers.BungeeController;
import myGameEngine.controllers.NodeOrbitController;
import myGameEngine.myRage.HUDString;
import net.java.games.input.Controller;
import ray.input.GenericInputManager;
import ray.input.InputManager;
import ray.networking.IGameConnection.ProtocolType;
import ray.rage.Engine;
import ray.rage.asset.material.Material;
import ray.rage.asset.texture.Texture;
import ray.rage.asset.texture.TextureManager;
import ray.rage.game.Game;
import ray.rage.game.VariableFrameRateGame;
import ray.rage.rendersystem.RenderSystem;
import ray.rage.rendersystem.RenderWindow;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.shader.GpuShaderProgram;
import ray.rage.rendersystem.states.*;
import ray.rage.rendersystem.states.RenderState.*;
import ray.rage.rendersystem.states.TextureState.WrapMode;
import ray.rage.scene.Camera;
import ray.rage.scene.Entity;
import ray.rage.scene.Light;
import ray.rage.scene.ManualObject;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneNode;
import ray.rage.scene.SkyBox;
import ray.rage.scene.Tessellation;
import ray.rage.scene.Camera.Frustum.*;
import ray.rage.scene.controllers.RotationController;
import ray.rage.util.BufferUtil;
import ray.rage.util.Configuration;
import ray.rml.Degreef;
import ray.rml.Matrix3;
import ray.rml.Vector3;
import ray.rml.Vector3f;

import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.rmi.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.ImageIcon;

import net.java.games.input.Component;

public class MyGame extends VariableFrameRateGame {

	private GenericInputManager im = new GenericInputManager();
	private GL4RenderSystem rs;
	private int score = 0;
	private String bannerMsg = "";
	private int bannerTime = 2000;
	private float MOVE_SPEED = 0.01f;
	private float ROTATE_SPEED = 0.1f;
	private Planet[] planets = new Planet[] {
		new Planet("sun", 80f, 100f, 10f, 11.2f, 0.0004f),
		new Planet("Mercury", 50f, 0.7467f, 50f, 0.383f, 0.00017f),
		new Planet("Venus", 38f, 1.85f, -30f, 0.949f, -0.00004f),
		new Planet("Earth", 20f, 1.9496f, 10f, 1.0f, .01f),
		new Planet("Mars", -2f, 1.0372f, 40f, 0.532f, 0.0097f),
		new Planet("Jupiter", -30f, 11.308f, -40f, 5.8f, .024f),
		new Planet("Saturn", -40f, 8.68f, 45f, 4.45f, .022f),
		new Planet("Uranus", -20f, 3.919f, 5f, 2.01f, -.0138f),
		new Planet("Neptune", 15f, 3.665f, -30f, 1.88f, .0148f)
	};
	private ScriptEngine jsEngine = null;
	private File script = new File("script.js");
	private long lastScriptModifiedTime = 0;
	private SceneNode playerNode = null;
	private String serverAddr;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient clientProtocol;
	private boolean isConnected = false;
	private HashMap<UUID, GhostAvatar> ghostAvatars = new HashMap<UUID, GhostAvatar>();
	Entity ghostE = null;

	public MyGame(String serverAddr, int serverPort) {
		super();
		this.serverAddr = serverAddr;
		this.serverPort = serverPort;
		this.serverProtocol = ProtocolType.UDP;
		jsEngine = new ScriptEngineManager().getEngineByName("js");
		System.out.println("Initializing game.");
	}

	public static void main(String[] args) {
		Game game = new MyGame(args[0], Integer.parseInt(args[1]));
		try {
			game.startup();
			game.run();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			game.shutdown();
			game.exit();
		}
	}
	
	@Override
	protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
		RenderWindow rw = rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
		Configuration conf = getEngine().getConfiguration();
		ImageIcon icon = new ImageIcon(conf.valueOf("assets.icons.window"));
		rw.setIconImage(icon.getImage());
		rw.setTitle("Dolphin Rover (Aaron Hartigan)");
	}

	@Override
	protected void setupCameras(SceneManager sm, RenderWindow rw) {
		SceneNode rootNode = sm.getRootSceneNode();

		Camera camera = sm.createCamera("MainCamera", Projection.PERSPECTIVE);
		rw.getViewport(0).setCamera(camera);
		camera.setRt((Vector3f)Vector3f.createFrom(1.0f, 0.0f, 0.0f));
		camera.setUp((Vector3f)Vector3f.createFrom(0.0f, 1.0f, 0.0f));
		camera.setFd((Vector3f)Vector3f.createFrom(0.0f, 0.0f, -1.0f));
		camera.setPo((Vector3f)Vector3f.createFrom(0.0f, 0.0f, 0.0f));
		SceneNode cameraNode = rootNode.createChildSceneNode(camera.getName() + "Node");
		cameraNode.attachObject(camera);
		camera.getFrustum().setFarClipDistance(2000.0f);
		camera.getFrustum().setFieldOfViewY(Degreef.createFrom(60.0f));
	}
	
	@Override
	protected void setupWindowViewports(RenderWindow rw) {
		rw.addKeyListener(this);
	}
	
	@Override
	protected void setupScene(Engine eng, SceneManager sm) throws IOException {
		this.ghostE = sm.createEntity("ghost", "dolphinHighPoly.obj");
		ghostE.setPrimitive(Primitive.TRIANGLES);
		setupNetworking();
		executeScript(script);
		setupHUD();
		createDolphinWithCamera(sm);
		for (Planet p : this.planets) {
			createPlanet(sm, p);
		}
		createGroundPlane(sm);

		setupInputs();
		createSkyBox(eng, sm);
		// Render manuel object twice so it can be seen from both sides
		createManualObject(eng, sm, false);
		createManualObject(eng, sm, true);

		setupAmbientLight(sm);
		setupPointLight(sm, getEngine().getSceneManager().getSceneNode("sunNode"));
		Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
		mat.setEmissive(Color.WHITE);
		Entity sunE = getEngine().getSceneManager().getEntity("sun");
		sunE.setMaterial(mat);
	}

	private void setupNetworking() {
		try {
			clientProtocol = new ProtocolClient(
				InetAddress.getByName(serverAddr),
				serverPort,
				serverProtocol,
				this
			);
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		if (clientProtocol == null) {
			System.out.println("Missing protocol host");
		}
		else {
			clientProtocol.sendJoinMessage();
		}
	}

	protected void updateScriptConstants() {
		executeScript(script);
		MOVE_SPEED = ((Double)(jsEngine.get("MOVE_SPEED"))).floatValue();
		ROTATE_SPEED = ((Double)(jsEngine.get("ROTATE_SPEED"))).floatValue();
	}

	protected void setupHUD() {
		GL4RenderSystem rs = (GL4RenderSystem) getEngine().getRenderSystem();
		try {
			ArrayList<HUDString> stringList = rs.getHUDStringsList();
			stringList.add(new HUDString("", 0, 0));
			stringList.add(new HUDString("", 0, 0));
			stringList.add(new HUDString("", 0, 0));
			stringList.add(new HUDString("", 0, 0));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void update(Engine engine) {
		// build and set HUD
		float elapsTime = engine.getElapsedTimeMillis();
		processNetworking(elapsTime);
		im.update(elapsTime);
		this.checkCollisions();
		rs = (GL4RenderSystem) engine.getRenderSystem();
		
		ArrayList<HUDString> stringList = rs.getHUDStringsList();

		int x = 15;
		int y = 15;
		stringList.get(0).setAll("Player " + (1) + "    Score: " + score, x, y);
		if (bannerTime < 0) {
			stringList.get(2).setStr("");
		}
		else if (bannerMsg.length() > 0) {
			this.bannerTime -= elapsTime;
			int bannerX = rs.getCanvas().getWidth() / 2 -  (int) (bannerMsg.length() * 4.1f);
			int BannerY = rs.getCanvas().getHeight() - 50;
			stringList.get(2).setAll(bannerMsg, bannerX, BannerY);
		}
		updateDolphinHeight();
		long modifiedTime = script.lastModified();
		if (modifiedTime > lastScriptModifiedTime) {
			lastScriptModifiedTime = modifiedTime;
			updateScriptConstants();
		}
		clientProtocol.updatePlayerInformation(
			this.getPlayerPosition(),
			this.getPlayerRotation()
		);
	}

	private void processNetworking(float elapsTime) {
		if (clientProtocol != null) {
			clientProtocol.processPackets();
		}
	}

	protected void updateDolphinHeight() {
		SceneNode dolphin = getEngine().getSceneManager().getSceneNode("dolphinNode");
		Vector3 wp = dolphin.getWorldPosition();
		Tessellation plane = getEngine().getSceneManager().getTessellation("plane");
		
		float newHeight = plane.getWorldHeight(wp.x(), wp.z()) + 1.3f;
		dolphin.setLocalPosition(wp.x(), newHeight, wp.z());
		
	}

	protected void createDolphinWithCamera(SceneManager sm) throws IOException {
		Entity dolphinE = sm.createEntity("dolphin", "dolphinHighPoly.obj");
		dolphinE.setPrimitive(Primitive.TRIANGLES);
		SceneNode dolphinN = sm.getRootSceneNode().createChildSceneNode(dolphinE.getName() + "Node");
		playerNode = dolphinN;
		//dolphinN.moveBackward(2.0f);
		//dolphinN.moveUp(0.3f);
		dolphinN.attachObject(dolphinE);

		SceneNode dolphinCamera = dolphinN.createChildSceneNode(dolphinN.getName() + "Camera");

		Camera camera = sm.getCamera("MainCamera");
		dolphinCamera.attachObject(camera);
		camera.setMode('n');
		
		NodeOrbitController noc = new NodeOrbitController(
			dolphinN,
			(GL4RenderSystem) getEngine().getRenderSystem(),
			im
		);
		noc.addNode(dolphinCamera);
		sm.addController(noc);
	}
	
	protected void setupAmbientLight(SceneManager sm) {
		sm.getAmbientLight().setIntensity(new Color(0.2f, 0.2f, 0.2f));
	}
	
	protected void setupPointLight(SceneManager sm, SceneNode node) {
		Light sunlight = sm.createLight("sunLight", Light.Type.POINT);
		sunlight.setAmbient(new Color(.15f, .15f, .15f));
		sunlight.setDiffuse(new Color(1f, 1f, .8f));
		sunlight.setSpecular(new Color(.1f, .1f, .1f));
		sunlight.setRange(1000f);
		sunlight.setConstantAttenuation(0.8f);
		sunlight.setLinearAttenuation(0.0000001f);
		sunlight.setQuadraticAttenuation(0f);
		sunlight.setFalloffExponent(0f);
		node.attachObject(sunlight);
	}
	
	// Setup all inputs (keyboard and controller) needed for the game
	protected void setupInputs() {
		ArrayList<Controller> controllers = im.getControllers();
		SceneNode dolphin = getEngine().getSceneManager().getSceneNode("dolphinNode");
		for (Controller c : controllers) {
			if (c.getType() == Controller.Type.KEYBOARD) {
				im.associateAction(
					c,
					Component.Identifier.Key.W,
					new MoveNodeForwardAction(dolphin, this),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
				im.associateAction(
					c,
					Component.Identifier.Key.Q,
					new MoveNodeLeftAction(dolphin, this),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
				im.associateAction(
					c,
					Component.Identifier.Key.S,
					new MoveNodeBackwardAction(dolphin, this),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
				im.associateAction(
					c,
					Component.Identifier.Key.E,
					new MoveNodeRightAction(dolphin, this),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
				im.associateAction(
					c,
					Component.Identifier.Key.A,
					new RotateNodeLeftAction(dolphin, this),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
				im.associateAction(
					c,
					Component.Identifier.Key.D,
					new RotateNodeRightAction(dolphin, this),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
				im.associateAction(
					c,
					Component.Identifier.Key.F,
					new ToggleWireframe((GL4RenderSystem) getEngine().getRenderSystem()),
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY
				);
				im.associateAction(
					c,
					Component.Identifier.Key.M,
					new ToggleShadowMap((GL4RenderSystem) getEngine().getRenderSystem()),
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY
				);
				im.associateAction(
					c,
					Component.Identifier.Key.P,
					new TogglePerspective((GL4RenderSystem) getEngine().getRenderSystem()),
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY
				);
			}
			else if (c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK) {
				im.associateAction(
					c,
					Component.Identifier.Axis.Y,
					new MoveNodeForwardBackwardDeviceAction(dolphin, this),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
				im.associateAction(
					c,
					Component.Identifier.Axis.X,
					new MoveNodeLeftRightDeviceAction(dolphin, this),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
				im.associateAction(
					c,
					Component.Identifier.Axis.RX,
					new RotateNodeLeftRightDeviceAction(dolphin, this),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
			}
		}
	}

	// Create and add planet to scene
	protected void createPlanet(SceneManager sm, Planet p) throws IOException {
		Entity planetE = sm.createEntity(p.name(), p.name() + ".obj");
		planetE.setPrimitive(Primitive.TRIANGLES);
		
		SceneNode planetN = sm.getRootSceneNode().createChildSceneNode(planetE.getName() + "Node");
		planetN.moveForward(p.z());
		planetN.moveUp(p.y());
		planetN.moveRight(p.x());
		planetN.scale(p.scale(), p.scale(), p.scale());
		planetN.attachObject(planetE);
	}
	
	protected void createGroundPlane(SceneManager sm) throws IOException {
		Tessellation plane = sm.createTessellation("plane");
		SceneNode planeN = sm.getRootSceneNode().createChildSceneNode("planeNode");
		planeN.attachObject(plane);
		planeN.scale(100, 1, 100);
		planeN.translate(0f, -1f, 0f);

		plane.getTextureState().setWrapMode(WrapMode.REPEAT_MIRRORED);
		plane.setTexture(getEngine(), "hexagons.jpeg");
		plane.setTextureTiling(8);

		plane.setHeightMap(getEngine(), "height_map.png");
		plane.setQuality(7);
		plane.setMultiplier(4);
		
		ManualObject ground = sm.createManualObject("ground");
		ground.createManualSection("GroundSection");
		ground.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		ground.setDepthShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.DEPTH));
		

		float[] vertices = new float[] {
	       -250f, -0.1f,  250f,
	        250f, -0.1f,  250f,
	       -250f, -0.1f, -250f,
	       -250f, -0.1f, -250f,
	        250f, -0.1f,  250f,
	        250f, -0.1f, -250f,
		};
		
		float[] texcoords = new float[vertices.length];
		for (int i = 0; i < vertices.length / 18; i++) {
			texcoords[i * 12    ] = 1.0f;
			texcoords[i * 12 + 1] = 0.0f;
			texcoords[i * 12 + 2] = 0.0f;
			texcoords[i * 12 + 3] = 1.0f;
			texcoords[i * 12 + 4] = 0.0f;
			texcoords[i * 12 + 5] = 0.0f;
			texcoords[i * 12 + 6] = 1.0f;
			texcoords[i * 12 + 7] = 0.0f;
			texcoords[i * 12 + 8] = 1.0f;
			texcoords[i * 12 + 9] = 1.0f;
			texcoords[i * 12 +10] = 0.0f;
			texcoords[i * 12 +11] = 1.0f;
		}

		// Since the shape is a 2D shape, all normals point straight up
		float[] normals = new float[vertices.length];
		for (int i = 0; i < vertices.length / 3; i++) {
			normals[i * 3] = 0.0f;
			normals[i * 3 + 1] = 1.0f;
			normals[i * 3 + 2] = 0.0f;
		}

		int[] indices = new int[vertices.length];
		for (int i = 0; i < vertices.length; i++) {
			indices[i] = i;
		}

		ground.setVertexBuffer(BufferUtil.directFloatBuffer(vertices));
		ground.setNormalsBuffer(BufferUtil.directFloatBuffer(normals));
		ground.setTextureCoordBuffer(BufferUtil.directFloatBuffer(texcoords));
		ground.setIndexBuffer(BufferUtil.directIntBuffer(indices));
		
		Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
		TextureState tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		tstate.setTexture(sm.getTextureManager().getAssetByPath("default.png"));

		FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);
		ZBufferState zstate = (ZBufferState) sm.getRenderSystem().createRenderState(Type.ZBUFFER);
		
		ground.setDataSource(DataSource.INDEX_BUFFER);
		ground.setRenderState(faceState);
		ground.setRenderState(tstate);
		ground.setRenderState(zstate);
		ground.setMaterial(mat);

		SceneNode groundN = sm.getRootSceneNode().createChildSceneNode("groundNode");
		groundN.attachObject(ground);
	}
	
	// Checks and handles collision between the dolphin and the planets
	protected void checkCollisions() {
		SceneNode dolphin = getEngine().getSceneManager().getSceneNode("dolphinNode");
		for (Planet p : this.planets) {
			if (!isCloseEnough(p, dolphin.getWorldPosition())) {
				continue;
			}
			else if (!p.beenVisited()) {
				if (p.name() == "sun") {
					this.setBannerMsg("The sun is not a planet.");
					continue;
				}

				RotationController rc = new RotationController(Vector3f.createUnitVectorY(), 0.1f);
				rc.addNode(getEngine().getSceneManager().getSceneNode(p.name() + "Node"));
				getEngine().getSceneManager().addController(rc);

				BungeeController bc = new BungeeController();
				bc.addNode(getEngine().getSceneManager().getSceneNode(p.name() + "Node"));
				getEngine().getSceneManager().addController(bc);

				this.score++;
				p.setBeenVisited(true);
				if (this.score == 8) {
					String endGame = "You have visited ALL of the planets! ";
					this.setBannerMsg(endGame);
				}
				else {
					this.setBannerMsg("You have visited " + p.name() + "!");
				}
			}
			else if (bannerTime < 0 && p.beenVisited()) {
				this.setBannerMsg("You have already visited " + p.name() + ".");
			}
		}
	}
	
	// Check if player is close to a planet
	protected boolean isCloseEnough(Planet p, Vector3 po) {
		// 1.949634 is the max radius of the planet mesh
		double dist = calcDistance(p.x(), p.y(), p.z(), po.x(), po.y(), po.z());
		return dist < (p.scale() * 1.949634 + 0.5f);
	}
	
	protected double calcDistance(float x1, float y1, float z1, float x2, float y2, float z2) {
		float dx = (x1 - x2);
		float dy = (y1 - y2);
		float dz = (z1 - z2);
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	protected class Planet {
		private String name;
		private float x, y, z;
		private float scale;
		private float rotationSpeed;
		private boolean beenVisited = false;
 
		public Planet(String name, float x, float y, float z, float scale, float rotationSpeed) {
			this.name = name;
			this.x = x;
			this.y = y;
			this.z = z;
			this.scale = scale;
			this.rotationSpeed = rotationSpeed;
		}
		
		public String name( ) {
			return this.name;
		}
		
		public float x( ) {
			return this.x;
		}
		
		public float y( ) {
			return this.y;
		}
		
		public float z( ) {
			return this.z;
		}
		
		public float scale( ) {
			return this.scale;
		}
		
		public float rotationSpeed( ) {
			return this.rotationSpeed;
		}
		
		public Boolean beenVisited() {
			return this.beenVisited;
		}
		
		public void setBeenVisited(boolean b) {
			this.beenVisited = b;
		}
	}
	
	protected SceneNode createManualObject(Engine eng, SceneManager sm, Boolean rev) throws IOException {
		String nameAppend = rev ? "Reverse" : "";
		ManualObject rings = sm.createManualObject("Rings" + nameAppend);
		rings.createManualSection("RingsSection" + nameAppend);
		rings.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		rings.setDepthShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.DEPTH));
		// These points are generated by a script I wrote, not Blender
		float[] vertices = new float[] {
			0.000000f,  0.0f, 1.000000f, 0.024534f,  0.0f, 0.499398f, 0.000000f,  0.0f, 0.500000f,
			0.000000f,  0.0f, 1.000000f, 0.049068f,  0.0f, 0.998795f, 0.024534f,  0.0f, 0.499398f,
			0.049068f,  0.0f, 0.998795f, 0.049009f,  0.0f, 0.497592f, 0.024534f,  0.0f, 0.499398f,
			0.049068f,  0.0f, 0.998795f, 0.098017f,  0.0f, 0.995185f, 0.049009f,  0.0f, 0.497592f,
			0.098017f,  0.0f, 0.995185f, 0.073365f,  0.0f, 0.494588f, 0.049009f,  0.0f, 0.497592f,
			0.098017f,  0.0f, 0.995185f, 0.146730f,  0.0f, 0.989177f, 0.073365f,  0.0f, 0.494588f,
			0.146730f,  0.0f, 0.989177f, 0.097545f,  0.0f, 0.490393f, 0.073365f,  0.0f, 0.494588f,
			0.146730f,  0.0f, 0.989177f, 0.195090f,  0.0f, 0.980785f, 0.097545f,  0.0f, 0.490393f,
			0.195090f,  0.0f, 0.980785f, 0.121490f,  0.0f, 0.485016f, 0.097545f,  0.0f, 0.490393f,
			0.195090f,  0.0f, 0.980785f, 0.242980f,  0.0f, 0.970031f, 0.121490f,  0.0f, 0.485016f,
			0.242980f,  0.0f, 0.970031f, 0.145142f,  0.0f, 0.478470f, 0.121490f,  0.0f, 0.485016f,
			0.242980f,  0.0f, 0.970031f, 0.290285f,  0.0f, 0.956940f, 0.145142f,  0.0f, 0.478470f,
			0.290285f,  0.0f, 0.956940f, 0.168445f,  0.0f, 0.470772f, 0.145142f,  0.0f, 0.478470f,
			0.290285f,  0.0f, 0.956940f, 0.336890f,  0.0f, 0.941544f, 0.168445f,  0.0f, 0.470772f,
			0.336890f,  0.0f, 0.941544f, 0.191342f,  0.0f, 0.461940f, 0.168445f,  0.0f, 0.470772f,
			0.336890f,  0.0f, 0.941544f, 0.382683f,  0.0f, 0.923880f, 0.191342f,  0.0f, 0.461940f,
			0.382683f,  0.0f, 0.923880f, 0.213778f,  0.0f, 0.451995f, 0.191342f,  0.0f, 0.461940f,
			0.382683f,  0.0f, 0.923880f, 0.427555f,  0.0f, 0.903989f, 0.213778f,  0.0f, 0.451995f,
			0.427555f,  0.0f, 0.903989f, 0.235698f,  0.0f, 0.440961f, 0.213778f,  0.0f, 0.451995f,
			0.427555f,  0.0f, 0.903989f, 0.471397f,  0.0f, 0.881921f, 0.235698f,  0.0f, 0.440961f,
			0.471397f,  0.0f, 0.881921f, 0.257051f,  0.0f, 0.428864f, 0.235698f,  0.0f, 0.440961f,
			0.471397f,  0.0f, 0.881921f, 0.514103f,  0.0f, 0.857729f, 0.257051f,  0.0f, 0.428864f,
			0.514103f,  0.0f, 0.857729f, 0.277785f,  0.0f, 0.415735f, 0.257051f,  0.0f, 0.428864f,
			0.514103f,  0.0f, 0.857729f, 0.555570f,  0.0f, 0.831470f, 0.277785f,  0.0f, 0.415735f,
			0.555570f,  0.0f, 0.831470f, 0.297850f,  0.0f, 0.401604f, 0.277785f,  0.0f, 0.415735f,
			0.555570f,  0.0f, 0.831470f, 0.595699f,  0.0f, 0.803208f, 0.297850f,  0.0f, 0.401604f,
			0.595699f,  0.0f, 0.803208f, 0.317197f,  0.0f, 0.386505f, 0.297850f,  0.0f, 0.401604f,
			0.595699f,  0.0f, 0.803208f, 0.634393f,  0.0f, 0.773010f, 0.317197f,  0.0f, 0.386505f,
			0.634393f,  0.0f, 0.773010f, 0.335779f,  0.0f, 0.370476f, 0.317197f,  0.0f, 0.386505f,
			0.634393f,  0.0f, 0.773010f, 0.671559f,  0.0f, 0.740951f, 0.335779f,  0.0f, 0.370476f,
			0.671559f,  0.0f, 0.740951f, 0.353553f,  0.0f, 0.353553f, 0.335779f,  0.0f, 0.370476f,
			0.671559f,  0.0f, 0.740951f, 0.707107f,  0.0f, 0.707107f, 0.353553f,  0.0f, 0.353553f,
			0.707107f,  0.0f, 0.707107f, 0.370476f,  0.0f, 0.335779f, 0.353553f,  0.0f, 0.353553f,
			0.707107f,  0.0f, 0.707107f, 0.740951f,  0.0f, 0.671559f, 0.370476f,  0.0f, 0.335779f,
			0.740951f,  0.0f, 0.671559f, 0.386505f,  0.0f, 0.317197f, 0.370476f,  0.0f, 0.335779f,
			0.740951f,  0.0f, 0.671559f, 0.773010f,  0.0f, 0.634393f, 0.386505f,  0.0f, 0.317197f,
			0.773010f,  0.0f, 0.634393f, 0.401604f,  0.0f, 0.297850f, 0.386505f,  0.0f, 0.317197f,
			0.773010f,  0.0f, 0.634393f, 0.803208f,  0.0f, 0.595699f, 0.401604f,  0.0f, 0.297850f,
			0.803208f,  0.0f, 0.595699f, 0.415735f,  0.0f, 0.277785f, 0.401604f,  0.0f, 0.297850f,
			0.803208f,  0.0f, 0.595699f, 0.831470f,  0.0f, 0.555570f, 0.415735f,  0.0f, 0.277785f,
			0.831470f,  0.0f, 0.555570f, 0.428864f,  0.0f, 0.257051f, 0.415735f,  0.0f, 0.277785f,
			0.831470f,  0.0f, 0.555570f, 0.857729f,  0.0f, 0.514103f, 0.428864f,  0.0f, 0.257051f,
			0.857729f,  0.0f, 0.514103f, 0.440961f,  0.0f, 0.235698f, 0.428864f,  0.0f, 0.257051f,
			0.857729f,  0.0f, 0.514103f, 0.881921f,  0.0f, 0.471397f, 0.440961f,  0.0f, 0.235698f,
			0.881921f,  0.0f, 0.471397f, 0.451995f,  0.0f, 0.213778f, 0.440961f,  0.0f, 0.235698f,
			0.881921f,  0.0f, 0.471397f, 0.903989f,  0.0f, 0.427555f, 0.451995f,  0.0f, 0.213778f,
			0.903989f,  0.0f, 0.427555f, 0.461940f,  0.0f, 0.191342f, 0.451995f,  0.0f, 0.213778f,
			0.903989f,  0.0f, 0.427555f, 0.923880f,  0.0f, 0.382683f, 0.461940f,  0.0f, 0.191342f,
			0.923880f,  0.0f, 0.382683f, 0.470772f,  0.0f, 0.168445f, 0.461940f,  0.0f, 0.191342f,
			0.923880f,  0.0f, 0.382683f, 0.941544f,  0.0f, 0.336890f, 0.470772f,  0.0f, 0.168445f,
			0.941544f,  0.0f, 0.336890f, 0.478470f,  0.0f, 0.145142f, 0.470772f,  0.0f, 0.168445f,
			0.941544f,  0.0f, 0.336890f, 0.956940f,  0.0f, 0.290285f, 0.478470f,  0.0f, 0.145142f,
			0.956940f,  0.0f, 0.290285f, 0.485016f,  0.0f, 0.121490f, 0.478470f,  0.0f, 0.145142f,
			0.956940f,  0.0f, 0.290285f, 0.970031f,  0.0f, 0.242980f, 0.485016f,  0.0f, 0.121490f,
			0.970031f,  0.0f, 0.242980f, 0.490393f,  0.0f, 0.097545f, 0.485016f,  0.0f, 0.121490f,
			0.970031f,  0.0f, 0.242980f, 0.980785f,  0.0f, 0.195090f, 0.490393f,  0.0f, 0.097545f,
			0.980785f,  0.0f, 0.195090f, 0.494588f,  0.0f, 0.073365f, 0.490393f,  0.0f, 0.097545f,
			0.980785f,  0.0f, 0.195090f, 0.989177f,  0.0f, 0.146730f, 0.494588f,  0.0f, 0.073365f,
			0.989177f,  0.0f, 0.146730f, 0.497592f,  0.0f, 0.049009f, 0.494588f,  0.0f, 0.073365f,
			0.989177f,  0.0f, 0.146730f, 0.995185f,  0.0f, 0.098017f, 0.497592f,  0.0f, 0.049009f,
			0.995185f,  0.0f, 0.098017f, 0.499398f,  0.0f, 0.024534f, 0.497592f,  0.0f, 0.049009f,
			0.995185f,  0.0f, 0.098017f, 0.998795f,  0.0f, 0.049068f, 0.499398f,  0.0f, 0.024534f,
			0.998795f,  0.0f, 0.049068f, 0.500000f,  0.0f, -0.000000f, 0.499398f,  0.0f, 0.024534f,
			0.998795f,  0.0f, 0.049068f, 1.000000f,  0.0f, -0.000000f, 0.500000f,  0.0f, -0.000000f,
			1.000000f,  0.0f, -0.000000f, 0.499398f,  0.0f, -0.024534f, 0.500000f,  0.0f, -0.000000f,
			1.000000f,  0.0f, -0.000000f, 0.998795f,  0.0f, -0.049068f, 0.499398f,  0.0f, -0.024534f,
			0.998795f,  0.0f, -0.049068f, 0.497592f,  0.0f, -0.049009f, 0.499398f,  0.0f, -0.024534f,
			0.998795f,  0.0f, -0.049068f, 0.995185f,  0.0f, -0.098017f, 0.497592f,  0.0f, -0.049009f,
			0.995185f,  0.0f, -0.098017f, 0.494588f,  0.0f, -0.073365f, 0.497592f,  0.0f, -0.049009f,
			0.995185f,  0.0f, -0.098017f, 0.989177f,  0.0f, -0.146730f, 0.494588f,  0.0f, -0.073365f,
			0.989177f,  0.0f, -0.146730f, 0.490393f,  0.0f, -0.097545f, 0.494588f,  0.0f, -0.073365f,
			0.989177f,  0.0f, -0.146730f, 0.980785f,  0.0f, -0.195090f, 0.490393f,  0.0f, -0.097545f,
			0.980785f,  0.0f, -0.195090f, 0.485016f,  0.0f, -0.121490f, 0.490393f,  0.0f, -0.097545f,
			0.980785f,  0.0f, -0.195090f, 0.970031f,  0.0f, -0.242980f, 0.485016f,  0.0f, -0.121490f,
			0.970031f,  0.0f, -0.242980f, 0.478470f,  0.0f, -0.145142f, 0.485016f,  0.0f, -0.121490f,
			0.970031f,  0.0f, -0.242980f, 0.956940f,  0.0f, -0.290285f, 0.478470f,  0.0f, -0.145142f,
			0.956940f,  0.0f, -0.290285f, 0.470772f,  0.0f, -0.168445f, 0.478470f,  0.0f, -0.145142f,
			0.956940f,  0.0f, -0.290285f, 0.941544f,  0.0f, -0.336890f, 0.470772f,  0.0f, -0.168445f,
			0.941544f,  0.0f, -0.336890f, 0.461940f,  0.0f, -0.191342f, 0.470772f,  0.0f, -0.168445f,
			0.941544f,  0.0f, -0.336890f, 0.923880f,  0.0f, -0.382683f, 0.461940f,  0.0f, -0.191342f,
			0.923880f,  0.0f, -0.382683f, 0.451995f,  0.0f, -0.213778f, 0.461940f,  0.0f, -0.191342f,
			0.923880f,  0.0f, -0.382683f, 0.903989f,  0.0f, -0.427555f, 0.451995f,  0.0f, -0.213778f,
			0.903989f,  0.0f, -0.427555f, 0.440961f,  0.0f, -0.235698f, 0.451995f,  0.0f, -0.213778f,
			0.903989f,  0.0f, -0.427555f, 0.881921f,  0.0f, -0.471397f, 0.440961f,  0.0f, -0.235698f,
			0.881921f,  0.0f, -0.471397f, 0.428864f,  0.0f, -0.257051f, 0.440961f,  0.0f, -0.235698f,
			0.881921f,  0.0f, -0.471397f, 0.857729f,  0.0f, -0.514103f, 0.428864f,  0.0f, -0.257051f,
			0.857729f,  0.0f, -0.514103f, 0.415735f,  0.0f, -0.277785f, 0.428864f,  0.0f, -0.257051f,
			0.857729f,  0.0f, -0.514103f, 0.831470f,  0.0f, -0.555570f, 0.415735f,  0.0f, -0.277785f,
			0.831470f,  0.0f, -0.555570f, 0.401604f,  0.0f, -0.297850f, 0.415735f,  0.0f, -0.277785f,
			0.831470f,  0.0f, -0.555570f, 0.803208f,  0.0f, -0.595699f, 0.401604f,  0.0f, -0.297850f,
			0.803208f,  0.0f, -0.595699f, 0.386505f,  0.0f, -0.317197f, 0.401604f,  0.0f, -0.297850f,
			0.803208f,  0.0f, -0.595699f, 0.773010f,  0.0f, -0.634393f, 0.386505f,  0.0f, -0.317197f,
			0.773010f,  0.0f, -0.634393f, 0.370476f,  0.0f, -0.335779f, 0.386505f,  0.0f, -0.317197f,
			0.773010f,  0.0f, -0.634393f, 0.740951f,  0.0f, -0.671559f, 0.370476f,  0.0f, -0.335779f,
			0.740951f,  0.0f, -0.671559f, 0.353553f,  0.0f, -0.353553f, 0.370476f,  0.0f, -0.335779f,
			0.740951f,  0.0f, -0.671559f, 0.707107f,  0.0f, -0.707107f, 0.353553f,  0.0f, -0.353553f,
			0.707107f,  0.0f, -0.707107f, 0.335779f,  0.0f, -0.370476f, 0.353553f,  0.0f, -0.353553f,
			0.707107f,  0.0f, -0.707107f, 0.671559f,  0.0f, -0.740951f, 0.335779f,  0.0f, -0.370476f,
			0.671559f,  0.0f, -0.740951f, 0.317197f,  0.0f, -0.386505f, 0.335779f,  0.0f, -0.370476f,
			0.671559f,  0.0f, -0.740951f, 0.634393f,  0.0f, -0.773010f, 0.317197f,  0.0f, -0.386505f,
			0.634393f,  0.0f, -0.773010f, 0.297850f,  0.0f, -0.401604f, 0.317197f,  0.0f, -0.386505f,
			0.634393f,  0.0f, -0.773010f, 0.595699f,  0.0f, -0.803208f, 0.297850f,  0.0f, -0.401604f,
			0.595699f,  0.0f, -0.803208f, 0.277785f,  0.0f, -0.415735f, 0.297850f,  0.0f, -0.401604f,
			0.595699f,  0.0f, -0.803208f, 0.555570f,  0.0f, -0.831470f, 0.277785f,  0.0f, -0.415735f,
			0.555570f,  0.0f, -0.831470f, 0.257051f,  0.0f, -0.428864f, 0.277785f,  0.0f, -0.415735f,
			0.555570f,  0.0f, -0.831470f, 0.514103f,  0.0f, -0.857729f, 0.257051f,  0.0f, -0.428864f,
			0.514103f,  0.0f, -0.857729f, 0.235698f,  0.0f, -0.440961f, 0.257051f,  0.0f, -0.428864f,
			0.514103f,  0.0f, -0.857729f, 0.471397f,  0.0f, -0.881921f, 0.235698f,  0.0f, -0.440961f,
			0.471397f,  0.0f, -0.881921f, 0.213778f,  0.0f, -0.451995f, 0.235698f,  0.0f, -0.440961f,
			0.471397f,  0.0f, -0.881921f, 0.427555f,  0.0f, -0.903989f, 0.213778f,  0.0f, -0.451995f,
			0.427555f,  0.0f, -0.903989f, 0.191342f,  0.0f, -0.461940f, 0.213778f,  0.0f, -0.451995f,
			0.427555f,  0.0f, -0.903989f, 0.382683f,  0.0f, -0.923880f, 0.191342f,  0.0f, -0.461940f,
			0.382683f,  0.0f, -0.923880f, 0.168445f,  0.0f, -0.470772f, 0.191342f,  0.0f, -0.461940f,
			0.382683f,  0.0f, -0.923880f, 0.336890f,  0.0f, -0.941544f, 0.168445f,  0.0f, -0.470772f,
			0.336890f,  0.0f, -0.941544f, 0.145142f,  0.0f, -0.478470f, 0.168445f,  0.0f, -0.470772f,
			0.336890f,  0.0f, -0.941544f, 0.290285f,  0.0f, -0.956940f, 0.145142f,  0.0f, -0.478470f,
			0.290285f,  0.0f, -0.956940f, 0.121490f,  0.0f, -0.485016f, 0.145142f,  0.0f, -0.478470f,
			0.290285f,  0.0f, -0.956940f, 0.242980f,  0.0f, -0.970031f, 0.121490f,  0.0f, -0.485016f,
			0.242980f,  0.0f, -0.970031f, 0.097545f,  0.0f, -0.490393f, 0.121490f,  0.0f, -0.485016f,
			0.242980f,  0.0f, -0.970031f, 0.195090f,  0.0f, -0.980785f, 0.097545f,  0.0f, -0.490393f,
			0.195090f,  0.0f, -0.980785f, 0.073365f,  0.0f, -0.494588f, 0.097545f,  0.0f, -0.490393f,
			0.195090f,  0.0f, -0.980785f, 0.146730f,  0.0f, -0.989177f, 0.073365f,  0.0f, -0.494588f,
			0.146730f,  0.0f, -0.989177f, 0.049009f,  0.0f, -0.497592f, 0.073365f,  0.0f, -0.494588f,
			0.146730f,  0.0f, -0.989177f, 0.098017f,  0.0f, -0.995185f, 0.049009f,  0.0f, -0.497592f,
			0.098017f,  0.0f, -0.995185f, 0.024534f,  0.0f, -0.499398f, 0.049009f,  0.0f, -0.497592f,
			0.098017f,  0.0f, -0.995185f, 0.049068f,  0.0f, -0.998795f, 0.024534f,  0.0f, -0.499398f,
			0.049068f,  0.0f, -0.998795f, -0.000000f,  0.0f, -0.500000f, 0.024534f,  0.0f, -0.499398f,
			0.049068f,  0.0f, -0.998795f, -0.000000f,  0.0f, -1.000000f, -0.000000f,  0.0f, -0.500000f,
			-0.000000f,  0.0f, -1.000000f, -0.024534f,  0.0f, -0.499398f, -0.000000f,  0.0f, -0.500000f,
			-0.000000f,  0.0f, -1.000000f, -0.049068f,  0.0f, -0.998795f, -0.024534f,  0.0f, -0.499398f,
			-0.049068f,  0.0f, -0.998795f, -0.049009f,  0.0f, -0.497592f, -0.024534f,  0.0f, -0.499398f,
			-0.049068f,  0.0f, -0.998795f, -0.098017f,  0.0f, -0.995185f, -0.049009f,  0.0f, -0.497592f,
			-0.098017f,  0.0f, -0.995185f, -0.073365f,  0.0f, -0.494588f, -0.049009f,  0.0f, -0.497592f,
			-0.098017f,  0.0f, -0.995185f, -0.146730f,  0.0f, -0.989177f, -0.073365f,  0.0f, -0.494588f,
			-0.146730f,  0.0f, -0.989177f, -0.097545f,  0.0f, -0.490393f, -0.073365f,  0.0f, -0.494588f,
			-0.146730f,  0.0f, -0.989177f, -0.195090f,  0.0f, -0.980785f, -0.097545f,  0.0f, -0.490393f,
			-0.195090f,  0.0f, -0.980785f, -0.121490f,  0.0f, -0.485016f, -0.097545f,  0.0f, -0.490393f,
			-0.195090f,  0.0f, -0.980785f, -0.242980f,  0.0f, -0.970031f, -0.121490f,  0.0f, -0.485016f,
			-0.242980f,  0.0f, -0.970031f, -0.145142f,  0.0f, -0.478470f, -0.121490f,  0.0f, -0.485016f,
			-0.242980f,  0.0f, -0.970031f, -0.290285f,  0.0f, -0.956940f, -0.145142f,  0.0f, -0.478470f,
			-0.290285f,  0.0f, -0.956940f, -0.168445f,  0.0f, -0.470772f, -0.145142f,  0.0f, -0.478470f,
			-0.290285f,  0.0f, -0.956940f, -0.336890f,  0.0f, -0.941544f, -0.168445f,  0.0f, -0.470772f,
			-0.336890f,  0.0f, -0.941544f, -0.191342f,  0.0f, -0.461940f, -0.168445f,  0.0f, -0.470772f,
			-0.336890f,  0.0f, -0.941544f, -0.382683f,  0.0f, -0.923880f, -0.191342f,  0.0f, -0.461940f,
			-0.382683f,  0.0f, -0.923880f, -0.213778f,  0.0f, -0.451995f, -0.191342f,  0.0f, -0.461940f,
			-0.382683f,  0.0f, -0.923880f, -0.427555f,  0.0f, -0.903989f, -0.213778f,  0.0f, -0.451995f,
			-0.427555f,  0.0f, -0.903989f, -0.235698f,  0.0f, -0.440961f, -0.213778f,  0.0f, -0.451995f,
			-0.427555f,  0.0f, -0.903989f, -0.471397f,  0.0f, -0.881921f, -0.235698f,  0.0f, -0.440961f,
			-0.471397f,  0.0f, -0.881921f, -0.257051f,  0.0f, -0.428864f, -0.235698f,  0.0f, -0.440961f,
			-0.471397f,  0.0f, -0.881921f, -0.514103f,  0.0f, -0.857729f, -0.257051f,  0.0f, -0.428864f,
			-0.514103f,  0.0f, -0.857729f, -0.277785f,  0.0f, -0.415735f, -0.257051f,  0.0f, -0.428864f,
			-0.514103f,  0.0f, -0.857729f, -0.555570f,  0.0f, -0.831470f, -0.277785f,  0.0f, -0.415735f,
			-0.555570f,  0.0f, -0.831470f, -0.297850f,  0.0f, -0.401604f, -0.277785f,  0.0f, -0.415735f,
			-0.555570f,  0.0f, -0.831470f, -0.595699f,  0.0f, -0.803208f, -0.297850f,  0.0f, -0.401604f,
			-0.595699f,  0.0f, -0.803208f, -0.317197f,  0.0f, -0.386505f, -0.297850f,  0.0f, -0.401604f,
			-0.595699f,  0.0f, -0.803208f, -0.634393f,  0.0f, -0.773010f, -0.317197f,  0.0f, -0.386505f,
			-0.634393f,  0.0f, -0.773010f, -0.335779f,  0.0f, -0.370476f, -0.317197f,  0.0f, -0.386505f,
			-0.634393f,  0.0f, -0.773010f, -0.671559f,  0.0f, -0.740951f, -0.335779f,  0.0f, -0.370476f,
			-0.671559f,  0.0f, -0.740951f, -0.353553f,  0.0f, -0.353553f, -0.335779f,  0.0f, -0.370476f,
			-0.671559f,  0.0f, -0.740951f, -0.707107f,  0.0f, -0.707107f, -0.353553f,  0.0f, -0.353553f,
			-0.707107f,  0.0f, -0.707107f, -0.370476f,  0.0f, -0.335779f, -0.353553f,  0.0f, -0.353553f,
			-0.707107f,  0.0f, -0.707107f, -0.740951f,  0.0f, -0.671559f, -0.370476f,  0.0f, -0.335779f,
			-0.740951f,  0.0f, -0.671559f, -0.386505f,  0.0f, -0.317197f, -0.370476f,  0.0f, -0.335779f,
			-0.740951f,  0.0f, -0.671559f, -0.773010f,  0.0f, -0.634393f, -0.386505f,  0.0f, -0.317197f,
			-0.773010f,  0.0f, -0.634393f, -0.401604f,  0.0f, -0.297850f, -0.386505f,  0.0f, -0.317197f,
			-0.773010f,  0.0f, -0.634393f, -0.803208f,  0.0f, -0.595699f, -0.401604f,  0.0f, -0.297850f,
			-0.803208f,  0.0f, -0.595699f, -0.415735f,  0.0f, -0.277785f, -0.401604f,  0.0f, -0.297850f,
			-0.803208f,  0.0f, -0.595699f, -0.831470f,  0.0f, -0.555570f, -0.415735f,  0.0f, -0.277785f,
			-0.831470f,  0.0f, -0.555570f, -0.428864f,  0.0f, -0.257051f, -0.415735f,  0.0f, -0.277785f,
			-0.831470f,  0.0f, -0.555570f, -0.857729f,  0.0f, -0.514103f, -0.428864f,  0.0f, -0.257051f,
			-0.857729f,  0.0f, -0.514103f, -0.440961f,  0.0f, -0.235698f, -0.428864f,  0.0f, -0.257051f,
			-0.857729f,  0.0f, -0.514103f, -0.881921f,  0.0f, -0.471397f, -0.440961f,  0.0f, -0.235698f,
			-0.881921f,  0.0f, -0.471397f, -0.451995f,  0.0f, -0.213778f, -0.440961f,  0.0f, -0.235698f,
			-0.881921f,  0.0f, -0.471397f, -0.903989f,  0.0f, -0.427555f, -0.451995f,  0.0f, -0.213778f,
			-0.903989f,  0.0f, -0.427555f, -0.461940f,  0.0f, -0.191342f, -0.451995f,  0.0f, -0.213778f,
			-0.903989f,  0.0f, -0.427555f, -0.923880f,  0.0f, -0.382683f, -0.461940f,  0.0f, -0.191342f,
			-0.923880f,  0.0f, -0.382683f, -0.470772f,  0.0f, -0.168445f, -0.461940f,  0.0f, -0.191342f,
			-0.923880f,  0.0f, -0.382683f, -0.941544f,  0.0f, -0.336890f, -0.470772f,  0.0f, -0.168445f,
			-0.941544f,  0.0f, -0.336890f, -0.478470f,  0.0f, -0.145142f, -0.470772f,  0.0f, -0.168445f,
			-0.941544f,  0.0f, -0.336890f, -0.956940f,  0.0f, -0.290285f, -0.478470f,  0.0f, -0.145142f,
			-0.956940f,  0.0f, -0.290285f, -0.485016f,  0.0f, -0.121490f, -0.478470f,  0.0f, -0.145142f,
			-0.956940f,  0.0f, -0.290285f, -0.970031f,  0.0f, -0.242980f, -0.485016f,  0.0f, -0.121490f,
			-0.970031f,  0.0f, -0.242980f, -0.490393f,  0.0f, -0.097545f, -0.485016f,  0.0f, -0.121490f,
			-0.970031f,  0.0f, -0.242980f, -0.980785f,  0.0f, -0.195090f, -0.490393f,  0.0f, -0.097545f,
			-0.980785f,  0.0f, -0.195090f, -0.494588f,  0.0f, -0.073365f, -0.490393f,  0.0f, -0.097545f,
			-0.980785f,  0.0f, -0.195090f, -0.989177f,  0.0f, -0.146730f, -0.494588f,  0.0f, -0.073365f,
			-0.989177f,  0.0f, -0.146730f, -0.497592f,  0.0f, -0.049009f, -0.494588f,  0.0f, -0.073365f,
			-0.989177f,  0.0f, -0.146730f, -0.995185f,  0.0f, -0.098017f, -0.497592f,  0.0f, -0.049009f,
			-0.995185f,  0.0f, -0.098017f, -0.499398f,  0.0f, -0.024534f, -0.497592f,  0.0f, -0.049009f,
			-0.995185f,  0.0f, -0.098017f, -0.998795f,  0.0f, -0.049068f, -0.499398f,  0.0f, -0.024534f,
			-0.998795f,  0.0f, -0.049068f, -0.500000f,  0.0f, -0.000000f, -0.499398f,  0.0f, -0.024534f,
			-0.998795f,  0.0f, -0.049068f, -1.000000f,  0.0f, -0.000000f, -0.500000f,  0.0f, -0.000000f,
			-1.000000f,  0.0f, -0.000000f, -0.499398f,  0.0f, 0.024534f, -0.500000f,  0.0f, -0.000000f,
			-1.000000f,  0.0f, -0.000000f, -0.998795f,  0.0f, 0.049068f, -0.499398f,  0.0f, 0.024534f,
			-0.998795f,  0.0f, 0.049068f, -0.497592f,  0.0f, 0.049009f, -0.499398f,  0.0f, 0.024534f,
			-0.998795f,  0.0f, 0.049068f, -0.995185f,  0.0f, 0.098017f, -0.497592f,  0.0f, 0.049009f,
			-0.995185f,  0.0f, 0.098017f, -0.494588f,  0.0f, 0.073365f, -0.497592f,  0.0f, 0.049009f,
			-0.995185f,  0.0f, 0.098017f, -0.989177f,  0.0f, 0.146730f, -0.494588f,  0.0f, 0.073365f,
			-0.989177f,  0.0f, 0.146730f, -0.490393f,  0.0f, 0.097545f, -0.494588f,  0.0f, 0.073365f,
			-0.989177f,  0.0f, 0.146730f, -0.980785f,  0.0f, 0.195090f, -0.490393f,  0.0f, 0.097545f,
			-0.980785f,  0.0f, 0.195090f, -0.485016f,  0.0f, 0.121490f, -0.490393f,  0.0f, 0.097545f,
			-0.980785f,  0.0f, 0.195090f, -0.970031f,  0.0f, 0.242980f, -0.485016f,  0.0f, 0.121490f,
			-0.970031f,  0.0f, 0.242980f, -0.478470f,  0.0f, 0.145142f, -0.485016f,  0.0f, 0.121490f,
			-0.970031f,  0.0f, 0.242980f, -0.956940f,  0.0f, 0.290285f, -0.478470f,  0.0f, 0.145142f,
			-0.956940f,  0.0f, 0.290285f, -0.470772f,  0.0f, 0.168445f, -0.478470f,  0.0f, 0.145142f,
			-0.956940f,  0.0f, 0.290285f, -0.941544f,  0.0f, 0.336890f, -0.470772f,  0.0f, 0.168445f,
			-0.941544f,  0.0f, 0.336890f, -0.461940f,  0.0f, 0.191342f, -0.470772f,  0.0f, 0.168445f,
			-0.941544f,  0.0f, 0.336890f, -0.923880f,  0.0f, 0.382683f, -0.461940f,  0.0f, 0.191342f,
			-0.923880f,  0.0f, 0.382683f, -0.451995f,  0.0f, 0.213778f, -0.461940f,  0.0f, 0.191342f,
			-0.923880f,  0.0f, 0.382683f, -0.903989f,  0.0f, 0.427555f, -0.451995f,  0.0f, 0.213778f,
			-0.903989f,  0.0f, 0.427555f, -0.440961f,  0.0f, 0.235698f, -0.451995f,  0.0f, 0.213778f,
			-0.903989f,  0.0f, 0.427555f, -0.881921f,  0.0f, 0.471397f, -0.440961f,  0.0f, 0.235698f,
			-0.881921f,  0.0f, 0.471397f, -0.428864f,  0.0f, 0.257051f, -0.440961f,  0.0f, 0.235698f,
			-0.881921f,  0.0f, 0.471397f, -0.857729f,  0.0f, 0.514103f, -0.428864f,  0.0f, 0.257051f,
			-0.857729f,  0.0f, 0.514103f, -0.415735f,  0.0f, 0.277785f, -0.428864f,  0.0f, 0.257051f,
			-0.857729f,  0.0f, 0.514103f, -0.831470f,  0.0f, 0.555570f, -0.415735f,  0.0f, 0.277785f,
			-0.831470f,  0.0f, 0.555570f, -0.401604f,  0.0f, 0.297850f, -0.415735f,  0.0f, 0.277785f,
			-0.831470f,  0.0f, 0.555570f, -0.803208f,  0.0f, 0.595699f, -0.401604f,  0.0f, 0.297850f,
			-0.803208f,  0.0f, 0.595699f, -0.386505f,  0.0f, 0.317197f, -0.401604f,  0.0f, 0.297850f,
			-0.803208f,  0.0f, 0.595699f, -0.773010f,  0.0f, 0.634393f, -0.386505f,  0.0f, 0.317197f,
			-0.773010f,  0.0f, 0.634393f, -0.370476f,  0.0f, 0.335779f, -0.386505f,  0.0f, 0.317197f,
			-0.773010f,  0.0f, 0.634393f, -0.740951f,  0.0f, 0.671559f, -0.370476f,  0.0f, 0.335779f,
			-0.740951f,  0.0f, 0.671559f, -0.353553f,  0.0f, 0.353553f, -0.370476f,  0.0f, 0.335779f,
			-0.740951f,  0.0f, 0.671559f, -0.707107f,  0.0f, 0.707107f, -0.353553f,  0.0f, 0.353553f,
			-0.707107f,  0.0f, 0.707107f, -0.335779f,  0.0f, 0.370476f, -0.353553f,  0.0f, 0.353553f,
			-0.707107f,  0.0f, 0.707107f, -0.671559f,  0.0f, 0.740951f, -0.335779f,  0.0f, 0.370476f,
			-0.671559f,  0.0f, 0.740951f, -0.317197f,  0.0f, 0.386505f, -0.335779f,  0.0f, 0.370476f,
			-0.671559f,  0.0f, 0.740951f, -0.634393f,  0.0f, 0.773010f, -0.317197f,  0.0f, 0.386505f,
			-0.634393f,  0.0f, 0.773010f, -0.297850f,  0.0f, 0.401604f, -0.317197f,  0.0f, 0.386505f,
			-0.634393f,  0.0f, 0.773010f, -0.595699f,  0.0f, 0.803208f, -0.297850f,  0.0f, 0.401604f,
			-0.595699f,  0.0f, 0.803208f, -0.277785f,  0.0f, 0.415735f, -0.297850f,  0.0f, 0.401604f,
			-0.595699f,  0.0f, 0.803208f, -0.555570f,  0.0f, 0.831470f, -0.277785f,  0.0f, 0.415735f,
			-0.555570f,  0.0f, 0.831470f, -0.257051f,  0.0f, 0.428864f, -0.277785f,  0.0f, 0.415735f,
			-0.555570f,  0.0f, 0.831470f, -0.514103f,  0.0f, 0.857729f, -0.257051f,  0.0f, 0.428864f,
			-0.514103f,  0.0f, 0.857729f, -0.235698f,  0.0f, 0.440961f, -0.257051f,  0.0f, 0.428864f,
			-0.514103f,  0.0f, 0.857729f, -0.471397f,  0.0f, 0.881921f, -0.235698f,  0.0f, 0.440961f,
			-0.471397f,  0.0f, 0.881921f, -0.213778f,  0.0f, 0.451995f, -0.235698f,  0.0f, 0.440961f,
			-0.471397f,  0.0f, 0.881921f, -0.427555f,  0.0f, 0.903989f, -0.213778f,  0.0f, 0.451995f,
			-0.427555f,  0.0f, 0.903989f, -0.191342f,  0.0f, 0.461940f, -0.213778f,  0.0f, 0.451995f,
			-0.427555f,  0.0f, 0.903989f, -0.382683f,  0.0f, 0.923880f, -0.191342f,  0.0f, 0.461940f,
			-0.382683f,  0.0f, 0.923880f, -0.168445f,  0.0f, 0.470772f, -0.191342f,  0.0f, 0.461940f,
			-0.382683f,  0.0f, 0.923880f, -0.336890f,  0.0f, 0.941544f, -0.168445f,  0.0f, 0.470772f,
			-0.336890f,  0.0f, 0.941544f, -0.145142f,  0.0f, 0.478470f, -0.168445f,  0.0f, 0.470772f,
			-0.336890f,  0.0f, 0.941544f, -0.290285f,  0.0f, 0.956940f, -0.145142f,  0.0f, 0.478470f,
			-0.290285f,  0.0f, 0.956940f, -0.121490f,  0.0f, 0.485016f, -0.145142f,  0.0f, 0.478470f,
			-0.290285f,  0.0f, 0.956940f, -0.242980f,  0.0f, 0.970031f, -0.121490f,  0.0f, 0.485016f,
			-0.242980f,  0.0f, 0.970031f, -0.097545f,  0.0f, 0.490393f, -0.121490f,  0.0f, 0.485016f,
			-0.242980f,  0.0f, 0.970031f, -0.195090f,  0.0f, 0.980785f, -0.097545f,  0.0f, 0.490393f,
			-0.195090f,  0.0f, 0.980785f, -0.073365f,  0.0f, 0.494588f, -0.097545f,  0.0f, 0.490393f,
			-0.195090f,  0.0f, 0.980785f, -0.146730f,  0.0f, 0.989177f, -0.073365f,  0.0f, 0.494588f,
			-0.146730f,  0.0f, 0.989177f, -0.049009f,  0.0f, 0.497592f, -0.073365f,  0.0f, 0.494588f,
			-0.146730f,  0.0f, 0.989177f, -0.098017f,  0.0f, 0.995185f, -0.049009f,  0.0f, 0.497592f,
			-0.098017f,  0.0f, 0.995185f, -0.024534f,  0.0f, 0.499398f, -0.049009f,  0.0f, 0.497592f,
			-0.098017f,  0.0f, 0.995185f, -0.049068f,  0.0f, 0.998795f, -0.024534f,  0.0f, 0.499398f,
			-0.049068f,  0.0f, 0.998795f, 0.000000f,  0.0f, 0.500000f, -0.024534f,  0.0f, 0.499398f,
			-0.049068f,  0.0f, 0.998795f, 0.000000f,  0.0f, 1.000000f, 0.000000f,  0.0f, 0.500000f,
		};
		// The rings are made up of squares (two triangles)
		// Each square has the exact same texture coordinates
		// So texture coordinates can be generated via a loop
		float[] texcoords = new float[vertices.length];
		for (int i = 0; i < vertices.length / 18; i++) {
			texcoords[i * 12    ] = 1.0f;
			texcoords[i * 12 + 1] = 0.0f;
			texcoords[i * 12 + 2] = 0.0f;
			texcoords[i * 12 + 3] = 1.0f;
			texcoords[i * 12 + 4] = 0.0f;
			texcoords[i * 12 + 5] = 0.0f;
			texcoords[i * 12 + 6] = 1.0f;
			texcoords[i * 12 + 7] = 0.0f;
			texcoords[i * 12 + 8] = 1.0f;
			texcoords[i * 12 + 9] = 1.0f;
			texcoords[i * 12 +10] = 0.0f;
			texcoords[i * 12 +11] = 1.0f;
		}

		// Since the shape is a 2D shape, all normals point straight up
		float[] normals = new float[vertices.length];
		for (int i = 0; i < vertices.length / 3; i++) {
			normals[i * 3] = 0.0f;
			normals[i * 3 + 1] = 1.0f;
			normals[i * 3 + 2] = 0.0f;
		}

		int[] indices = new int[vertices.length];
		for (int i = 0; i < vertices.length; i++) {
			indices[i] = i;
		}

		rings.setVertexBuffer(BufferUtil.directFloatBuffer(vertices));
		rings.setNormalsBuffer(BufferUtil.directFloatBuffer(normals));
		rings.setTextureCoordBuffer(BufferUtil.directFloatBuffer(texcoords));
		rings.setIndexBuffer(BufferUtil.directIntBuffer(indices));
		
		Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
		TextureState tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		tstate.setTexture(sm.getTextureManager().getAssetByPath("2k_saturn_ring_black.png"));

		FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);
		if (rev)
			faceState.setVertexWinding(FrontFaceState.VertexWinding.CLOCKWISE);
		ZBufferState zstate = (ZBufferState) sm.getRenderSystem().createRenderState(Type.ZBUFFER);
		
		rings.setDataSource(DataSource.INDEX_BUFFER);
		rings.setRenderState(faceState);
		rings.setRenderState(tstate);
		rings.setRenderState(zstate);
		rings.setMaterial(mat);
		

		Planet saturn = planets[6]; // sloppy style
		
		SceneNode ringsN = sm.getSceneNode(saturn.name() + "Node").createChildSceneNode("ringsNode" + nameAppend);
		ringsN.attachObject(rings);

		final float scaleAmount = 4f;
		ringsN.scale(scaleAmount, scaleAmount, scaleAmount);
		return ringsN;
	}
	
	// Set the HUD banner message
	protected void setBannerMsg(String str) {
		this.bannerMsg = str;
		this.bannerTime = 3000;
	}

	// Setup and add a sky box to the scene
	private void createSkyBox(Engine engine, SceneManager sm) throws IOException {
		Configuration conf = engine.getConfiguration();
		TextureManager textureMgr = engine.getTextureManager();

		textureMgr.setBaseDirectoryPath(conf.valueOf("assets.skyboxes.path"));
		Texture front = textureMgr.getAssetByPath("2k_stars_milky_way.jpg");
		Texture back = textureMgr.getAssetByPath("2k_stars.jpg");
		Texture left = textureMgr.getAssetByPath("2k_stars.jpg");
		Texture right = textureMgr.getAssetByPath("2k_stars.jpg");
		Texture top = textureMgr.getAssetByPath("2k_stars.jpg");
		Texture bottom = textureMgr.getAssetByPath("2k_stars.jpg");
		textureMgr.setBaseDirectoryPath(conf.valueOf("assets.textures.path"));

		SkyBox sb = sm.createSkyBox("SkyBox");
		sb.setTexture(front, SkyBox.Face.FRONT);
		sb.setTexture(back, SkyBox.Face.BACK);
		sb.setTexture(left, SkyBox.Face.LEFT);
		sb.setTexture(right, SkyBox.Face.RIGHT);
		sb.setTexture(top, SkyBox.Face.TOP);
		sb.setTexture(bottom, SkyBox.Face.BOTTOM);
		sm.setActiveSkyBox(sb);
	}
	
	private void executeScript(File scriptFile) {
		try {
			FileReader fileReader = new FileReader(scriptFile);
			jsEngine.eval(fileReader); //execute the script statements in the file
			fileReader.close();
		}
		catch (FileNotFoundException e1) {
			System.out.println(scriptFile + " not found " + e1);
		}
		catch (IOException e2) {
			System.out.println("IO problem with " + scriptFile + e2);
		}
		catch (ScriptException e3) {
			System.out.println("ScriptException in " + scriptFile + e3);
		}
		catch (NullPointerException e4) {
			System.out.println ("Null ptr exception in " + scriptFile + e4);
		}
	}
	
	public float getMoveSpeed() {
		return MOVE_SPEED;
	}
	
	public float getRotateSpeed() {
		return ROTATE_SPEED;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	public Vector3 getPlayerPosition() {
		return playerNode.getWorldPosition();
	}
	
	public Matrix3 getPlayerRotation() {
		return playerNode.getWorldRotation();
	}
	
	public void createGhostAvatar(UUID ghostID, Vector3 ghostPosition) {
		SceneManager sm = getEngine().getSceneManager();
		SceneNode ghostN = sm.getRootSceneNode().createChildSceneNode(ghostID.toString());
		ghostN.setLocalPosition(ghostPosition);
		ghostN.attachObject(ghostE);
		ghostAvatars.put(ghostID, new GhostAvatar(ghostID, ghostN, ghostE));
	}
	
	public void updateGhostAvatar(UUID ghostID, Vector3 ghostPosition, Matrix3 ghostRotation) {
		SceneManager sm = getEngine().getSceneManager();
		try {
			SceneNode ghostN = sm.getSceneNode(ghostID.toString());
			ghostN.setLocalPosition(ghostPosition);
			ghostN.setLocalRotation(ghostRotation);
		}
		catch (RuntimeException E) {
			createGhostAvatar(ghostID, ghostPosition);
		}
	}
	
	public void removeGhostAvatar(UUID ghostID) {
		SceneManager sm = getEngine().getSceneManager();
		SceneNode ghostToRemove = sm.getSceneNode(ghostID.toString());
		sm.getRootSceneNode().detachChild(ghostToRemove);
		ghostAvatars.remove(ghostID);
	}
}