package myGameEngine;

import a3.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rml.*;

public class RotateCameraDownAction extends AbstractInputAction
{
	private Camera camera;
	private MyGame g;

	public RotateCameraDownAction(Camera c, MyGame g) { 
		camera = c;
		this.g = g;
	}
	
	public void performAction(float time, Event e) {
		Vector3f cFd = camera.getFd();
		Vector3f cRt = camera.getRt();
		Vector3f cUp = camera.getUp();
		
		float toRotate = -time * g.getRotateSpeed();

		Vector3 newFd = cFd.rotate(Degreef.createFrom(toRotate), cRt).normalize();
		Vector3 newUp = cUp.rotate(Degreef.createFrom(toRotate), cRt).normalize();
		camera.setFd((Vector3f) newFd);
		camera.setUp((Vector3f) newUp);
	}
}