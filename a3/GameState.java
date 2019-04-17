package a3;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ray.rml.Matrix3;
import ray.rml.Vector3;

public class GameState {
	private Map<UUID, GhostAvatar> ghostAvatars = Collections.synchronizedMap(new HashMap<UUID, GhostAvatar>());
	private Map<UUID, ItemBox> itemBoxes = Collections.synchronizedMap(new HashMap<UUID, ItemBox>());
	private HashMap<UUID, GhostAvatar> dynamicObjects;
	
	public GameState() {
		
	}

	public Map<UUID, GhostAvatar> getGhostAvatars() {
		return ghostAvatars;
	}

	public void setGhostAvatars(HashMap<UUID, GhostAvatar> ghostAvatars) {
		this.ghostAvatars = ghostAvatars;
	}

	public Map<UUID, ItemBox> getItemBoxes() {
		return itemBoxes;
	}

	public void setItemBoxes(HashMap<UUID, ItemBox> itemBoxes) {
		this.itemBoxes = itemBoxes;
	}
	
	public void createItemBox(UUID id, Vector3 pos) {
		itemBoxes.put(id, new ItemBox(id, pos));
	}
	
	public void updateItemBox(UUID id, Vector3 pos, int isActive, int isGrowing, long growthTimer) {
		itemBoxes.get(id).setPos(pos);
		itemBoxes.get(id).setIsActive(isActive);
		itemBoxes.get(id).setIsGrowing(isGrowing);
		itemBoxes.get(id).setGrowthTimer(growthTimer);
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
