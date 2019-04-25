package a3;

import java.util.UUID;

import ray.rml.Vector3;
import ray.rml.Vector3f;

public class Track1 {
	private int ID = 1;
	private Vector3[] itemBoxes = new Vector3[] {
		Vector3f.createFrom(9.78f, 1f, -14.77f),
		Vector3f.createFrom(8.38f, 1f, -13.19f),
		Vector3f.createFrom(7.02f, 1f, -11.65f),
		Vector3f.createFrom(5.47f, 1f, -9.89f),
		Vector3f.createFrom(4.05f, 1f, -8.29f),
		
		Vector3f.createFrom(37f, 1f, 98.38f),
		Vector3f.createFrom(37f, 1f, 100.54f),
		Vector3f.createFrom(37f, 1f, 102.69f),
		Vector3f.createFrom(37f, 1f, 104.85f),
		Vector3f.createFrom(37f, 1f, 107f),
		
		Vector3f.createFrom(-12.13f, 1f, -92.69f),
		Vector3f.createFrom(-10.28f, 1f, -92.69f),
		Vector3f.createFrom(-8.44f, 1f, -92.69f),
		Vector3f.createFrom(-6.59f, 1f, -92.69f),
		Vector3f.createFrom(-4.74f, 1f, -92.69f),
	};
	
	private static Vector3[] startingPositions = new Vector3[] {
		Vector3f.createFrom(-38.59f, 1f, -86.84f),
		Vector3f.createFrom(-41.17f, 1f, -87.63f),
		Vector3f.createFrom(-43.64f, 1f, -88.40f),
		Vector3f.createFrom(-46.07f, 1f, -89.22f),
		Vector3f.createFrom(-39.92f, 1f, -90.14f),
		Vector3f.createFrom(-42.24f, 1f, -90.96f),
		Vector3f.createFrom(-44.60f, 1f, -91.69f),
		Vector3f.createFrom(-47.04f, 1f, -92.50f),
	};

	public Vector3[] getItemBoxes() {
		return itemBoxes;
	}
	
	public static Vector3 getPosition(int position) {
		return startingPositions[position - 1];
	}
	
	public void initTrack(GameState gs) {
		for (Vector3 itemBox : itemBoxes) {
			// I think this is not creating a copy
			// Which will keep this track in memory.
			// Should probably change this later if we get memory leaks
			gs.createItemBox(UUID.randomUUID(), itemBox);
		}
	}
}
