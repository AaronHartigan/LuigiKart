package ray.rage.rendersystem.shader.glsl;

import com.jogamp.opengl.awt.GLCanvas;

import ray.rage.rendersystem.shader.glsl.AbstractGlslProgram;
import ray.rage.rendersystem.shader.glsl.GlslProgramUniformInt;

public class GlslFramebufferProgram extends AbstractGlslProgram {

	private GlslProgramUniformInt screenTexture;

	public GlslFramebufferProgram(GLCanvas glc) {
		super(glc);
	}

	@Override
	public Type getType() {
		return Type.FRAMEBUFFER;
	}

	@Override
	protected void fetchImpl(Context ctx) {
		// TODO Auto-generated method stub
		
	}
	
    @Override
    public void notifyDispose() {
        if (screenTexture != null)
        	screenTexture.notifyDispose();

        screenTexture = null;
        super.notifyDispose();
    }
}
