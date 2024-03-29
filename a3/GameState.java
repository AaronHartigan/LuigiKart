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
	private Map<UUID, Item> items =  Collections.synchronizedMap(new HashMap<UUID, Item>());
	private long elapsedRaceTime;
	private RaceState raceState = RaceState.LOBBY;

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

	public Map<UUID, Item> getItems() {
		return items;
	}

	public void setItems(HashMap<UUID, Item> items) {
		this.items = items;
	}
	
	public void createGhostAvatar(UUID ghostID, Vector3 ghostPosition) {
		ghostAvatars.put(ghostID, new GhostAvatar(ghostID));
	}
	
	public void updateGhostAvatar(UUID ghostID, Vector3 ghostPosition, Matrix3 ghostRotation, float vForward, float actualTurn, long time) {
		if (!(ghostAvatars.containsKey(ghostID))) {
			return;
		}
		ghostAvatars.get(ghostID).setPos(ghostPosition);
		ghostAvatars.get(ghostID).setRot(ghostRotation);
		ghostAvatars.get(ghostID).setVelocityForward(vForward);
		ghostAvatars.get(ghostID).setActualTurn(actualTurn);
		ghostAvatars.get(ghostID).setLastUpdateTime(time);
	}
	
	public void removeGhostAvatar(UUID ghostID) {
		ghostAvatars.remove(ghostID);
	}

	public void updateItem(UUID itemID, Vector3 itemPos, Matrix3 itemRot) {
		if (items.get(itemID) != null) {
			items.get(itemID).setPos(itemPos);
			items.get(itemID).setRot(itemRot);
		}
	}

	public long getElapsedRaceTime() {
		return elapsedRaceTime;
	}

	public void setElapsedRaceTime(long elapsedRaceTime) {
		this.elapsedRaceTime = elapsedRaceTime;
	}

	public void shouldRemoveGhostAvatar(UUID ghostID) {
		ghostAvatars.get(ghostID).setShouldRemove(true);
	}

	public RaceState getRaceState() {
		return raceState;
	}

	public void setRaceState(RaceState raceState) {
		this.raceState = raceState;
	}
}
