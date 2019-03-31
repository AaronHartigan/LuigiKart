package myGameEngine;

import a3.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rml.*;

public class MoveCameraForwardAction extends AbstractInputAction
{
	private Camera camera;
	private MyGame g;

	public MoveCameraForwardAction(Camera c, MyGame g) { 
		camera = c;
		this.g = g;
	}
	
	public void performAction(float time, Event e) {
		Vector3f v = camera.getFd();
		Vector3f p = camera.getPo();
		float toMove = time * this.g.getMoveSpeed();
		Vector3f p1 = (Vector3f) Vector3f.createFrom(toMove * v.x(), toMove * v.y(), toMove * v.z());
		Vector3f p2 = (Vector3f) p.add((Vector3)p1);
		camera.setPo((Vector3f)Vector3f.createFrom(p2.x(), p2.y(), p2.z()));
	}
}