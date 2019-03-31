package myGameEngine;

import a3.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rml.*;

public class RotateCameraLeftAction extends AbstractInputAction
{
	private Camera camera;
	private MyGame g;

	public RotateCameraLeftAction(Camera c, MyGame g) { 
		camera = c;
		this.g = g;
	}
	
	public void performAction(float time, Event e) {
		Vector3f cFd = camera.getFd();
		Vector3f cRt = camera.getRt();
		Vector3f cUp = camera.getUp();
		
		float toRotate = time * g.getRotateSpeed();

		Vector3 newFd = cFd.rotate(Degreef.createFrom(toRotate), cUp).normalize();
		Vector3 newRt = cRt.rotate(Degreef.createFrom(toRotate), cUp).normalize();
		camera.setFd((Vector3f) newFd);
		camera.setRt((Vector3f) newRt);
	}
}