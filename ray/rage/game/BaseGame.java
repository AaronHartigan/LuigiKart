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

package ray.rage.game;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import ray.rage.*;
import ray.rage.asset.shader.*;
import ray.rage.game.AbstractGame;
import ray.rage.game.Game;
import ray.rage.game.VariableFrameRateGame;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.gl4.*;
import ray.rage.rendersystem.shader.*;
import ray.rage.rendersystem.shader.GpuShaderProgram.*;
import ray.rage.scene.*;
import ray.rage.scene.generic.*;
import ray.rage.util.*;

/**
 * The <i>base game</i> provides a base implementation for the mayority of a
 * {@link Game game's} behavior during setup, teardown, and the proper sequence
 * to follow. It's a convenience to allow users to get "up and running" more
 * quickly.
 * <p>
 * Due to different possibilities in how a main loop might be implemented (e.g.
 * variable vs fixed frame rates), this base implementation does not contain a
 * main-loop. In other words, this class is loop-agnostic.
 *
 * @author Raymond L. Rivera
 *
 * @see VariableFrameRateGame
 *
 */
public abstract class BaseGame extends AbstractGame
                               implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

    private final Engine engine    = new Engine();
    private State        gameState = State.INVALID;

    @Override
    public void startup() {
        gameState = State.STARTING;
        engine.startup();
        try {
            startup(engine);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void run() {
        gameState = State.RUNNING;
        run(engine);
    }

    @Override
    public void shutdown() {
        gameState = State.ENDING;
        shutdown(engine);
        engine.shutdown();
        gameState = State.INVALID;
    }

    @Override
    public void setState(State state) {
        // TODO: consider a FSM to manage valid state transitions
        gameState = state;
    }

    @Override
    public State getState() {
        return gameState;
    }

    @Override
    public Engine getEngine() {
        return engine;
    }

    @Override
    public void exit() {
        System.exit(0);
    }

    @Override
    protected void startup(Engine engine) throws IOException {
        Configuration conf = engine.getConfiguration();
        loadConfiguration(conf);
        engine.getMeshManager().setBaseDirectoryPath(conf.valueOf("assets.meshes.path"));
        engine.getAnimationManager().setBaseDirectoryPath(conf.valueOf("assets.animations.path"));
        engine.getSkeletonManager().setBaseDirectoryPath(conf.valueOf("assets.skeletons.path"));
        engine.getMaterialManager().setBaseDirectoryPath(conf.valueOf("assets.materials.path"));
        engine.getShaderManager().setBaseDirectoryPath(conf.valueOf("assets.shaders.path"));
        engine.getTextureManager().setBaseDirectoryPath(conf.valueOf("assets.textures.path"));

        RenderSystemFactory rsf = createRenderSystemFactory();
        RenderSystem rs = rsf.createInstance();
        engine.registerRenderSystem(rs);
        engine.setActiveRenderSystem(rs.getAPI());
        setupWindow(rs, GraphicsEnvironment.getLocalGraphicsEnvironment());

        waitForRenderSystem(rs);

        setupWindowViewports(rs.getRenderWindow());
        setupGpuShaderPrograms(rs, engine.getShaderManager());

        SceneManagerFactory smf = createSceneManagerFactory();
        SceneManager sm = smf.createInstance();
        engine.registerSceneManager(sm);
        engine.setActiveSceneManager(sm.getEnvironment());
        sm.setRenderSystem(rs);
        setupCameras(sm, rs.getRenderWindow());
        setupScene(engine, sm);
    }

    private void waitForRenderSystem(RenderSystem rs) {
        final int colLimit = 40;
        final int napsLimit = 2000;
        final int napTimeMsec = 10;
        int naps = 0;
        while (!rs.isInitialized()) {
            System.out.print(". ");
            try {
                Thread.sleep(napTimeMsec);
            } catch (InterruptedException e) {
                throw new RuntimeException("Render window initialization interrupted");
            }

            if (++naps % colLimit == 0)
                System.out.println();

            if (naps > napsLimit)
                throw new RuntimeException("Unable to create render window context");
        }
    }

    @Override
    protected RenderSystemFactory createRenderSystemFactory() {
        return new GL4RenderSystemFactory();
    }

    @Override
    protected SceneManagerFactory createSceneManagerFactory() {
        return new GenericSceneManagerFactory();
    }

    @Override
    protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
        Configuration conf = getEngine().getConfiguration();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        ImageIcon icon = new ImageIcon(conf.valueOf("assets.icons.window"));

        RenderWindow rw = rs.createRenderWindow(gd.getDisplayMode(), false);
        rw.setIconImage(icon.getImage());
    }

    @Override
    protected void setupWindowViewports(RenderWindow rw) {
        rw.addKeyListener(this);
        rw.addMouseListener(this);
        rw.addMouseMotionListener(this);
        rw.addMouseWheelListener(this);

        Viewport vp = rw.getViewport(0);
        vp.setClearColor(Color.BLACK);
    }

    @Override
    protected void setupGpuShaderPrograms(RenderSystem rs, ShaderManager sm) throws IOException {
        setupRenderingProgram(rs, sm);
        setupIemBoxProgram(rs, sm);
        setupSkyBoxProgram(rs, sm);
        setupSkeletalRenderingProgram(rs, sm);
        setupTessProgram(rs, sm);
        setupDepthProgram(rs, sm);
    }

	@Override
    protected void shutdown(Engine engine) {}

    @Override
    protected void loadConfiguration(Configuration config) throws IOException {
        config.load();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                setState(State.STOPPING);
                break;
        }
    }

    private void setupRenderingProgram(RenderSystem rs, ShaderManager sm) throws IOException {
        GpuShaderProgram program = rs.createGpuShaderProgram(GpuShaderProgram.Type.RENDERING);

        Shader vs = sm.getAssetByPath("renderables.vert");
        Shader fs = sm.getAssetByPath("renderables.frag");

        program.addSourceCode(vs.getSource(), Stage.VERTEX_PROGRAM);
        program.addSourceCode(fs.getSource(), Stage.FRAGMENT_PROGRAM);

        program.build();
    }
    
    private void setupIemBoxProgram(RenderSystem rs, ShaderManager sm) throws IOException {
        GpuShaderProgram program = rs.createGpuShaderProgram(GpuShaderProgram.Type.ITEM_BOX);

        Shader vs = sm.getAssetByPath("itembox.vert");
        Shader fs = sm.getAssetByPath("itembox.frag");

        program.addSourceCode(vs.getSource(), Stage.VERTEX_PROGRAM);
        program.addSourceCode(fs.getSource(), Stage.FRAGMENT_PROGRAM);

        program.build();
    }

    private void setupSkyBoxProgram(RenderSystem rs, ShaderManager sm) throws IOException {
        GpuShaderProgram program = rs.createGpuShaderProgram(GpuShaderProgram.Type.SKYBOX);

        Shader vs = sm.getAssetByPath("skybox.vert");
        Shader fs = sm.getAssetByPath("skybox.frag");

        program.addSourceCode(vs.getSource(), Stage.VERTEX_PROGRAM);
        program.addSourceCode(fs.getSource(), Stage.FRAGMENT_PROGRAM);

        program.build();
    }
    
    private void setupSkeletalRenderingProgram(RenderSystem rs, ShaderManager sm) throws IOException {
        GpuShaderProgram program = rs.createGpuShaderProgram(GpuShaderProgram.Type.SKELETAL_RENDERING);

        Shader vs = sm.getAssetByPath("skeletal_renderables.vert");
        Shader fs = sm.getAssetByPath("renderables.frag");

        program.addSourceCode(vs.getSource(), Stage.VERTEX_PROGRAM);
        program.addSourceCode(fs.getSource(), Stage.FRAGMENT_PROGRAM);

        program.build();
    }
    
    private void setupTessProgram(RenderSystem rs, ShaderManager sm) throws IOException {
        GpuShaderProgram program = rs.createGpuShaderProgram(GpuShaderProgram.Type.TESSELLATION);

        Shader vs = sm.getAssetByPath("tessellation.vert");
        Shader cs = sm.getAssetByPath("tessellation.tesc");
        Shader es = sm.getAssetByPath("tessellation.tese");
        Shader fs = sm.getAssetByPath("tessellation.frag");

        program.addSourceCode(vs.getSource(), Stage.VERTEX_PROGRAM);
        program.addSourceCode(cs.getSource(), Stage.CONTROL_PROGRAM);
        program.addSourceCode(es.getSource(), Stage.EVALUATION_PROGRAM);
        program.addSourceCode(fs.getSource(), Stage.FRAGMENT_PROGRAM);

        program.build();
    }
    

    private void setupDepthProgram(RenderSystem rs, ShaderManager sm) throws IOException {
    	GpuShaderProgram program = rs.createGpuShaderProgram(GpuShaderProgram.Type.DEPTH);
    	
    	Shader vs = sm.getAssetByPath("depth_shader.vert");
        Shader fs = sm.getAssetByPath("depth_shader.frag");

        program.addSourceCode(vs.getSource(), Stage.VERTEX_PROGRAM);
        program.addSourceCode(fs.getSource(), Stage.FRAGMENT_PROGRAM);

        program.build();
	}

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {}

}
