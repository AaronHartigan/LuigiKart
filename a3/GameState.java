package a3;

import java.util.HashMap;
import java.util.UUID;

import ray.rml.Matrix3;
import ray.rml.Vector3;

public class GameState {
	private HashMap<UUID, GhostAvatar> ghostAvatars = new HashMap<UUID, GhostAvatar>();
	private HashMap<UUID, GhostAvatar> staticObjects;
	private HashMap<UUID, GhostAvatar> dynamicObjects;
	
	public GameState() {
		
	}

	public HashMap<UUID, GhostAvatar> getGhostAvatars() {
		return ghostAvatars;
	}

	public void setGhostAvatars(HashMap<UUID, GhostAvatar> ghostAvatars) {
		this.ghostAvatars = ghostAvatars;
	}

	public HashMap<UUID, GhostAvatar> getStaticObjects() {
		return staticObjects;
	}

	public void setStaticObjects(HashMap<UUID, GhostAvatar> staticObjects) {
		this.staticObjects = staticObjects;
	}

	public HashMap<UUID, GhostAvatar> getDynamicObjects() {
		return dynamicObjects;
	}

	public void setDynamicObjects(HashMap<UUID, GhostAvatar> dynamicObjects) {
		this.dynamicObjects = dynamicObjects;
	}
	
	public void createGhostAvatar(UUID ghostID, Vector3 ghostPosition) {
		ghostAvatars.put(ghostID, new GhostAvatar(ghostID));
	}
	
	public void updateGhostAvatar(UUID ghostID, Vector3 ghostPosition, Matrix3 ghostRotation) {
		ghostAvatars.get(ghostID).setPos(ghostPosition);
		ghostAvatars.get(ghostID).setRot(ghostRotation);
	}
	
	public void removeGhostAvatar(UUID ghostID) {
		ghostAvatars.remove(ghostID);
	}
}
