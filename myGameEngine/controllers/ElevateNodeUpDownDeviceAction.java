package myGameEngine.controllers;

import net.java.games.input.Event;
import ray.input.action.Action;

public class ElevateNodeUpDownDeviceAction implements Action {
	private float elevateAmount;
	private NodeOrbitController oc;
	private float JOYSTICK_DEADZONE = 0.4f;

	public ElevateNodeUpDownDeviceAction(NodeOrbitController oc, float elevateAmount) {
		this.oc = oc;
		this.elevateAmount = elevateAmount;
	}

	@Override
	public void performAction(float time, Event evt) {
		if (evt.getValue() < -JOYSTICK_DEADZONE) {
			oc.setElevation(oc.getElevation() + elevateAmount * time);
		}
		else if (evt.getValue() > JOYSTICK_DEADZONE) {
			oc.setElevation(oc.getElevation() - elevateAmount * time);
		}
	}

}