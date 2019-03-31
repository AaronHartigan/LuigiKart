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

package ray.rage.asset.skeleton.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import ray.rage.asset.skeleton.Skeleton;
import ray.rage.asset.skeleton.SkeletonLoader;
import ray.rage.asset.skeleton.loaders.SkeletalSkeletonLoader;
import ray.rage.util.BufferUtil;



/**
 * A {@link ray.rage.asset.skeleton.SkeletonLoader skeleton-loader} capable of processing a custom
 * skeletal animation skeleton format created specifically for RAGE.
 * More info about this format can be found in the .rks Blender 3D file format exporters.
 *
 * The established file extension is <code>.rks</code>
 * <p>
 *
 * @author Luis Gutierrez
 *
 */

public final class SkeletalSkeletonLoader implements SkeletonLoader {
    private static final String TAG = SkeletalSkeletonLoader.class.getName();
    private static final Logger logger = Logger.getLogger(TAG);


    private int boneCount;
    private List<String> boneNamesList = new ArrayList<>();
    private List<Float> boneLengthsList = new ArrayList<>();
    private List<Float> boneRestRotationsList = new ArrayList<>();
    private List<Float> boneRestLocationsList = new ArrayList<>();
    private List<Integer> boneParentsList = new ArrayList<>();


    @Override
    public void loadAsset(Skeleton skel, Path path) throws IOException, NullPointerException {

        if (skel == null)
            throw new NullPointerException("Null skeleton");

        BufferedReader br = Files.newBufferedReader(path);
        String line;

        try {
            // Read the header data
            if((line = br.readLine()) != null) {
                // Split this tab-delimited line into an array
                String[] header = line.split("\t");
                boneCount = Integer.parseInt(header[0]);
            }

            // Read each bone
            for(int i = 0; i < boneCount && ((line = br.readLine()) != null); i++) {
                String[] bone = line.split("\t");

                // First string is the bone's name
                boneNamesList.add(bone[0]);
                // Next float is the bone's length
                boneLengthsList.add(Float.parseFloat(bone[1]));
                // Next 3 floats is the bone's rest location
                boneRestLocationsList.add(Float.parseFloat(bone[2]));
                boneRestLocationsList.add(Float.parseFloat(bone[3]));
                boneRestLocationsList.add(Float.parseFloat(bone[4]));
                // Next 4 floats is the bone's rest rotation
                boneRestRotationsList.add(Float.parseFloat(bone[5]));
                boneRestRotationsList.add(Float.parseFloat(bone[6]));
                boneRestRotationsList.add(Float.parseFloat(bone[7]));
                boneRestRotationsList.add(Float.parseFloat(bone[8]));
                // Last int is the bone's parent's index
                boneParentsList.add(Integer.parseInt(bone[9]));
            }
        }
        catch (IOException e) {
            throw new RuntimeException(TAG + ": " + e, e);
        }
        finally {
            if(br != null)
                br.close();
        }

        skel.setBoneCount(boneCount);
        skel.setBoneNames(toStringArray(boneNamesList));
        skel.setBoneLengthsBuffer(toFloatBuffer(boneLengthsList));
        skel.setBoneRestLocationsBuffer(toFloatBuffer(boneRestLocationsList));
        skel.setBoneRestRotationsBuffer(toFloatBuffer(boneRestRotationsList));
        skel.setBoneParentsBuffer(toIntBuffer(boneParentsList));

        safeReset();
    }

    private static String[] toStringArray(List<String> list) {
        String[] values = new String[list.size()];
        for (int i = 0; i < values.length; i++)
            values[i] = list.get(i);

        return values;
    }

    private static FloatBuffer toFloatBuffer(List<Float> list) {
        float[] values = new float[list.size()];
        for (int i = 0; i < values.length; i++)
            values[i] = list.get(i);

        return BufferUtil.directFloatBuffer(values);
    }

    private static IntBuffer toIntBuffer(List<Integer> list) {
        int[] values = new int[list.size()];
        for (int i = 0; i < values.length; i++)
            values[i] = list.get(i);

        return BufferUtil.directIntBuffer(values);
    }


    private void safeReset() {
        boneCount = 0;
        boneNamesList.clear();
        boneLengthsList.clear();
        boneRestRotationsList.clear();
        boneRestLocationsList.clear();
        boneParentsList.clear();
    }

    @Override
    public void notifyDispose() {
        boneCount = 0;
        boneNamesList = null;
        boneLengthsList = null;
        boneRestLocationsList = null;
        boneRestRotationsList = null;
        boneParentsList = null;
    }
}
