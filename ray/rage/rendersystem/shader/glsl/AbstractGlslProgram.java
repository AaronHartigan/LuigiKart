/**
 * Copyright (C) 2016 Raymond L. Rivera <ray.l.rivera@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ray.rage.rendersystem.shader.glsl;

import java.util.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;
import com.jogamp.opengl.util.glsl.*;

import ray.rage.rendersystem.shader.*;
import ray.rage.rendersystem.shader.glsl.GlslContextUtil;
import ray.rage.rendersystem.shader.glsl.GlslProgramContext;

/**
 * Base implementation of the {@link GpuShaderProgram shader-program} interface.
 *
 * @author Raymond L. Rivera
 *
 */
abstract class AbstractGlslProgram implements GpuShaderProgram {

    private final static int   INVALID_ID = -1;

    private GLCanvas           canvas;
    private Map<Stage, String> sourceMap  = new HashMap<>();

    private ShaderProgram      program    = new ShaderProgram();

    public AbstractGlslProgram(GLCanvas glc) {
        if (glc == null)
            throw new NullPointerException("Null canvas");

        canvas = glc;
    }

    @Override
    public int getId() {
        return program.program();
    }

    @Override
    public Context createContext() {
        return new GlslProgramContext();
    }

    @Override
    public void addSourceCode(String code, Stage stage) {
        if (code.isEmpty())
            throw new IllegalArgumentException("Source code is empty");
        if (stage == null)
            throw new NullPointerException("Null " + Stage.class.getSimpleName());

        sourceMap.put(stage, code);
    }

    @Override
    public void build() {
        if (program.linked())
            throw new RuntimeException(getType() + " is already built");

        // a minimum of 1 vertex + 1 fragment program are required, so if we
        // have anything less than 2, either one or both of them are missing
        if (sourceMap.size() < 2)
            throw new IllegalStateException("Missing source code. Verify all stages are set.");

        GLContext ctx = GlslContextUtil.getCurrentGLContext(canvas);
        GL4 gl = ctx.getGL().getGL4();

        ShaderCode vs = compileShader(gl, sourceMap.get(Stage.VERTEX_PROGRAM), Stage.VERTEX_PROGRAM);
        ShaderCode fs = compileShader(gl, sourceMap.get(Stage.FRAGMENT_PROGRAM), Stage.FRAGMENT_PROGRAM);

        linkProgram(gl, program, vs, fs);
        ctx.release();

        // no need to keep source code around anymore
        sourceMap.clear();
    }

    @Override
    public void bind() {
        GLContext ctx = GlslContextUtil.getCurrentGLContext(canvas);
        GL4 gl = ctx.getGL().getGL4();
        program.useProgram(gl, true);
        ctx.release();
    }

    @Override
    public void fetch(Context ctx) {
        if (ctx == null)
            throw new NullPointerException("Null " + Context.class.getSimpleName());

        fetchImpl(ctx);
    }

    /**
     * Lets concrete implementations receive arguments for
     * {@link GpuShaderProgram programs}.
     *
     * @param gl
     *            The {@link GL4} instance.
     * @param v
     *            The {@link GpuShaderProgram.Context context}
     */
    protected abstract void fetchImpl(Context ctx);

    protected GLCanvas getCanvas() {
        return canvas;
    }
    
    protected ShaderProgram getShaderProgram() {
        return program;
    }
    
    protected Map<Stage, String> getSourceMap() {
        return sourceMap;
    }

    @Override
    public void unbind() {
        GLContext ctx = GlslContextUtil.getCurrentGLContext(canvas);
        GL4 gl = ctx.getGL().getGL4();
        program.useProgram(gl, false);
        ctx.release();
    }

    @Override
    public void notifyDispose() {
        GLContext ctx = GlslContextUtil.getCurrentGLContext(canvas);
        GL4 gl = ctx.getGL().getGL4();
        program.destroy(gl);
        ctx.release();

        sourceMap.clear();

        sourceMap = null;
        program = null;
        canvas = null;
    }

    protected static ShaderCode compileShader(GL4 gl, String source, Stage stage) {
        int glStage = INVALID_ID;
        switch (stage) {
            case VERTEX_PROGRAM:
                glStage = GL4.GL_VERTEX_SHADER;
                break;
            case FRAGMENT_PROGRAM:
                glStage = GL4.GL_FRAGMENT_SHADER;
                break;
            case CONTROL_PROGRAM:
                glStage = GL4.GL_TESS_CONTROL_SHADER;
                break;
            case EVALUATION_PROGRAM:
                glStage = GL4.GL_TESS_EVALUATION_SHADER;
                break;
            default:
                throw new UnsupportedOperationException("Not implemented: " + stage);
        }

        String[][] sources = new String[1][1];
        sources[0] = new String[] { source };

        ShaderCode shader = new ShaderCode(glStage, sources.length, sources);
        if (!shader.compile(gl, System.err))
            throw new RuntimeException(stage + " compilation failed");

        return shader;
    }

    protected void linkProgram(GL4 gl, ShaderProgram program, ShaderCode... shaders) {
        program.init(gl);

        for (ShaderCode sc : shaders)
            program.add(sc);

        program.link(gl, System.err);

        if (!program.validateProgram(gl, System.err))
            throw new RuntimeException("Program linking failed: " + getType());

        // no longer necessary after the program has been built
        for (ShaderCode sc : shaders)
            sc.destroy(gl);
    }

}
