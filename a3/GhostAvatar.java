package a3;

import java.util.UUID;

import ray.rml.Matrix3;
import ray.rml.Matrix3f;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class GhostAvatar {
	private UUID id;
	private Item item = null;
	private Vector3 pos = Vector3f.createFrom(0f, 0f, 0f);
	private Matrix3 rot = Matrix3f.createIdentityMatrix();

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
	
	public void setItem(Item item) {
		this.item = item;
	}
}
