package myGameEngine;

import a3.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;

public class MoveNodeForwardAction extends AbstractInputAction
{
	private SceneNode n;
	private MyGame g;

	public MoveNodeForwardAction(SceneNode n, MyGame g) { 
		this.n = n;
		this.g = g;
	}
	
	public void performAction(float time, Event e) {
		n.moveForward(time * this.g.getMoveSpeed());
	}
}