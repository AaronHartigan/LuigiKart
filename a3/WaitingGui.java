package a3;

import java.io.IOException;

import ray.rage.rendersystem.shader.GpuShaderProgram;
import ray.rage.rendersystem.states.RenderState;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.scene.Entity;
import ray.rage.scene.SceneNode;

public class WaitingGui {
	private Entity startGame;
	private TextureState startGameT;
	private SceneNode startGameN;

	public WaitingGui(MyGame g) {
		try {
			startGame = g.getEngine().getSceneManager().createEntity("startGane", "plane.obj");
			
			startGame.setGpuShaderProgram(g.getEngine().getSceneManager().getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.GUI));

			startGameT = (TextureState) g.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);

			startGameT.setTexture(g.getEngine().getSceneManager().getTextureManager().getAssetByPath("start_game.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		startGame.setRenderState(startGameT);

		startGameN = g.getEngine().getSceneManager().getRootSceneNode().createChildSceneNode("startGame");
		startGameN.attachObject(startGame);
		startGameN.scale(0.1f * 3.89f, 0.1f, 1f);
		startGameN.translate(0f, 0.4f, 0f);
	}
	
	public void hide() {
		startGameN.translate(100000f, 100000f, 0f);
	}
	
	public void show() {
		startGameN.translate(-100000f, -100000f, 0f);
	}
}
