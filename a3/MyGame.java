package a3;

import myGameEngine.*;
import myGameEngine.Networking.ProtocolClient;
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
import ray.rage.rendersystem.states.TextureState.WrapMode;
import ray.rage.scene.Camera;
import ray.rage.scene.Entity;
import ray.rage.scene.Light;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneNode;
import ray.rage.scene.SkyBox;
import ray.rage.scene.Tessellation;
import ray.rage.scene.Camera.Frustum.*;
import ray.rage.util.Configuration;
import ray.rml.Degreef;
import ray.rml.Matrix3;
import ray.rml.Matrix3f;
import ray.rml.Vector3;
import ray.rml.Vector3f;

import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.rmi.UnknownHostException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.UUID;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.ImageIcon;

import net.java.games.input.Component;

public class MyGame extends VariableFrameRateGame {

	private GenericInputManager im = new GenericInputManager();
	private GL4RenderSystem rs;
	private int elapsedMs = 0;
	private float MOVE_SPEED = 0.01f;
	private float ROTATE_SPEED = 0.1f;
	private ScriptEngine jsEngine = null;
	private File script = new File("script.js");
	private long lastScriptModifiedTime = 0;
	private SceneNode playerNode = null;
	private SceneNode playerAvatar = null;
	private String serverAddr;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient clientProtocol;
	private boolean isConnected = false;
	private boolean isOnGround = true;
	private float gravity = -10f;
	private boolean isAccelerating = false;
	private boolean isDeccelerating = false;
	private boolean isBraking = false;
	private float accelerationRate = 10f;
	private float deccelerationRate = -15f;
	private float MAX_SPEED = 20f;
	private float MAX_REVERSE_SPEED = -5f;
	private float roadFriction = 5f;
	private float vForward = 0f; // forward velicty
	private float vUp = 0f; // upward velocity
	private float currentPitch = 0f;
	private float currentRoll = 0f;
	private GameState gameState = new GameState();

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
		rw.setTitle("Luigi Kart (Aaron Hartigan + Alexandru Seremet)");
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
		setupNetworking();
		executeScript(script);
		setupHUD();
		createGroundPlane(sm);
		createDolphinWithCamera(sm);
		createBanana(sm);

		setupInputs();
		createSkyBox(eng, sm);

