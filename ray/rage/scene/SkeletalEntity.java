/**
 * Copyright (C) 2017 Luis Gutierrez <lg24834@gmail.com>
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

package ray.rage.scene;

import java.io.IOException;

import ray.rage.asset.mesh.Mesh;
import ray.rage.scene.Entity;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SkeletalEntity;

/**
 * A <i>Skeletal Entity</i> extends the notion of an {@link Entity} to add support for Skeletal animations
 * using a {@link ray.rage.asset.skeleton.Skeleton skeleton}, and a {@link Mesh mesh}.
 * <p>
 *
 * A skeletalEntity is the ultimate union of a {@link ray.rage.asset.skeleton.Skeleton skeleton} and a
 * {@link Mesh mesh}.
 *
 * A skeletal-entity also allows the user to load and play skeletal animations.
 * It is the responsibility of the <i>skeletal entity</i> to properly apply the animation data contained in
 * the {@link ray.rage.asset.animation.Animation animation} class in order to yield the desired
 * transformations.
 *
 * It is also the duty of the <i>skeletal entity</i> to handle the animation state and logic with the
 * {@link SkeletalEntity#update()} method. This method is intended to be called every frame, but that is
 * not a requirement by design.
 *
 * SkeletalEntities are not meant to be created directly. Instead, you should use the
 * {@link SceneManager#createSkeletalEntity(String, String, String)} factory method.
 *
 * @author Luis Gutierrez
 *
 * @see ray.rage.asset.skeleton.Skeleton
 * @see Mesh
 * @see ray.rage.asset.animation.Animation
 *
 */
public interface SkeletalEntity extends Entity {

    // Updates the Skeletal Entity's animation logic
    void update();

    // Loads an Animation into the skeletal entity
    void loadAnimation(String animName, String animationPath) throws IOException;


    // These are literals for animation end types
    // These allow the client to specify how the animation logic should handle an animation that has ended
    enum EndType {
        // Default (equivalent in function to STOP)
        NONE,
        // Stops the animation, reverts to root pose
        STOP,
        // Freezes the animation at the last frame
        PAUSE,
        // Restarts the animation from the first frame
        LOOP,
        // Plays the animation in reverse from the current frame
        PINGPONG
    }

    // Plays the specified animation, if it has been loaded,
    // at the specified speed and with specified endType for endTypeCount times
    void playAnimation(String animName, float animSpeed, EndType endType, int endTypeCount);

    // Pauses the current animation, causing the SkeletalEntity to freeze
    void pauseAnimation();

    // Stops the current animation, causing the SkeletalEntity to return to its root pose
    void stopAnimation();
}
