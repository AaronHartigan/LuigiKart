package myGameEngine;

import a3.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;

public class StartDriftingAction extends AbstractInputAction {
	private MyGame g;
	
	public StartDriftingAction(MyGame g) { 
		this.g = g;
	}

	@Override
	public void performAction(float time, Event evt) {
		if (!g.isRacingInputDisabled()) {
			g.setDrifting(true);
		}
	}
}
