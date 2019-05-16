package myGameEngine.Networking;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import a3.GameState;
import a3.GhostAvatar;
import a3.Item;
import a3.ItemBox;
import a3.ItemType;
import a3.PhysicsBody;
import a3.RaceState;
import a3.Track1;
import ray.networking.server.GameConnectionServer;
import ray.networking.server.IClientInfo;
import ray.rml.Matrix3f;
import ray.rml.Vector2;
import ray.rml.Vector2f;
import ray.rml.Vector3;
import ray.rml.Vector3f;
import ray.rml.Vector4;
import ray.rml.Vector4f;

public class GameServerUDP extends GameConnectionServer<UUID> {
	private GameState gameState = null;
	private ServerState serverState = new ServerState();
	private long gameTimer = System.currentTimeMillis();
	private long elapsedTime = 0;
	private int currentTrack = -1;
	private long TICK_RATE = 60;
	private boolean[] claimedColors = {false, false, false, false, false, false, false, false};
	private int MAX_PLAYERS_PER_TRACK = 8;
	private boolean shouldInitRace = false;
	private boolean isRaceInited = false;

	public GameServerUDP(int localPort, ProtocolType protocolType, GameState gameState) throws IOException {
		super(localPort, protocolType);
		this.gameState = gameState;
		initTrack(1);
		TimerTask updateServer = new TimerTask() {
			public void run() {
				sendPackets();
			}
		};
		Timer timer = new Timer("Timer");
	    long period = 1000 / TICK_RATE;
	    timer.schedule(updateServer, 0, period);
	}
	
