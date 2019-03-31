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

import ray.rage.*;
import ray.rage.common.*;
import ray.rage.game.Game;
/**
 * A <i>game</i> is the basic interface clients implement to have access to the
 * framework and its functionality.
 *
 * @author Raymond L. Rivera
 *
 */
public interface Game extends Manageable {

    /**
     * The <i>state</i> is used to inform and determine some aspects of a
     * {@link Game game's} behavior.
     *
     * @author Raymond L. Rivera
     *
     */
    enum State {
        /**
         * The {@link Game.State state} <i>before</i>
         * {@link Manageable#startup() startup} has begun and <i>after</i>
         * {@link Manageable#shutdown() shutdown} has ended.
         */
        INVALID,

        /**
         * The {@link Game.State state} during {@link Manageable#startup()
         * startup}.
         */
        STARTING,

        /**
         * The {@link Game.State state} the {@link Game game} is in during its
         * {@link Game#run() run}.
         */
        RUNNING,

        /**
         * The {@link Game.State state} the {@link Game game} gets set to in
         * order to exit its {@link Game#run() run}.
         */
        STOPPING,

        /**
         * The {@link Game.State state} during {@link Manageable#shutdown()
         * shutdown}.
         */
        ENDING
    }

    /**
     * Places the {@link Game game} in the {@link Game.State#RUNNING running}
     * {@link Game.State state}, enters the main loop, and actually allows it to
     * be playable.
     */
    void run();

    /**
     * Causes the {@link Game game} to return terminate execution and return a
     * value to the operating system.
     */
    void exit();

    /**
     * Sets the {@link Game game's} {@link Game.State state} to the specified
     * value.
     *
     * @param state
     *            The {@link Game.State state} to set the {@link Game game} to.
     */
    void setState(State state);

    /**
     * Gets the current {@link Game.State state} of the {@link Game game}.
     *
     * @return The current {@link Game.State state}.
     */
    State getState();

    /**
     * Gets the {@link Game game's} reference to the {@link Engine engine} and
     * allows it to access its functionality.
     *
     * @return The {@link Engine engine}.
     */
    Engine getEngine();

}