		setupAmbientLight(sm);
		setupPointLight(sm);
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
		float elapsTime = engine.getElapsedTimeMillis();
		processNetworking(elapsTime);
		setAccelerating(false);
		setDeccelerating(false);
		im.update(elapsTime);
		updateHUD(engine, elapsTime);
		updatePlayerPhysics(elapsTime);
		long modifiedTime = script.lastModified();
		if (modifiedTime > lastScriptModifiedTime) {
			lastScriptModifiedTime = modifiedTime;
			updateScriptConstants();
		}
		clientProtocol.updatePlayerInformation(
			this.getPlayerPosition(),
			this.getPlayerRotation()
		);
		updateGameState();
	}
	
	
	public void updateHUD(Engine engine, float elapsTime) {
		rs = (GL4RenderSystem) engine.getRenderSystem();
		elapsedMs += elapsTime;
		
		ArrayList<HUDString> stringList = rs.getHUDStringsList();

		int bottomLeftX = 15;
		int bottomLeftY = 15;
		stringList.get(0).setAll("Lap 1/3", bottomLeftX, bottomLeftY);
		
		int topLeftX = 15;
		int topLeftY = rs.getCanvas().getHeight() - 30;
		stringList.get(1).setAll("1st", topLeftX, topLeftY);

		int bottomRightX = rs.getCanvas().getWidth() - 100;
		int bottomRightY = 15;
		stringList.get(2).setAll("" + Math.round(vForward * 3) + " MPH", bottomRightX, bottomRightY);
		
		int topRightX = rs.getCanvas().getWidth() - 175;
		int topRightY = rs.getCanvas().getHeight() - 30;
		int ms = elapsedMs % 1000;
		int sec = ((elapsedMs - ms) / 1000) % 60;
		int min = (((elapsedMs - ms) / 1000) - sec) / 60;
		stringList.get(3).setAll(
			"time " +
			String.format("%02d", min) +
			":" +
			String.format("%02d", sec) +
			":" +
			String.format("%03d", ms),
			topRightX,
			topRightY
		);
	}

	private void processNetworking(float elapsTime) {
		if (clientProtocol != null) {
			clientProtocol.processPackets();
		}
	}
	
	protected float getGroundHeight(float x, float z) {
		Tessellation plane = getEngine().getSceneManager().getTessellation("plane");
		return plane.getWorldHeight(x, z);
	}

	protected void updatePlayerPhysics(float elapsedMS) {
		float elapsedSec = elapsedMS / 1000;
		Vector3 lp = playerNode.getLocalPosition();
		Vector3 fv = playerNode.getLocalForwardAxis();

		// Handle forward movement
		if (isOnGround) {
			// adjust forward velocity
			if (isAccelerating()) {
				vForward += accelerationRate * elapsedSec;
			}
			if (isDeccelerating()) {
				vForward += deccelerationRate * elapsedSec;
			}
			if (!isAccelerating() && !isDeccelerating() && Math.abs(vForward) < 0.1f) {
				// Clamp to prevent small movement when player should be stationary
				vForward = 0f;
			}
			else {
				float frictionDirection = vForward > 0f ? -1f : 1f;
				vForward += roadFriction * frictionDirection * elapsedSec;
				vForward = Math.max(vForward, MAX_REVERSE_SPEED);
				vForward = Math.min(vForward, MAX_SPEED);
			}
		}
		// apply forward velocity
		fv = fv.mult(vForward * elapsedSec);
		playerNode.setLocalPosition(lp.add(fv));
		
		// Handle new location's height
		lp = playerNode.getLocalPosition();
		float OFFSET = 0.3f;
		float groundHeight = getGroundHeight(lp.x(), lp.z()) + OFFSET;
		float currentHeight = lp.y();
		// If avatar was already on the ground, and the ground is near, "snap" the avatar to the ground
		if (isOnGround && (currentHeight - groundHeight) < 0.2f) {
			currentHeight = groundHeight;
		}
		// If falling, update currentHeight with gravity
		if (currentHeight > groundHeight) {
			isOnGround = false;
			vUp += gravity * elapsedSec;
			currentHeight += vUp * elapsedSec;
		}
		// Must check if gravity has put us below ground (or going uphill)
		if (currentHeight < groundHeight) {
			isOnGround = true;
			vUp = 0;
			currentHeight = groundHeight;
		}
		playerNode.setLocalPosition(lp.x(), currentHeight, lp.z());

		// Change the avatar's angle (visual change only)
		if (isOnGround) {
			Vector3 heading;
			float heightChange;
			playerAvatar.setLocalRotation(Matrix3f.createIdentityMatrix());
			
			// Calculate pitch
			float CAR_LENGTH = 0.5f;
			playerNode.moveForward(CAR_LENGTH);
			heading = playerNode.getLocalPosition();
			playerNode.moveBackward(CAR_LENGTH);
			heightChange = (getGroundHeight(heading.x(), heading.z()) + OFFSET) - currentHeight;
			float pitchAngle = (float) Math.toDegrees(Math.atan(heightChange / CAR_LENGTH));
			float newPitch = (pitchAngle - currentPitch) * elapsedSec * 3 + currentPitch;
			playerAvatar.pitch(Degreef.createFrom(-newPitch));
			currentPitch = newPitch;
			
			// Calculate roll
			float CAR_WIDTH = 0.25f;
			playerNode.moveRight(CAR_WIDTH);
			heading = playerNode.getLocalPosition();
			playerNode.moveLeft(CAR_WIDTH);
			heightChange = (getGroundHeight(heading.x(), heading.z()) + OFFSET) - currentHeight;
			float rollAngle = (float) Math.toDegrees(Math.atan(heightChange / CAR_WIDTH));
			float newRoll = (rollAngle - currentRoll) * elapsedSec * 5 + currentRoll;
			playerAvatar.roll(Degreef.createFrom(newRoll));
			currentRoll = newRoll;
		}
	}
	
	protected void createBanana(SceneManager sm) throws IOException {
		Entity bananaE = sm.createEntity("banana", "banana.obj");
		SceneNode bananaN = sm.getRootSceneNode().createChildSceneNode(bananaE.getName() + "Node");
		bananaN.attachObject(bananaE);
		Vector3 lp = bananaN.getLocalPosition();
		float height = getGroundHeight(lp.x(), lp.z());
		bananaN.translate(0f, height, 0f);
		bananaN.scale(0.3f, 0.3f, 0.3f);
	}

	protected void createDolphinWithCamera(SceneManager sm) throws IOException {
		Entity dolphinE = sm.createEntity("dolphin", "dolphinHighPoly.obj");
		dolphinE.setPrimitive(Primitive.TRIANGLES);
		SceneNode dolphinN = sm.getRootSceneNode().createChildSceneNode(dolphinE.getName() + "Node");
		SceneNode playerAvatarN = dolphinN.createChildSceneNode("playerAvatar");
		playerNode = dolphinN;
		playerAvatar = playerAvatarN;
		dolphinN.translate(-40f, 0, -80f);
		playerAvatarN.attachObject(dolphinE);
		//dolphinN.setPhysicsObject(new PhysicsObject());

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
		sm.getAmbientLight().setIntensity(new Color(0.3f, 0.3f, 0.3f));
	}
	
	protected void setupPointLight(SceneManager sm) throws IOException {
		Entity lightE = sm.createEntity("sun", "cube.obj");
		lightE.setPrimitive(Primitive.TRIANGLES);

		SceneNode lightN = sm.getRootSceneNode().createChildSceneNode("lightNode");
		lightN.translate(80f, 100f, 10f);
		lightN.scale(0.0000001f, 0.0000001f, 0.0000001f);
		Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
		mat.setEmissive(Color.WHITE);
		lightE.setMaterial(mat);
		lightN.attachObject(lightE);
		
		Light sunlight = sm.createLight("sunLight", Light.Type.POINT);
		sunlight.setAmbient(new Color(.15f, .15f, .15f));
		sunlight.setDiffuse(new Color(1f, 1f, .8f));
		sunlight.setSpecular(new Color(.1f, .1f, .1f));
		sunlight.setRange(1000f);
		sunlight.setConstantAttenuation(0.8f);
		sunlight.setLinearAttenuation(0.0000001f);
		sunlight.setQuadraticAttenuation(0f);
		sunlight.setFalloffExponent(0f);
		lightN.attachObject(sunlight);
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
					new StartAccelerationAction(this),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
				im.associateAction(
					c,
					Component.Identifier.Key.S,
					new StartDeccelerationAction(this),
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
	
	protected void createGroundPlane(SceneManager sm) throws IOException {
		Tessellation plane = sm.createTessellation("plane");
		SceneNode planeN = sm.getRootSceneNode().createChildSceneNode("planeNode");
		planeN.attachObject(plane);
		planeN.scale(250, 1, 250);

		plane.getTextureState().setWrapMode(WrapMode.REPEAT_MIRRORED);
		plane.setTexture(getEngine(), "hexagons.jpeg");
		plane.setTextureTiling(16);

		plane.setHeightMap(getEngine(), "height_map.png");
		plane.setQuality(8);
		plane.setMultiplier(10);
	}
	
	protected double calcDistance(float x1, float y1, float z1, float x2, float y2, float z2) {
		float dx = (x1 - x2);
		float dy = (y1 - y2);
		float dz = (z1 - z2);
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
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
		return playerAvatar.getWorldRotation();
	}
	
	public void createGhostAvatar(UUID ghostID, Vector3 ghostPosition) {
		try {
			SceneManager sm = getEngine().getSceneManager();
			SceneNode ghostN = sm.getRootSceneNode().createChildSceneNode(ghostID.toString());
			ghostN.setLocalPosition(ghostPosition);
			Entity dolphinE = sm.createEntity(ghostID.toString(), "dolphinHighPoly.obj");
			ghostN.attachObject(dolphinE);
			gameState.createGhostAvatar(ghostID, ghostPosition);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updateGhostAvatar(UUID ghostID, Vector3 ghostPosition, Matrix3 ghostRotation) {
		try {
			SceneManager sm = getEngine().getSceneManager();
			if (!sm.hasSceneNode(ghostID.toString())) {
				System.out.println("Ghost does not exist.  Creating: " + ghostID.toString());
				createGhostAvatar(ghostID, ghostPosition);
				return;
			}
			gameState.updateGhostAvatar(ghostID, ghostPosition, ghostRotation);
		}
		catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
	
	public void updateGameState() {
		SceneManager sm = getEngine().getSceneManager();
		for (Entry<UUID, GhostAvatar> entry : gameState.getGhostAvatars().entrySet()) {
			SceneNode ghostN = sm.getSceneNode(entry.getKey().toString());
			ghostN.setLocalPosition(entry.getValue().getPos());
			ghostN.setLocalRotation(entry.getValue().getRot());
		}
	}
	
	public void removeGhostAvatar(UUID ghostID) {
		try {
			SceneManager sm = getEngine().getSceneManager();
			sm.destroySceneNode(ghostID.toString());
			gameState.getGhostAvatars().remove(ghostID);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    @Override
    public void shutdown() {
    	super.shutdown();
        if (clientProtocol != null) {
        	clientProtocol.sendByeMessage();
        }
    }

	public boolean isAccelerating() {
		return isAccelerating;
	}

	public void setAccelerating(boolean isAccelerating) {
		this.isAccelerating = isAccelerating;
	}

	public boolean isBraking() {
		return isBraking;
	}

	public void setBraking(boolean isBraking) {
		this.isBraking = isBraking;
	}

	public boolean isDeccelerating() {
		return isDeccelerating;
	}

	public void setDeccelerating(boolean isDeccelerating) {
		this.isDeccelerating = isDeccelerating;
	}

}