package a3;

import myGameEngine.*;
import myGameEngine.Networking.ProtocolClient;
import myGameEngine.controllers.BananaDeathAnimationController;
import myGameEngine.controllers.ItemGrowthController;
import myGameEngine.controllers.NodeOrbitController;
import myGameEngine.controllers.ParticleController;
import myGameEngine.myRage.HUDString;
import net.java.games.input.Controller;
import ray.audio.AudioManagerFactory;
import ray.audio.AudioResource;
import ray.audio.AudioResourceType;
import ray.audio.IAudioManager;
import ray.audio.Sound;
import ray.audio.SoundType;
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
import ray.rage.scene.SkeletalEntity;
import static ray.rage.scene.SkeletalEntity.EndType.LOOP;
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
	private GameState gameState = new GameState();
	private static long particleID = 0;
	private PreloadTextures textures = null;
	private TimerGui timerGui = null;
	private int raceLap = 0;
	private int currentZone = 3;
	private final boolean SHOW_PACKET_MESSAGES = false;
	private NodeOrbitController cameraController = null; 
	private PhysicsBody physicsBody;
	private LobbyGui lobbyGui = null;
	private long frametime;
	private Light lobbyLight = null;
	private Light sunlight = null;
	private Entity playerEntity = null;
	private RotationController lobbyRotator = null;
	private IAudioManager audioMgr = null;
	private Sound song1;
	private TextureState carTexture = null;
	private int carTextureNum = 1;
	private long totalElapsedTime = 0;
	private int NUM_TREES = 10;

	public int getCarTextureNum() {
		return carTextureNum;
	}

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
		camera.getFrustum().setFarClipDistance(500.0f);
		camera.getFrustum().setFieldOfViewY(Degreef.createFrom(60.0f));
	}
	
	@Override
	protected void setupWindowViewports(RenderWindow rw) {
		rw.addKeyListener(this);
	}
	
	@Override
	protected void setupScene(Engine eng, SceneManager sm) throws IOException {
		setTextures(new PreloadTextures(this));
		lobbyRotator = new RotationController(Vector3f.createUnitVectorY(), 0.02f);
		getEngine().getSceneManager().addController(lobbyRotator);
		setupNetworking();
		createTrees(sm);
		executeScript(script);
		setupHUD();
		createGroundPlane(sm);
		setupAmbientLight(sm);
		setupPointLight(sm);
		createDolphinWithCamera(sm);
		physicsBody = new PhysicsBody(playerNode.getWorldPosition(), playerNode.getWorldRotation());
		initMeshes();
		setupInputs();
		createSkyBox(eng, sm);
		timerGui = new TimerGui(this);
		lobbyGui = new LobbyGui(this);
		initAudio(sm);
	}

	private void initAudio(SceneManager sm) {
		AudioResource songResource1;
		audioMgr = AudioManagerFactory.createAudioManager("ray.audio.joal.JOALAudioManager");
		if (!audioMgr.initialize()) {
			System.out.println("Audio Manager failed to initialize.");
			return;
		}
		songResource1 = audioMgr.createAudioResource("HavaNagila.wav", AudioResourceType.AUDIO_SAMPLE);
		song1 = new Sound(songResource1, SoundType.SOUND_MUSIC, 15, true);
		song1.initialize(audioMgr);
		
		setEarParameters(sm);
		song1.play();
	}

	private void setEarParameters(SceneManager sm) {
		Camera camera = sm.getCamera("MainCamera");
		audioMgr.getEar().setLocation(camera.getPo());
		audioMgr.getEar().setOrientation(camera.getFd(), Vector3f.createUnitVectorY());
	}

	private void createTrees(SceneManager sm) throws IOException {
		//Entity treeE = getEngine().getSceneManager().createEntity("tree", "tree.obj");
		
		for (int i = 0; i < NUM_TREES; i++) {
			SkeletalEntity treeE = sm.createSkeletalEntity("tree" + i, "tree.rkm", "tree.rks");
			Texture tex = sm.getTextureManager().getAssetByPath("tree.png");
			TextureState tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
			tstate.setTexture(tex);
			treeE.setRenderState(tstate);
			
			treeE.loadAnimation("waveAnimation", "tree.rka");
			treeE.stopAnimation();
			treeE.playAnimation("waveAnimation", 0.5f, LOOP, 0);
			SceneNode treeN = getEngine().getSceneManager().getRootSceneNode().createChildSceneNode(treeE.getName());
			treeN.attachObject(treeE);
		}
	}

	private void initMeshes() throws IOException {
		createBanana();
	}
	
	private void createBanana() throws IOException {
		Entity bananaE = getEngine().getSceneManager().createEntity("banana", "banana.obj");
		SceneNode bananaN = getEngine().getSceneManager().getRootSceneNode().createChildSceneNode(bananaE.getName() + "Node");
		bananaN.attachObject(bananaE);
		bananaN.scale(0.01f, 0.01f, 0.01f);
		//bananaN.translate(Vector3f.createFrom(-36.376953125f, 3f, -67.3828125f));
		bananaN.translate(-1000000f, 0f, 0f);
	}
	
	private void setupNetworking() {
		try {
			System.out.println("Starting Protocol Client");
			clientProtocol = new ProtocolClient(
				InetAddress.getByName(serverAddr),
				serverPort,
				serverProtocol,
				this
			);
		}
		catch (Exception e) {
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
		// System.out.println(Math.round(1000 / elapsTime));
		processNetworking(elapsTime);
		updateRaceState();
		frametime = System.currentTimeMillis();
		physicsBody.resetInputs();
		im.update(elapsTime);
		if (gameState.getRaceState() != RaceState.LOBBY
			&& gameState.getRaceState() != RaceState.FINISH
		) {
			physicsBody.updatePhysics(elapsTime);	
		}
		playerNode.setLocalPosition(physicsBody.getPosition());
		playerNode.setLocalRotation(physicsBody.getDirection());
		playerAvatar.setLocalRotation(physicsBody.getRotation());
		if (gameState.getRaceState() != RaceState.LOBBY) {
			playerAvatarRotator.setLocalRotation(physicsBody.getSpinRotation());
		}
		updateLapInfo();
		updateHUD(engine, elapsTime);
		long modifiedTime = script.lastModified();
		if (modifiedTime > lastScriptModifiedTime) {
			lastScriptModifiedTime = modifiedTime;
			executeScript(script);
		}
		if (gameState.getRaceState() == RaceState.RACING ||
			gameState.getRaceState() == RaceState.COUNTDOWN
		) {
			if (SHOW_PACKET_MESSAGES) System.out.println("Sending Update Information");
			clientProtocol.updatePlayerInformation(
				this.getPlayerPosition(),
				this.getPlayerRotation(),
				physicsBody.getVForward(),
				physicsBody.getActualTurn(),
				carTextureNum
			);
		}
		for (int i = 0; i < NUM_TREES; i++) {
			((SkeletalEntity) engine.getSceneManager().getEntity("tree" + i)).update();
		}
		updatePlayerItem();
		updateGameStateDisplay();
		updateItemBoxesRotation();
		setEarParameters(getEngine().getSceneManager());
		if ((gameState.getRaceState() == RaceState.RACING ||
			gameState.getRaceState() == RaceState.COUNTDOWN)
			&& !clientState.isConnected()
		) {
			totalElapsedTime += elapsTime;
			updateRaceTime(totalElapsedTime);
		}
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
		stringList.get(2).setAll("" + Math.round((physicsBody.getVForward() + physicsBody.getGravityForce()) * 3) + " MPH", bottomRightX, bottomRightY);
	}

	private void processNetworking(float elapsTime) {
		if (clientProtocol != null) {
			clientProtocol.processPackets();
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
	
	float CAR_HEIGHT_OFFSET = 0.25f;
	float CAR_FORWARD_OFFSET = 0f;
	protected void createDolphinWithCamera(SceneManager sm) throws IOException {
		playerEntity = sm.createEntity("dolphin", "car1.obj");
		CullingState cullingState = (CullingState) getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.CULLING);
		cullingState.setCulling(CullingState.Culling.DISABLED);
		playerEntity.setRenderState(cullingState);
		playerEntity.setPrimitive(Primitive.TRIANGLES);
		SceneNode dolphinN = sm.getRootSceneNode().createChildSceneNode(playerEntity.getName() + "Node");
		SceneNode playerAvatarN = dolphinN.createChildSceneNode("playerAvatar");
		SceneNode playerAvatarRotatorN = playerAvatarN.createChildSceneNode("playerAvatarCollisionRotator");
		playerNode = dolphinN;
		playerAvatar = playerAvatarN;
		playerAvatarRotator = playerAvatarRotatorN;
		playerAvatarRotatorN.attachObject(playerEntity);
		carTexture = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		playerEntity.setRenderState(carTexture);
		updateCarTexture();
		playerAvatarRotatorN.translate(0f, CAR_HEIGHT_OFFSET, CAR_FORWARD_OFFSET);
		playerAvatarRotatorN.scale(0.3f, 0.3f, 0.3f);
		//dolphinN.setPhysicsObject(new PhysicsObject());
		
		// front left
		Entity wheel1 = sm.createEntity("wheel1", "wheelSpikes.obj");
		SceneNode wheel1N = playerAvatarRotator.createChildSceneNode("wheel1");
		SceneNode wheel1yawN = wheel1N.createChildSceneNode("wheel1yaw");
		wheel1yawN.attachObject(wheel1);
		wheel1.setRenderState(cullingState);
		wheel1N.translate(2.2f, -0.4f, 2.4f);
		wheel1N.scale(0.15f, 0.15f, 0.15f);
		
		// front right
		Entity wheel2 = sm.createEntity("wheel2", "wheelSpikes.obj");
		SceneNode wheel2N = playerAvatarRotator.createChildSceneNode("wheel2");
		SceneNode wheel2yawN = wheel2N.createChildSceneNode("wheel2yaw");
		wheel2yawN.attachObject(wheel2);
		wheel2.setRenderState(cullingState);
		wheel2N.translate(-2.0f, -0.4f, 2.4f);
		wheel2yawN.roll(Degreef.createFrom(180f));
		wheel2N.scale(0.15f, 0.15f, 0.15f);
		
		// back left
		Entity wheel3 = sm.createEntity("wheel3", "wheelSpikes.obj");
		SceneNode wheel3N = playerAvatarRotator.createChildSceneNode("wheel3");
		wheel3N.attachObject(wheel3);
		wheel3.setRenderState(cullingState);
		wheel3N.translate(2.2f, -0.4f, -1.45f);
		wheel3N.scale(0.15f, 0.15f, 0.15f);
		
		// back right
		Entity wheel4 = sm.createEntity("wheel4", "wheelSpikes.obj");
		SceneNode wheel4N = playerAvatarRotator.createChildSceneNode("wheel4");
		wheel4N.attachObject(wheel4);
		wheel4.setRenderState(cullingState);
		wheel4N.translate(-2.0f, -0.4f, -1.45f);
		wheel4N.roll(Degreef.createFrom(180f));
		wheel4N.scale(0.15f, 0.15f, 0.15f);

		SceneNode skyN = sm.getRootSceneNode().createChildSceneNode("skyNode");
		skyN.translate(-50f, 15f, -110f);
		skyN.rotate(Degreef.createFrom(20f), Vector3f.createUnitVectorY());
		skyN.rotate(Degreef.createFrom(15f), Vector3f.createUnitVectorX());
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

		playerNode.translate(skyN.getWorldPosition());
		playerNode.moveForward(3f);
		playerNode.moveRight(1.2f);
		playerNode.moveDown(0.5f);
		playerNode.yaw(Degreef.createFrom(180f));
		
		lobbyRotator.addNode(playerAvatarRotator);

		if (lobbyLight == null) {
			lobbyLight = getEngine().getSceneManager().createLight("lobbyLight", Light.Type.POINT);
			lobbyLight.setAmbient(new Color(0.3f, 0.3f, 0.3f));
			lobbyLight.setDiffuse(new Color(1f, 1f, 1f));
			lobbyLight.setSpecular(new Color(1f, 1f, 1f));
			lobbyLight.setRange(10f);
			lobbyLight.setConstantAttenuation(0.8f);
			lobbyLight.setLinearAttenuation(0.0000001f);
			lobbyLight.setQuadraticAttenuation(0f);
			lobbyLight.setFalloffExponent(0f);
			SceneNode lobbyLightN = getEngine().getSceneManager().getRootSceneNode().createChildSceneNode("lobbyLightNode");
			lobbyLightN.translate(skyN.getWorldPosition());
			lobbyLightN.attachObject(lobbyLight);
		}
		playerEntity.setCanReceiveShadows(false);
		getEngine().getSceneManager().getEntity("wheel1").setCanReceiveShadows(false);
		getEngine().getSceneManager().getEntity("wheel2").setCanReceiveShadows(false);
		getEngine().getSceneManager().getEntity("wheel3").setCanReceiveShadows(false);
		getEngine().getSceneManager().getEntity("wheel4").setCanReceiveShadows(false);
		sunlight.setVisible(false);
		lobbyLight.setVisible(true);
	}
	
	public void setCameraToAvatar() {
		Camera camera = getEngine().getSceneManager().getCamera("MainCamera");
		SceneNode dolphinCamera = getEngine().getSceneManager().getSceneNode("dolphinNodeCamera");
		SceneNode dolphinN = getEngine().getSceneManager().getSceneNode("dolphinNode");

		camera.detachFromParent();
		dolphinCamera.attachObject(camera);
		camera.setMode('n');
		
		lobbyRotator.removeNode(playerAvatarRotator);
		
		if (cameraController == null) {
			cameraController = new NodeOrbitController(
				dolphinN,
				(GL4RenderSystem) getEngine().getRenderSystem(),
				im
			);
			getEngine().getSceneManager().addController(cameraController);
		}
		cameraController.addNode(dolphinCamera);
		playerEntity.setCanReceiveShadows(true);
		getEngine().getSceneManager().getEntity("wheel1").setCanReceiveShadows(true);
		getEngine().getSceneManager().getEntity("wheel2").setCanReceiveShadows(true);
		getEngine().getSceneManager().getEntity("wheel3").setCanReceiveShadows(true);
		getEngine().getSceneManager().getEntity("wheel4").setCanReceiveShadows(true);
		sunlight.setVisible(true);
		lobbyLight.setVisible(false);
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
		
		sunlight = sm.createLight("sunLight", Light.Type.POINT);
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
		plane.setQuality(9);
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
		jsEngine.put("sm", getEngine().getSceneManager());
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
	
	public void createGhostAvatar(UUID ghostID, Vector3 ghostPosition, int color) {
		try {
			CullingState cullingState = (CullingState) getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.CULLING);
			cullingState.setCulling(CullingState.Culling.DISABLED);

			SceneManager sm = getEngine().getSceneManager();
			SceneNode ghostN = sm.getRootSceneNode().createChildSceneNode(ghostID.toString());
			ghostN.setLocalPosition(ghostPosition);
			Entity dolphinE = sm.createEntity(ghostID.toString(), "car1.obj");
			dolphinE.setRenderState(cullingState);
			ghostN.attachObject(dolphinE);
			TextureState ghostCarTexture = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
			ghostCarTexture.setTexture(getTextures().getCarTexture(color));
			dolphinE.setRenderState(ghostCarTexture);
			ghostN.scale(0.3f, 0.3f, 0.3f);
			gameState.createGhostAvatar(ghostID, ghostPosition);
			
			// front left
			Entity wheel1 = sm.createEntity("wheel1" + ghostID.toString(), "wheelSpikes.obj");
			SceneNode wheel1N = ghostN.createChildSceneNode("wheel1" + ghostID.toString());
			SceneNode wheel1yawN = wheel1N.createChildSceneNode("wheel1yaw" + ghostID.toString());
			wheel1yawN.attachObject(wheel1);
			wheel1.setRenderState(cullingState);
			wheel1N.translate(2.2f, -0.4f, 2.4f);
			wheel1N.scale(0.15f, 0.15f, 0.15f);
			
			// front right
			Entity wheel2 = sm.createEntity("wheel2" + ghostID.toString(), "wheelSpikes.obj");
			SceneNode wheel2N = ghostN.createChildSceneNode("wheel2" + ghostID.toString());
			SceneNode wheel2yawN = wheel2N.createChildSceneNode("wheel2yaw" + ghostID.toString());
			wheel2yawN.attachObject(wheel2);
			wheel2.setRenderState(cullingState);
			wheel2N.translate(-2.0f, -0.4f, 2.4f);
			wheel2yawN.roll(Degreef.createFrom(180f));
			wheel2N.scale(0.15f, 0.15f, 0.15f);
			
			// back left
			Entity wheel3 = sm.createEntity("wheel3" + ghostID.toString(), "wheelSpikes.obj");
			SceneNode wheel3N = ghostN.createChildSceneNode("wheel3" + ghostID.toString());
			wheel3N.attachObject(wheel3);
			wheel3.setRenderState(cullingState);
			wheel3N.translate(2.2f, -0.4f, -1.45f);
			wheel3N.scale(0.15f, 0.15f, 0.15f);
			
			// back right
			Entity wheel4 = sm.createEntity("wheel4" + ghostID.toString(), "wheelSpikes.obj");
			SceneNode wheel4N = ghostN.createChildSceneNode("wheel4" + ghostID.toString());
			wheel4N.attachObject(wheel4);
			wheel4.setRenderState(cullingState);
			wheel4N.translate(-2.0f, -0.4f, -1.45f);
			wheel4N.roll(Degreef.createFrom(180f));
			wheel4N.scale(0.15f, 0.15f, 0.15f);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updateGhostAvatar(UUID ghostID, Vector3 ghostPosition, Matrix3 ghostRotation, float vForward, float actualTurn, int color, long time) {
		try {
			SceneManager sm = getEngine().getSceneManager();
			if (!sm.hasSceneNode(ghostID.toString())) {
				System.out.println("Ghost does not exist.  Creating: " + ghostID.toString());
				createGhostAvatar(ghostID, ghostPosition, color);
				return;
			}
			gameState.updateGhostAvatar(ghostID, ghostPosition, ghostRotation, vForward, actualTurn, time);
		}
		catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
	
	public void updateGameStateDisplay() {
		final float SPIN_FACTOR = 1.5f;
		getEngine().getSceneManager().getSceneNode("wheel3").pitch(Degreef.createFrom(SPIN_FACTOR * (physicsBody.getVForward() + physicsBody.getGravityForce())));
		getEngine().getSceneManager().getSceneNode("wheel4").pitch(Degreef.createFrom(-SPIN_FACTOR * (physicsBody.getVForward() + physicsBody.getGravityForce())));
		
		getEngine().getSceneManager().getSceneNode("wheel1yaw").pitch(Degreef.createFrom(SPIN_FACTOR * (physicsBody.getVForward() + physicsBody.getGravityForce())));
		getEngine().getSceneManager().getSceneNode("wheel1").setLocalRotation(Matrix3f.createIdentityMatrix());
		getEngine().getSceneManager().getSceneNode("wheel1").yaw(Degreef.createFrom(physicsBody.getActualTurn() * 30f));
		
		getEngine().getSceneManager().getSceneNode("wheel2yaw").pitch(Degreef.createFrom(-SPIN_FACTOR * (physicsBody.getVForward() + physicsBody.getGravityForce())));
		getEngine().getSceneManager().getSceneNode("wheel2").setLocalRotation(Matrix3f.createIdentityMatrix());
		getEngine().getSceneManager().getSceneNode("wheel2").yaw(Degreef.createFrom(physicsBody.getActualTurn() * 30f));

		SceneManager sm = getEngine().getSceneManager();
		for (Entry<UUID, GhostAvatar> entry : gameState.getGhostAvatars().entrySet()) {
			String id = entry.getKey().toString();
			SceneNode ghostN = sm.getSceneNode(id);
			ghostN.setLocalPosition(entry.getValue().getPos());
			ghostN.moveUp(CAR_HEIGHT_OFFSET);
			ghostN.setLocalRotation(entry.getValue().getRot());
			
			GhostAvatar ga = entry.getValue();

			sm.getSceneNode("wheel3" + id).pitch(Degreef.createFrom(SPIN_FACTOR * ga.getVelocityForward()));
			sm.getSceneNode("wheel4" + id).pitch(Degreef.createFrom(-SPIN_FACTOR * ga.getVelocityForward()));
			
			sm.getSceneNode("wheel1yaw" + id).pitch(Degreef.createFrom(SPIN_FACTOR * ga.getVelocityForward()));
			sm.getSceneNode("wheel1" + id).setLocalRotation(Matrix3f.createIdentityMatrix());
			sm.getSceneNode("wheel1" + id).yaw(Degreef.createFrom(ga.getActualTurn() * 30f));
			
			sm.getSceneNode("wheel2yaw" + id).pitch(Degreef.createFrom(-SPIN_FACTOR * ga.getVelocityForward()));
			sm.getSceneNode("wheel2" + id).setLocalRotation(Matrix3f.createIdentityMatrix());
			sm.getSceneNode("wheel2" + id).yaw(Degreef.createFrom(ga.getActualTurn() * 30f));

		}
		for (Entry<UUID, Item> entry : gameState.getItems().entrySet()) {
			SceneNode itemN = sm.getSceneNode(entry.getKey().toString());
			itemN.setLocalPosition(entry.getValue().getPos());
			itemN.setLocalRotation(entry.getValue().getRot());
		}
		handleInterpolation();
	}
	

	private void handleInterpolation() {
		for (HashMap.Entry<UUID, GhostAvatar> entry : gameState.getGhostAvatars().entrySet()) {
			UUID id = entry.getKey();
			
			SceneNode ghostAvatarN = getEngine().getSceneManager().getSceneNode(id.toString());
			GhostAvatar ga = entry.getValue();
			long time = frametime - ga.getLastUpdateTime();
			if (time > 0) {
				ghostAvatarN.moveForward(((float) time / 1000) * ga.getVelocityForward());
			}
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
		physicsBody.handleCollision();
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
			gameState.setRaceState(RaceState.COUNTDOWN);
		}
	}
	
	public void inputAction() {
		if (clientState.isRaceFinished()) {
			if (SHOW_PACKET_MESSAGES) System.out.println("Sending Finish Track");
			if (clientState.isConnected()) {
				clientProtocol.finishTrack(clientState.getSelectedTrack());
			}
			clientState.setJoinedTrack(0);
			clientState.setRaceFinished(false);
			setCameraToSky();
		}
		else if (!clientState.hasTrack()) {
			if (SHOW_PACKET_MESSAGES) System.out.println("Sending Join Track");
			if (clientState.isConnected()) {
				clientProtocol.joinTrack(clientState.getSelectedTrack(), carTextureNum);
			}
			else {
				joinTrack(1);
				setCameraToAvatar();
				setStartingPosition(1);
			}
		}
		else {
			if (SHOW_PACKET_MESSAGES) System.out.println("Sending Start Track");
			if (clientState.isConnected()) {
				clientProtocol.sendStartMessage(clientState.getJoinedTrack());
			}
			else {
				startRace(1);
				totalElapsedTime = -3500;
			}
		}
	}

	public ClientState getClientState() {
		return clientState;
	}
	
	public boolean isRacing() {
		return gameState.getRaceState() == RaceState.RACING;
	}
	
	public boolean hasRaceFinished() {
		return clientState.isRaceFinished();
	}

	public void updateRaceTime(long raceTime) {
		if (gameState.getRaceState() == RaceState.RACING ||
			gameState.getRaceState() == RaceState.COUNTDOWN
		) {
			timerGui.update(raceTime);
		}
	}
	
	protected void finishRace() {
		gameState.setRaceState(RaceState.FINISH);
		clientState.setRaceFinished(true);
		clientProtocol.completedRace(clientState.getSelectedTrack());
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
		final float ZONE_1_Z = 75f;
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
		startingPos = Vector3f.createFrom(
			startingPos.x(),
			getGroundHeight(startingPos.x(), startingPos.z()),
			startingPos.z()
		);
		physicsBody.setPosition(startingPos);
		playerNode.setLocalPosition(
			startingPos
		);
	}
	
	public PhysicsBody getPhysicsBody() {
		return physicsBody;
	}
	
	public boolean isRacingInputDisabled() {
		if (!(gameState.getRaceState() == RaceState.RACING)) {
			return true;
		}
		if (physicsBody.isSpinning()) {
			return true;
		}
		return false;
	}
	
	public Item getItem() {
		return item;
	}
	
	public void updateRaceState() {
		switch (gameState.getRaceState()) {
		case COUNTDOWN:
			if (gameState.getElapsedRaceTime() > 0) {
				gameState.setRaceState(RaceState.RACING);
			}
			break;
		default:
			break;
		}
	}

	public void joinTrack(int trackID) {
		clientState.setJoinedTrack(trackID);
		gameState.setRaceState(RaceState.WAITING);
		lobbyGui.hide();
	}

	public void updateAvatar(Vector3 ghostPosition, Matrix3 ghostRotation) {
		physicsBody.setPosition(ghostPosition);
		physicsBody.setRotation(ghostRotation);
	}
	
	private long lastCarTextureUpdate = 0;
	private long INPUT_DELAY = 200;
	public void guiInputLeft() {
		switch (gameState.getRaceState()) {
		case LOBBY:
			if ((System.currentTimeMillis() - lastCarTextureUpdate)  < INPUT_DELAY) {
				return;
			}
			lastCarTextureUpdate = System.currentTimeMillis();
			carTextureNum = (carTextureNum - 1);
			if (carTextureNum <= 0) {
				carTextureNum += 8;
			}
			updateCarTexture();
			break;
		default:
			break;
		}
	}
	
	public void guiInputRight() {
		switch (gameState.getRaceState()) {
		case LOBBY:
			if ((System.currentTimeMillis() - lastCarTextureUpdate)  < INPUT_DELAY) {
				return;
			}
			lastCarTextureUpdate = System.currentTimeMillis();
			carTextureNum = carTextureNum + 1;
			if (carTextureNum >= 9) {
				carTextureNum = carTextureNum - 8;
			}
			updateCarTexture();
			break;
		default:
			break;
		}
	}


	private void updateCarTexture() {
		carTexture.setTexture(getTextures().getCarTexture(carTextureNum));
	}
}