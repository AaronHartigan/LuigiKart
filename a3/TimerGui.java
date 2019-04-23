package a3;

import java.io.IOException;

import ray.rage.rendersystem.shader.GpuShaderProgram;
import ray.rage.rendersystem.states.RenderState;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.scene.Entity;
import ray.rage.scene.SceneNode;

public class TimerGui {
	private MyGame g;
	private Entity minute1;
	private Entity second0;
	private Entity second1;
	private Entity ms0;
	private Entity ms1;
	private Entity ms2;
	private Entity colon0;
	private Entity colon1;
	private Entity countdown;
	private Entity background;
	private SceneNode countdownN;

	private TextureState tstate;
	private final float NUMBER_SCALE = 0.035f;
	private final float STARTING_X = 0.4f;
	private final float STARTING_Y = 0.8f;
	private final float SHIFT_AMOUNT = NUMBER_SCALE * 1.9f;
	private final float COLON_OFFSET = 0.045f;
	private final float BACKGROUND_SCALE = 0.3f;
	private final float COUNTDOWN_SCALE = 0.5f;
	
	public TimerGui(MyGame g) {
		this.g = g;
		try {
			background = g.getEngine().getSceneManager().createEntity("background", "plane.obj");
			minute1 = g.getEngine().getSceneManager().createEntity("minute1", "plane.obj");
			second0 = g.getEngine().getSceneManager().createEntity("second0", "plane.obj");
			second1 = g.getEngine().getSceneManager().createEntity("second1", "plane.obj");
			ms0 = g.getEngine().getSceneManager().createEntity("ms0", "plane.obj");
			ms1 = g.getEngine().getSceneManager().createEntity("ms1", "plane.obj");
			ms2 = g.getEngine().getSceneManager().createEntity("ms2", "plane.obj");
			colon0 = g.getEngine().getSceneManager().createEntity("colon0", "plane.obj");
			colon1 = g.getEngine().getSceneManager().createEntity("colon1", "plane.obj");
			countdown = g.getEngine().getSceneManager().createEntity("countdown", "plane.obj");
			
			background.setGpuShaderProgram(g.getEngine().getSceneManager().getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.GUI_BACKGROUND));
			minute1.setGpuShaderProgram(g.getEngine().getSceneManager().getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.GUI));
			second0.setGpuShaderProgram(g.getEngine().getSceneManager().getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.GUI));
			second1.setGpuShaderProgram(g.getEngine().getSceneManager().getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.GUI));
			ms0.setGpuShaderProgram(g.getEngine().getSceneManager().getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.GUI));
			ms1.setGpuShaderProgram(g.getEngine().getSceneManager().getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.GUI));
			ms2.setGpuShaderProgram(g.getEngine().getSceneManager().getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.GUI));
			colon0.setGpuShaderProgram(g.getEngine().getSceneManager().getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.GUI));
			colon1.setGpuShaderProgram(g.getEngine().getSceneManager().getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.GUI));
			countdown.setGpuShaderProgram(g.getEngine().getSceneManager().getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.GUI));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		tstate = (TextureState) g.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		tstate.setTexture(g.getTextures().getTexture(PreloadTextures.TEXTURE.BACKGROUND));
		background.setRenderState(tstate);
		
		tstate = (TextureState) g.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		tstate.setTexture(g.getTextures().getTexture(PreloadTextures.TEXTURE.N0));

		minute1.setRenderState(tstate);
		second0.setRenderState(tstate);
		second1.setRenderState(tstate);
		ms0.setRenderState(tstate);
		ms1.setRenderState(tstate);
		ms2.setRenderState(tstate);
		countdown.setRenderState(tstate);
		
		tstate = (TextureState) g.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		tstate.setTexture(g.getTextures().getTexture(PreloadTextures.TEXTURE.COLON));

		colon0.setRenderState(tstate);
		colon1.setRenderState(tstate);
		
		countdownN = g.getEngine().getSceneManager().getRootSceneNode().createChildSceneNode(countdown.getName());
		countdownN.scale(COUNTDOWN_SCALE, COUNTDOWN_SCALE, COUNTDOWN_SCALE);
		
		SceneNode backgroundN = g.getEngine().getSceneManager().getRootSceneNode().createChildSceneNode(background.getName());
		backgroundN.attachObject(background);
		backgroundN.scale(BACKGROUND_SCALE, 0.2f * BACKGROUND_SCALE, 1f);
		backgroundN.translate(STARTING_X + 0.3f, STARTING_Y, 0f);
		
		SceneNode minute1N = g.getEngine().getSceneManager().getRootSceneNode().createChildSceneNode(minute1.getName());
		minute1N.attachObject(minute1);
		minute1N.scale(NUMBER_SCALE, NUMBER_SCALE, NUMBER_SCALE);
		minute1N.translate(STARTING_X + SHIFT_AMOUNT * 1, STARTING_Y, 0f);
		
		SceneNode colon0N = g.getEngine().getSceneManager().getRootSceneNode().createChildSceneNode(colon0.getName());
		colon0N.attachObject(colon0);
		colon0N.scale(NUMBER_SCALE, NUMBER_SCALE, NUMBER_SCALE);
		colon0N.translate(STARTING_X + SHIFT_AMOUNT * 1 + COLON_OFFSET, STARTING_Y, 0f);
		
		SceneNode second0N = g.getEngine().getSceneManager().getRootSceneNode().createChildSceneNode(second0.getName());
		second0N.attachObject(second0);
		second0N.scale(NUMBER_SCALE, NUMBER_SCALE, NUMBER_SCALE);
		second0N.translate(STARTING_X + SHIFT_AMOUNT * 1 + COLON_OFFSET * 2, STARTING_Y, 0f);
		
		SceneNode second1N = g.getEngine().getSceneManager().getRootSceneNode().createChildSceneNode(second1.getName());
		second1N.attachObject(second1);
		second1N.scale(NUMBER_SCALE, NUMBER_SCALE, NUMBER_SCALE);
		second1N.translate(STARTING_X + SHIFT_AMOUNT * 2 + COLON_OFFSET * 2, STARTING_Y, 0f);
		
		SceneNode colon1N = g.getEngine().getSceneManager().getRootSceneNode().createChildSceneNode(colon1.getName());
		colon1N.attachObject(colon1);
		colon1N.scale(NUMBER_SCALE, NUMBER_SCALE, NUMBER_SCALE);
		colon1N.translate(STARTING_X + SHIFT_AMOUNT * 2 + COLON_OFFSET * 3, STARTING_Y, 0f);
		
		SceneNode ms0N = g.getEngine().getSceneManager().getRootSceneNode().createChildSceneNode(ms0.getName());
		ms0N.attachObject(ms0);
		ms0N.scale(NUMBER_SCALE, NUMBER_SCALE, NUMBER_SCALE);
		ms0N.translate(STARTING_X + SHIFT_AMOUNT * 2 + COLON_OFFSET * 4, STARTING_Y, 0f);
		
		SceneNode ms1N = g.getEngine().getSceneManager().getRootSceneNode().createChildSceneNode(ms1.getName());
		ms1N.attachObject(ms1);
		ms1N.scale(NUMBER_SCALE, NUMBER_SCALE, NUMBER_SCALE);
		ms1N.translate(STARTING_X + SHIFT_AMOUNT * 3 + COLON_OFFSET * 4, STARTING_Y, 0f);
		
		SceneNode ms2N = g.getEngine().getSceneManager().getRootSceneNode().createChildSceneNode(ms2.getName());
		ms2N.attachObject(ms2);
		ms2N.scale(NUMBER_SCALE, NUMBER_SCALE, NUMBER_SCALE);
		ms2N.translate(STARTING_X + SHIFT_AMOUNT * 4 + COLON_OFFSET * 4, STARTING_Y, 0f);
	}
	
	public void update(long timeL) {
		g.getGameState().setElapsedRaceTime(timeL);
		int time = (int) timeL;
		if (time < 0) {
			time = 0;
		}
		int ms2T = time % 10;
		int ms1T = (time / 10) % 10;
		int ms0T = (time / 100) % 10;
		
		int sec1T = (time / 1000) % 10;
		int sec0T = ((time / 1000) % 60) / 10;
		
		int min1T = ((time / 1000) / 60) % 10;
		final int COLON = 10;

		// System.out.println("" + min0T + min1T + ":" + sec0T + sec1T + ":" + ms0T + ms1T + ms2T);
		countdownN.detachAllObjects();
		if (timeL < 0) {
			countdownN.attachObject(countdown);
			tstate = (TextureState) g.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
			setTexture((int) Math.min(3, (Math.abs(timeL) / 1000) % 10 + 1));
			countdown.setRenderState(tstate);
		}
		
		tstate = (TextureState) g.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		setTexture(min1T);
		minute1.setRenderState(tstate);
		
		tstate = (TextureState) g.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		setTexture(COLON);
		colon0.setRenderState(tstate);
		
		tstate = (TextureState) g.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		setTexture(sec0T);
		second0.setRenderState(tstate);
		
		tstate = (TextureState) g.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		setTexture(sec1T);
		second1.setRenderState(tstate);
		
		tstate = (TextureState) g.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		setTexture(COLON);
		colon1.setRenderState(tstate);
		
		tstate = (TextureState) g.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		setTexture(ms0T);
		ms0.setRenderState(tstate);
		
		tstate = (TextureState) g.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		setTexture(ms1T);
		ms1.setRenderState(tstate);
		
		tstate = (TextureState) g.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		setTexture(ms2T);
		ms2.setRenderState(tstate);
	}
	
	private void setTexture(int num) {
		switch (num) {
		case 0:
			tstate.setTexture(g.getTextures().getTexture(PreloadTextures.TEXTURE.N0));
			break;
		case 1:
			tstate.setTexture(g.getTextures().getTexture(PreloadTextures.TEXTURE.N1));
			break;
		case 2:
			tstate.setTexture(g.getTextures().getTexture(PreloadTextures.TEXTURE.N2));
			break;
		case 3:
			tstate.setTexture(g.getTextures().getTexture(PreloadTextures.TEXTURE.N3));
			break;
		case 4:
			tstate.setTexture(g.getTextures().getTexture(PreloadTextures.TEXTURE.N4));
			break;
		case 5:
			tstate.setTexture(g.getTextures().getTexture(PreloadTextures.TEXTURE.N5));
			break;
		case 6:
			tstate.setTexture(g.getTextures().getTexture(PreloadTextures.TEXTURE.N6));
			break;
		case 7:
			tstate.setTexture(g.getTextures().getTexture(PreloadTextures.TEXTURE.N7));
			break;
		case 8:
			tstate.setTexture(g.getTextures().getTexture(PreloadTextures.TEXTURE.N8));
			break;
		case 9:
			tstate.setTexture(g.getTextures().getTexture(PreloadTextures.TEXTURE.N9));
			break;
		case 10:
			tstate.setTexture(g.getTextures().getTexture(PreloadTextures.TEXTURE.COLON));
			break;
		default: 
			System.out.println("Error returning TextureState: " + num);
			break;
		}
	}
}
