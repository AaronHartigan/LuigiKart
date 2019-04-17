package myGameEngine;

import a3.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;

public class TurnLeftAction extends AbstractInputAction {
	private MyGame g;
	
	public TurnLeftAction(MyGame g) { 
		this.g = g;
	}

	@Override
	public void performAction(float time, Event evt) {
		g.setDesiredTurn(1f);
	}
}
