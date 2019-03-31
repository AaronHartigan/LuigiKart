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

package ray.rage.game;

import ray.rage.*;
import ray.rage.game.BaseGame;

/**
 * This class implements a tight game loop that runs as fast as it can, rather
 * than running at a pre-set maximum number of frames per interval step.
 *
 * @author Raymond L. Rivera
 *
 */
public abstract class VariableFrameRateGame extends BaseGame {

    @Override
    protected void run(Engine engine) {
        long updateStart;
        long updateEnd = System.nanoTime();
        float elapsedMSec;

        while (getState() == State.RUNNING) {
            updateStart = System.nanoTime();
            elapsedMSec = (updateStart - updateEnd) * 1e-6f;
            updateEnd = updateStart;

            engine.notifyElapsedTimeMillis(elapsedMSec);
            update(engine);
            engine.renderOneFrame();

            Thread.yield();
        }
    }

}
