package myGameEngine.Networking;

import java.util.UUID;

public class PlayerState {
	private UUID id;
	private long heartbeat;
	private int trackID = 0;
	
	public PlayerState(UUID id, long heartbeat) {
		this(id, 0, heartbeat);
	}
	
	public PlayerState(UUID id, int trackID, long heartbeat) {
		this.id = id;
		this.trackID = trackID;
		this.heartbeat = heartbeat;
	}
	
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public long getHeartbeat() {
		return heartbeat;
	}
	public void setHeartbeat(long heartbeat) {
		this.heartbeat = heartbeat;
	}
	
	public boolean hasTrack() {
		return trackID != 0;
	}
}
