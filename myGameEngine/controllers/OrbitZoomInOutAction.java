package myGameEngine.controllers;

import net.java.games.input.Event;
import ray.input.action.Action;
import ray.rage.rendersystem.gl4.GL4RenderSystem;

public class OrbitZoomInOutAction implements Action {
	private float zoomAmount;
	private NodeOrbitController oc;
	private GL4RenderSystem rs;
	private float JOYSTICK_DEADZONE = 0.2f;

	public OrbitZoomInOutAction(NodeOrbitController oc, float zoomAmount, GL4RenderSystem rs) {
		this.oc = oc;
		this.rs = rs;
		this.zoomAmount = zoomAmount;
	}

	@Override
	public void performAction(float time, Event evt) {
		if (evt.getValue() < -JOYSTICK_DEADZONE) {
			oc.setOrbitDistance(oc.getOrbitDistance() + zoomAmount * time);
			rs.setBoxSize(rs.getBoxSize() + zoomAmount * time);
		}
		else if (evt.getValue() > JOYSTICK_DEADZONE) {
			oc.setOrbitDistance(oc.getOrbitDistance() - zoomAmount * time);
			rs.setBoxSize(rs.getBoxSize() - zoomAmount * time);
		}
	}
}