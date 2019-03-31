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

package ray.rage.asset.mesh.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

import ray.rage.asset.mesh.Mesh;
import ray.rage.asset.mesh.MeshLoader;
import ray.rage.asset.mesh.SubMesh;
import ray.rage.asset.mesh.loaders.SkeletalMeshLoader;
import ray.rage.util.BufferUtil;

/**
 * A {@link MeshLoader mesh-loader} capable of processing a custom
 * skeletal animation mesh format created specifically for RAGE.
 *  More info about this format can be found in the .rkm Blender 3D file format exporters.

 *
 * The established file extension is <code>.rkm</code>
 * <p>
 * The common file extension is <code>.obj</code>.
 *
 * @author Luis Gutierrez
 *
 */
public final class SkeletalMeshLoader implements MeshLoader {

    private static final String TAG = SkeletalMeshLoader.class.getName();
    private static final Logger logger = Logger.getLogger(TAG);


    private List<Float> vertexPositionsList = new ArrayList<>();
    private List<Float> vertexTexCoordsList = new ArrayList<>();
    private List<Float> vertexBoneWeightsList = new ArrayList<>();
    private List<Float> vertexBoneIndicesList = new ArrayList<>();
    private List<Float> vertexNormalsList = new ArrayList<>();

    private List<Integer> vertexIndicesList = new ArrayList<>();

    //private String materialLib = null;

    @Override
    public void loadAsset(Mesh mesh, Path path) throws IOException, NullPointerException {
        if (mesh == null)
            throw new NullPointerException("Null mesh");

        BufferedReader br = Files.newBufferedReader(path);
        String line;

        int vertCount = 0;
        int triCount = 0;
        int boneCount = 0;

        try {
            // Read the header data
            if((line = br.readLine()) != null) {
                // Split this tab-delimited line into an array
                String[] header = line.split("\t");
                vertCount = Integer.parseInt(header[0]);
                triCount = Integer.parseInt(header[1]);
                boneCount = Integer.parseInt(header[2]);
            }

            // Read the vertices
            for(int i = 0; i < vertCount && ((line = br.readLine()) != null); i++) {
                String[] vert = line.split("\t");

                // First 3 floats are positions
                vertexPositionsList.add(Float.parseFloat(vert[0]));
                vertexPositionsList.add(Float.parseFloat(vert[1]));
                vertexPositionsList.add(Float.parseFloat(vert[2]));

                // Next 2 floats are texture coordinates
                vertexTexCoordsList.add(Float.parseFloat(vert[3]));
                vertexTexCoordsList.add(Float.parseFloat(vert[4]));

                // Next 6 floats are bone weights and bone indices for 3 bones
                vertexBoneWeightsList.add(Float.parseFloat(vert[5]));
                vertexBoneIndicesList.add(Float.parseFloat(vert[6]));

                vertexBoneWeightsList.add(Float.parseFloat(vert[7]));
                vertexBoneIndicesList.add(Float.parseFloat(vert[8]));

                vertexBoneWeightsList.add(Float.parseFloat(vert[9]));
                vertexBoneIndicesList.add(Float.parseFloat(vert[10]));

                // Next 3 floats are the normal vector
                vertexNormalsList.add(Float.parseFloat(vert[11]));
                vertexNormalsList.add(Float.parseFloat(vert[12]));
                vertexNormalsList.add(Float.parseFloat(vert[13]));

                // Next 3 floats are the tangent vector
                //RAGE does not currently support vertex tangent vectors
                // Next 3 floats are the binormal vector
                //RAGE does not currently support vertex binormal vectors
            }

            // Read the triangles
            for(int i = 0; i < triCount && ((line = br.readLine()) != null); i++) {
                String[] tri_verts = line.split("\t");
                // Each line contains 3 floats
                vertexIndicesList.add(Integer.parseInt(tri_verts[0]));
                vertexIndicesList.add(Integer.parseInt(tri_verts[1]));
                vertexIndicesList.add(Integer.parseInt(tri_verts[2]));
            }
        }
        catch (IOException e) {
            throw new RuntimeException(TAG + ": " + e, e);
        }
        finally {
            if(br != null)
                br.close();
        }

        SubMesh sm = mesh.createSubMesh(mesh.getName());

        sm.setBoneCount(boneCount);
        sm.setVertexBuffer(toFloatBuffer(vertexPositionsList));
        sm.setTextureCoordBuffer(toFloatBuffer(vertexTexCoordsList));
        sm.setBoneIndexBuffer(toFloatBuffer(vertexBoneIndicesList));
        sm.setBoneWeightBuffer(toFloatBuffer(vertexBoneWeightsList));
        sm.setNormalBuffer(toFloatBuffer(vertexNormalsList));
        sm.setIndexBuffer(toIntBuffer(vertexIndicesList));


        //if (materialLib != null)
        //    sm.setMaterialFilename(materialLib);

        safeReset();
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
        vertexPositionsList.clear();
        vertexTexCoordsList.clear();
        vertexBoneWeightsList.clear();
        vertexBoneIndicesList.clear();
        vertexNormalsList.clear();

        vertexIndicesList.clear();
        //materialLib = null;
    }

    @Override
    public void notifyDispose() {
        vertexPositionsList = null;
        vertexTexCoordsList = null;
        vertexBoneWeightsList = null;
        vertexBoneIndicesList = null;
        vertexNormalsList = null;

        vertexIndicesList = null;
        //materialLib = null;
    }
}
