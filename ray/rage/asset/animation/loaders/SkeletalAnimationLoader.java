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

package ray.rage.asset.animation.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import ray.rage.asset.animation.*;
import ray.rage.asset.animation.loaders.SkeletalAnimationLoader;
import ray.rage.util.BufferUtil;

public final class SkeletalAnimationLoader implements AnimationLoader {
    private static final String TAG = SkeletalAnimationLoader.class.getName();
    private static final Logger logger = Logger.getLogger(TAG);


    private int boneCount;
    private int frameCount;

    private List<List<Float>> framesList = new ArrayList<List<Float>>();

    @Override
    public void loadAsset(Animation anim, Path path) throws IOException, NullPointerException {

        if (anim == null)
            throw new NullPointerException("Null animation");

        BufferedReader br = Files.newBufferedReader(path);
        String line;

        try {
            // Read the header data
            if((line = br.readLine()) != null) {
                // Split this tab-delimited line into an array
                String[] header = line.split("\t");
                // First number is boneCount
                boneCount = Integer.parseInt(header[0]);
                // Second number is frameCount
                frameCount = Integer.parseInt(header[1]);
            }

            // Read each bone
            // Iterate through each frame
            for(int i = 0; i < frameCount; i++) {
                framesList.add( new ArrayList<Float>() );
                // Iterate through each bone
                for(int j = 0; j < boneCount && ((line = br.readLine()) != null); j++) {
                    String[] boneTransform = line.split("\t");
                    //In the animation file, each line is a single bone's frame's transform.

                    // Add each of the 10 bone transform values:
                    for(int k = 0; k < 10; k++)
                        framesList.get(i).add(Float.parseFloat(boneTransform[k]));
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(TAG + ": " + e, e);
        }
        finally {
            if(br != null)
                br.close();
        }

        anim.setBoneCount(boneCount);
        anim.setFrameCount(frameCount);

        // Iterate through each frame in framesList adding it as a FloatBuffer to anim.framesList
        for(List<Float> frame : framesList) {
            anim.appendFrame(toFloatBuffer(frame));
        }
        
        safeReset();
    }
    
    private void safeReset() {
    	framesList.clear();
    }

    private static FloatBuffer toFloatBuffer(List<Float> list) {
        float[] values = new float[list.size()];
        for (int i = 0; i < values.length; i++)
            values[i] = list.get(i);

        return BufferUtil.directFloatBuffer(values);
    }

    @Override
    public void notifyDispose() {
        for(int i = 0; i < framesList.size(); i++) {
            framesList.set(i,null);
        }

        framesList = null;

        frameCount = 0;
        boneCount = 0;
    }

}
