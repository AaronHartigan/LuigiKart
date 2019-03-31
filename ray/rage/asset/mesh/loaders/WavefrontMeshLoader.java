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

package ray.rage.asset.mesh.loaders;

import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;

import ray.rage.asset.mesh.*;
import ray.rage.util.*;

/**
 * A {@link MeshLoader mesh-loader} capable of processing the
 * <a href= "https://en.wikipedia.org/wiki/Wavefront_.obj_file">Wavefront</a>
 * format.
 * <p>
 * The common file extension is <code>.obj</code>.
 *
 * @author Raymond L. Rivera
 *
 */
public final class WavefrontMeshLoader implements MeshLoader {

    private static final String  TAG                    = WavefrontMeshLoader.class.getName();
    private static final Logger  logger                 = Logger.getLogger(TAG);

    private static final int     INVALID                = -1;
    private static final int     TEXCOORDS_PER_TRIANGLE = 2;
    private static final int     VERTS_PER_TRIANGLE     = 3;
    private static final int     SCALARS_PER_VERTEX     = 3;

    private static final String  SPACE_REGEX            = "\\s+";
    private static final String  LETTERS_REGEX          = "^[a-z]+$";

    private static final String  COMMENT                = "#";
    private static final String  VERTEX_COMMAND         = "v ";
    private static final String  TEXTURE_COORD_COMMAND  = "vt";
    private static final String  NORMAL_COMMAND         = "vn";
    private static final String  FACE_COMMAND           = "f ";
    private static final String  MATERIAL_COMMAND       = "mtllib";

    private List<Float>          vertexPositionsList    = new ArrayList<>();
    private List<Float>          vertexTexCoordsList    = new ArrayList<>();
    private List<Float>          vertexNormalsList      = new ArrayList<>();
    private List<Integer>        vertexIndicesList      = new ArrayList<>();

    private Map<Vertex, Integer> vertexIndicesMap       = new HashMap<>();

    private List<int[]>          facePositionIndices    = new ArrayList<>();
    private List<int[]>          faceCoordIndices       = new ArrayList<>();
    private List<int[]>          faceNormalIndices      = new ArrayList<>();

    private String               materialLib            = null;

    @Override
    public void loadAsset(Mesh mesh, Path path) throws IOException, NullPointerException {
        if (mesh == null)
            throw new NullPointerException("Null mesh");

        List<Float> verts = new ArrayList<>();
        List<Float> norms = new ArrayList<>();
        List<Float> texcoords = new ArrayList<>();

        BufferedReader br = Files.newBufferedReader(path);
        String line;
        try {
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0 || line.startsWith(COMMENT))
                    continue;

                if (line.startsWith(VERTEX_COMMAND)) {
                    processVertexPositionCommand(line, verts);
                } else if (line.startsWith(TEXTURE_COORD_COMMAND)) {
                    processVertexTextureCommand(line, texcoords);
                } else if (line.startsWith(NORMAL_COMMAND)) {
                    processVertexNormalCommand(line, norms);
                } else if (line.startsWith(FACE_COMMAND)) {
                    processVertexFaceCommand(line);
                } else if (line.startsWith(MATERIAL_COMMAND)) {
                    materialLib = line.split(" ")[1];
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(TAG + ": " + e, e);
        } finally {
            if (br != null)
                br.close();
        }

        SubMesh sm = mesh.createSubMesh(mesh.getName());

        createVertexBuffers(verts, norms, texcoords);

        sm.setVertexBuffer(toFloatBuffer(vertexPositionsList));
        sm.setNormalBuffer(toFloatBuffer(vertexNormalsList));
        sm.setTextureCoordBuffer(toFloatBuffer(vertexTexCoordsList));
        sm.setIndexBuffer(toIntBuffer(vertexIndicesList));

        if (materialLib != null)
            sm.setMaterialFilename(materialLib);

        safeReset();
    }

    private void processVertexPositionCommand(final String line, List<Float> verts) {
        for (String s : line.split(SPACE_REGEX))
            if (!s.matches(LETTERS_REGEX) && !s.matches(SPACE_REGEX))
                verts.add(Float.valueOf(s));
    }

