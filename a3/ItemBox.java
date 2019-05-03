package a3;

import java.util.UUID;

import ray.rml.Vector3;

public class ItemBox {
	private UUID id = null;
	private Vector3 pos = null;
	private int isActive = 1;
	private long RESPAWN_TIME_MS = 2000;
	private long respawnTimer = 0;
	private int isGrowing = 0;
	private long GROWTH_TIME_MS = 1000;
	private long regrowthTimer = 0;
	
	public ItemBox(UUID id, Vector3 pos) {
		this.setId(id);
		this.setPos(pos);
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getId() {
		return id;
	}
	
	public Vector3 getPos() {
		return pos;
	}

	public void setPos(Vector3 pos) {
		this.pos = pos;
	}

	public int getIsActive() {
		return isActive;
	}
	
	public int isGrowing() {
		return isGrowing;
	}
	
	public float scaleFactor() {
		if (isGrowing() == 0) {
			return 1f;
		}
		return Math.max(0.01f, (float) regrowthTimer / GROWTH_TIME_MS);
	}

	public void setIsActive(int isActive) {
		this.isActive = isActive;
	}
	
	public void updateTimers(float elapsedMS) {
		if (getIsActive() == 0) {
			respawnTimer += elapsedMS;
			isGrowing = 0;
			if (respawnTimer >= RESPAWN_TIME_MS) {
				setIsActive(1);
				isGrowing = 1;
				respawnTimer = 0;
				regrowthTimer = 0;
			}
		}
		if (isGrowing() == 1) {
			regrowthTimer += elapsedMS;
			if (regrowthTimer > GROWTH_TIME_MS) {
				isGrowing = 0;
				regrowthTimer = 0;
			}
		}
	}

	public void setIsGrowing(int isGrowing) {
		this.isGrowing = isGrowing;
	}
	
	public long getRegrowthTimer() {
		return regrowthTimer;
	}

	public void setGrowthTimer(long growthTimer) {
		// System.out.println(growthTimer);
		this.regrowthTimer = growthTimer;
	}
}
