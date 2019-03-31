package myGameEngine.controllers;

import net.java.games.input.Event;
import ray.input.action.Action;

public class ElevateNodeDownAction implements Action {
	private float elevateAmount;
	private NodeOrbitController oc;

	public ElevateNodeDownAction(NodeOrbitController oc, float elevateAmount) {
		this.oc = oc;
		this.elevateAmount = elevateAmount;
	}

	@Override
	public void performAction(float time, Event evt) {
		oc.setElevation(oc.getElevation() - elevateAmount * time);
	}
}