    private void processVertexTextureCommand(final String line, List<Float> texcoords) {
        for (String s : line.split(SPACE_REGEX))
            if (!s.matches(LETTERS_REGEX) && !s.matches(SPACE_REGEX))
                texcoords.add(Float.valueOf(s));
    }

    private void processVertexNormalCommand(final String line, List<Float> norms) {
        for (String s : line.split(SPACE_REGEX))
            if (!s.matches(LETTERS_REGEX) && !s.matches(SPACE_REGEX))
                norms.add(Float.valueOf(s));
    }

    private void processVertexFaceCommand(final String line) {
        // add default/dummy texture coords if they're missing (they're optional
        // in the file)
        String[] vertexTokens = line.replaceAll("//", "/0/").split(SPACE_REGEX);

        // skip the command at idx 0
        vertexTokens = Arrays.copyOfRange(vertexTokens, 1, vertexTokens.length);

        int[] v = new int[vertexTokens.length];
        int[] vt = new int[vertexTokens.length];
        int[] vn = new int[vertexTokens.length];

        // each token refers to a single vertex with in the "pos/tex/norm"
        // format
        for (int i = 0; i < vertexTokens.length; i++) {
            String[] values = vertexTokens[i].split("/");
            try {
                v[i] = Integer.valueOf(values[0]) - 1;
                vt[i] = values.length > 1 ? Integer.valueOf(values[1]) - 1 : INVALID;
                vn[i] = values.length > 2 ? Integer.valueOf(values[2]) - 1 : INVALID;
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                logger.log(Level.FINER, e.getMessage());
            }
        }

        if (vertexTokens.length > VERTS_PER_TRIANGLE) {
            facePositionIndices.add(triangularize(v));
            faceCoordIndices.add(triangularize(vt));
            faceNormalIndices.add(triangularize(vn));
        } else {
            facePositionIndices.add(v);
            faceCoordIndices.add(vt);
            faceNormalIndices.add(vn);
        }
    }

    // method based on sage.model.loader.OBJLoader
    private void createVertexBuffers(List<Float> vertices, List<Float> normals, List<Float> texcoords) {
        float x, y, z; // positions
        float u, v, n; // normals
        float s, t; // textures

        for (int face = 0; face < facePositionIndices.size(); face++) {
            for (int tri = 0; tri < (facePositionIndices.get(face).length) / VERTS_PER_TRIANGLE; tri++) {
                for (int vert = 0; vert < VERTS_PER_TRIANGLE; vert++) {
                    int vertPosIdx = facePositionIndices.get(face)[tri * VERTS_PER_TRIANGLE + vert];
                    int vertOffset = vertPosIdx * VERTS_PER_TRIANGLE;

                    x = vertices.get(vertOffset);
                    y = vertices.get(vertOffset + 1);
                    z = vertices.get(vertOffset + 2);

                    // check if any valid texture coordinates were really added
                    // or if they need to be ignored; they may've been added as
                    // default/dummy values if they got replaced from "//" to
                    // "/0/"
                    if (texcoords.size() > 0) {
                        int texIndex = faceCoordIndices.get(face)[tri * VERTS_PER_TRIANGLE + vert];
                        int texOffset = texIndex * TEXCOORDS_PER_TRIANGLE;

                        s = texcoords.get(texOffset);
                        t = texcoords.get(texOffset + 1);
                    } else {
                        s = t = INVALID;
                    }

                    if (normals.size() > 0) {
                        int normIndex = faceNormalIndices.get(face)[tri * VERTS_PER_TRIANGLE + vert];
                        int normOffset = normIndex * VERTS_PER_TRIANGLE;
                        u = normals.get(normOffset);
                        v = normals.get(normOffset + 1);
                        n = normals.get(normOffset + 2);
                    } else {
                        // 0 = illegal for normals
                        u = v = n = 0;
                    }
                    addVertexIndex(new Vertex(x, y, z, u, v, n, s, t));
                }
            }
        }
    }

