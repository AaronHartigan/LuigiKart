package myGameEngine;

import a3.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;

public class MoveNodeBackwardAction extends AbstractInputAction
{
	private SceneNode n;
	private MyGame g;
	
	public MoveNodeBackwardAction(SceneNode n, MyGame g) { 
		this.n = n;
		this.g = g;
	}
	
	public void performAction(float time, Event e) {
		n.moveBackward(time * this.g.getMoveSpeed());
	}
}