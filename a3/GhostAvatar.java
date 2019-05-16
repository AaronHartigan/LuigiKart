package a3;

import java.util.UUID;

import ray.audio.Sound;
import ray.rml.Matrix3;
import ray.rml.Matrix3f;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class GhostAvatar {
	private UUID id;
	private Item item = null;
	private float velocityForward = 0f;
	private Vector3 pos = Vector3f.createFrom(0f, 0f, 0f);
	private Matrix3 rot = Matrix3f.createIdentityMatrix();
	private float actualTurn;
	private boolean shouldRemove = false;
	private boolean isNPC = false;
	private PhysicsBody physicsBody = null;
	private long lastUpdateTime = 0;
	private int waypoint = -1;
	private int color;
	private Sound sound;

	public GhostAvatar(UUID id) {
		this.setId(id);
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Vector3 getPos() {
		return pos;
	}

	public void setPos(Vector3 pos) {
		this.pos = pos;
	}

	public Matrix3 getRot() {
		return rot;
	}

	public void setRot(Matrix3 rot) {
		this.rot = rot;
	}
	
	public boolean hasItem() {
		return (item != null);
	}
	
	public void removeItem() {
		item = null;
	}

	public Item getItem() {
		return item;
	}
	
	public void setItem(Item item) {
		this.item = item;
	}

	public float getVelocityForward() {
		return velocityForward;
	}

	public void setVelocityForward(float velocityForward) {
		this.velocityForward = velocityForward;
	}

	public boolean isShouldRemove() {
		return shouldRemove;
	}

	public void setShouldRemove(boolean shouldRemove) {
		this.shouldRemove = shouldRemove;
	}

	public boolean isNPC() {
		return isNPC;
	}

	public void setNPC(boolean isNPC) {
		this.isNPC = isNPC;
	}

	public PhysicsBody getPhysicsBody() {
		return physicsBody;
	}

	public void setPhysicsBody(PhysicsBody physicsBody) {
		this.physicsBody = physicsBody;
	}

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public int getWaypoint() {
		return waypoint;
	}

	public void setWaypoint(int waypoint) {
		this.waypoint = waypoint;
	}

	public void setActualTurn(float actualTurn) {
		this.actualTurn = actualTurn;
	}
	
	public float getActualTurn() {
		return actualTurn;
	}

	public void setColor(int color) {
		this.color = color;
	}
	
	public int getColor() {
		return color;
	}

	public Sound getSound() {
		return sound;
	}

	public void setSound(Sound sound) {
		this.sound = sound;
	}
}
