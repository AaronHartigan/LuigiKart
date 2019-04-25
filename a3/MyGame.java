package a3;

import myGameEngine.*;
import myGameEngine.Networking.ProtocolClient;
import myGameEngine.controllers.BananaDeathAnimationController;
import myGameEngine.controllers.ItemGrowthController;
import myGameEngine.controllers.NodeOrbitController;
import myGameEngine.controllers.ParticleController;
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
import ray.rage.rendersystem.Renderable.Primitive;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.states.RenderState;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.rendersystem.states.CullingState;
import ray.rage.rendersystem.states.TextureState.WrapMode;
import ray.rage.rendersystem.shader.GpuShaderProgram;
import ray.rage.scene.Camera;
import ray.rage.scene.Entity;
import ray.rage.scene.Light;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneNode;
import ray.rage.scene.SkyBox;
import ray.rage.scene.Tessellation;
import ray.rage.scene.controllers.RotationController;
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
import java.util.HashMap;
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
	private float MOVE_SPEED = 0.01f;
	private float ROTATE_SPEED = 0.1f;
	private ScriptEngine jsEngine = null;
	private File script = new File("script.js");
	private long lastScriptModifiedTime = 0;
	private SceneNode playerNode = null;
	private SceneNode playerAvatar = null;
	private SceneNode playerAvatarRotator = null;
	private String serverAddr;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient clientProtocol;
	private Item item = null;
	private ClientState clientState = new ClientState();
	private boolean isOnGround = true;
	private float gravity = -10f;
	private boolean isAccelerating = false;
	private boolean isDeccelerating = false;
	private boolean isBraking = false;
	private boolean isOnSpeedBoost = false;
	private float accelerationRate = 20f;
	private float deccelerationRate = -15f;
	private float TURN_RATE = 50f;
	private float spinDirection = 0f;
	private float spinoutTimer = 0f;
	private float SPINOUT_DURATION = 1500f;
	private float speedBoostTimer = 0f;
	private float SPEED_BOOST_DURATION = 1000f;
	private float DRIFTING_TURN_RATE = 75f;
	private boolean isDrifting = false;
	private float driftingDirection = 0f;
	private float MAX_BASE_SPEED = 20f;
	private float roadFriction = 10f;
	private float desiredTurn = 0f;
	private float desiredTurnRollingAverage = 0f;
	private float actualTurn = 0f;
	private float vForward = 0f; // forward velicty
	private float vUp = 0f; // upward velocity
	private float currentPitch = 0f;
	private float currentRoll = 0f;
	private GameState gameState = new GameState();
	private static long particleID = 0;
	private PreloadTextures textures = null;
	private TimerGui timerGui = null;
	private int raceLap = 0;
	private int currentZone = 3;
	private final boolean SHOW_PACKET_MESSAGES = false;
	private NodeOrbitController cameraController = null; 

	public static synchronized String createID() {
	    return String.valueOf(particleID++);
	}    

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
		GraphicsDevice device = ge.getDefaultScreenDevice();
		DisplayMode[] modes = device.getDisplayModes();
		DisplayMode fullscreen = modes[modes.length - 1];
		DisplayMode windowed = new DisplayMode(1000, 700, 32, 60);
		/*
		DisplaySettingsDialog dsd = new DisplaySettingsDialog(ge.getDefaultScreenDevice());
		dsd.showIt();
		System.out.println(dsd.getSelectedDisplayMode());
		RenderWindow rw = rs.createRenderWindow(
			dsd.getSelectedDisplayMode(),
			dsd.isFullScreenModeSelected()
		);
		*/
		RenderWindow rw = rs.createRenderWindow(
			windowed,
			false
		);
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
		initMeshes();

		setupInputs();
		createSkyBox(eng, sm);

		setupAmbientLight(sm);
		setupPointLight(sm);
		setTextures(new PreloadTextures(this));
		timerGui = new TimerGui(this);
	}

	private void initMeshes() throws IOException {
		createBanana();
	}
	
	private void createBanana() throws IOException {
		Entity bananaE = getEngine().getSceneManager().createEntity("banana", "banana.obj");
		SceneNode bananaN = getEngine().getSceneManager().getRootSceneNode().createChildSceneNode(bananaE.getName() + "Node");
		bananaN.attachObject(bananaE);
		bananaN.scale(0.01f, 0.01f, 0.01f);
		bananaN.translate(-1000000f, 0f, 0f);
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
			if (SHOW_PACKET_MESSAGES) System.out.println("Sending Join Message");
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
		setDrifting(false);
		setDesiredTurn(0f);
		im.update(elapsTime);
		updateHUD(engine, elapsTime);
		updatePlayerPhysics(elapsTime);
		updateLapInfo();
		long modifiedTime = script.lastModified();
		if (modifiedTime > lastScriptModifiedTime) {
			lastScriptModifiedTime = modifiedTime;
			updateScriptConstants();
		}
		if (hasRaceStarted()) {
			if (SHOW_PACKET_MESSAGES) System.out.println("Sending Update Information");
			clientProtocol.updatePlayerInformation(
				this.getPlayerPosition(),
				this.getPlayerRotation(),
				vForward
			);
		}
		updatePlayerItem();
		updateGameStateDisplay();
		updateItemBoxesRotation();
		// PRINT PLAYER POSITION
		// System.out.println(playerNode.getWorldPosition());
	}

	public void updateHUD(Engine engine, float elapsTime) {
		rs = (GL4RenderSystem) engine.getRenderSystem();
		
		ArrayList<HUDString> stringList = rs.getHUDStringsList();

		int bottomLeftX = 15;
		int bottomLeftY = 15;
		int lap = raceLap == 0 ? 1 : raceLap;
		stringList.get(0).setAll("Lap " + lap + "/3", bottomLeftX, bottomLeftY);
		
		int topLeftX = 15;
		int topLeftY = rs.getCanvas().getHeight() - 30;
		stringList.get(1).setAll("1st", topLeftX, topLeftY);

		int bottomRightX = rs.getCanvas().getWidth() - 100;
		int bottomRightY = 15;
		stringList.get(2).setAll("" + Math.round((vForward + getGravityForce()) * 3) + " MPH", bottomRightX, bottomRightY);
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

	protected boolean getIsSpeedBoost(float x, float z, Vector3 facing, float scale) {
		Tessellation plane = getEngine().getSceneManager().getTessellation("plane");
		return (
			plane.getIsSpeedBoost(x + facing.x() * scale, z + facing.z() * scale) ||
			plane.getIsSpeedBoost(x, z) ||
			plane.getIsSpeedBoost(x - facing.x() * scale, z - facing.z() * scale)
		);
	}

	
	protected void updatePlayerPhysics(float elapsedMS) {
		updateTimers(elapsedMS);
		float elapsedSec = elapsedMS / 1000;
		Vector3 lp = playerNode.getWorldPosition();
		Vector3 fv = playerAvatar.getWorldForwardAxis();
		
		// Handle speed boost
		setOnSpeedBoost(getIsSpeedBoost(lp.x(), lp.z(), fv, 1.5f));
		
		// Handle collision spinning
		if (isSpinning()) {
			playerAvatarRotator.setLocalRotation(Matrix3f.createIdentityMatrix());
			playerAvatarRotator.yaw(Degreef.createFrom(getSpinDirection()));
		}
		else {
			playerAvatarRotator.setLocalRotation(Matrix3f.createIdentityMatrix());
		}
		
		// Handle turning
		if (!isRacingInputDisabled()) {
			desiredTurnRollingAverage = desiredTurnRollingAverage * 0.9f + getDesiredTurn() * 0.1f;
			float dTurn = desiredTurnRollingAverage - actualTurn;
			actualTurn += dTurn / 6;
			float turnDegrees = actualTurn * elapsedSec * getTurnRate();
			// Reduce turn rate if car is moving slowly
			if (Math.abs(vForward) <= 1f) {
				if (Math.abs(vForward) <= 0.05f) {
					turnDegrees = 0;
				}
				else {
					turnDegrees *= ((Math.abs(vForward) * 16 / 19) + 3/19);	
				}
			}
			if (isOnGround) {
				playerNode.yaw(Degreef.createFrom(turnDegrees));
			}
		}
		else {
			desiredTurnRollingAverage = 0f;
		}

		// Handle forward movement
		if (speedBoostTimer > 0f) {
			vForward = getMaxSpeed();
		}
		else if (isOnGround) {
			// adjust forward velocity
			if (isAccelerating()) {
				vForward += accelerationRate * elapsedSec;
			}
			if (isDeccelerating()) {
				vForward += deccelerationRate * elapsedSec;
			}
			if (
				(!isAccelerating() && !isDeccelerating()
				|| isAccelerating() && isDeccelerating())
				&& Math.abs(vForward) < 0.05f
			) {
				// Clamp to prevent small movement when player should be stationary
				vForward = 0f;
			}
			else {
				float frictionDirection = vForward > 0f ? -1f : 1f;
				vForward += roadFriction * frictionDirection * elapsedSec;
				vForward = Math.max(vForward, getMaxReverseSpeed());
				vForward = Math.min(vForward, getMaxSpeed());
			}
		}
		// apply forward velocity
		fv = fv.mult(vForward * elapsedSec + getGravityForce() * elapsedSec);
		
		// Check if player is running into a wall by checking the angle of the avatar
		float currentHeight = lp.y();
		float tCAR_LENGTH = vForward > 0 ? 0.5f : -0.5f;
		playerNode.moveForward(tCAR_LENGTH);
		Vector3 newLP = playerNode.getWorldPosition();
		playerNode.moveBackward(tCAR_LENGTH);
		float heightDifferential = (getGroundHeight(newLP.x(), newLP.z())) - currentHeight;
		float tpitchAngle = (float) Math.toDegrees(Math.atan(heightDifferential / Math.abs(tCAR_LENGTH)));
		if (tpitchAngle > 45f) {
			// Calculate angle and set new angle and velocity
			vForward = 0f;
		}
		else {
			// Move forward
			playerNode.setLocalPosition(lp.add(fv));
		}
		
		// Handle new location's height
		lp = playerNode.getWorldPosition();
		float groundHeight = getGroundHeight(lp.x(), lp.z());
		currentHeight = lp.y();
		// If avatar was already on the ground, and the ground is near, "snap" the avatar to the ground
		if (isOnGround && (currentHeight - groundHeight) < 0.2f) {
			currentHeight = groundHeight;
		}
		// If falling, update currentHeight with gravityd
		if (currentHeight > groundHeight) {
			isOnGround = false;
			vUp += gravity * elapsedSec;
			currentHeight += ((gravity * elapsedSec * elapsedSec / 2) + vUp * elapsedSec);
		}
		// Must check if gravity has put us below ground (or going uphill)
		if (currentHeight < groundHeight) {
			// if we were falling with enough speed, bounce
			if (!isOnGround && vUp < -5f) {
				vUp = Math.abs(vUp) / 8;
			}
			// If not enough downward velocity, just be on the ground
			else {
				vUp = 0;
				isOnGround = true;
			}
			currentHeight = groundHeight;
		}
		playerNode.setLocalPosition(lp.x(), currentHeight, lp.z());
		
		// Change the avatar's angle
		if (isOnGround) {
			Vector3 heading;
			float heightChange;
			playerAvatar.setLocalRotation(Matrix3f.createIdentityMatrix());
			// Calculate pitch
			float CAR_LENGTH = 0.5f;
			playerNode.moveForward(CAR_LENGTH);
			heading = playerNode.getLocalPosition();
			playerNode.moveBackward(CAR_LENGTH);
			heightChange = (getGroundHeight(heading.x(), heading.z())) - currentHeight;
			if (heightChange < -1f) {
				// if falling, don't correct pitch much
				float pitchAngle = (float) Math.toDegrees(Math.atan(heightChange / CAR_LENGTH));
				float newPitch = (pitchAngle - currentPitch) * elapsedSec / 3 + currentPitch;
				playerAvatar.pitch(Degreef.createFrom(-newPitch));
				currentPitch = newPitch;
			}
			else {
				float pitchAngle = (float) Math.toDegrees(Math.atan(heightChange / CAR_LENGTH));
				float newPitch = (pitchAngle - currentPitch) * elapsedSec * 3 + currentPitch;
				playerAvatar.pitch(Degreef.createFrom(-newPitch));
				currentPitch = newPitch;
			}
			
			// Calculate roll
			float CAR_WIDTH = 0.25f;
			playerNode.moveRight(CAR_WIDTH);
			heading = playerNode.getLocalPosition();
			playerNode.moveLeft(CAR_WIDTH);
			heightChange = (getGroundHeight(heading.x(), heading.z())) - currentHeight;
			float rollAngle = (float) Math.toDegrees(Math.atan(heightChange / CAR_WIDTH));
			float newRoll = (rollAngle - currentRoll) * elapsedSec * 5 + currentRoll;
			playerAvatar.roll(Degreef.createFrom(newRoll));
			currentRoll = newRoll;
		}
		// If in air, straighten car out
		else {
			playerAvatar.setLocalRotation(Matrix3f.createIdentityMatrix());
			float newPitch = -currentPitch * elapsedSec + currentPitch;
			playerAvatar.pitch(Degreef.createFrom(-newPitch));
			currentPitch = newPitch;
			
			float newRoll = -currentRoll * elapsedSec + currentRoll;
			playerAvatar.roll(Degreef.createFrom(newRoll));
			currentRoll = newRoll;
		}
	}

	protected void updateItemBoxesRotation() {
		SceneManager sm = getEngine().getSceneManager();
		for (HashMap.Entry<UUID, ItemBox> entry : gameState.getItemBoxes().entrySet()) {
			UUID id = entry.getKey();
			
			SceneNode itemBoxN = sm.getSceneNode(id.toString());
			ItemBox itemBox = entry.getValue();
			Vector3 lp = itemBoxN.getLocalPosition();
			if (itemBox.getIsActive() == 0) {
				itemBoxN.setLocalPosition(-1000000f, lp.y(), lp.z());
			}
			else {
				itemBoxN.setLocalPosition(itemBox.getPos().x(), lp.y(), lp.z());
			}

			float scale = entry.getValue().scaleFactor();
			itemBoxN.setLocalScale(scale * 0.6f, scale * 0.6f, scale * 0.6f);
			
			SceneNode questionmarkbody = sm.getSceneNode(id.toString() + "questionmarkbody");
			SceneNode questionmarkdot = sm.getSceneNode(id.toString() + "questionmarkdot");
			SceneNode cameraNode = sm.getSceneNode("dolphinNodeCamera");
			
			Vector3 qmWP = questionmarkbody.getWorldPosition();
			Vector3 cWP = cameraNode.getWorldPosition();
			float angle = (float) Math.toDegrees(Math.atan((qmWP.x() - cWP.x())/(qmWP.z() - cWP.z())));
			if (qmWP.z() > cWP.z()) {
				angle += 180f;
			}
			
			questionmarkbody.setLocalRotation(Matrix3f.createIdentityMatrix());
			questionmarkbody.yaw(Degreef.createFrom(angle));
			questionmarkdot.setLocalRotation(Matrix3f.createIdentityMatrix());
			questionmarkdot.yaw(Degreef.createFrom(angle));
		}
	}

	protected void createDolphinWithCamera(SceneManager sm) throws IOException {
		Entity dolphinE = sm.createEntity("dolphin", "dolphinHighPoly.obj");
		dolphinE.setPrimitive(Primitive.TRIANGLES);
		SceneNode dolphinN = sm.getRootSceneNode().createChildSceneNode(dolphinE.getName() + "Node");
		SceneNode playerAvatarN = dolphinN.createChildSceneNode("playerAvatar");
		SceneNode playerAvatarRotatorN = playerAvatarN.createChildSceneNode("playerAvatarCollisionRotator");
		playerNode = dolphinN;
		playerAvatar = playerAvatarN;
		playerAvatarRotator = playerAvatarRotatorN;
		dolphinN.translate(-45.2f, 0, -89.7f);
		playerAvatarRotatorN.attachObject(dolphinE);
		playerAvatarRotatorN.translate(0f, 0.3f, 0f);
		//dolphinN.setPhysicsObject(new PhysicsObject());

		SceneNode skyN = sm.getRootSceneNode().createChildSceneNode("skyNode");
		skyN.translate(-50f, 15f, -110f);
		dolphinN.createChildSceneNode(dolphinN.getName() + "Camera");
		setCameraToSky();
	}
	
	protected void setCameraToSky() {
		if (cameraController != null) {
			cameraController.removeAllNodes();
		}
		Camera camera = getEngine().getSceneManager().getCamera("MainCamera");
		SceneNode skyN = getEngine().getSceneManager().getSceneNode("skyNode");
		camera.detachFromParent();
		skyN.attachObject(camera);
		camera.setMode('n');
	}
	
	public void setCameraToAvatar() {
		Camera camera = getEngine().getSceneManager().getCamera("MainCamera");
		SceneNode dolphinCamera = getEngine().getSceneManager().getSceneNode("dolphinNodeCamera");
		SceneNode dolphinN = getEngine().getSceneManager().getSceneNode("dolphinNode");

		camera.detachFromParent();
		dolphinCamera.attachObject(camera);
		camera.setMode('n');
		
		if (cameraController == null) {
			cameraController = new NodeOrbitController(
				dolphinN,
				(GL4RenderSystem) getEngine().getRenderSystem(),
				im
			);
			getEngine().getSceneManager().addController(cameraController);
		}
		cameraController.addNode(dolphinCamera);
	}
	
	protected void setupAmbientLight(SceneManager sm) {
		sm.getAmbientLight().setIntensity(new Color(0.3f, 0.3f, 0.3f));
	}
	
	protected void setupPointLight(SceneManager sm) throws IOException {
		Entity lightE = sm.createEntity("sun", "cube.obj");
		lightE.setPrimitive(Primitive.TRIANGLES);

		SceneNode lightN = sm.getRootSceneNode().createChildSceneNode("lightNode");
		lightN.translate(25f, 150f, 200f);
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
					new TurnLeftAction(this),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
				im.associateAction(
					c,
					Component.Identifier.Key.D,
					new TurnRightAction(this),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
				im.associateAction(
					c,
					Component.Identifier.Key.Q,
					new StartDriftingAction(this),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
				im.associateAction(
					c,
					Component.Identifier.Key.R,
					new ThrowItemAction(this),
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY
				);
				im.associateAction(
					c,
					Component.Identifier.Key.E,
					new StartDriftingAction(this),
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
				im.associateAction(
					c,
					Component.Identifier.Key.RETURN,
					new JoinTrackAction(this),
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY
				);
			}
			else if (c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK) {
				im.associateAction(
					c,
					Component.Identifier.Button._0,
					new StartAccelerationAction(this),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
				im.associateAction(
					c,
					Component.Identifier.Button._1,
					new StartDeccelerationAction(this),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
				im.associateAction(
					c,
					Component.Identifier.Button._2,
					new ThrowItemAction(this),
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY
				);
				im.associateAction(
					c,
					Component.Identifier.Button._3,
					new ThrowItemAction(this),
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY
				);
				im.associateAction(
					c,
					Component.Identifier.Axis.X,
					new TurnLeftRightAction(this),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
				im.associateAction(
					c,
					Component.Identifier.Button._5,
					new StartDriftingAction(this),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
				im.associateAction(
					c,
					Component.Identifier.Button._6,
					new StartDriftingAction(this),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
				im.associateAction(
					c,
					Component.Identifier.Axis.Z,
					new StartDriftingDeviceAction(this),
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
		//plane.setTexture(getEngine(), "hexagons.jpeg");
		//plane.setTextureTiling(16);
		plane.setTexture(getEngine(), "Track1_texture.png");
		plane.setTextureTiling(1);

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
		Texture front = textureMgr.getAssetByPath("bluecloud_bk.jpg");
		Texture back = textureMgr.getAssetByPath("bluecloud_ft.jpg");
		Texture left = textureMgr.getAssetByPath("bluecloud_lf.jpg");
		Texture right = textureMgr.getAssetByPath("bluecloud_rt.jpg");
		Texture top = textureMgr.getAssetByPath("bluecloud_up.jpg");
		Texture bottom = textureMgr.getAssetByPath("bluecloud_dn.jpg");
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

	public Vector3 getPlayerPosition() {
		return playerNode.getWorldPosition();
	}
	
	public Matrix3 getPlayerRotation() {
		return playerAvatarRotator.getWorldRotation();
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
	
	public void updateGhostAvatar(UUID ghostID, Vector3 ghostPosition, Matrix3 ghostRotation, float vForward) {
		try {
			SceneManager sm = getEngine().getSceneManager();
			if (!sm.hasSceneNode(ghostID.toString())) {
				System.out.println("Ghost does not exist.  Creating: " + ghostID.toString());
				createGhostAvatar(ghostID, ghostPosition);
				return;
			}
			gameState.updateGhostAvatar(ghostID, ghostPosition, ghostRotation, vForward);
		}
		catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
	
	public void updateGameStateDisplay() {
		SceneManager sm = getEngine().getSceneManager();
		float CAR_HEIGHT_OFFSET = 0.3f;
		for (Entry<UUID, GhostAvatar> entry : gameState.getGhostAvatars().entrySet()) {
			SceneNode ghostN = sm.getSceneNode(entry.getKey().toString());
			ghostN.setLocalPosition(entry.getValue().getPos());
			ghostN.moveUp(CAR_HEIGHT_OFFSET);
			ghostN.setLocalRotation(entry.getValue().getRot());
		}
		for (Entry<UUID, Item> entry : gameState.getItems().entrySet()) {
			SceneNode itemN = sm.getSceneNode(entry.getKey().toString());
			itemN.setLocalPosition(entry.getValue().getPos());
			itemN.setLocalRotation(entry.getValue().getRot());
		}
	}

	public void createItemBox(UUID id, Vector3 pos) {
		try {
			SceneManager sm = getEngine().getSceneManager();
			Entity itemBoxE = sm.createEntity(id.toString(), "itembox.obj");
			SceneNode itemBoxN = sm.getRootSceneNode().createChildSceneNode(id.toString());
			SceneNode itemBoxRotator = itemBoxN.createChildSceneNode(itemBoxE.getName() + "Rotator");
			itemBoxE.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.ITEM_BOX));
			itemBoxRotator.attachObject(itemBoxE);
			float height = getGroundHeight(pos.x(), pos.z());
			itemBoxN.translate(pos.x(), height + pos.y(), pos.z());
			itemBoxN.scale(0.6f, 0.6f, 0.6f);
			
			Entity questionMarkBodyE = sm.createEntity(id.toString() + "questionmarkbody", "questionmarkbody.obj");
			questionMarkBodyE.setCanReceiveShadows(false);
			SceneNode questionMarkBodyN = itemBoxN.createChildSceneNode(id.toString() + "questionmarkbody");
			questionMarkBodyN.attachObject(questionMarkBodyE);
			questionMarkBodyN.scale(0.5f, 0.5f, 0.5f);
			questionMarkBodyN.translate(0f, 0.2f, 0f);
			
			Entity questionMarkDotE = sm.createEntity(id.toString() + "questionmarkdot", "questionmarkdot.obj");
			questionMarkDotE.setCanReceiveShadows(false);
			SceneNode questionMarkDotN = itemBoxN.createChildSceneNode(id.toString() + "questionmarkdot");
			questionMarkDotN.attachObject(questionMarkDotE);
			questionMarkDotN.scale(0.5f, 0.5f, 0.5f);
			questionMarkDotN.translate(0f, -0.8f, 0f);
	
			RotationController verticalRotation = new RotationController(Vector3f.createUnitVectorX(), 0.04f);
			RotationController horizontalRotation = new RotationController(Vector3f.createUnitVectorY(), 0.07f);
			verticalRotation.addNode(itemBoxRotator);
			horizontalRotation.addNode(itemBoxRotator);
			sm.addController(verticalRotation);
			sm.addController(horizontalRotation);
			
			gameState.createItemBox(id, pos);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateItemBox(UUID id, Vector3 pos, int isActive, int isGrowing, long growthTimer) {
		try {
			SceneManager sm = getEngine().getSceneManager();
			if (!sm.hasSceneNode(id.toString())) {
				System.out.println("Item Box does not exist.  Creating: " + id.toString());
				createItemBox(id, pos);
				return;
			}
			gameState.updateItemBox(id, pos, isActive, isGrowing, growthTimer);
		}
		catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
	
	
	private void updatePlayerItem() {
		if (!hasItem()) {
			return;
		}
		SceneManager sm = getEngine().getSceneManager();
		SceneNode itemN = sm.getSceneNode(item.getID().toString());
		itemN.setLocalPosition(playerNode.getWorldPosition());
		itemN.setLocalRotation(playerAvatar.getWorldRotation());
		itemN.moveBackward(1.1f);
		gameState.updateItem(item.getID(), itemN.getWorldPosition(), itemN.getWorldRotation());
		if (SHOW_PACKET_MESSAGES) System.out.println("Sending Update Item");
		clientProtocol.updateItem(
			item.getID(),
			itemN.getWorldPosition(),
			itemN.getWorldRotation(),
			item.getType()
		);
	}
	
	public void removeGhostAvatar(UUID ghostID) {
		try {
			SceneManager sm = getEngine().getSceneManager();
			sm.destroySceneNode(ghostID.toString());
			sm.destroyEntity(ghostID.toString());
			gameState.getGhostAvatars().remove(ghostID);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// We just assume client ALWAYS gets this message from the server
	// If packet is lost, client will never be able to pick up an item again
	float BANANA_SCALE = 0.4f;
	public void setPlayerItem(UUID itemID, int itemType) {
		item = createItem(itemID, itemType);
	}
	
	public Item createItem(UUID itemID, int itemType) {
		try {
			SceneManager sm = getEngine().getSceneManager();
			Entity itemE = sm.createEntity(itemID.toString(), "banana.obj");
			itemE.setCanReceiveShadows(false);
			SceneNode itemN = sm.getRootSceneNode().createChildSceneNode(itemID.toString());
			itemN.scale(BANANA_SCALE, BANANA_SCALE, BANANA_SCALE);
			itemN.attachObject(itemE);
			Item newItem = new Item(itemID, ItemType.getType(itemType));
			gameState.getItems().put(itemID, newItem);
			ItemGrowthController itemC = new ItemGrowthController();
			itemC.addNode(itemN);
			getEngine().getSceneManager().addController(itemC);
			
			return newItem;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void throwItem() {
		if (hasItem()) {
			SceneManager sm = getEngine().getSceneManager();
			SceneNode itemN = sm.getSceneNode(item.getID().toString());
			SceneNode itemNParent = (SceneNode) itemN.getParent();
			Vector3 currentPos = itemN.getWorldPosition();
			itemNParent.detachChild(itemN);
			sm.getRootSceneNode().attachChild(itemN);
			float height = getGroundHeight(currentPos.x(), currentPos.z());
			itemN.setLocalPosition(currentPos.x(), height, currentPos.z());
			item = null;
			// Assume server ALWAYS gets this message.
			// If packet is lost, client will never be able to pick up another item
			if (SHOW_PACKET_MESSAGES) System.out.println("Sending Throw Item");
			clientProtocol.sendThrowItem();
		}
	}
	

	public void handlePlayerHitItem(UUID itemID) {
		handleCollision();
	}

	public void removeItem(UUID itemID, Vector3 force) {
		if (hasItem() && itemID.equals(item.getID())) {
			item = null;
		}
		try {
			SceneManager sm = getEngine().getSceneManager();
			SceneNode item = sm.getSceneNode(itemID.toString());
			if (gameState.getItems().get(itemID).getType().equals(ItemType.BANANA)) {
				BananaDeathAnimationController bdaC = new BananaDeathAnimationController(
					this,
					item,
					force
				);
				bdaC.addNode(item);
				sm.addController(bdaC);
			}
			gameState.getItems().remove(itemID);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean hasItem() {
		return item != null;
	}
	
    @Override
    public void shutdown() {
    	super.shutdown();
        if (clientProtocol != null) {
        	if (SHOW_PACKET_MESSAGES) System.out.println("Sending Bye");
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
	
	protected float getGravityForce() {
		return (float) Math.sin(Math.toRadians(currentPitch)) * gravity;
	}
	
	protected float getMaxSpeed() {
		return (MAX_BASE_SPEED + getGravityForce()) * getMaxSpeedModifier();
	}
	
	protected float getMaxReverseSpeed() {
		return -getMaxSpeed() / 4;
	}
	
	protected float getTurnRate() {
		float direction = 1f;
		if (vForward < 0) {
			direction = -1f;
		}
		if (isDrifting()) {
			return direction * DRIFTING_TURN_RATE;
		}
		return direction * TURN_RATE;
	}

	public float getDesiredTurn() {
		return desiredTurn;
	}

	public void setDesiredTurn(float desiredTurn) {
		this.desiredTurn = desiredTurn;
	}

	public float getActualTurn() {
		return actualTurn;
	}

	public void setActualTurn(float actualTurn) {
		this.actualTurn = actualTurn;
	}
	
	public float getDriftingDirection() {
		return driftingDirection;
	}

	public void setDriftingDirection(float driftingDirection) {
		this.driftingDirection = driftingDirection;
	}

	public boolean isDrifting() {
		return isDrifting;
	}

	public void setDrifting(boolean isDrifting) {
		this.isDrifting = isDrifting;
	}
	
	protected boolean isSpinning() {
		return spinoutTimer > 0;
	}

    protected void handleCollision() {
    	setSpinoutTimer(SPINOUT_DURATION);
    }

    private void setSpinoutTimer(float f) {
		this.spinoutTimer = f;
	}
    
    private float getSpinDirection() {
    	return spinDirection;
    }
    
    private void calculateSpinDirection() {
    	float timer = spinoutTimer;
    	if (spinoutTimer > 750) {
    		timer -= 750;
    	}
    	setSpinDirection(timer * 360 / 750);
    }
    
    private void setSpinDirection(float direction) {
    	spinDirection = direction;
    }

	protected float getMaxSpeedModifier() {
    	if (spinoutTimer <= 0f) {
    		if (speedBoostTimer > 0f) {
    			return 1.2f;
    		}
    		return 1f;
    	}
    	return ((spinoutTimer / SPINOUT_DURATION) * 0.3f ) + 0.4f;
    }

	public void updateTimers(float elapsedMS) {
		setSpinoutTimer(Math.max(0, spinoutTimer - elapsedMS));
		setSpeedBoostTimer(Math.max(0, speedBoostTimer - elapsedMS));
		calculateSpinDirection();
	}

	private void setSpeedBoostTimer(float time) {
		this.speedBoostTimer = time;
	}

	public void updateItem(UUID itemID, Vector3 itemPos, Matrix3 itemRot, int itemType) {
		try {
			SceneManager sm = getEngine().getSceneManager();
			if (!sm.hasSceneNode(itemID.toString())) {
				System.out.println("Item does not exist.  Creating: " + itemID.toString());
				createItem(itemID, itemType);
				return;
			}
			gameState.updateItem(itemID, itemPos, itemRot);
		}
		catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	public boolean isOnSpeedBoost() {
		return isOnSpeedBoost;
	}

	public void setOnSpeedBoost(boolean isOnSpeedBoost) {
		if (isOnSpeedBoost && !isSpinning()) {
			setSpeedBoostTimer(SPEED_BOOST_DURATION);
		}
		this.isOnSpeedBoost = isOnSpeedBoost;
	}

	public void itemBoxExplosion(Vector3 pos, Vector3 force) {
		final float PARTICLE_COUNT  = 15;
		for (int i = 0; i < PARTICLE_COUNT; i++) {
			Entity particleE = null;
			try {
				particleE = getEngine().getSceneManager().createEntity(createID(), "plane.obj");
			} catch (IOException e) {
				e.printStackTrace();
			}
			particleE.setCanReceiveShadows(false);
			SceneNode particleN = getEngine().getSceneManager().getRootSceneNode().createChildSceneNode(particleE.getName());
			float height = getGroundHeight(pos.x(), pos.z());
			particleN.setLocalPosition(pos.x(), height, pos.z());
			particleN.attachObject(particleE);
			ParticleController particleC = new ParticleController(
				this,
				particleN,
				force
			);
			particleC.addNode(particleN);
			getEngine().getSceneManager().addController(particleC);
			
			
			CullingState cullingState = (CullingState) getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.CULLING);
			cullingState.setCulling(CullingState.Culling.DISABLED);
			particleE.setRenderState(cullingState);
			TextureState tstate = (TextureState) getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
			tstate.setTexture(getTextures().getRandomTexture());
			particleE.setRenderState(tstate);
			particleE.setGpuShaderProgram(getEngine().getSceneManager().getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.TRANSPARENT));
		}
	}

	public PreloadTextures getTextures() {
		return textures;
	}

	public void setTextures(PreloadTextures textures) {
		this.textures = textures;
	}
	
	public void startRace(int trackID) {
		if (clientState.getJoinedTrack() == trackID) {
			gameState.setRaceStarted(true);
		}
	}
	
	public boolean hasRaceStarted() {
		return gameState.hasRaceStarted();
	}
	
	public void inputAction() {
		if (clientState.isRaceFinished()) {
			if (SHOW_PACKET_MESSAGES) System.out.println("Sending Finish Track");
			clientProtocol.finishTrack(clientState.getSelectedTrack());
			clientState.setJoinedTrack(0);
			clientState.setRaceFinished(false);
			setCameraToSky();
		}
		else if (!clientState.hasTrack()) {
			if (SHOW_PACKET_MESSAGES) System.out.println("Sending Join Track");
			clientProtocol.joinTrack(clientState.getSelectedTrack());
		}
		else {
			if (SHOW_PACKET_MESSAGES) System.out.println("Sending Start Track");
			clientProtocol.sendStartMessage(clientState.getJoinedTrack());
		}
	}

	public ClientState getClientState() {
		return clientState;
	}
	
	public boolean isRacingInputDisabled() {
		if (isRacing() && gameState.getElapsedRaceTime() < 0) {
			return true;
		}
		if (isRacing() && !isSpinning()) {
			return false;
		}
		return true;
	}
	
	public boolean isRacing() {
		return hasRaceStarted() && !hasRaceFinished();
	}
	
	public boolean hasRaceFinished() {
		return clientState.isRaceFinished();
	}

	public void updateRaceTime(long raceTime) {
		if (isRacing()) {
			timerGui.update(raceTime);
		}
	}
	
	protected void finishRace() {
		clientState.setRaceFinished(true);
	}
	
	
	public void updateLapInfo() {
		int newZone = getZone();
		// System.out.println(currentZone + " -> " + newZone);
		if (currentZone != newZone && newZone > currentZone) {
			currentZone = newZone;
		}
		else if (currentZone == 3 && newZone == 0) {
			if (raceLap < 3) {
				raceLap += 1;
			}
			else {
				finishRace();
			}
			currentZone = newZone;
		}
	}

	protected int getZone() {
		if (hasRaceFinished()) {
			return currentZone;
		}
		Vector3 coord = playerNode.getWorldPosition();
		final float ZONE_3_Z = -85.36f;
		final float ZONE_1_Z = 50f;
		final float ZONE_0_2_X = -35.8f;
		switch (currentZone) {
		case 0:
			if (coord.z() > ZONE_1_Z) {
				return 1;
			}
			return 0;
		case 1:
			if (coord.z() < ZONE_1_Z && coord.x() > ZONE_0_2_X) {
				return 2;
			}
			return 1;
		case 2:
			if (coord.z() < ZONE_3_Z) {
				return 3;
			}
			return 2;
		case 3:
			if (coord.z() > ZONE_3_Z && coord.x() < ZONE_0_2_X) {
				return 0;
			}
			return 3;
		}
		return -1;
	}

	public GameState getGameState() {
		return gameState;
	}

	public void setStartingPosition(int position) {
		Vector3 startingPos = Track1.getPosition(position);
		playerNode.setLocalPosition(
			startingPos.x(),
			getGroundHeight(startingPos.x(), startingPos.z()),
			startingPos.z()
		);
	}
}