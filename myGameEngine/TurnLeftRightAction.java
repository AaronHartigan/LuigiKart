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
			if (e.getValue() < 0) {
				g.guiInputLeft();
			}
			else {
				g.guiInputRight();
			}
			if (!g.isRacingInputDisabled()) {
				g.getPhysicsBody().setDesiredTurn(-e.getValue());
			}
		}
	}
}
