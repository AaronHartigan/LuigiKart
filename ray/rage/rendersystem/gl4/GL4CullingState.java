package ray.rage.rendersystem.gl4;

import java.util.logging.Logger;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.awt.GLCanvas;

import ray.rage.rendersystem.states.CullingState;

public final class GL4CullingState extends GL4AbstractRenderState implements CullingState{

    private static final Logger logger = Logger.getLogger(GL4FrontFaceState.class.getName());
    private Culling culling = Culling.ENABLED;

    public GL4CullingState(GLCanvas canvas) {
        super(canvas);
    }
    
    @Override
    public Type getType() {
        return Type.CULLING;
    }

    @Override
    public void setCulling(Culling c) {
        if (c == null)
            throw new NullPointerException("Null " + Culling.class.getSimpleName());

        culling = c;
    }

    @Override
    public Culling getCulling() {
        return culling;
    }

    @Override
    protected void applyImpl(GL4 gl) {
    	switch (culling) {
        case ENABLED:
            gl.glEnable(GL4.GL_CULL_FACE);
            return;
        case DISABLED:
            gl.glDisable(GL4.GL_CULL_FACE);
            return;
    	}
    }

    @Override
    protected void disposeImpl(GL4 gl) {
        culling = null;
    }
}
