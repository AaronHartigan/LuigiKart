package myGameEngine.Networking;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import a3.GameState;
import a3.GhostAvatar;
import ray.networking.server.GameConnectionServer;
import ray.networking.server.IClientInfo;
import ray.rml.Matrix3f;
import ray.rml.Vector3f;

public class GameServerUDP extends GameConnectionServer<UUID> {
	private GameState gameState = null;
	private long TICK_RATE = 60;

	public GameServerUDP(int localPort, ProtocolType protocolType, GameState gameState) throws IOException {
		super(localPort, protocolType);
		this.gameState = gameState;
		sendPackets();
		System.out.println("TeeHee");
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
	}

	public void sendPackets() {
        while (true) {
        	long time = System.currentTimeMillis();
            Iterator<Entry<UUID, GhostAvatar>> it = gameState.getGhostAvatars().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, GhostAvatar> pair = (Map.Entry<UUID, GhostAvatar>) it.next();
                UUID id = pair.getKey();
                GhostAvatar ga = pair.getValue();
                // System.out.println("Updating: " + id.toString());
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
    		try {
    			Thread.sleep(Math.max(0, (1000 / TICK_RATE) - (System.currentTimeMillis() - time)));
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
    	}
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

}
