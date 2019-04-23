package myGameEngine;

import a3.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;

public class TurnLeftRightAction extends AbstractInputAction {
	private MyGame g;
	private float JOYSTICK_DEADZONE = 0.3f;
	
	public TurnLeftRightAction(MyGame g) { 
		this.g = g;
	}

	@Override
	public void performAction(float time, Event e) {
		if (Math.abs(e.getValue()) > JOYSTICK_DEADZONE) {
			if (!g.isRacingInputDisabled()) {
				g.setDesiredTurn(-e.getValue());
			}
		}
	}
}
