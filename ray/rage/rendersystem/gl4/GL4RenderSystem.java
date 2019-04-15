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

package ray.rage.rendersystem.gl4;

import static com.jogamp.opengl.GL3ES3.GL_PATCHES;
import static com.jogamp.opengl.GL3ES3.GL_PATCH_VERTICES;

import java.awt.Canvas;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GL4bc;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.gl2.GLUT;

import myGameEngine.myRage.HUDString;
import ray.rage.rendersystem.RenderQueue;
import ray.rage.rendersystem.RenderSystem;
import ray.rage.rendersystem.RenderWindow;
import ray.rage.rendersystem.Renderable;
import ray.rage.rendersystem.Viewport;
import ray.rage.rendersystem.Renderable.DataSource;
import ray.rage.rendersystem.Renderable.Primitive;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.shader.GpuShaderProgram;
import ray.rage.rendersystem.shader.GpuShaderProgramFactory;
import ray.rage.rendersystem.shader.GpuShaderProgram.Type;
import ray.rage.rendersystem.shader.glsl.GlslContextUtil;
import ray.rage.rendersystem.shader.glsl.GlslProgramFactory;
import ray.rage.rendersystem.states.RenderState;
import ray.rage.scene.AmbientLight;
import ray.rage.scene.Light;
import ray.rage.scene.TessellationBody;
import ray.rage.util.BufferUtil;
import ray.rml.Matrix4;
import ray.rml.Matrix4f;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public final class GL4RenderSystem implements RenderSystem, GLEventListener {

    private static final Logger         logger             = Logger.getLogger(GL4RenderSystem.class.getName());
    private static final int            INVALID_ID         = -1;

    private int                         vertexArrayObjId   = INVALID_ID;

    private Map<Type, GpuShaderProgram> gpuProgramMap      = new HashMap<>();
    private GpuShaderProgramFactory     gpuProgramFactory  = new GlslProgramFactory();

    private RenderQueue                 renderQueue;

    private Vector3                     posVector          = Vector3f.createFrom(0f, 0f, 0f);
    private Matrix4                     viewMatrix         = Matrix4f.createIdentityMatrix();
    private Matrix4                     projMatrix         = Matrix4f.createIdentityMatrix();

    private RenderWindow                window;
    private GLCanvas                    canvas;

    private AtomicBoolean               updateRequested    = new AtomicBoolean(false);
    private AtomicBoolean               contextInitialized = new AtomicBoolean(false);

    private RenderSystem.Capabilities   capabilities;

    private List<Light>                 lightsList;
    private AmbientLight                ambientLight;
    

    private final int                   SHADOW_WIDTH       = 8192;
    private final int                   SHADOW_HEIGHT      = 8192;
    private boolean RENDER_DEPTH = true;
    private boolean RENDER_SCENE = true;
    private boolean RENDER_DEBUG = false;
    private boolean wireframe = false;
    private boolean perspective = true;
    private float MIN_BOX_SIZE = 1f;
    private float MAX_BOX_SIZE = 10f;
    private float boxSize = 10f;
    private float clipSize = 10 * boxSize;
    private Viewport viewport = null;

    public void togglePerspective() {
    	perspective = !perspective;
    }

    public void toggleShadowMap() {
    	RENDER_DEBUG = !RENDER_DEBUG;
    }
	
	public void toggleWireframe() {
		GLContext ctx = GlslContextUtil.getCurrentGLContext(canvas);
		GL4 gl = ctx.getGL().getGL4();
		if (!wireframe) {
			gl.glPolygonMode(GL4.GL_FRONT_AND_BACK, GL4.GL_LINE);
			wireframe = true;
		}
		else {
			gl.glPolygonMode(GL4.GL_FRONT_AND_BACK, GL4.GL_FILL);
			wireframe = false;
		}
		ctx.release();
	}

	//  these variables support the GLUT string used to generate a simple HUD.
    //  note that these are deprecated as of OpenGL version 3.
	private GLUT glut = new GLUT();
	private ArrayList<HUDString> HUDStringsList = new ArrayList<HUDString>();

    public GL4RenderSystem() {
        try {
            final GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL4bc));
            caps.setBackgroundOpaque(true);
            caps.setDoubleBuffered(true);
            caps.setRedBits(8);
            caps.setGreenBits(8);
            caps.setBlueBits(8);
            caps.setAlphaBits(8);
            canvas = new GLCanvas(caps);
            canvas.addGLEventListener(this);
            canvas.setAutoSwapBufferMode(false);
        } catch (GLException e) {
            throw new RuntimeException("Could not create OpenGL 4 context. Check your hardware or drivers", e);
        }
    //    canvas.getContext().setSwapInterval(0);
    }
    
    @Override
    public API getAPI() {
        return API.OPENGL_4;
    }

    @Override
    public Canvas getCanvas() {
        return canvas;
    }

    @Override
    public RenderWindow createRenderWindow(DisplayMode displayMode, boolean fullScreen) {
        if (window == null) {
            window = new GL4RenderWindow(canvas, displayMode, fullScreen);
        }
        return window;
    }

    @Override
    public RenderWindow createRenderWindow(boolean fullScreen) {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        return createRenderWindow(gd.getDisplayMode(), fullScreen);
    }

    @Override
    public RenderQueue createRenderQueue() {
        return new GL4RenderQueue();
    }

    @Override
    public boolean isInitialized() {
        return contextInitialized.get();
    }

    @Override
    public void processRenderQueue(RenderQueue rq, Viewport vp, Vector3 pos, Matrix4 view, Matrix4 proj) {
        if (rq == null)
            throw new NullPointerException("Null " + RenderQueue.class.getSimpleName());
        if (view == null)
            throw new NullPointerException("Null view matrix");
        if (proj == null)
            throw new NullPointerException("Null projection matrix");

        renderQueue = rq;
        posVector = pos;
        viewMatrix = view;
        projMatrix = proj;
        viewport = vp;

        updateRequested.set(true);
        canvas.display();
        updateRequested.set(false);

        // avoid leftovers; these must be reset on every frame
        renderQueue = null;
        posVector = null;
        viewMatrix = null;
        projMatrix = null;
        viewport = null;
    }

    @Override
    public void clearViewport(Viewport vp) {
        if (vp == null)
            throw new NullPointerException("Null viewport");

        GLContext ctx = canvas.getContext();
        try {
            ctx.makeCurrent();
        } catch (GLException e) {
            throw new RuntimeException(e);
        }

        GL4 gl = ctx.getGL().getGL4();

        // @formatter:off
        gl.glViewport(
            vp.getActualLeft(),
            vp.getActualBottom(),
            vp.getActualWidth(),
            vp.getActualHeight()
        );
        gl.glScissor(
            vp.getActualScissorLeft(),
            vp.getActualScissorBottom(),
            vp.getActualScissorWidth(),
            vp.getActualScissorHeight()
        );
        // @formatter:on

        gl.glClearBufferfv(GL4.GL_COLOR, 0, vp.getClearColorBuffer());
        gl.glClearBufferfv(GL4.GL_DEPTH, 0, vp.getClearDepthBuffer());

        ctx.release();
    }

    @Override
    public void swapBuffers() {
        canvas.swapBuffers();
    }

    private void setRenderStates(Renderable r) {
        for (RenderState state : r.getRenderStates())
            if (state.isEnabled())
                state.apply();
    }


    @Override
    public void reshape(GLAutoDrawable glad, int x, int y, int width, int height) {
        for (Viewport vp : window.getViewports())
            vp.notifyDimensionsChanged();
    }

    @Override
    public void dispose(GLAutoDrawable glad) {
        GL4 gl = (GL4) glad.getGL();

        gl.glBindVertexArray(0);

        for (GpuShaderProgram program : gpuProgramMap.values())
            program.notifyDispose();
        gpuProgramMap.clear();

        int[] vaos = new int[] { vertexArrayObjId };
        gl.glDeleteVertexArrays(vaos.length, vaos, 0);

        vertexArrayObjId = INVALID_ID;

        contextInitialized = null;
        capabilities = null;
        gpuProgramMap = null;
        gpuProgramFactory = null;
        renderQueue = null;
        projMatrix = null;
        viewMatrix = null;
        window = null;
        lightsList = null;
        ambientLight = null;
    }

    @Override
    public RenderWindow getRenderWindow() {
        return window;
    }

    @Override
    public Capabilities getCapabilities() {
        return capabilities;
    }

    @Override
    public RenderState createRenderState(RenderState.Type type) {
        if (type == null)
            throw new NullPointerException("Null " + RenderState.Type.class.getSimpleName());

        switch (type) {
            case ZBUFFER:
                return new GL4ZBufferState(canvas);
            case TEXTURE:
                // TODO: Add TBO ref to validate buffer IDs, etc
                return new GL4TextureState(capabilities, canvas);
            case FRONT_FACE:
                return new GL4FrontFaceState(canvas);
            default:
                throw new RuntimeException("Unimplemented " + RenderState.Type.class.getSimpleName() + ": " + type);
        }
    }

    @Override
    public GpuShaderProgram createGpuShaderProgram(GpuShaderProgram.Type type) {
        if (gpuProgramMap.containsKey(type))
            throw new RuntimeException(GpuShaderProgram.class.getSimpleName() + " already exists: " + type);

        GpuShaderProgram program = gpuProgramFactory.createInstance(this, type);
        gpuProgramMap.put(type, program);
        return program;
    }

    @Override
    public GpuShaderProgram getGpuShaderProgram(GpuShaderProgram.Type type) {
        GpuShaderProgram program = gpuProgramMap.get(type);

        if (program == null)
            throw new RuntimeException(GpuShaderProgram.class.getSimpleName() + " does not exist: " + type);

        return program;
    }

    @Override
    public void setActiveLights(List<Light> lights) {
        lightsList = lights;
    }

    @Override
    public void setAmbientLight(AmbientLight ambient) {
        ambientLight = ambient;
    }

    @Override
    public void notifyDispose() {
        window.notifyDispose();
        canvas.disposeGLEventListener(this, false);
    }

    private static int getGLPrimitive(Primitive primitive) {
        switch (primitive) {
            case TRIANGLES:
                return GL4.GL_TRIANGLES;
            case TRIANGLE_STRIP:
                return GL4.GL_TRIANGLE_STRIP;
            case LINES:
                return GL4.GL_LINES;
            case POINTS:
                return GL4.GL_POINTS;
            default:
                logger.severe("Unimplemented primitive: " + primitive + ". Using " + Primitive.TRIANGLES);
                return GL4.GL_TRIANGLES;
        }
    }

	@Override
	public void setHUD(String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHUD(String string, int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHUD2(String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHUD2(String string, int x, int y) {
		// TODO Auto-generated method stub
		
	}
	
	public ArrayList<HUDString> getHUDStringsList() {
		return HUDStringsList;
	}
	
	int shader;
    int simpleDepthShader;
    int debugDepthQuad;
    int[] EBO = new int[1];
    int[] quadVAO = new int[1];
    int[] quadVBO = new int[1];
    int[] depthMap = new int[1];
	int[] depthMapFBO = new int[1];

    @Override
    public void init(GLAutoDrawable glad) {
        // glad.setGL(new TraceGL4(new DebugGL4(glad.getGL().getGL4()), System.out));
        GL4 gl = (GL4) glad.getGL();

        capabilities = new GL4RenderSystemCaps(gl);

        // TODO: turn all remaining enable/disable/etc. calls to render states
        // at some point to allow changes after this initialization(?)
      
        gl.glEnable(GL4.GL_CULL_FACE);
        gl.glEnable(GL4.GL_DEPTH_TEST);
        gl.glDepthFunc(GL4.GL_LEQUAL);
        gl.glEnable(GL4.GL_PROGRAM_POINT_SIZE);
        gl.glEnable(GL4.GL_TEXTURE_CUBE_MAP_SEAMLESS);
        //gl.glFrontFace(GL4.GL_CW);

      //---------------------------------------------------------
        String quadShaderVSource =
    		"#version 430 core\n" +
			"layout (location = 0) in vec3 aPos;\n" +
			"layout (location = 1) in vec2 aTexCoords;\n" +
			"out vec2 TexCoords;\n" +
			"void main()\n" +
			"{\n" +
			"    TexCoords = aTexCoords;\n" +
			"    gl_Position = vec4(aPos, 1.0);\n" +
			"}\n";
        ;
        String quadShaderFSource = 
    		"#version 430 core\n"
    		+ "out vec4 FragColor;\n"
    		+ "in vec2 TexCoords;\n"
    		+ "uniform sampler2D depthMap;\n"
    		//+ "const float near_plane = 0.1f;\n"
    		//+ "const float far_plane = 2000f;\n"
    		//+ "float LinearizeDepth(float depth)\n"
    		//+ "{\n"
    		//+ "    float z = depth * 2.0 - 1.0; // Back to NDC\n"
    		//+ "    return (2.0 * near_plane * far_plane) / (far_plane + near_plane - z * (far_plane - near_plane));\n"
    		//+ "}\n"
    		+ "void main() {\n"
    		+ "float depthValue = texture(depthMap, TexCoords).r;\n"
    		//+ "FragColor = vec4(vec3(LinearizeDepth(depthValue) / far_plane), 1.0)\n"
    		+ " FragColor = vec4(vec3(depthValue), 1.0); // orthographic\n"
    		+ "}\n"
        ;
        debugDepthQuad = gl.glCreateProgram();
        int quadShaderV, quadShaderF;
        quadShaderV = gl.glCreateShader(GL4.GL_VERTEX_SHADER);
        gl.glShaderSource(quadShaderV, 1, new String[] { quadShaderVSource }, null);
        gl.glCompileShader(quadShaderV);
        quadShaderF = gl.glCreateShader(GL4.GL_FRAGMENT_SHADER);
        gl.glShaderSource(quadShaderF, 1, new String[] { quadShaderFSource }, null);
        gl.glCompileShader(quadShaderF);
        gl.glAttachShader(debugDepthQuad, quadShaderV);
        gl.glAttachShader(debugDepthQuad, quadShaderF);
        gl.glLinkProgram(debugDepthQuad);
        gl.glDeleteShader(quadShaderV);
        gl.glDeleteShader(quadShaderF);
        
        
        gl.glUseProgram(debugDepthQuad);
        gl.glUniform1i(gl.glGetUniformLocation(debugDepthQuad, "depthMap"), 0); 
      //---------------------------------------------------------
  
        // framebuffer configuration
        gl.glGenFramebuffers(1, depthMapFBO, 0);
        // create a color attachment texture
        gl.glGenTextures(1, depthMap, 0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, depthMap[0]);
        gl.glTexImage2D(GL4.GL_TEXTURE_2D, 0, GL4.GL_DEPTH_COMPONENT, SHADOW_WIDTH, SHADOW_HEIGHT, 0, GL4.GL_DEPTH_COMPONENT, GL4.GL_FLOAT, null);
        gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_NEAREST);
        gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_NEAREST);
        gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_S, GL4.GL_CLAMP_TO_BORDER);
        gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_T, GL4.GL_CLAMP_TO_BORDER);
        float[] borderColor = { 1.0f, 1.0f, 1.0f, 1.0f };
        gl.glTexParameterfv(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_BORDER_COLOR, borderColor, 0);  
        // attach depth texture as FBO's depth buffer
        gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, depthMapFBO[0]);
        gl.glFramebufferTexture2D(GL4.GL_FRAMEBUFFER, GL4.GL_DEPTH_ATTACHMENT, GL4.GL_TEXTURE_2D, depthMap[0], 0);
        gl.glDrawBuffer(GL4.GL_NONE);
        gl.glReadBuffer(GL4.GL_NONE);
        gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
        
        // screen quad VAO
        gl.glGenVertexArrays(1, quadVAO, 0);
        gl.glGenBuffers(1, quadVBO, 0);
        gl.glBindVertexArray(quadVAO[0]);
        gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, quadVBO[0]);
        FloatBuffer quadData = BufferUtil.directFloatBuffer(quadVertices);
        gl.glBufferData(GL4.GL_ARRAY_BUFFER, quadData.capacity() * Float.BYTES, quadData, GL4.GL_STATIC_DRAW);
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL4.GL_FLOAT, false, 5 * Float.BYTES, 0);
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 2, GL4.GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, 0);
        gl.glBindVertexArray(0);

        contextInitialized.set(true);
    }

    @Override
    public void display(GLAutoDrawable glad) {
        // this prevents automatic display invocations during window/context
        // creation from proceeding, which will result in a crash b/c the other
        // assets/dependencies will not be ready/setup before this is
        // auto-invoked the first time; it also prevents attempts to draw before
        // the context has been initialized in its own separate thread
        if (!updateRequested.get() || !contextInitialized.get())
            return;

        GL4 gl = (GL4) glad.getGL();
        gl.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);

        Vector3 lightOffset = Vector3f.createFrom(4f, 2.84f, 1f); // -2.0f, 4.0f, -1.0f
        final float BOX_SIZE = 40f;
        final float CLIP_SIZE = 40f;
        Matrix4 lightProjection = Matrix4f.createOrthographicMatrix(-BOX_SIZE, BOX_SIZE, -BOX_SIZE, BOX_SIZE, -CLIP_SIZE, CLIP_SIZE);
        Matrix4 orthoProjection = Matrix4f.createOrthographicMatrix(-boxSize, boxSize, -boxSize, boxSize, -clipSize, clipSize);
        Vector3 lightPos = posVector.add(lightOffset);
        Vector3 up = Vector3f.createFrom(0f, 1f, 0f);
        Matrix4 lightView  = Matrix4f.createViewMatrix(
        	posVector,
        	lightPos,
    		up
        );
        Matrix4 lightSpaceMatrix = lightProjection.mult(lightView);

     // 1. First pass
        if (RENDER_DEPTH) {
	        gl.glViewport(0, 0, SHADOW_WIDTH, SHADOW_HEIGHT);
	        gl.glCullFace(GL4.GL_FRONT);
	        gl.glDisable(GL4.GL_SCISSOR_TEST);
	        gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, depthMapFBO[0]);
	        gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);
	        for (Renderable r : renderQueue) {
		        GpuShaderProgram program = r.getGpuShaderProgram();
	            if (program == null) {
	                logger.severe(Renderable.class.getSimpleName() + " skipped. No "
	                        + GpuShaderProgram.class.getSimpleName() + " set");
	                continue;
	            }
	            if (program.getType() == Type.SKYBOX) {
	            	continue;
	            }
	            if (!(program.getType() == Type.TESSELLATION)) {
		            program = getGpuShaderProgram(GpuShaderProgram.Type.DEPTH);
		            if (program == null) {
		                logger.severe(Renderable.class.getSimpleName() + " skipped. No "
		                        + GpuShaderProgram.class.getSimpleName() + " set");
		                continue;
		            }
		            final GpuShaderProgram.Context ctx = program.createContext();
		            ctx.setRenderable(r);
		            ctx.setLightSpaceMatrix(lightSpaceMatrix);
		
		            program.bind();
		            program.fetch(ctx);
		            drawRenderable(gl, r);
		            program.unbind();
		            ctx.notifyDispose();
	            }
	            else {
	            	setRenderStates(r);
		            final GpuShaderProgram.Context ctx = program.createContext();
		            ctx.setRenderable(r);
		            ctx.setViewMatrix(lightView);
		            ctx.setProjectionMatrix(lightProjection);
		            ctx.setLightsList(lightsList);
		            ctx.setAmbientLight(ambientLight);
		            ctx.setLightSpaceMatrix(lightSpaceMatrix);
		
		            program.bind();
		            program.fetch(ctx);
		            drawRenderable(gl, r);
		            program.unbind();
		
		            ctx.notifyDispose();
	            }
	        }
	        gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
            gl.glViewport(
                viewport.getActualLeft(),
                viewport.getActualBottom(),
                viewport.getActualWidth(),
                viewport.getActualHeight()
            );
            gl.glCullFace(GL4.GL_BACK);
            gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);
            gl.glEnable(GL4.GL_SCISSOR_TEST);
        }

     // 2. render scene as normal using the generated depth/shadow map  
        // --------------------------------------------------------------
        if (RENDER_SCENE) {
	        for (Renderable r : renderQueue) {
	            GpuShaderProgram program = r.getGpuShaderProgram();
	            if (program == null) {
	                logger.severe(Renderable.class.getSimpleName() + " skipped. No "
	                        + GpuShaderProgram.class.getSimpleName() + " set");
	                continue;
	            }
	            setRenderStates(r);
	            final GpuShaderProgram.Context ctx = program.createContext();
	            ctx.setRenderable(r);
	            ctx.setViewMatrix(viewMatrix);
	            if (perspective) {
		            ctx.setProjectionMatrix(projMatrix);
	            }
	            else {
		            ctx.setProjectionMatrix(orthoProjection);
	            }
	            ctx.setLightsList(lightsList);
	            ctx.setAmbientLight(ambientLight);
	            ctx.setLightSpaceMatrix(lightSpaceMatrix);
	
	            program.bind();
	            program.fetch(ctx);
		        gl.glActiveTexture(GL4.GL_TEXTURE5);
		        gl.glBindTexture(GL4.GL_TEXTURE_2D, depthMap[0]);
	            drawRenderable(gl, r);
	            program.unbind();
	
	            ctx.notifyDispose();
	        }
        }

        //--------------
     // render Depth map to quad for visual debugging
        if (RENDER_DEBUG) {
	        gl.glViewport(
        		0,
                0,
	        	viewport.getActualWidth() / 4,
	        	viewport.getActualWidth() / 4
	        );
	        gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
	        gl.glUseProgram(debugDepthQuad);
	        gl.glDisable(GL4.GL_DEPTH_TEST);
	        gl.glActiveTexture(GL4.GL_TEXTURE0);
	        gl.glBindTexture(GL4.GL_TEXTURE_2D, depthMap[0]);
	        gl.glBindVertexArray(quadVAO[0]);
	        gl.glDrawArrays(GL4.GL_TRIANGLE_STRIP, 0, 4);
	        gl.glBindVertexArray(0);
	        gl.glViewport(
                viewport.getActualLeft(),
                viewport.getActualBottom(),
                viewport.getActualWidth(),
                viewport.getActualHeight()
            );
	        gl.glEnable(GL4.GL_DEPTH_TEST);
        }

        gl.glUseProgram(0);
        // draw the HUD using a GLUT string.
        // note that this section contains deprecated code.
        GL4 gl4 = canvas.getGL().getGL4();
        GL4bc gl4bc = (GL4bc) gl4;
        for (HUDString hs : HUDStringsList) {
        	gl4bc.glWindowPos2d (hs.getX(), hs.getY());
    		glut.glutBitmapString (hs.getHUDfont(), hs.getStr());
        }
    }

    private void drawRenderable(GL4 gl, Renderable r) {
        final DataSource source = r.getDataSource();
        final int primitive = getGLPrimitive(r.getPrimitive());
        switch (source) {
            case INDEX_BUFFER:
                gl.glDrawElements(primitive, r.getIndexBuffer().capacity(), GL4.GL_UNSIGNED_INT, 0);
                break;
            case VERTEX_BUFFER:
                gl.glDrawArrays(primitive, 0, r.getVertexBuffer().capacity());
                break;
            case TESS_VERT_BUFFER:
            	int quality = ((TessellationBody) r).getQualityTotal();
            	gl.glPatchParameteri(GL_PATCH_VERTICES, 4);
            	gl.glDrawArraysInstanced(GL_PATCHES, 0, 4, quality * quality);
                break;
            default:
                logger.severe("Draw call skipped. Invalid " + DataSource.class.getName() + ": " + source);
                break;
        }
    }
    
    public float getBoxSize() {
		return boxSize;
	}

	public void setBoxSize(float boxSize) {
		if (boxSize < MIN_BOX_SIZE) {
			this.boxSize = MIN_BOX_SIZE;
		}
		else if (boxSize > MAX_BOX_SIZE) {
			this.boxSize = MAX_BOX_SIZE;
		}
		else {
			this.boxSize = boxSize;
		}
		this.clipSize = 10 * this.boxSize;
	}

    float[] quadVertices = new float[] { // vertex attributes for a quad that fills the entire screen in Normalized Device Coordinates.
		// positions        // texture Coords
        -1.0f,  1.0f, 0.0f, 0.0f, 1.0f,
        -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
         1.0f,  1.0f, 0.0f, 1.0f, 1.0f,
         1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
    };
}
