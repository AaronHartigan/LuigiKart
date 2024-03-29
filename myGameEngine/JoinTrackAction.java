package myGameEngine;

import a3.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;

public class JoinTrackAction extends AbstractInputAction {
	private MyGame g;
	
	public JoinTrackAction(MyGame g) { 
		this.g = g;
	}
	@Override
	public void performAction(float time, Event evt) {
		g.inputAction();
	}
}
