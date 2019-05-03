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

package ray.rage.scene.controllers;

import java.util.*;

import ray.rage.scene.*;

/**
 * An abstract, package-private, implementation of a {@link Node.Controller}.
 *
 * @author Raymond L. Rivera
 *
 */
public abstract class AbstractController implements Node.Controller {

    protected List<Node> controlledNodesList = new ArrayList<>();
    private boolean      isEnabled           = true;
    private boolean      shouldDelete        = false;

    @Override
    public void addNode(Node node) {
        if (node == null)
            throw new NullPointerException("Null " + Node.class.getSimpleName());

        controlledNodesList.add(node);
    }

    @Override
    public void removeNode(Node node) {
        if (node == null)
            throw new NullPointerException("Null " + Node.class.getSimpleName());

        controlledNodesList.remove(node);
    }

    @Override
    public void removeNode(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("Empty name");

        // the scene manager already guarantees that no two nodes are named the
        // same, so there's no need to keep searching after removing a single
        // match
        for (Node n : controlledNodesList) {
            if (n.getName().equals(name)) {
                controlledNodesList.remove(n);
                return;
            }
        }
    }

    @Override
    public void removeAllNodes() {
        controlledNodesList.clear();
    }

    @Override
    public void update(float elapsedTimeMillis) {
        if (elapsedTimeMillis < 0)
            throw new IllegalArgumentException("elapsed time < 0");

        // while we could add a check on whether the controller is (not) enabled
        // here, that's left to the clients in case they want to manually force
        // updates regardless of an enabled/disabled state
        if (controlledNodesList.size() > 0)
            updateImpl(elapsedTimeMillis);
    }

    /**
     * Abstract method to be implemented by concrete types.
     *
     * @param elapsedTimeMillis
     *            The time elapsed since the last update.
     * @see #update(float)
     */
    protected abstract void updateImpl(float elapsedTimeMillis);

    @Override
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void notifyDispose() {
        isEnabled = false;
        controlledNodesList.clear();
        controlledNodesList = null;
    }

	public boolean isShouldDelete() {
		return shouldDelete;
	}

	public void setShouldDelete(boolean shouldDelete) {
		this.shouldDelete = shouldDelete;
	}

}
