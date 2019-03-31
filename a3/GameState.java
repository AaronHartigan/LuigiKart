package a3;

import java.util.HashMap;
import java.util.UUID;

public class GameState {
	private HashMap<UUID, GhostAvatar> ghostAvatars = new HashMap<UUID, GhostAvatar>();
	private HashMap<UUID, GhostAvatar> staticObjects;
	
	public GameState() {
		
	}
}
