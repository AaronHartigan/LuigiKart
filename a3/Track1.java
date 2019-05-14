package a3;

import java.util.UUID;

import ray.rml.Vector2;
import ray.rml.Vector2f;
import ray.rml.Vector3;
import ray.rml.Vector3f;
import ray.rml.Vector4;
import ray.rml.Vector4f;

public class Track1 {
	private int ID = 1;
	public static int NUM_WAYPOINTS = 24;
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
		Vector3f.createFrom(-38.59f, 1f, -87.54f),
		Vector3f.createFrom(-41.17f, 1f, -88.33f),
		Vector3f.createFrom(-43.64f, 1f, -89.10f),
		Vector3f.createFrom(-46.07f, 1f, -89.92f),
		Vector3f.createFrom(-39.92f, 1f, -90.84f),
		Vector3f.createFrom(-42.24f, 1f, -91.66f),
		Vector3f.createFrom(-44.60f, 1f, -92.39f),
		Vector3f.createFrom(-47.04f, 1f, -93.20f),
	};
	
	private static Vector3[] waypoints = new Vector3[] {
		Vector3f.createFrom(-36.376953125f, 1f, -67.3828125f),
		Vector3f.createFrom(-25.87890625f, 1f, -49.8046875f),
		Vector3f.createFrom(-17.822265625f, 1f, -33.935546875f),
		Vector3f.createFrom(-2.685546875f, 1f, -19.53125f),
		Vector3f.createFrom(27.099609375f, 1f, 5.126953125f),
		Vector3f.createFrom(42.48046875f, 1f, 29.78515625f),
		Vector3f.createFrom(41.259765625f, 1f, 40.771484375f),
		Vector3f.createFrom(30.2734375f, 1f, 55.419921875f),
		Vector3f.createFrom(12.6953125f, 1f, 68.84765625f),
		Vector3f.createFrom(9.765625f, 1f, 85.205078125f),
		Vector3f.createFrom(37.59765625f, 1f, 101.318359375f),
		Vector3f.createFrom(61.5234375f, 1f, 91.064453125f),
		Vector3f.createFrom(52.490234375f, 1f, 42.48046875f),
		Vector3f.createFrom(40.0390625f, 1f, 19.287109375f),
		Vector3f.createFrom(-2.197265625f, 1f, -20.751953125f),
		Vector3f.createFrom(-18.310546875f, 1f, -40.283203125f),
		Vector3f.createFrom(-24.658203125f, 1f, -62.5f),
		Vector3f.createFrom(-19.04296875f, 1f, -75.439453125f),
		Vector3f.createFrom(-10.25390625f, 1f, -87.158203125f),
		Vector3f.createFrom(-14.6484375f, 1f, -100.830078125f),
		Vector3f.createFrom(-22.216796875f, 1f, -102.294921875f),
		Vector3f.createFrom(-33.447265625f, 1f, -99.609375f),
		Vector3f.createFrom(-43.9453125f, 1f, -89.35546875f),
		Vector3f.createFrom(-41.259765625f, 1f, -79.58984375f),
	};
	
	private static Vector2[] pointInWaypoint = new Vector2[] {
		Vector2f.createFrom(-36.376953125f, -66.162109375f),
		Vector2f.createFrom(-25.87890625f, -48.583984375f),
		Vector2f.createFrom(-17.822265625f, -32.71484375f),
		Vector2f.createFrom(-2.685546875f, -18.310546875f),
		Vector2f.createFrom(27.099609375f, 6.34765625f),
		Vector2f.createFrom(42.48046875f, 31.005859375f),
		Vector2f.createFrom(41.259765625f, 41.9921875f),
		Vector2f.createFrom(30.2734375f, 56.640625f),
		Vector2f.createFrom(12.6953125f, 70.068359375f),
		Vector2f.createFrom(10.986328125f, 85.205078125f),
		Vector2f.createFrom(38.818359375f, 101.318359375f),
		Vector2f.createFrom(61.5234375f, 89.84375f),
		Vector2f.createFrom(52.490234375f, 41.259765625f),
		Vector2f.createFrom(40.0390625f, 18.06640625f),
		Vector2f.createFrom(-2.197265625f, -21.97265625f),
		Vector2f.createFrom(-18.310546875f, -41.50390625f),
		Vector2f.createFrom(-24.658203125f, -63.720703125f),
		Vector2f.createFrom(-19.04296875f, -76.66015625f),
		Vector2f.createFrom(-10.25390625f, -88.37890625f),
		Vector2f.createFrom(-14.6484375f, -102.05078125f),
		Vector2f.createFrom(-23.4375f, -102.294921875f),
		Vector2f.createFrom(-34.66796875f, -99.609375f),
		Vector2f.createFrom(-45.166015625f, -89.35546875f),
		Vector2f.createFrom(-41.259765625f, -78.369140625f),
	};
	
	private static Vector4[] waypointsLines = new Vector4[] {
		Vector4f.createFrom(-36.376953125f, -67.3828125f, -36.29345211344588f, -67.61222964374656f),
		Vector4f.createFrom(-25.87890625f, -49.8046875f, -25.7568359375f, -50.01611948334582f),
		Vector4f.createFrom(-17.822265625f, -33.935546875f, -17.7001953125f, -34.14697885834582f),
		Vector4f.createFrom(-2.685546875f, -19.53125f, -2.528616306228855f, -19.718272569120842f),
		Vector4f.createFrom(27.099609375f, 5.126953125f, 27.142003949625703f, 4.88652154467475f),
		Vector4f.createFrom(42.48046875f, 29.78515625f, 42.48046875f, 29.541015625f),
		Vector4f.createFrom(41.259765625f, 40.771484375f, 41.1376953125f, 40.560052391654196f),
		Vector4f.createFrom(30.2734375f, 55.419921875f, 30.086414930879158f, 55.262991306228855f),
		Vector4f.createFrom(12.6953125f, 68.84765625f, 12.652917925374297f, 68.60722466967476f),
		Vector4f.createFrom(9.765625f, 85.205078125f, 9.977056983345804f, 85.0830078125f),
		Vector4f.createFrom(37.59765625f, 101.318359375f, 37.809088233345804f, 101.4404296875f),
		Vector4f.createFrom(61.5234375f, 91.064453125f, 61.606938511554105f, 91.29387026874656f),
		Vector4f.createFrom(52.490234375f, 42.48046875f, 52.3681640625f, 42.691900733345804f),
		Vector4f.createFrom(40.0390625f, 19.287109375f, 39.9169921875f, 19.498541358345804f),
		Vector4f.createFrom(-2.197265625f, -20.751953125f, -2.2807666365541337f, -20.522535981253412f),
		Vector4f.createFrom(-18.310546875f, -40.283203125f, -18.352941449625717f, -40.042771544674764f),
		Vector4f.createFrom(-24.658203125f, -62.5f, -24.658203125f, -62.255859375f),
		Vector4f.createFrom(-19.04296875f, -75.439453125f, -18.95946773844588f, -75.21003598125344f),
		Vector4f.createFrom(-10.25390625f, -87.158203125f, -10.096975681228855f, -86.97118055587916f),
		Vector4f.createFrom(-14.6484375f, -100.830078125f, -14.606042925374283f, -100.58964654467476f),
		Vector4f.createFrom(-22.216796875f, -102.294921875f, -22.42822885834582f, -102.1728515625f),
		Vector4f.createFrom(-33.447265625f, -99.609375f, -33.69140625f, -99.609375f),
		Vector4f.createFrom(-43.9453125f, -89.35546875f, -44.13233506912084f, -89.51239931877114f),
		Vector4f.createFrom(-41.259765625f, -79.58984375f, -41.30216019962572f, -79.83027533032524f),
	};

	public Vector3[] getItemBoxes() {
		return itemBoxes;
	}
	
	public static Vector3 getPosition(int position) {
		return startingPositions[position - 1];
	}
	
	public static Vector3 getWaypoint(int i) {
		return waypoints[i];
	}
	
	public static Vector2 getPointInWaypoint(int i) {
		return pointInWaypoint[i];
	}
	
	public static Vector4 getWaypointLine(int i) {
		return waypointsLines[i];
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