    private void addVertexIndex(Vertex vert) {
        if (vertexIndicesMap.containsKey(vert)) {
            // avoid duplicate vertex indices
            vertexIndicesList.add(vertexIndicesMap.get(vert));
            return;
        }

        vertexPositionsList.add(vert.x);
        vertexPositionsList.add(vert.y);
        vertexPositionsList.add(vert.z);

        vertexTexCoordsList.add(vert.s);
        vertexTexCoordsList.add(vert.t);

        vertexNormalsList.add(vert.u);
        vertexNormalsList.add(vert.v);
        vertexNormalsList.add(vert.n);

        int idx = vertexPositionsList.size() / SCALARS_PER_VERTEX - 1;
        vertexIndicesMap.put(vert, idx);
        vertexIndicesList.add(idx);
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

    // code based on sage.model.loader.OBJLoader
    private static int[] triangularize(int[] indices) {
        // the first 2 indices are never re-used in other triangles, so
        // they're not counted
        int triangleIndexCount = indices.length - 2;
        int[] triangles = new int[triangleIndexCount * 3];

        // copy the triangle specified by the first set of vertex indices
        triangles[0] = indices[0];
        triangles[1] = indices[1];
        triangles[2] = indices[2];
        int lastUsed = 2;

        // construct additional triangles as needed
        for (int i = 1; i < triangleIndexCount; i++) {
            // 1. use the triangle's 1st vertex index,
            // 2. re-use the previous triangle's vertex index
            // 3. set the next vertex index before repeating step 1
            triangles[i * 3] = indices[0];
            triangles[i * 3 + 1] = indices[lastUsed];
            triangles[i * 3 + 2] = indices[lastUsed + 1];
            lastUsed++;
        }
        return triangles;
    }

    private void safeReset() {
        vertexPositionsList.clear();
        vertexTexCoordsList.clear();
        vertexNormalsList.clear();
        vertexIndicesList.clear();
        vertexIndicesMap.clear();
        facePositionIndices.clear();
        faceCoordIndices.clear();
        faceNormalIndices.clear();
        materialLib = null;
    }

    @Override
    public void notifyDispose() {
        vertexPositionsList = null;
        vertexTexCoordsList = null;
        vertexNormalsList = null;
        vertexIndicesList = null;
        vertexIndicesMap = null;
        facePositionIndices = null;
        faceCoordIndices = null;
        faceNormalIndices = null;
        materialLib = null;
    }

    /**
     * Utility class to avoid duplicating vertex index data. Based on
     * <code>sage.model.loader.OBJLoader.Vertex</code>
     *
     * @author Raymond L. Rivera
     *
     */
    private class Vertex {

        private float x, y, z;
        private float u, v, n;
        private float s, t;

        public Vertex(float x, float y, float z, float u, float v, float n, float s, float t) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.u = u;
            this.v = v;
            this.n = n;
            this.s = s;
            this.t = t;
        }

        @Override
        public int hashCode() {
            int prime = 31;
            int result = 7;
            result = prime * result + Float.floatToIntBits(x);
            result = prime * result + Float.floatToIntBits(y);
            result = prime * result + Float.floatToIntBits(z);
            result = prime * result + Float.floatToIntBits(u);
            result = prime * result + Float.floatToIntBits(v);
            result = prime * result + Float.floatToIntBits(n);
            result = prime * result + Float.floatToIntBits(s);
            result = prime * result + Float.floatToIntBits(t);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Vertex))
                return false;

            Vertex other = (Vertex) obj;
            if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
                return false;
            if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
                return false;
            if (Float.floatToIntBits(z) != Float.floatToIntBits(other.z))
                return false;
            if (Float.floatToIntBits(u) != Float.floatToIntBits(other.u))
                return false;
            if (Float.floatToIntBits(v) != Float.floatToIntBits(other.v))
                return false;
            if (Float.floatToIntBits(n) != Float.floatToIntBits(other.n))
                return false;
            if (Float.floatToIntBits(s) != Float.floatToIntBits(other.s))
                return false;
            if (Float.floatToIntBits(t) != Float.floatToIntBits(other.t))
                return false;

            return true;
        }

    }

}
