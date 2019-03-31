package ray.rage.rendersystem.shader.glsl;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.awt.GLCanvas;

import ray.rage.rendersystem.Renderable;
import ray.rage.rendersystem.shader.glsl.AbstractGlslProgram;
import ray.rage.rendersystem.shader.glsl.GlslProgramAttributeBufferVec3;
import ray.rage.rendersystem.shader.glsl.GlslProgramIndexBuffer;
import ray.rage.rendersystem.shader.glsl.GlslProgramUniformMat4;
import ray.rml.Matrix4;

public class GlslDepthProgram extends AbstractGlslProgram {

    private boolean                        initialized = false;

    private GlslProgramAttributeBufferVec3 positionsBuffer;
    private GlslProgramIndexBuffer         indexBuffer;

    private GlslProgramUniformMat4         modelMatrix;
    private GlslProgramUniformMat4         lightSpaceMatrix;

    public GlslDepthProgram(GLCanvas canvas) {
        super(canvas);
    }

    @Override
    public Type getType() {
        return Type.DEPTH;
    }

    @Override
    public void fetchImpl(Context ctx) {
        if (!initialized)
            init();
        
        final Renderable r = ctx.getRenderable();
        final Matrix4 model = r.getWorldTransformMatrix();
        final Matrix4 lightSpace = ctx.getLightSpaceMatrix();

        setRenderable(r);
        setMatrixUniforms(model, lightSpace);
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

    private void setMatrixUniforms(Matrix4 model, Matrix4 lightSpace) {
        modelMatrix.set(model);
        lightSpaceMatrix.set(lightSpace);
    }

    private void init() {
        final GLCanvas canvas = getCanvas();

        positionsBuffer = new GlslProgramAttributeBufferVec3(this, canvas, "vertex_position");
        indexBuffer = new GlslProgramIndexBuffer(this, canvas);

        modelMatrix = new GlslProgramUniformMat4(this, canvas, "matrix.model");
        lightSpaceMatrix = new GlslProgramUniformMat4(this, canvas, "matrix.lightSpaceMatrix");

        initialized = true;
    }

    @Override
    public void notifyDispose() {
        if (initialized) {
            positionsBuffer.notifyDispose();
            indexBuffer.notifyDispose();

            modelMatrix.notifyDispose();
            lightSpaceMatrix.notifyDispose();

            positionsBuffer = null;
            indexBuffer = null;

            modelMatrix = null;
            lightSpaceMatrix = null;

            initialized = false;
        }
        super.notifyDispose();
    }

    private static boolean canSubmitBuffer(Buffer buff) {
        return buff != null && buff.capacity() > 0;
    }
}
