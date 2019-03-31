package myGameEngine.Networking;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import ray.networking.server.GameConnectionServer;
import ray.networking.server.IClientInfo;

public class GameServerUDP extends GameConnectionServer<UUID> {

	public GameServerUDP(int localPort, ProtocolType protocolType) throws IOException {
		super(localPort, protocolType);
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
			sendCreateMessages(clientID, pos);
		}
		else if (messageTokens[0].compareTo("update") == 0) {
			UUID clientID = UUID.fromString(messageTokens[1]);
			String[] pos = { messageTokens[2], messageTokens[3], messageTokens[4] };
			String[] rot = {
				messageTokens[5], messageTokens[6], messageTokens[7],
				messageTokens[8], messageTokens[9], messageTokens[10],
				messageTokens[11], messageTokens[12], messageTokens[13]
			};
			sendUpdateMessages(clientID, pos, rot);
		}
		else if (messageTokens[0].compareTo("bye") == 0) {
			UUID clientID = UUID.fromString(messageTokens[1]);
			sendByeMessages(clientID);
			removeClient(clientID);
		}
	}

	private void sendByeMessages(UUID clientID) {
		// TODO Auto-generated method stub
		
	}

	private void sendCreateMessages(UUID clientID, String[] pos) {
		System.out.println("Sending Create Message");
		try {
			String message = new String("create," + clientID.toString());
			message += "," + pos[0];
			message += "," + pos[1];
			message += "," + pos[2];
			forwardPacketToAll(message, clientID);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendUpdateMessages(UUID clientID, String[] pos, String[] rot) {
		try {
			String message = new String("update," + clientID.toString());
			message += "," + pos[0];
			message += "," + pos[1];
			message += "," + pos[2];
			for (int i = 0; i < 9; i++) {
				message += "," + rot[i];	
			}
			// System.out.println("Sending Update Message: " + message);
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
