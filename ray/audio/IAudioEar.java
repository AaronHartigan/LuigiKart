package ray.audio;

import ray.rml.*;

/**
 * The <code>IAudioEar</code> interface exposes the public methods that all audio ears must implement.
 * An audio ear is an object which exists in 3D space and "hears" audio.
 * 
 * @author Kenneth Barnett
 * 
 * Modified from the SAGE Audio package for the RAGE game engine by Juan E. Ruiz.
 */
public interface IAudioEar 
{
	/**
	 * Returns the volume of the ear.  This is equivalent to returning the master (global) volume
	 * of the sound system. 
	 */
	public int getVolume();
	
	/**
	 * Sets the volume of the ear.  This is equivalent to setting the master (global) volume
	 * of the sound system. 
	 * <p>
	 * NOTE: This method is implementation dependent.
	 * 
	 * @param volume - The volume value between 0 and 100 inclusive.
	 */
	public void setVolume(int volume);
	
	/**
	 * Sets the location of the {@link IAudioEar} in 3D space. 
	 * <p>
	 * NOTE: This method is implementation dependent.
	 * 
	 * @param point - The point in 3D space.
	 */
	public void setLocation(Vector3 point);
	
	/**
	 * Sets the velocity of the {@link IAudioEar}.
	 * <p>
	 * NOTE: This method is implementation dependent.
	 * 
	 * @param velocity - The velocity vector.
	 */
	public void setVelocity(Vector3 velocity);
	
	/**
	 * Sets the orientation of the {@link IAudioEar}.
	 * <p>
	 * Think of "FORWARD" as a string attached to your nose, and think of "UP" as a string attached to the top of your head.
	 * Without the string attached to the top of your head, you could tilt your head clockwise/counterclockwise and still be 
	 * facing "FORWARD". But since you can tilt your head, there's no way for the computer to be sure whether something to the canonical 
	 * "right" should sound in your right ear (the top of your head faces "upwards") or your left ear (the top of your head faces 
	 * "downwards" because you're upside down). The "FORWARD" and "UP" vectors pin the listener's "head" such that there's no ambiguity 
	 * for which way it's facing, and which way it's oriented.
	 * <p>
	 * There are actually 3 vectors you need to set: Position, "FORWARD", and "UP". Position 0,0,0 means the head is at the center of the 
	 * universe. "FORWARD" 0,0,-1 means the head is looking into the screen, and "UP" is usually 0,1,0, such that the top of the "head" is 
	 * pointing up. With this setup, anything the user sees on the left side of the screen will sound in his left ear. The only time 
	 * you'd choose something different is in a first-person style game where the player moves around in a virtual 3d world. The vectors 
	 * don't have to be normalized, actually, so you could use 0,42,0 for "UP" and it would do the same thing as 0,1,0.
	 * <p>
	 * If you do change "FORWARD" and "UP" from their canonical values, the vectors MUST be perpendicular.
	 * <p>
	 * NOTE: This method is implementation dependent.
	 * 
	 * @param forward - The forward facing direction of the ear. 
	 * @param up - The up direction vector.
	 */
	public void setOrientation(Vector3 forward, Vector3 up);
	
	/**
	 * Returns the location in 3D space of the {@link IAudioEar}.
	 * <p>
	 * NOTE: This method is implementation dependent.
	 */
	public Vector3 getLocation();
	
	/**
	 * Returns the velocity vector of the {@link IAudioEar}.
	 * <p>
	 * NOTE: This method is implementation dependent.
	 */
	public Vector3 getVelocity();
}
