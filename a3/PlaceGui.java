package a3;

import java.io.IOException;

import ray.rage.rendersystem.shader.GpuShaderProgram;
import ray.rage.rendersystem.states.RenderState;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.scene.Entity;
import ray.rage.scene.SceneNode;

public class PlaceGui {
	private MyGame g;
	private Entity place;
	private SceneNode placeN;
	private TextureState placeT;
	private long updateTimer = 0;
	private float UPDATE_TIME = 75f;
	
	public PlaceGui(MyGame g) {
		this.g = g;
		try {
			place = g.getEngine().getSceneManager().createEntity("first", "plane.obj");

			place.setGpuShaderProgram(g.getEngine().getSceneManager().getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.GUI));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		placeT = (TextureState) g.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		placeT.setTexture(g.getTextures().getPlace(1));
		place.setRenderState(placeT);
		
		placeN = g.getEngine().getSceneManager().getRootSceneNode().createChildSceneNode("placeNode");
		placeN.attachObject(place);
		placeN.scale(0.12f, 0.12f, 1f);
		placeN.translate(-0.8f, 0.8f, 0f);
	}
	
	public void update(int place, float time) {
		updateTimer += time;
		if (updateTimer >= UPDATE_TIME) {
			if (place >= 0 && place <= 7) {
				placeT.setTexture(g.getTextures().getPlace(place));
			}
			updateTimer = 0;
		}
	}

	public void hide() {
		placeN.translate(-100000f, -100000f, 0f);
	}
	
	public void show() {
		placeN.translate(100000f, 100000f, 0f);
	}
}
