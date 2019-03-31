package myGameEngine;

import a3.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rml.Degreef;

public class RotateNodeDownAction extends AbstractInputAction
{
	private SceneNode n;
	private MyGame g;
	
	public RotateNodeDownAction(SceneNode n, MyGame g) { 
		this.n = n;
		this.g = g;
	}
	
	public void performAction(float time, Event e) {
		n.pitch(Degreef.createFrom(time * g.getRotateSpeed()));
	}
}