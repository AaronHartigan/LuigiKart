package a3;

import java.util.UUID;

import ray.rml.Matrix3;
import ray.rml.Matrix3f;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class Item {
	private UUID ID;
	private ItemType type;
	private Vector3 pos = Vector3f.createFrom(0f, 0f, 0f);
	private Matrix3 rot = Matrix3f.createIdentityMatrix();
	
	public Item(ItemType type) {
		this.type = type;
		setID(UUID.randomUUID());
	}
	
	public Item(UUID itemID, ItemType type) {
		this.type = type;
		this.ID = itemID;
	}

	public UUID getID() {
		return ID;
	}
	public void setID(UUID iD) {
		ID = iD;
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
	public ItemType getType() {
		return type;
	}
	public void setType(ItemType type) {
		this.type = type;
	}
}
