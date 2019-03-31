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

package ray.rage.rendersystem;

import ray.rage.rendersystem.RenderQueue;
import ray.rage.rendersystem.RenderSystem;
import ray.rage.rendersystem.Renderable;
import ray.rage.scene.*;

/**
 * A <i>render queue</i> defines an ordered container for {@link Renderable
 * renderables} to be submitted to the {@link RenderSystem render-system} by the
 * {@link SceneManager scene-manager}.
 *
 * @author Raymond L. Rivera
 *
 */
public interface RenderQueue extends Iterable<Renderable> {

    /**
     * A listener that allows interested clients to receive {@link RenderQueue
     * render-queue} events.
     *
     * @author Raymond L. Rivera
     *
     * @see SceneManager#addRenderQueueListener(Listener)
     *
     */
    interface Listener {

        /**
         * Event raised <i>before</i> any of the {@link RenderQueue
         * render-queues} are processed.
         */
        void onPreRenderQueues();

        /**
         * Event raised <i>after</i> all the {@link RenderQueue render-queues}
         * have been processed.
         */
        void onPostRenderQueues();

        /**
         * Event raised immediately <i>before</i> the specified
         * {@link RenderQueue render-queue} is processed by a
         * {@link RenderSystem render-system}.
         * <p>
         * The client can determine whether the queue processing should proceed
         * by returning <code>true</code>, or cancelled by returning
         * <code>false</code>. If there're multiple listeners already
         * registered, then the last listener to be invoked gets to make the
         * final decision. No guarantees are given regarding the order in which
         * the invocations may take place.
         *
         * @param rq
         *            The {@link RenderQueue render-queue} about to be
         *            processed.
         * @return True if the contents of <code>this</code> {@link RenderQueue
         *         render-queue} should be rendered by the {@link RenderSystem
         *         render-system} in this invocation. False if it should be
         *         skipped.
         */
        boolean onRenderQueueStarted(RenderQueue rq);

        /**
         * Event raised immediately <i>after</i> the specified
         * {@link RenderQueue render-queue} has been processed by a
         * {@link RenderSystem render-system}.
         * <p>
         * The client can determine whether the queue should be immediately
         * processed again by returning <code>true</code>, or only once by
         * returning <code>false</code>. If there're multiple listeners already
         * registered, then the last listener to be invoked gets to make the
         * final decision. If a repeated invocation is specified, then the
         * {@link #onRenderQueueStarted(RenderQueue)} event will be re-emitted
         * prior to processing, followed again by this event.
         * <p>
         * The client is responsible for <i><strong>avoiding an infinite event
         * processing loop</strong></i>
         *
         * @param rq
         *            The {@link RenderQueue render-queue} that just got
         *            processed.
         * @return True if the contents of <code>this</code> {@link RenderQueue
         *         render-queue} should be rendered <i>again</i> by the
         *         {@link RenderSystem render-system} in this invocation. False
         *         otherwise.
         */
        boolean onRenderQueueEnded(RenderQueue rq);

    }

    /**
     * Adds the {@link Renderable renderable} to <code>this</code>
     * {@link RenderQueue queue}.
     *
     * @param r
     *            The {@link Renderable renderable} to add.
     */
    void add(Renderable r);

    /**
     * Gets, but does not remove, the {@link Renderable renderable} at the head
     * of <code>this</code> {@link RenderQueue queue}, or <code>null</code> if
     * it's empty.
     *
     * @return The head {@link Renderable renderable}, if available. Otherwise
     *         <code>null</code>.
     * @see #isEmpty()
     */
    Renderable peek();

    /**
     * Removes the {@link Renderable renderable} from <code>this</code>
     * {@link RenderQueue queue}.
     *
     * @param r
     *            The {@link Renderable renderable} to remove.
     */
    void remove(Renderable r);

    /**
     * Gets the number of {@link Renderable renderables} in <code>this</code>
     * {@link RenderQueue queue}.
     *
     * @return The number of elements.
     */
    int size();

    /**
     * Gets whether <code>this</code> {@link RenderQueue queue} is empty or not.
     *
     * @return True if it's empty. Otherwise false.
     */
    boolean isEmpty();

    /**
     * Removes all the {@link Renderable renderables} from <code>this</code>
     * {@link RenderQueue queue}.
     */
    void clear();

}
