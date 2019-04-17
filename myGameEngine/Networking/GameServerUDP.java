package myGameEngine.Networking;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import a3.GameState;
import a3.GhostAvatar;
import a3.ItemBox;
import a3.Track1;
import ray.networking.server.GameConnectionServer;
import ray.networking.server.IClientInfo;
import ray.rml.Matrix3f;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class GameServerUDP extends GameConnectionServer<UUID> {
	private GameState gameState = null;
	private long gameTimer = System.currentTimeMillis();
	private long elapsedTime = 0;
	private int currentTrack = -1;
	private long TICK_RATE = 60;

	public GameServerUDP(int localPort, ProtocolType protocolType, GameState gameState) throws IOException {
		super(localPort, protocolType);
		this.gameState = gameState;
		sendPackets();
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
			gameState.createGhostAvatar(clientID, Vector3f.createFrom(pos));
		}
		else if (messageTokens[0].compareTo("update") == 0) {
			UUID clientID = UUID.fromString(messageTokens[1]);
			String[] pos = { messageTokens[2], messageTokens[3], messageTokens[4] };
			String[] rot = {
				messageTokens[5], messageTokens[6], messageTokens[7],
				messageTokens[8], messageTokens[9], messageTokens[10],
				messageTokens[11], messageTokens[12], messageTokens[13]
			};
			gameState.updateGhostAvatar(
				clientID,
				Vector3f.createFrom(pos),
				Matrix3f.createFrom(rot)
			);
		}
		else if (messageTokens[0].compareTo("bye") == 0) {
			UUID clientID = UUID.fromString(messageTokens[1]);
			gameState.removeGhostAvatar(clientID);
			sendByeMessages(clientID);
			removeClient(clientID);
		}
		else if (messageTokens[0].compareTo("track") == 0) {
			int trackID = Integer.parseInt(messageTokens[1]);
			if (isATrackSelected()) {
				return;
			}
			initTrack(trackID);
			sendTrackMessages(trackID);
		}
	}

	private void sendTrackMessages(int trackID) {
		System.out.println("Sending Track Message");
		try {
			String message = new String("track," + trackID);
			sendPacketToAll(message);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendPackets() {
        while (true) {
        	long newTime = System.currentTimeMillis();
        	elapsedTime = newTime - gameTimer;
        	gameTimer = newTime;
        	checkCollisions();
        	updateItemBoxTimers();
            Iterator<Entry<UUID, GhostAvatar>> avatarIter = gameState.getGhostAvatars().entrySet().iterator();
            while (avatarIter.hasNext()) {
                Map.Entry<UUID, GhostAvatar> pair = (Map.Entry<UUID, GhostAvatar>) avatarIter.next();
                UUID id = pair.getKey();
                GhostAvatar ga = pair.getValue();
        		try {
        			String message = new String("update," + id.toString() + ",");
        			message += ga.getPos().serialize();
        			message += "," + ga.getRot().serialize();
        			forwardPacketToAll(message, id);
        		}
        		catch (IOException e) {
        			e.printStackTrace();
        		}
        	}
            Iterator<Entry<UUID, ItemBox>> itemBoxIter = gameState.getItemBoxes().entrySet().iterator();
            while (itemBoxIter.hasNext()) {
                Map.Entry<UUID, ItemBox> pair = (Map.Entry<UUID, ItemBox>) itemBoxIter.next();
                UUID id = pair.getKey();
                ItemBox itemBox = pair.getValue();
        		try {
        			String message = new String("uIB," + id.toString() + ",");
        			message += itemBox.getPos().serialize();
        			message += "," + itemBox.getIsActive();
        			message += "," + itemBox.isGrowing();
        			message += "," + itemBox.getRegrowthTimer();
        			sendPacketToAll(message);
        		}
        		catch (IOException e) {
        			e.printStackTrace();
        		}
            }
    		try {
    			Thread.sleep(Math.max(0, (1000 / TICK_RATE) - elapsedTime));
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
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
		Iterator<Entry<UUID, ItemBox>> itemBoxIter = gameState.getItemBoxes().entrySet().iterator();
		while (itemBoxIter.hasNext()) {
			Map.Entry<UUID, ItemBox> itemPair = (Map.Entry<UUID, ItemBox>) itemBoxIter.next();
			ItemBox itemBox = itemPair.getValue();
			if (itemBox.getIsActive() == 0 || itemBox.isGrowing() == 1) {
				continue;
			}
			Iterator<Entry<UUID, GhostAvatar>> avatarIter = gameState.getGhostAvatars().entrySet().iterator();
			while (avatarIter.hasNext()) {
				Map.Entry<UUID, GhostAvatar> avatarPair = (Map.Entry<UUID, GhostAvatar>) avatarIter.next();
				
				GhostAvatar avatar = avatarPair.getValue();
				Vector3 ibPos = itemBox.getPos();
				Vector3 gaPos = avatar.getPos();
				
				double dist = calcDistance(
					ibPos.x(), ibPos.z(),
					gaPos.x(), gaPos.z()
				);
				if (dist < 1f) {
					itemBox.setIsActive(0);
	        		try {
	        			String message = new String("gIB," + itemBox.getId().toString() + ",");
	        			message += itemBox.getPos().serialize();
	        			sendPacket(message, avatar.getId());
	        		}
	        		catch (IOException e) {
	        			e.printStackTrace();
	        		}
				}
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
			if (success) message += "success";
			else message += "failure";
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

	private void initTrack(int trackID) {
		System.out.println("Initializing Track: " + trackID);
		setTrack(trackID);
		new Track1().initTrack(gameState);
	}
}
