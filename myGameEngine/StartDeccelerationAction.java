package myGameEngine;

import a3.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;

public class StartDeccelerationAction extends AbstractInputAction {
	private MyGame g;
	
	public StartDeccelerationAction(MyGame g) { 
		this.g = g;
	}
	@Override
	public void performAction(float time, Event evt) {
		if (!g.isRacingInputDisabled()) {
			g.getPhysicsBody().setDeccelerating(true);
		}
	}

}