	@Override
	public void processPacket(Object o, InetAddress senderIP, int sendPort) {
		String message = (String) o;
		// System.out.println("Server Message: " + message);
		String[] messageTokens = message.split(",");
		
		if (messageTokens.length <= 0) {
			return;
		}
		
		if (messageTokens[0].compareTo("join") == 0) {
			try {
				IClientInfo ci;
				ci = getServerSocket().createClientInfo(senderIP, sendPort);
				UUID clientID = UUID.fromString(messageTokens[1]);
				addClient(ci, clientID);
				sendJoinedMessage(clientID, true);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if (messageTokens[0].compareTo("create") == 0) {
			UUID clientID = UUID.fromString(messageTokens[1]);
			String[] pos = { messageTokens[2], messageTokens[3], messageTokens[4] };
			int color = Integer.parseInt(messageTokens[5]);
			claimedColors[color - 1] = true;
			gameState.createGhostAvatar(clientID, Vector3f.createFrom(pos));
			gameState.getGhostAvatars().get(clientID).setPhysicsBody(
				new PhysicsBody(Vector3f.createFrom(0f, 0f, 0f), Matrix3f.createIdentityMatrix())
			);
		}
		else if (messageTokens[0].compareTo("update") == 0) {
			UUID clientID = UUID.fromString(messageTokens[1]);
			String[] pos = { messageTokens[2], messageTokens[3], messageTokens[4] };
			String[] rot = {
				messageTokens[5], messageTokens[6], messageTokens[7],
				messageTokens[8], messageTokens[9], messageTokens[10],
				messageTokens[11], messageTokens[12], messageTokens[13]
			};
			float vForward = Float.parseFloat(messageTokens[14]);
			float actualTurn = Float.parseFloat(messageTokens[15]);
			int color = Integer.parseInt(messageTokens[16]);
			gameState.updateGhostAvatar(
				clientID,
				Vector3f.createFrom(pos),
				Matrix3f.createFrom(rot),
				vForward,
				actualTurn,
				0
			);
			gameState.getGhostAvatars().get(clientID).setColor(color);
		}
		else if (messageTokens[0].compareTo("bye") == 0) {
			UUID clientID = UUID.fromString(messageTokens[1]);
			gameState.removeGhostAvatar(clientID);
			sendByeMessages(clientID);
			removeClient(clientID);
			serverState.getConnectedPlayers().remove(clientID);
		}
		else if (messageTokens[0].compareTo("joinTrack") == 0) {
			UUID clientID = UUID.fromString(messageTokens[1]);
			int trackID = Integer.parseInt(messageTokens[2]);
			int color = Integer.parseInt(messageTokens[3]);
			if (serverState.getConnectedPlayers().size() < MAX_PLAYERS_PER_TRACK) {
				serverState.getConnectedPlayers().put(clientID, new PlayerState(clientID, trackID, System.currentTimeMillis()));
				claimedColors[color - 1] = true;
				sendTrackJoinMessages(trackID, clientID, true);
			}
			else {
				sendTrackJoinMessages(trackID, clientID, false);
			}
		}
		else if (messageTokens[0].compareTo("finishTrack") == 0) {
			UUID clientID = UUID.fromString(messageTokens[1]);
			gameState.shouldRemoveGhostAvatar(clientID);
			serverState.getConnectedPlayers().remove(clientID);
		}
		else if (messageTokens[0].compareTo("throwItem") == 0) {
			UUID avatarID = UUID.fromString(messageTokens[1]);
			GhostAvatar ga = gameState.getGhostAvatars().get(avatarID);
			ga.removeItem();
		}
		else if (messageTokens[0].compareTo("updateItem") == 0) {
			UUID itemID = UUID.fromString(messageTokens[1]);
			String[] pos = { messageTokens[2], messageTokens[3], messageTokens[4] };
			String[] rot = {
				messageTokens[5], messageTokens[6], messageTokens[7],
				messageTokens[8], messageTokens[9], messageTokens[10],
				messageTokens[11], messageTokens[12], messageTokens[13]
			};
			gameState.updateItem(
				itemID,
				Vector3f.createFrom(pos),
				Matrix3f.createFrom(rot)
			);
		}
		else if (messageTokens[0].compareTo("startRace") == 0) {
			UUID clientID = UUID.fromString(messageTokens[1]);
			int trackID = Integer.parseInt(messageTokens[2]);
			setShouldInitRace(true);
			sendStartRace(trackID);
		}
		else if (messageTokens[0].compareTo("completedRace") == 0) {
			UUID clientID = UUID.fromString(messageTokens[1]);
			GhostAvatar ga = gameState.getGhostAvatars().get(clientID);
			ga.getPhysicsBody().setPosition(ga.getPos());
			ga.getPhysicsBody().setRotation(ga.getRot());
			ga.setNPC(true);
		}
	}

	private void generateNPCs() {
		synchronized (gameState) {
			int players = gameState.getGhostAvatars().size();
			for (int i = 0; i < MAX_PLAYERS_PER_TRACK - players; i++) {
				UUID ghostID = UUID.randomUUID();
				GhostAvatar npc = new GhostAvatar(ghostID);
				npc.setNPC(true);
				npc.setPos(Track1.getPosition(MAX_PLAYERS_PER_TRACK - i));
				npc.setColor(getNextColor());
				gameState.getGhostAvatars().put(ghostID, npc);
				gameState.getGhostAvatars().get(ghostID).setPhysicsBody(
					new PhysicsBody(Track1.getPosition(MAX_PLAYERS_PER_TRACK - i), Matrix3f.createIdentityMatrix())
				);
				gameState.getGhostAvatars().get(ghostID).getPhysicsBody().randomizeConstants();
			}
		}
	}

	private void updateNPC(GhostAvatar ga, long elapsedTime) {
		PhysicsBody physicsBody = ga.getPhysicsBody();
		setInputs(physicsBody, ga);
		physicsBody.updatePhysics(elapsedTime);
		ga.setPos(physicsBody.getPosition());
		ga.setRot(physicsBody.getDirection().mult(physicsBody.getRotation().mult(physicsBody.getSpinRotation())));
		ga.setVelocityForward(physicsBody.getVForward());
		ga.setActualTurn(physicsBody.getActualTurn());
		int newWaypoint = determineWaypoint(ga);
		// System.out.println("Waypoint: " + newWaypoint);
		ga.setWaypoint(newWaypoint);
		if (ga.hasItem()) {
			Item item = ga.getItem();
			item.setRot(physicsBody.getDirection().mult(physicsBody.getRotation()));
			item.setPos(physicsBody.getPosition());
			item.setPos(item.getPos().add(item.getRot().column(2).mult(-1.1f)));
		}
	}

	private int determineWaypoint(GhostAvatar ga) {
		int nextWaypoint = (ga.getWaypoint() + 1) % Track1.NUM_WAYPOINTS;
		Vector4 nextWaypointLine = Track1.getWaypointLine(nextWaypoint);
		Vector3 pos = ga.getPos();
		boolean sign = getSign(Vector2f.createFrom(pos.x(), pos.z()), nextWaypointLine);
		return (sign == getSign(Track1.getPointInWaypoint(nextWaypoint), nextWaypointLine))
			? nextWaypoint : 
			ga.getWaypoint();
	}
	
	private boolean getSign(Vector2 a, Vector4 b) {
		return (
			(a.x() - b.x()) * (b.w() - b.y())
			-
			(a.y() - b.y()) * (b.z() - b.x())
		) < 0;
	}

	private void setInputs(PhysicsBody pb, GhostAvatar ga) {
		pb.resetInputs();
		if (isRacingInputDisabled(pb)) {
			return;
		}
		else {
			pb.setDesiredTurn(computeTurnAndSetAcceleration(ga, pb));
		}
	}
	
	private float computeTurnAndSetAcceleration(GhostAvatar ga, PhysicsBody pb) {
		Vector3 nextWaypoint = Track1.getWaypoint((ga.getWaypoint() + 1) % Track1.NUM_WAYPOINTS);
		Vector3 pos = ga.getPos();
		Vector3 heading = pos.add(ga.getRot().column(2));
		Vector4 playerLine = Vector4f.createFrom(pos.x(), pos.z(), heading.x(), heading.z());
		float sign =
			(nextWaypoint.x() - playerLine.x()) * (playerLine.w() - playerLine.y())
			-
			(nextWaypoint.z() - playerLine.y()) * (playerLine.z() - playerLine.x())
		;
		double dist = calcDistance(
			pos.x(), pos.z(),
			nextWaypoint.x(), nextWaypoint.z()
		);
		float angle1 = (float) Math.atan2(playerLine.y() - nextWaypoint.z(), playerLine.x() - nextWaypoint.x());
		float carAngle = (float) Math.atan2(pos.z() - heading.z(), pos.x() - heading.x());
		angle1 = (float) (angle1 * 180 / Math.PI);
		carAngle = (float) (carAngle * 180 / Math.PI);
		float angleDif1 = Math.abs(Math.abs(angle1) - Math.abs(carAngle));
		//System.out.println("------");
		//System.out.println(angleDif);
		//System.out.println(angle1 + "," + carAngle);
		
		sign = (sign > 0) ? 1f : -1f;
		float damping1 = 1f;
		if (angleDif1 < 30f) {
			damping1 = angleDif1 / 30f;
		}
		final float DISTANCE = 30f;
		float factor1 = (float) (dist / DISTANCE);
		float turn = sign * factor1 * damping1;
		if (dist < DISTANCE && dist > 0f) {
			Vector3 nextWaypoint2 = Track1.getWaypoint((ga.getWaypoint() + 2) % Track1.NUM_WAYPOINTS);
			float sign2 =
				(nextWaypoint2.x() - playerLine.x()) * (playerLine.w() - playerLine.y())
				-
				(nextWaypoint2.z() - playerLine.y()) * (playerLine.z() - playerLine.x())
			;
			sign2 = (sign2 > 0) ? 1f : -1f;
			float factor2 = 1 - factor1;
			
			float angle2 = (float) Math.atan2(playerLine.y() - nextWaypoint2.z(), playerLine.x() - nextWaypoint2.x());
			angle2 = (float) (angle2 * 180 / Math.PI);
			float damping2 = 1f;
			float angleDif2 = Math.abs(Math.abs(angle2) - Math.abs(carAngle));
			if (angleDif2 < 30f) {
				damping2 = angleDif2 / 30f;
			}
			turn = sign * factor1 * damping1 + sign2 * factor2 * damping2;
			/*
			System.out.println(sign + ", " + factor1 + ", " + angle1 + ", " + damping1);
			System.out.println(sign2 + ", " + factor2 + ", " + angle2 + ", " + damping2);
			System.out.println(turn);
			System.out.println("------------");
			*/
		}
		if (angleDif1 > 30f) {
			pb.setDrifting(true);
		}
		if (angleDif1 < 45f){
			pb.setAccelerating(true);
		}
		if (pb.getVForward() < 3f) {
			pb.setAccelerating(true);
		}
		return turn;
	}
	
	private Vector2 intersectionPoint(Vector4 line1, Vector4 line2) {
		return Vector2f.createFrom(
			0f, 0f
		);
	}

	private boolean isRacingInputDisabled(PhysicsBody pb) {
		if (gameState.getRaceState() == RaceState.LOBBY) {
			return true;
		}
		if (gameState.getRaceState() == RaceState.COUNTDOWN) {
			return true;
		}
		if (pb.isSpinning()) {
			return true;
		}
		return false;
	}
	
	private void updateRaceState() {
		switch (gameState.getRaceState()) {
		case COUNTDOWN:
			if (gameState.getElapsedRaceTime() >= 0) {
				gameState.setRaceState(RaceState.RACING);
			}
			break;
		case RACING:
			if (serverState.getConnectedPlayers().size() <= 0) {
				gameState.setRaceState(RaceState.FINISH);
			}
			break;
		default:
			break;
		}
	}

	public void sendPackets() {
		updateRaceState();
    	if (shouldInitRace) {
    		System.out.println("Initting race");
    		initRace();
    	}
    	else if (gameState.getRaceState() == RaceState.FINISH) {
    		resetTrack(1);
    		return;
    	}
    	long newTime = System.currentTimeMillis();
    	elapsedTime = newTime - gameTimer;
    	gameTimer = newTime;
 
    	if (gameState.getRaceState() == RaceState.LOBBY) {
    		return;
    	}

    	gameState.setElapsedRaceTime(gameState.getElapsedRaceTime() + elapsedTime);
    	try {
			String message = new String("raceTime," + gameState.getElapsedRaceTime());
			sendPacketToAll(message);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
    	checkCollisions();
    	updateItemBoxTimers();
    	try {
        	Iterator<Entry<UUID, GhostAvatar>> avatarIter = gameState.getGhostAvatars().entrySet().iterator();
        	String message = new String();
        	while (avatarIter.hasNext()) {
                Map.Entry<UUID, GhostAvatar> pair = (Map.Entry<UUID, GhostAvatar>) avatarIter.next();
                UUID id = pair.getKey();
                GhostAvatar ga = pair.getValue();
                if (ga.isShouldRemove()) {
        			sendByeMessages(ga.getId());
                	avatarIter.remove();
                	continue;
                }
                if (ga.isNPC()) {
                	updateNPC(ga, elapsedTime);
                }
                if (message.length() > 0) {
                	message += '\0';
                }
    			message += "update," + id.toString() + "," + newTime + ",";
    			message += ga.getPos().serialize();
    			message += "," + ga.getRot().serialize();
    			message += "," + ga.getVelocityForward();
    			message += "," + ga.getActualTurn();
    			message += "," + ga.getColor(); // getNextColor();
        	}
        	sendPacketToAll(message);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		try {
			Iterator<Entry<UUID, ItemBox>> itemBoxIter = gameState.getItemBoxes().entrySet().iterator();
			String message = new String();
            while (itemBoxIter.hasNext()) {
                Map.Entry<UUID, ItemBox> pair = (Map.Entry<UUID, ItemBox>) itemBoxIter.next();
                UUID id = pair.getKey();
                ItemBox itemBox = pair.getValue();
                if (message.length() > 0) {
                	message += '\0';
                }
                message += "uIB," + id.toString() + ",";
                message += itemBox.getPos().serialize();
    			message += "," + itemBox.getIsActive();
    			message += "," + itemBox.isGrowing();
    			message += "," + itemBox.getRegrowthTimer();
            }
			sendPacketToAll(message);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		try {
			Iterator<Entry<UUID, Item>> itemIter = gameState.getItems().entrySet().iterator();
			String message = new String();
			while (itemIter.hasNext()) {
				Map.Entry<UUID, Item> pair = (Map.Entry<UUID, Item>) itemIter.next();
                UUID id = pair.getKey();
                Item item = pair.getValue();
                if (message.length() > 0) {
                	message += '\0';
                }
                message += "itemUpdate," + id.toString() + ",";
                message += item.getPos().serialize();
    			message += "," + item.getRot().serialize();
    			message += "," + ItemType.getValue(item.getType());
            }
			sendPacketToAll(message);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private int getNextColor() {
		for (int i = 0; i < 8; i++) {
			if (!claimedColors[i]) {
				claimedColors[i] = true;
				return i + 1;
			}
		}
		return 1;
	}

	private void sendTrackJoinMessages(int trackID, UUID clientID, boolean success) {
		System.out.println("Sending Track Join Message");
		if (success) {
			try {
				String message = new String(
					"joinTrack,"
					+ trackID + ","
					+ clientID.toString() + ","
					+ serverState.getConnectedPlayers().size() + ","
					+ "success"
				);
				sendPacket(message, clientID);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				String message = new String(
						"joinTrack,"
						+ trackID + ","
						+ clientID.toString() + ","
						+ "-1" + ","
						+ "failure"
					);
				sendPacket(message, clientID);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void sendStartRace(int trackID) {
		try {
			String message = new String(
				"startRace,"
				+ trackID
			);
			sendPacketToAll(message);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void updateItemBoxTimers() {
		Iterator<Entry<UUID, ItemBox>> itemBoxIter = gameState.getItemBoxes().entrySet().iterator();
		while (itemBoxIter.hasNext()) {
			Map.Entry<UUID, ItemBox> itemBoxPair = (Map.Entry<UUID, ItemBox>) itemBoxIter.next();
			ItemBox itemBox = itemBoxPair.getValue();
			itemBox.updateTimers(elapsedTime);
		}
	}

	private void checkCollisions() {
		Iterator<Entry<UUID, GhostAvatar>> avatarIter = gameState.getGhostAvatars().entrySet().iterator();
		while (avatarIter.hasNext()) {
			Map.Entry<UUID, GhostAvatar> avatarPair = (Map.Entry<UUID, GhostAvatar>) avatarIter.next();
			GhostAvatar avatar = avatarPair.getValue();
			Vector3 gaPos = avatar.getPos();
			Iterator<Entry<UUID, ItemBox>> itemBoxIter = gameState.getItemBoxes().entrySet().iterator();
			// Check collisions with item boxes
			while (itemBoxIter.hasNext()) {
				Map.Entry<UUID, ItemBox> itemBoxPair = (Map.Entry<UUID, ItemBox>) itemBoxIter.next();
				ItemBox itemBox = itemBoxPair.getValue();
				if (itemBox.getIsActive() == 0 || itemBox.isGrowing() == 1) {
					continue;
				}
				Vector3 ibPos = itemBox.getPos();
				
				double dist = calcDistance(
					ibPos.x(), ibPos.z(),
					gaPos.x(), gaPos.z()
				);
				// If a player has hit an item box
				if (dist < 1f) {
					itemBox.setIsActive(0);
					Vector3 forwardVector = avatar.getRot().column(2);
					float velocityForward = avatar.getVelocityForward();
        			String message = new String(
        				"itemBoxExplosion," +
						ibPos.serialize() +
	        			"," + forwardVector.x() * velocityForward +
	        			"," + forwardVector.y() * velocityForward + 
	        			"," + forwardVector.z() * velocityForward
        			);
        			try {
						sendPacketToAll(message);
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (avatar.hasItem()) {
						continue;
					}
					Item newItem = new Item(ItemType.getRandomItemType());
					avatar.setItem(newItem);
	        		gameState.getItems().put(newItem.getID(), newItem);
	        		if (avatar.isNPC()) {
	        			continue;
	        		}
	        		try {
	        			message = new String(
	        				"gotItem," +
	        				newItem.getID() + "," + 
    						ItemType.getValue(newItem.getType())
	        			);
	        			sendPacket(message, avatar.getId());
	        		}
	        		catch (IOException e) {
	        			e.printStackTrace();
	        		}
				}
			}
			
			Iterator<Entry<UUID, Item>> itemsIter = gameState.getItems().entrySet().iterator();
			while (itemsIter.hasNext()) {
				Map.Entry<UUID, Item> itemPair = (Map.Entry<UUID, Item>) itemsIter.next();
				Item item = itemPair.getValue();
				if (avatar.hasItem() && avatar.getItem().getID().equals(item.getID())) {
					continue;
				}
				
				Vector3 iPos = item.getPos();
				
				double dist = calcDistance(
					iPos.x(), iPos.z(),
					gaPos.x(), gaPos.z()
				);
				// System.out.println(dist);
				// If a player has hit an item
				if (dist < 1f) {
					removeItemFromAvatar(item.getID());
					Vector3 forwardVector = avatar.getRot().column(2);
					float velocityForward = avatar.getVelocityForward();
	        		try {
	        			String message = new String(
	        				"hitItem" +
		        			"," + avatar.getId() +
		        			"," + item.getID() +
		        			"," + forwardVector.x() * velocityForward +
		        			"," + forwardVector.y() * velocityForward + 
		        			"," + forwardVector.z() * velocityForward
	        			);
	        			sendPacketToAll(message);
	        		}
	        		catch (IOException e) {
	        			e.printStackTrace();
	        		}
	        		if (avatar.isNPC()) {
	        			avatar.getPhysicsBody().handleCollision();
	        		}
	        		itemsIter.remove();
				}
			}
		}
	}
	
	protected void removeItemFromAvatar(UUID itemID) {
		Iterator<Entry<UUID, GhostAvatar>> avatarIter = gameState.getGhostAvatars().entrySet().iterator();
		while (avatarIter.hasNext()) {
			Map.Entry<UUID, GhostAvatar> avatarPair = (Map.Entry<UUID, GhostAvatar>) avatarIter.next();
			GhostAvatar avatar = avatarPair.getValue();
			if (avatar.hasItem() && avatar.getItem().getID().equals(itemID)) {
				avatar.removeItem();
			}
		}
	}
	
	protected double calcDistance(float x1, float y1, float x2, float y2) {
		float dx = (x1 - x2);
		float dy = (y1 - y2);
		return Math.sqrt(dx * dx + dy * dy);
	}
	
	protected double calcDistance(float x1, float y1, float z1, float x2, float y2, float z2) {
		float dx = (x1 - x2);
		float dy = (y1 - y2);
		float dz = (z1 - z2);
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	private void sendByeMessages(UUID clientID) {
		System.out.println("Sending Bye Message");
		try {
			String message = new String("bye," + clientID.toString());
			forwardPacketToAll(message, clientID);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendJoinedMessage(UUID clientID, boolean success) {
		// format: join, success or join, failure
		try {
			String message = new String("join,");
			if (success) {
				message += "success";
			}
			else {
				message += "failure";
			}
			sendPacket(message, clientID);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setTrack(int trackID) {
		this.currentTrack = trackID;
	}
	
	public Boolean isATrackSelected() {
		return currentTrack != -1;
	}
	
	private void initRace() {
		if (!isRaceInited) {
			generateNPCs();
			gameState.setRaceState(RaceState.COUNTDOWN);
			gameState.setElapsedRaceTime(-5000);
			isRaceInited = true;
		}
		setShouldInitRace(false);
	}
	
	private void resetTrack(int trackID) {
		isRaceInited = false;
		synchronized(gameState.getGhostAvatars()) {
			Iterator<Entry<UUID, GhostAvatar>> avatarIter = gameState.getGhostAvatars().entrySet().iterator();
	    	while (avatarIter.hasNext()) {
	            Map.Entry<UUID, GhostAvatar> pair = (Map.Entry<UUID, GhostAvatar>) avatarIter.next();
	            UUID id = pair.getKey();
	            if (!(pair.getValue().isNPC())) {
	            	continue;
	            }
	    		gameState.removeGhostAvatar(id);
	    		sendByeMessages(id);
	    		avatarIter.remove();
	    	}
	    	gameState.setElapsedRaceTime(0l);
		}
	}

	private void initTrack(int trackID) {
		System.out.println("Initializing Track: " + trackID);
		setTrack(trackID);
		new Track1().initTrack(gameState);
		System.out.println("Finished Initializing Track: " + trackID);
	}

	public boolean isShouldInitRace() {
		return shouldInitRace;
	}

	public void setShouldInitRace(boolean shouldInitRace) {
		this.shouldInitRace = shouldInitRace;
	}
}
