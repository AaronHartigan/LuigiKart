package myGameEngine;

import a3.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;

public class StartAccelerationAction extends AbstractInputAction {
	private MyGame g;
	
	public StartAccelerationAction(MyGame g) { 
		this.g = g;
	}
	@Override
	public void performAction(float time, Event evt) {
		if (!g.isRacingInputDisabled()) {
			g.setAccelerating(true);
		}
	}

}
