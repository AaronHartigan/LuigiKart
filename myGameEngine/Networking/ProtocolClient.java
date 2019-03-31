package myGameEngine.Networking;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

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
		System.out.println("Client Message: " + message);
		String[] messageTokens = strMessage.split(",");
		if (messageTokens.length <= 0) {
			return;
		}
		
		if (messageTokens[0].compareTo("join") == 0) {
			// format: join, success or join, failure
			if (messageTokens[1].compareTo("success") == 0) {
				game.setConnected(true);
				sendCreateMessage(game.getPlayerPosition());
			}
			else if (messageTokens[1].compareTo("failure") == 0) {
				game.setConnected(false);
			}
		}
		else if (messageTokens[0].compareTo("bye") == 0) { // format: bye, remoteId
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
			// format: update, remoteId, x,y,z
			UUID ghostID = UUID.fromString(messageTokens[1]);
			Vector3 ghostPosition = Vector3f.createFrom(
				Float.parseFloat(messageTokens[2]),
				Float.parseFloat(messageTokens[3]),
				Float.parseFloat(messageTokens[4])
			);
			float[] floats = new float[9];
			for (int i = 0; i < 9; i++){
			    floats[i] = Float.parseFloat(messageTokens[i + 5]);
			}
			Matrix3 ghostRotation = Matrix3f.createFrom(floats);
			game.updateGhostAvatar(ghostID, ghostPosition, ghostRotation);
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

	public void updatePlayerInformation(Vector3 pos, Matrix3 rot) {
		try {
			String message = new String("update," + id.toString());
			message += "," + pos.x()+"," + pos.y() + "," + pos.z();
			message += "," + rot.serialize();
			sendPacket(message);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
