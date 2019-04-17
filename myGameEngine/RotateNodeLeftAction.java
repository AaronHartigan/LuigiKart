package myGameEngine;

import a3.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rml.Degreef;

public class RotateNodeLeftAction extends AbstractInputAction
{
	private SceneNode n;
	private MyGame g;
	
	public RotateNodeLeftAction(SceneNode n, MyGame g) { 
		this.n = n;
		this.g = g;
	}
	
	public void performAction(float time, Event e) {
		n.yaw(Degreef.createFrom(time * g.getRotateSpeed()));
	}
}