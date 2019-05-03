package ray.rage.rendersystem.shader.glsl;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.awt.GLCanvas;

import ray.rage.rendersystem.Renderable;


public class GlslGUIBackgroundProgram extends AbstractGlslProgram {
	private boolean                        initialized = false;

    private GlslProgramAttributeBufferVec3 positionsBuffer;
    private GlslProgramIndexBuffer         indexBuffer;
    
    private GlslProgramUniformMat4         transformationMatrix;

    public GlslGUIBackgroundProgram(GLCanvas canvas) {
        super(canvas);
    }

    @Override
    public Type getType() {
        return Type.GUI_BACKGROUND;
    }

    @Override
    public void fetchImpl(Context ctx) {
        if (!initialized)
            init();
        
        final Renderable r = ctx.getRenderable();

        setRenderable(r);
        transformationMatrix.set(r.getWorldTransformMatrix());
    }

	private void setRenderable(Renderable r) {
        FloatBuffer fb = r.getVertexBuffer();
        if (canSubmitBuffer(fb)) {
            positionsBuffer.set(fb);
        }

        IntBuffer ib = r.getIndexBuffer();
        if (canSubmitBuffer(ib)) {
            indexBuffer.set(ib);
        }
    }

    private void init() {
        final GLCanvas canvas = getCanvas();
        positionsBuffer = new GlslProgramAttributeBufferVec3(this, canvas, "vertex_position");
        transformationMatrix = new GlslProgramUniformMat4(this, canvas, "transformationMatrix");
        indexBuffer = new GlslProgramIndexBuffer(this, canvas);
        initialized = true;
    }

    @Override
    public void notifyDispose() {
        if (initialized) {
            positionsBuffer.notifyDispose();
            indexBuffer.notifyDispose();
            transformationMatrix.notifyDispose();

            positionsBuffer = null;
            indexBuffer = null;
            transformationMatrix = null;

            initialized = false;
        }
        super.notifyDispose();
    }

    private static boolean canSubmitBuffer(Buffer buff) {
        return buff != null && buff.capacity() > 0;
    }
}
