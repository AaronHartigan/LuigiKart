/**
 * Copyright (C) 2017 Raymond L. Rivera <ray.l.rivera@gmail.com>
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

import java.util.logging.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;

import ray.rage.rendersystem.gl4.GL4AbstractRenderState;
import ray.rage.rendersystem.gl4.GL4FrontFaceState;
import ray.rage.rendersystem.states.*;

/**
 * A concrete implementation of the {@link FrontFaceState front-face-state}
 * interface for a {@link GL4RenderSystem}.
 *
 * @author Raymond L. Rivera
 *
 */
public final class GL4FrontFaceState extends GL4AbstractRenderState implements FrontFaceState {

    private static final Logger logger        = Logger.getLogger(GL4FrontFaceState.class.getName());

    private VertexWinding       vertexWinding = VertexWinding.COUNTER_CLOCKWISE;

    public GL4FrontFaceState(GLCanvas canvas) {
        super(canvas);
    }

    @Override
    public Type getType() {
        return Type.FRONT_FACE;
    }

    @Override
    public void setVertexWinding(VertexWinding vw) {
        if (vw == null)
            throw new NullPointerException("Null " + VertexWinding.class.getSimpleName());

        vertexWinding = vw;
    }

    @Override
    public VertexWinding getVertexWinding() {
        return vertexWinding;
    }

    @Override
    protected void applyImpl(GL4 gl) {
        gl.glFrontFace(getGLFrontFace(vertexWinding));
    }

    @Override
    protected void disposeImpl(GL4 gl) {
        vertexWinding = null;
    }

    private static int getGLFrontFace(VertexWinding winding) {
        switch (winding) {
            case CLOCKWISE:
                return GL4.GL_CW;
            case COUNTER_CLOCKWISE:
                return GL4.GL_CCW;
            default:
                logger.severe("Undefined: " + winding + ". Using " + VertexWinding.COUNTER_CLOCKWISE);
                return GL4.GL_CCW;
        }
    }

}
