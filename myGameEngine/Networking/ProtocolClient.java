package myGameEngine.Networking;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import a3.ItemType;
import a3.MyGame;
import ray.networking.client.GameConnectionClient;
import ray.rml.Matrix3;
import ray.rml.Matrix3f;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class ProtocolClient extends GameConnectionClient {

	private MyGame game;
	private UUID id;

	public ProtocolClient(InetAddress remoteAddr, int remotePort, ProtocolType protocolType, MyGame game) throws IOException {
		super(remoteAddr, remotePort, protocolType);
		this.game = game;
		this.id = UUID.randomUUID();
	}
	
	@Override
	protected void processPacket(Object message) {
		String strMessage = (String) message;
		if (strMessage == null) {
			return;
		}
		String[] messageTokens = strMessage.split(",");
		if (messageTokens.length <= 0) {
			return;
		}
		
		if (messageTokens[0].compareTo("join") == 0) {
			System.out.println("Client Message: " + message);
			if (messageTokens[1].compareTo("success") == 0) {
				game.setConnected(true);
				sendCreateMessage(game.getPlayerPosition());
			}
			else if (messageTokens[1].compareTo("failure") == 0) {
				game.setConnected(false);
			}
		}
		else if (messageTokens[0].compareTo("bye") == 0) {
			System.out.println("Client Message: " + message);
			UUID ghostID = UUID.fromString(messageTokens[1]);
			game.removeGhostAvatar(ghostID);
		}
		else if (messageTokens[0].compareTo("create") == 0) {
			// format: create, remoteId, x,y,z
			UUID ghostID = UUID.fromString(messageTokens[1]);
			Vector3 ghostPosition = Vector3f.createFrom(
				Float.parseFloat(messageTokens[2]),
				Float.parseFloat(messageTokens[3]),
				Float.parseFloat(messageTokens[4])
			);
			game.createGhostAvatar(ghostID, ghostPosition);
		}
		else if(messageTokens[0].compareTo("update") == 0) { // etc…..
			// format: update, remoteId, x,y,z, rot
			UUID ghostID = UUID.fromString(messageTokens[1]);
			Vector3 ghostPosition = Vector3f.createFrom(
				messageTokens[2],
				messageTokens[3],
				messageTokens[4]
			);
			float[] floats = new float[9];
			for (int i = 0; i < 9; i++){
			    floats[i] = Float.parseFloat(messageTokens[i + 5]);
			}
			Matrix3 ghostRotation = Matrix3f.createFrom(floats);
			float vForward = Float.parseFloat(messageTokens[14]);
			game.updateGhostAvatar(ghostID, ghostPosition, ghostRotation, vForward);
		}
		else if(messageTokens[0].compareTo("uIB") == 0) {
			// Update ItemBox
			// format: uIB, remoteId, x,y,z, isActive
			UUID id = UUID.fromString(messageTokens[1]);
			Vector3 pos = Vector3f.createFrom(
				messageTokens[2],
				messageTokens[3],
				messageTokens[4]
			);
			int isActive = Integer.parseInt(messageTokens[5]);
			int isGrowing = Integer.parseInt(messageTokens[6]);
			long growthTimer = Long.parseLong(messageTokens[7]);
			game.updateItemBox(id, pos, isActive, isGrowing, growthTimer);
		}
		else if(messageTokens[0].compareTo("track") == 0) { 
			int trackID = Integer.parseInt(messageTokens[1]);
			// do something with this later...
		}
		else if(messageTokens[0].compareTo("gotItem") == 0) { 
			UUID itemID = UUID.fromString(messageTokens[1]);
			int itemType = Integer.parseInt(messageTokens[2]);
			game.setPlayerItem(itemID, itemType);
		}
		else if(messageTokens[0].compareTo("hitItem") == 0) { 
			UUID playerID = UUID.fromString(messageTokens[1]);
			UUID itemID = UUID.fromString(messageTokens[2]);
			Vector3 force = Vector3f.createFrom(
				messageTokens[3],
				messageTokens[4],
				messageTokens[5]
			);
			if (playerID.equals(id)) {
				game.handlePlayerHitItem(itemID);
			}
			game.removeItem(itemID, force);
		}
		else if(messageTokens[0].compareTo("itemUpdate") == 0) { 
			UUID itemID = UUID.fromString(messageTokens[1]);
			Vector3 itemPos = Vector3f.createFrom(
				messageTokens[2],
				messageTokens[3],
				messageTokens[4]
			);
			float[] floats = new float[9];
			for (int i = 0; i < 9; i++){
			    floats[i] = Float.parseFloat(messageTokens[i + 5]);
			}
			Matrix3 itemRot = Matrix3f.createFrom(floats);
			int itemType = Integer.parseInt(messageTokens[14]);
			game.updateItem(itemID, itemPos, itemRot, itemType);
		}
		else if(messageTokens[0].compareTo("wsds") == 0) { 
			// etc…..
			// rec. “wants…”
		}
		else if(messageTokens[0].compareTo("move") == 0) { 
			// rec. “move...”
			// etc….. 
		}
	}

	public void sendCreateMessage(Vector3 pos) {
		try {
			String message = new String("create," + id.toString());
			message += "," + pos.x()+"," + pos.y() + "," + pos.z();
			sendPacket(message);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendJoinMessage() {
		try {
			String message = "join," + id.toString();
			sendPacket(message);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void updatePlayerInformation(Vector3 pos, Matrix3 rot, float vForward) {
		try {
			String message = new String("update," + id.toString());
			message += "," + pos.x()+"," + pos.y() + "," + pos.z();
			message += "," + rot.serialize();
			message += "," + vForward;
			sendPacket(message);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendByeMessage() {
		try {
			String message = "bye," + id.toString();
			sendPacket(message);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void selectTrack(int i) {
		try {
			String message = "track," + i;
			sendPacket(message);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendThrowItem() {
		try {
			String message = "throwItem," + id.toString();
			sendPacket(message);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void updateItem(UUID itemID, Vector3 itemPos, Matrix3 itemRot, ItemType type) {
		try {
			String message = new String("updateItem," + itemID.toString());
			message += "," + itemPos.x()+"," + itemPos.y() + "," + itemPos.z();
			message += "," + itemRot.serialize();
			message += "," + ItemType.getValue(type);
			sendPacket(message);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
