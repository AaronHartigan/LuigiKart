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

import java.util.*;

import ray.rage.rendersystem.*;

/**
 * A concrete implementation of the {@link RenderQueue render-queue} interface
 * for a {@link GL4RenderSystem}.
 * <p>
 * This approach allows each {@link RenderSystem render-system} to to have a
 * queue that's can make decisions that could benefit the {@link RenderSystem
 * render-system}-specific performance.
 *
 * @author Raymond L. Rivera
 *
 */
public final class GL4RenderQueue implements RenderQueue {

    private Queue<Renderable> q = new ArrayDeque<>();

    @Override
    public void add(Renderable r) {
        q.add(r);
    }

    @Override
    public Renderable peek() {
        return q.peek();
    }

    @Override
    public void remove(Renderable r) {
        q.remove(r);
    }

    @Override
    public void clear() {
        q.clear();
    }

    @Override
    public boolean isEmpty() {
        return q.isEmpty();
    }

    @Override
    public int size() {
        return q.size();
    }

    @Override
    public Iterator<Renderable> iterator() {
        return q.iterator();
    }

}
