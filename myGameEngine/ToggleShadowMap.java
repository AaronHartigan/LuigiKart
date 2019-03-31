package myGameEngine;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.rendersystem.gl4.GL4RenderSystem;

public class ToggleShadowMap extends AbstractInputAction {
	private GL4RenderSystem rs;
	
	public ToggleShadowMap(GL4RenderSystem rs) { 
		this.rs = rs;
	}
	@Override
	public void performAction(float time, Event evt) {
		rs.toggleShadowMap();
	}

}
