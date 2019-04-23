package myGameEngine;

import a3.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;

public class TurnRightAction extends AbstractInputAction {
	private MyGame g;
	
	public TurnRightAction(MyGame g) { 
		this.g = g;
	}

	@Override
	public void performAction(float time, Event evt) {
		if (!g.isRacingInputDisabled()) {
			g.setDesiredTurn(-1f);
		}
	}
}
