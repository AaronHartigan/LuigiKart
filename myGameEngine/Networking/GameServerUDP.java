package myGameEngine.Networking;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import a3.GameState;
import a3.GhostAvatar;
import a3.Item;
import a3.ItemBox;
import a3.ItemType;
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
            Iterator<Entry<UUID, Item>> itemIter = gameState.getItems().entrySet().iterator();
            while (itemIter.hasNext()) {
                Map.Entry<UUID, Item> pair = (Map.Entry<UUID, Item>) itemIter.next();
                UUID id = pair.getKey();
                Item item = pair.getValue();
        		try {
        			String message = new String("itemUpdate," + id.toString() + ",");
        			message += item.getPos().serialize();
        			message += "," + item.getRot().serialize();
        			message += "," + ItemType.getValue(item.getType());
        			forwardPacketToAll(message, id);
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
					if (avatar.hasItem()) {
						continue;
					}
					Item newItem = new Item(ItemType.getRandomItemType());
					avatar.setItem(newItem);
	        		try {
	        			String message = new String(
	        				"gotItem," +
	        				newItem.getID() + "," + 
    						ItemType.getValue(newItem.getType())
	        			);
	        			sendPacket(message, avatar.getId());
	        		}
	        		catch (IOException e) {
	        			e.printStackTrace();
	        		}
	        		gameState.getItems().put(newItem.getID(), newItem);
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
	        		try {
	        			String message = new String(
	        				"hitItem," + avatar.getId() + "," + item.getID()
	        			);
	        			sendPacketToAll(message);
	        		}
	        		catch (IOException e) {
	        			e.printStackTrace();
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
