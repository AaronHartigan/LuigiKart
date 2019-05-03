package a3;

public class ClientState {
	private int selectedTrack = 1; // Might use later if client can select from tracks
	private int joinedTrack = 0;
	private boolean isConnected;
	private String connectionError;
	private int selectedAvatar;
	private boolean raceFinished = false;

	public int getSelectedTrack() {
		return selectedTrack;
	}
	public void setSelectedTrack(int selectedTrack) {
		this.selectedTrack = selectedTrack;
	}
	public boolean isConnected() {
		return isConnected;
	}
	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}
	public int getSelectedAvatar() {
		return selectedAvatar;
	}
	public void setSelectedAvatar(int selectedAvatar) {
		this.selectedAvatar = selectedAvatar;
	}
	public String getConnectionError() {
		return connectionError;
	}
	public void setConnectionError(String connectionError) {
		this.connectionError = connectionError;
	}
	public int getJoinedTrack() {
		return joinedTrack;
	}
	public void setJoinedTrack(int joinedTrack) {
		this.joinedTrack = joinedTrack;
	}
	
	public boolean hasTrack() {
		return joinedTrack != 0;
	}
	
	public boolean isRaceFinished() {
		return raceFinished;
	}

	public void setRaceFinished(boolean raceFinished) {
		this.raceFinished = raceFinished;
	}
}
