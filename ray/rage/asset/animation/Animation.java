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

package ray.rage.asset.animation;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import ray.rage.asset.AbstractAsset;
import ray.rage.asset.animation.AnimationManager;
import ray.rml.*;

/**
 * An <i>Animation</i> describes a sequence of frames that can be applied to a {@link ray.rage.scene.SkeletalEntity}
 * object. Each frame holds a list of transformations that are applied to each bone.
 *
 * The number of bones in an animation must match the number of bones in the {@link ray.rage.scene.SkeletalEntity}
 * Loading specific Animations is typically handled by the client at runtime to allow manual selection of
 * Animations to be loaded per individual objects.
 *
 * @author Luis Gutierrez
 *
 */

public final class Animation extends AbstractAsset {

    Animation(AnimationManager sm, String name) {
        super(sm, name);
    }

    // Used for compatibility checking, to ensure an animation is compatible with a Skeleton.
    private int boneCount;

    // Stores the number of frames in the animation
    private int frameCount;


    // This framesList holds a list of FloatBuffers
    // Each FloatBuffer holds the transformation data of all bones for that frame.
    // Each bone transform consists of 10 floats, locX, locY, locZ, rotW, rotX, rotY, rotZ, scaleX, scaleY, scaleZ
    private ArrayList<FloatBuffer> framesList = new ArrayList<>();

    public void setBoneCount(int boneCount) {
        this.boneCount = boneCount;
    }

    public int getBoneCount() {
        return boneCount;
    }

    public void setFrameCount(int frameCount) {
        this.frameCount = frameCount;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public FloatBuffer getFrame(int frameIndex) {
        return framesList.get(frameIndex);
    }

    public void setFrame(int frameIndex, FloatBuffer frame) {
        if(frame == null)
            throw new NullPointerException();
        if(frameIndex >= framesList.size() || frameIndex < 0)
            throw new IndexOutOfBoundsException();

        framesList.set(frameIndex, frame);
    }

    public void appendFrame(FloatBuffer frame) {
        framesList.add(frame);
    }

    public Vector3 getFrameBoneLoc(int frameIndex, int boneIndex) {
        if(frameIndex >= framesList.size() || frameIndex < 0)
            throw new IndexOutOfBoundsException();

        FloatBuffer frame = framesList.get(frameIndex);

        if(boneIndex < 0 || (boneIndex * 10) + 9 >= frame.capacity())
            throw new IndexOutOfBoundsException();

        float locX = frame.get(boneIndex * 10);
        float locY = frame.get(boneIndex * 10 + 1);
        float locZ = frame.get(boneIndex * 10 + 2);

        return Vector3f.createFrom(locX, locY, locZ);
    }

    public Quaternion getFrameBoneRot(int frameIndex, int boneIndex) {
        if(frameIndex >= framesList.size() || frameIndex < 0)
            throw new IndexOutOfBoundsException();

        FloatBuffer frame = framesList.get(frameIndex);

        if(boneIndex < 0 || (boneIndex * 10) + 9 >= frame.capacity())
            throw new IndexOutOfBoundsException();

        float rotW = frame.get(boneIndex * 10 + 3);
        float rotX = frame.get(boneIndex * 10 + 4);
        float rotY = frame.get(boneIndex * 10 + 5);
        float rotZ = frame.get(boneIndex * 10 + 6);

        return Quaternionf.createFrom(rotW, rotX, rotY, rotZ);
    }

    public Vector3 getFrameBoneScl(int frameIndex, int boneIndex) {
        if(frameIndex >= framesList.size() || frameIndex < 0)
            throw new IndexOutOfBoundsException();

        FloatBuffer frame = framesList.get(frameIndex);

        if(boneIndex < 0 || (boneIndex * 10) + 9 >= frame.capacity())
            throw new IndexOutOfBoundsException();

        float scaleX = frame.get(boneIndex * 10 + 7);
        float scaleY = frame.get(boneIndex * 10 + 8);
        float scaleZ = frame.get(boneIndex * 10 + 9);

        return Vector3f.createFrom(scaleX, scaleY, scaleZ);
    }


    @Override
    public void notifyDispose() {

        for(int i = 0; i < framesList.size(); i++) {
            framesList.set(i,null);
        }

        framesList = null;
        boneCount = 0;
        frameCount = 0;
    }

}
