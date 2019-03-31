package NetworkingServer;

import java.io.IOException;

import a3.GameState;
import myGameEngine.Networking.GameServerUDP;
import ray.networking.IGameConnection.ProtocolType;

public class Server {
	private GameServerUDP udpServer;
	// private GameServerTCP tcpServer;
	private GameState gameState = new GameState();
	
	public Server(int serverPort, String protocol) {
		try {
			if (protocol.toUpperCase().compareTo("UDP") == 0) {
				udpServer = new GameServerUDP(serverPort, ProtocolType.UDP, gameState);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		if (args.length <= 1) {
			System.out.println("Incorrect number of arguments");
			return;
		}
		
		Server server = new Server(Integer.parseInt(args[0]), args[1]);
	}
}
