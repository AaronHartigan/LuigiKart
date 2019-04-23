package myGameEngine.Networking;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerState {
	private Map<UUID, PlayerState> connectedPlayers = 
		Collections.synchronizedMap(new HashMap<UUID, PlayerState>());

	public ServerState() {
		
	}
	
	public Map<UUID, PlayerState> getConnectedPlayers() {
		return connectedPlayers;
	}
}
