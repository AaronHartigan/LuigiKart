package myGameEngine.controllers;

import net.java.games.input.Event;
import ray.input.action.Action;

public class OrbitNodeLeftAction implements Action {
	private float orbitAmount;
	private NodeOrbitController oc;

	public OrbitNodeLeftAction(NodeOrbitController oc, float orbitAmount) {
		this.oc = oc;
		this.orbitAmount = orbitAmount;
	}

	@Override
	public void performAction(float time, Event evt) {
		oc.setAzimuth(oc.getAzimuth() - orbitAmount * time);
	}
}