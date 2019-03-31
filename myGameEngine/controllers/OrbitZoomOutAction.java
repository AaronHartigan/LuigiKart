package myGameEngine.controllers;

import net.java.games.input.Event;
import ray.input.action.Action;

public class OrbitZoomOutAction implements Action {
	private float zoomAmount;
	private NodeOrbitController oc;

	public OrbitZoomOutAction(NodeOrbitController oc, float zoomAmount) {
		this.oc = oc;
		this.zoomAmount = zoomAmount;
	}

	@Override
	public void performAction(float time, Event evt) {
		oc.setOrbitDistance(oc.getOrbitDistance() + zoomAmount * time);
	}
}