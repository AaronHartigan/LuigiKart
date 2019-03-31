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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;

import ray.rage.rendersystem.*;
import ray.rage.rendersystem.gl4.GL4RenderWindow;
import ray.rage.rendersystem.gl4.GL4Viewport;

/**
 * A concrete implementation of the {@link RenderWindow render-window} interface
 * for a {@link GL4RenderSystem}.
 *
 * @author Raymond L. Rivera
 *
 */
public final class GL4RenderWindow implements RenderWindow {

    private static final Logger logger       = Logger.getLogger(GL4RenderWindow.class.getName());

    private final JFrame        frame;
    private final Canvas        canvas;
    private final DisplayMode   dispMode;
    private boolean             isInFullScreenMode;

    private List<Viewport>      viewportList = new ArrayList<>();

    public GL4RenderWindow(Canvas renderTarget, DisplayMode requestedMode, boolean fullScreen) {
        canvas = renderTarget;

        frame = new JFrame(GL4RenderWindow.class.getName());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(requestedMode.getWidth(), requestedMode.getHeight());
        frame.setLocationRelativeTo(null);
        frame.getContentPane().add(canvas, BorderLayout.CENTER);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();

        if (fullScreen)
            tryFullScreenMode(gd, requestedMode);

        dispMode = gd.getDisplayMode();

        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                // @formatter:off
                int result = JOptionPane.showConfirmDialog(
                    e.getWindow(),
                    "Are you sure you want to exit?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                // @formatter:on

                // FIXME: This will shutdown the client without allowing other
                // parts of the system to properly clean up after themselves.
                // Need a way to notify higher-levels that we want to exit
                // gracefully without causing exceptions
                if (result == JOptionPane.YES_OPTION) {
                    notifyDispose();
                    System.exit(1);
                }
            }
        });

        createViewport(0, 0, 1, 1);
        frame.setVisible(true);
    }

    @Override
    public Viewport createViewport(float bottom, float left, float width, float height) {
        Viewport vp = new GL4Viewport(canvas, bottom, left, width, height);
        viewportList.add(vp);
        return vp;
    }

    @Override
    public Viewport getViewport(int index) {
        return viewportList.get(index);
    }

    @Override
    public int getViewportCount() {
        return viewportList.size();
    };

    @Override
    public Iterable<Viewport> getViewports() {
        return viewportList;
    }

    @Override
    public void removeViewport(int index) {
        Viewport vp = viewportList.remove(index);
        vp.notifyDispose();
    }

    @Override
    public void removeViewport(Viewport vp) {
        vp.notifyDispose();
        viewportList.remove(vp);
    }

    @Override
    public void removeAllViewports() {
        for (Viewport vp : viewportList)
            vp.notifyDispose();

        viewportList.clear();
    }

    @Override
    public void setTitle(String title) {
        frame.setTitle(title);
    }

    @Override
    public int getWidth() {
        return dispMode.getWidth();
    }

    @Override
    public int getHeight() {
        return dispMode.getHeight();
    }

    @Override
    public int getBitDepth() {
        return dispMode.getBitDepth();
    }

    @Override
    public int getRefreshRate() {
        return dispMode.getRefreshRate();
    }

    @Override
    public void setIconImage(Image icon) {
        frame.setIconImage(icon);
    }

    @Override
    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    @Override
    public boolean isVisible() {
        return frame.isVisible();
    }

    @Override
    public boolean isFullScreen() {
        return isInFullScreenMode;
    }

    @Override
    public void notifyDispose() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        try {
            Window window = gd.getFullScreenWindow();
            window.dispose();
            gd.setFullScreenWindow(null);
        } catch (NullPointerException e) {
            // TODO: add logging
        } finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    @Override
    public void addKeyListener(KeyListener listener) {
        canvas.addKeyListener(listener);
        canvas.requestFocus();
    }

    @Override
    public void addMouseListener(MouseListener listener) {
        canvas.addMouseListener(listener);
    }

    @Override
    public void addMouseMotionListener(MouseMotionListener listener) {
        canvas.addMouseMotionListener(listener);
    }

    @Override
    public void addMouseWheelListener(MouseWheelListener listener) {
        canvas.addMouseWheelListener(listener);
    }

    private void tryFullScreenMode(GraphicsDevice gd, DisplayMode dispMode) {
        isInFullScreenMode = false;

        if (gd.isFullScreenSupported()) {
            frame.setUndecorated(true);
            frame.setResizable(false);
            frame.setIgnoreRepaint(true); // AWT repaint events ignored for
                                          // active rendering

            gd.setFullScreenWindow(frame);

            if (gd.isDisplayChangeSupported()) {
                try {
                    gd.setDisplayMode(dispMode);
                    frame.setSize(dispMode.getWidth(), dispMode.getHeight());
                    isInFullScreenMode = true;
                } catch (IllegalArgumentException e) {
                    logger.warning(e.getLocalizedMessage());
                    frame.setUndecorated(false);
                    frame.setResizable(true);
                }
            } else {
                logger.fine("FSEM not supported");
            }
        } else {
            frame.setUndecorated(false);
            frame.setResizable(true);
            frame.setSize(dispMode.getWidth(), dispMode.getHeight());
            frame.setLocationRelativeTo(null);
        }
    }
    public int getLocationTop() { return (int)frame.getLocationOnScreen().getY(); }
    public int getLocationLeft() { return (int)frame.getLocationOnScreen().getX(); }
}
