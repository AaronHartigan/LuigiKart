package a3;

import java.io.IOException;

import ray.rage.rendersystem.shader.GpuShaderProgram;
import ray.rage.rendersystem.states.RenderState;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.scene.Entity;
import ray.rage.scene.SceneNode;

public class LobbyGui {
	private Entity leftChevron;
	private Entity rightChevron;
	private Entity select;
	private TextureState leftChevronT, rightChevronT, selectT;
	private SceneNode leftChevronN, rightChevronN, selectN;
	private final float CHEVRON_SCALE = 0.06f;
	private final float SELECT_SCALE = 0.25f;
	private final float HIDE_TRANSLATE = 1000000f;

	public LobbyGui(MyGame g) {
		try {
			leftChevron = g.getEngine().getSceneManager().createEntity("leftChevron", "plane.obj");
			rightChevron = g.getEngine().getSceneManager().createEntity("rightChevron", "plane.obj");
			select = g.getEngine().getSceneManager().createEntity("select", "plane.obj");
			
			leftChevron.setGpuShaderProgram(g.getEngine().getSceneManager().getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.GUI));
			rightChevron.setGpuShaderProgram(g.getEngine().getSceneManager().getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.GUI));
			select.setGpuShaderProgram(g.getEngine().getSceneManager().getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.GUI));
		
			leftChevronT = (TextureState) g.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
			rightChevronT = (TextureState) g.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
			selectT = (TextureState) g.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		
			leftChevronT.setTexture(g.getEngine().getSceneManager().getTextureManager().getAssetByPath("chevron_left.png"));
			rightChevronT.setTexture(g.getEngine().getSceneManager().getTextureManager().getAssetByPath("chevron_right.png"));
			selectT.setTexture(g.getEngine().getSceneManager().getTextureManager().getAssetByPath("select.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		leftChevron.setRenderState(leftChevronT);
		rightChevron.setRenderState(rightChevronT);
		select.setRenderState(selectT);
		
		leftChevronN = g.getEngine().getSceneManager().getRootSceneNode().createChildSceneNode("leftChevron");
		leftChevronN.attachObject(leftChevron);
		leftChevronN.scale(CHEVRON_SCALE, CHEVRON_SCALE * 1.77f, 1f);
		leftChevronN.translate(-0.6f, 0.3f, 0f);
		
		rightChevronN = g.getEngine().getSceneManager().getRootSceneNode().createChildSceneNode("rightChevron");
		rightChevronN.attachObject(rightChevron);
		rightChevronN.scale(CHEVRON_SCALE, CHEVRON_SCALE * 1.77f, 1f);
		rightChevronN.translate(0.6f, 0.3f, 0f);
		
		selectN = g.getEngine().getSceneManager().getRootSceneNode().createChildSceneNode("select");
		selectN.attachObject(select);
		selectN.scale(SELECT_SCALE, SELECT_SCALE * 0.44f, 1f);
		selectN.translate(0f, -0.1f, 0f);
	}
	
	public void hide() {
		leftChevronN.translate(HIDE_TRANSLATE, HIDE_TRANSLATE, 0f);
		rightChevronN.translate(HIDE_TRANSLATE, HIDE_TRANSLATE, 0f);
		selectN.translate(HIDE_TRANSLATE, HIDE_TRANSLATE, 0f);
	}
	
	public void show() {
		leftChevronN.translate(-HIDE_TRANSLATE, -HIDE_TRANSLATE, 0f);
		rightChevronN.translate(-HIDE_TRANSLATE, -HIDE_TRANSLATE, 0f);
		selectN.translate(-HIDE_TRANSLATE, -HIDE_TRANSLATE, 0f);
	}
}
