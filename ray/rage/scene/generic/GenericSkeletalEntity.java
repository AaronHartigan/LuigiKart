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

package ray.rage.scene.generic;

import static ray.rage.scene.SkeletalEntity.EndType.NONE;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ray.rage.asset.animation.Animation;
import ray.rage.asset.material.Material;
import ray.rage.asset.mesh.Mesh;
import ray.rage.asset.mesh.SubMesh;
import ray.rage.asset.skeleton.Skeleton;
import ray.rage.rendersystem.Renderable;
import ray.rage.rendersystem.shader.GpuShaderProgram;
import ray.rage.rendersystem.states.RenderState;
import ray.rage.scene.Entity;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SkeletalEntity;
import ray.rage.scene.SubEntity;
import ray.rage.scene.generic.AbstractGenericSceneObject;
import ray.rage.scene.generic.GenericSubEntity;
import ray.rml.*;

/**
 * A GenericSkeletalEntity implementation of the {@link SkeletalEntity} interface.
 *
 * @author Luis Gutierrez
 *
 */
final class GenericSkeletalEntity extends AbstractGenericSceneObject implements SkeletalEntity {

    private Mesh mesh;
    private Skeleton skeleton;
    private List<SubEntity> subEntityList;

    private HashMap<String, Animation> animationsList = new HashMap<>();

    // Storing a reference to the current animation
    private Animation curAnimation = null;
    // The current frame of the animation
    private int curAnimFrame = -1;
    // The current frame of the animation that supports lerping between frames
    private float curLerpedAnimFrame = -1;
    // The speed of the animation, negative is backwards
    private float curAnimSpeed = 1.0f;
    // The current animation end type
    private EndType curAnimEndtype = NONE;
    // The total number of times to perform the end type (i.e. how many times do we loop?) (0 for forever)
    private int curAnimEndTypeTotal = -1;
    // The current total number of times we have performed this endtype
    private int curAnimEndTypeCount = 0;
    // Whether or not the animation is currently paused
    private boolean curAnimPaused = false;

    // Current Skeleton Pose Skinning Matrices
    // This array holds a list of 4x4 matrices
    // These matrices merely need to be multiplied by the vertices to yield their skinned model-space locations
    // This instance of this list of matrices is updated every frame
    private Matrix4[] curSkinMatrices;

    // Holds the inverse-transpose of the above curSkinMatrices matrices
    // This array is used for transforming the vertex normals
    // This instance of this list is also updated every frame
    private Matrix3[] curSkinMatricesIT;

    /**
     * Creates a new {@link Entity entity} with the given parent
     * {@link SceneManager manager}, name, and {@link Mesh mesh}.
     *
     * @param manager
     *            The parent {@link SceneManager manager}.
     * @param name
     *            The name for <code>this</code> {@link Entity entity}.
     * @param m
     *            The {@link Mesh mesh} <code>this</code> {@link Entity entity}
     *            is based on.
     * @throws NullPointerException
     *             If any of the arguments is <code>null</code>.
     * @throws IllegalArgumentException
     *             If the name is empty.
     * @throws RuntimeException
     *             If the {@link Mesh mesh} has no {@link SubMesh sub-meshes}.
     */
    GenericSkeletalEntity(SceneManager manager, String name, Mesh m, Skeleton s) {
        super(manager, name);
        if (m == null)
            throw new NullPointerException("Null " + Mesh.class.getSimpleName());
        if (m.getSubMeshCount() == 0)
            throw new RuntimeException(Mesh.class.getSimpleName() + " has 0 " + SubMesh.class.getSimpleName());


        // Checking to make sure the mesh and skeleton are compatible.
        if(m.getSubMesh(0).getBoneCount() != s.getBoneCount())
            throw new RuntimeException(SubMesh.class.getSimpleName() + " and " + Skeleton.class.getSimpleName() + " have different bone counts");

        mesh = m;
        skeleton = s;

        subEntityList = new ArrayList<>(mesh.getSubMeshCount());

        // Constructing the bone matrices array
        int boneCount = skeleton.getBoneCount();
        curSkinMatrices = new Matrix4[boneCount];
        curSkinMatricesIT = new Matrix3[boneCount];

        // Defaulting these matrices to an identity matrix
        for(int i = 0; i < curSkinMatrices.length; i++) {
            curSkinMatrices[i] = Matrix4f.createIdentityMatrix();
        }
        
        for(int i = 0; i < curSkinMatricesIT.length; i++) {
            curSkinMatricesIT[i] = Matrix3f.createIdentityMatrix();
        }

        for (SubMesh subMesh : mesh.getSubMeshes()) {
            subEntityList.add(new GenericSubEntity(this, subMesh));
        }

        // We only need to assign these once upon creation.
        // These arrays are reused with each update.
        for(SubEntity se : getSubEntities()) {
            se.setPoseSkinMatrices(curSkinMatrices);
            se.setPoseSkinMatricesIT(curSkinMatricesIT);
        }
    }

    public void update() {
        updateAnimation();

        // Updating current pose matrices based on current Frame Data
        updateCurrentPoseMatrices();
    }

    // This method calculates the skinning matrices for the current animation pose.
    private void updateCurrentPoseMatrices() {
        // Calculating the Vertex Skinning Matrices:

        for(int i = 0; i < curSkinMatrices.length; i++) {
            Matrix4 mat;
            // 1) get inverse of bone's local-space to model space
            mat = getBoneLocal2ModelSpaceTransform(i).inverse();

            int curBone = i;
            while(curBone != -1) {
                // 2) transforming based on bone's current frame transform
                mat = getBoneCurLocalTransform(curBone).mult(mat);
                // 3) transforming based on its position relative to its parent
                mat = getBoneRestTransformRel2Parent(curBone).mult(mat);

                curBone = skeleton.getBoneParentIndex(curBone);
            }
            curSkinMatrices[i] = mat;

        }

        // Calculating the IT pose
        for(int i = 0; i < curSkinMatrices.length; i++) {
            curSkinMatricesIT[i] = curSkinMatrices[i].inverse().transpose().toMatrix3();
        }
    }

    //====================================================
    //          Useful Transformations Matrix Maths
    //====================================================

    // Returns the ith bone's local space to model space (when all bones are in rest pose)
    public Matrix4 getBoneLocal2ModelSpaceTransform(int i) {
        Matrix4 mat = Matrix4f.createIdentityMatrix();

        int curBone = i;
        // Iterate until we reach root bone.
        while(curBone != -1) {
            mat = getBoneRestTransformRel2Parent(curBone).mult(mat);

            // Move onto curBone's parent.
            curBone = skeleton.getBoneParentIndex(curBone);
        }

        return mat;
    }

    // Returns the ith bone's current local transform for the current animation frame
    public Matrix4 getBoneCurLocalTransform(int i)
    {
        if(curAnimation == null)
            return Matrix4f.createIdentityMatrix();

        //TODO: use curLerpedAnimFrame to interpolate these three variables

        Vector3 scale = curAnimation.getFrameBoneScl(curAnimFrame,i);
        Quaternion rot = curAnimation.getFrameBoneRot(curAnimFrame, i);
        Vector3 loc = curAnimation.getFrameBoneLoc(curAnimFrame, i);

        Matrix4 mat;
        // 1) Apply scale 1st
        mat = Matrix4f.createScalingFrom(scale);
        // 2) Apply rotation 2nd
        mat = Matrix4f.createRotationFrom(rot.angle(),getQuatAxis(rot)).mult(mat);
        // 3) Apply translation 3rd
        mat = Matrix4f.createTranslationFrom(loc).mult(mat);
        return mat;
    }

    // Returns the Quaternion's axis, if is not the identity quaternion, else (1,0,0)
    // This is safer than calling q.axis(), as q.axis() ALWAYS attempts to normalize the axis vector.
    // However, if the Quaternion is the identity quaternion (1,0,0,0), it will attempt to normalize a zero-vector
    // Throwing an arithmetic exception.
    // If an arithmetic exception is thrown, I can return an arbitrary axis (namely, (1,0,0))
    // because an identity Quaternion is no rotation about any axis
    // ... and no rotation about (1,0,0) is identical to no rotation about any axis.
    private Vector3 getQuatAxis(Quaternion q)
    {
        Vector3 axis;
        try {
            axis = q.axis();
        }
        catch(ArithmeticException e) {
            axis = Vector3f.createFrom(1,0,0);
        }

        return axis;
    }

    // Returns the ith bone's rest transform relative to its parent-space
    private Matrix4 getBoneRestTransformRel2Parent(int i)
    {
        Matrix4 mat = Matrix4f.createIdentityMatrix();


        // 1) First apply ith bone's rest rotation
        Quaternion restRot = skeleton.getBoneRestRot(i);
        mat = Matrix4f.createRotationFrom(restRot.angle(),getQuatAxis(restRot)).mult(mat);

        // 2) Then apply ith bone's rest location
        Vector3 restLoc = skeleton.getBoneRestLoc(i);
        mat = Matrix4f.createTranslationFrom(restLoc).mult(mat);

        // 3) Then apply parent bone's length in vertical direction (is the y-axis correct?)
        // Get the ith bone's parent's index
        int parentIndex = skeleton.getBoneParentIndex(i);
        //If parent index is -1, this bone is the root bone
        if(parentIndex == -1)
            return mat;

        float parentBoneLength = skeleton.getBoneLength(parentIndex);

        // For the bone's local space, y is up along the bone
        //mat = Matrix4f.createTranslationFrom(0,0,-parentBoneLength).mult(mat);
        mat = Matrix4f.createTranslationFrom(0,parentBoneLength,0).mult(mat);


        return mat;
    }

    //====================================================
    //              Animation Update Logic
    //====================================================


    private void updateAnimation()
    {
        if(curAnimation != null && !curAnimPaused && curAnimSpeed != 0.0f) {
            curLerpedAnimFrame += curAnimSpeed;
            curAnimFrame = Math.round(curLerpedAnimFrame);

            // Check if the animation is over
            if(curAnimFrame >= curAnimation.getFrameCount() || curAnimFrame < 0) {
                handleAnimationEnd();
            }
        }
    }

    private void handleAnimationEnd()
    {
        curAnimEndTypeCount++;
        // Check if we have exceeded the number of times to perform the end type
        // 0 is loop forever
        if(curAnimEndTypeTotal != 0) {
            if(curAnimEndTypeCount > curAnimEndTypeTotal) {
                stopAnimation();
                return;
            }
        }


        switch(curAnimEndtype)
        {
            case NONE:
            // Completely stop the animation
            case STOP:
                stopAnimation();
                break;
            // Freeze model at the last frame
            case PAUSE:
                if(curAnimSpeed > 0.0) {
                    curAnimFrame = curAnimation.getFrameCount() - 1;
                    curLerpedAnimFrame = curAnimation.getFrameCount() - 1;
                }
                else if(curAnimSpeed < 0.0) {
                    curAnimFrame = 0;
                    curLerpedAnimFrame = 0;
                }
                curAnimSpeed = 0.0f;
                break;
            // Restart the animation at the opposite frame
            case LOOP:
                if(curAnimSpeed > 0.0) {
                    curAnimFrame = 0;
                    curLerpedAnimFrame = 0;
                }
                else if(curAnimSpeed < 0.0) {
                    curAnimFrame = curAnimation.getFrameCount() - 1;
                    curLerpedAnimFrame = curAnimation.getFrameCount() - 1;
                }
                break;
            // Play the animation backwards
            case PINGPONG:
                if(curAnimSpeed > 0.0) {
                    curAnimFrame = curAnimation.getFrameCount() - 2;
                    curLerpedAnimFrame = curAnimation.getFrameCount() - 1;
                    curAnimSpeed *= -1f;
                }
                else if(curAnimSpeed < 0.0) {
                    curAnimFrame = 1;
                    curLerpedAnimFrame = 1;
                    curAnimSpeed *= -1f;
                }
                break;
        }
    }

    // animName: the name of the animation to play
    // animSpeed: the speed at which to play the animation
    //         animSpeed = 1.0 for regular speed
    //         0 < animSpeed < 1.0 for slow motion
    //         animSpeed < 0 for backwards, etc...
    // endtype: What to do when the animation is over
    //        NONE/STOP: stops the animation, revert to default pose
    //        PAUSE: freezes the pose at the last frame of the animation
    //        LOOP: starts the animation from the beginning endTypeCount number of times
    //        PINGPONG: plays the animation backwards back and forth endTypeCount number of times
    public void playAnimation(String animName, float animSpeed, EndType endType, int endTypeCount)
    {
        Animation anim = animationsList.get(animName);

        // If the animation is not found, return
        if(anim == null)
            return;

        curAnimation = anim;
        curLerpedAnimFrame = 0;
        curAnimFrame = 0;
        curAnimEndTypeTotal = endTypeCount;
        curAnimEndTypeCount = 0;
        curLerpedAnimFrame = 0;
        curAnimSpeed = animSpeed;
        curAnimEndtype = endType;

        // If speed is negative, play the animation in reverse
        if(curAnimSpeed < 0) {
            curAnimFrame = anim.getFrameCount() - 1;
            curLerpedAnimFrame = anim.getFrameCount() - 1;
        }

        // Play the anim
        curAnimPaused = false;
    }

    public void pauseAnimation() {
        curAnimPaused = true;
    }

    public void stopAnimation() {
        curAnimation = null;
        curAnimEndtype = NONE;
        curAnimFrame = -1;
        curLerpedAnimFrame = -1;
        curAnimPaused = false;
        curAnimSpeed = 1.0f;
        curAnimEndTypeCount = 0;
        curAnimEndTypeTotal = 0;
    }


    //====================================================
    //              Animation Management Logic
    //====================================================

    // Loads an animation given a data path
    public void loadAnimation(String animName, String animationPath) throws IOException {
        if(animationPath.isEmpty())
            throw new IllegalArgumentException("animationPath is empty");
        if(animName.isEmpty())
            throw new IllegalArgumentException("animName is empty");


        Animation anim = this.getManager().getAnimationManager().getAsset(Paths.get(animationPath));

        if(anim == null)
            return;

        // Make sure the animation and skeleton are compatible
        if(anim.getBoneCount() != this.skeleton.getBoneCount())
            throw new RuntimeException(Animation.class.getSimpleName() + " and " + Skeleton.class.getSimpleName() + " have different bone counts");


        // Store the animation for the given name
        animationsList.put(animName, anim);
    }

    public void unloadAnimation(String animName) {
        if(animName.isEmpty())
            throw new IllegalArgumentException("animName is empty");

        if(!animationsList.containsKey(animName))
            return;

        animationsList.remove(animName);
    }

    //====================================================
    //              Entity Specific Methods
    //====================================================
    @Override
    public Mesh getMesh() {
        return mesh;
    }

    @Override
    public Iterable<SubEntity> getSubEntities() {
        return subEntityList;
    }

    @Override
    public SubEntity getSubEntity(int idx) {
        return subEntityList.get(idx);
    }

    @Override
    public int getSubEntityCount() {
        return subEntityList.size();
    }

    @Override
    public void setMaterial(Material mat) {
        for (SubEntity se : subEntityList)
            se.setMaterial(mat);
    }

    @Override
    public void setGpuShaderProgram(GpuShaderProgram prog) {
        for (SubEntity se : subEntityList)
            se.setGpuShaderProgram(prog);
    }

    @Override
    public void setPrimitive(Renderable.Primitive prim) {
        for (SubEntity se : subEntityList)
            se.setPrimitive(prim);
    }

    @Override
    public void visitSubEntities(SubEntity.Visitor visitor) {
        for (SubEntity se : subEntityList)
            visitor.visit(se);
    }

    @Override
    public void setRenderState(RenderState rs) {
        for (SubEntity se : subEntityList)
            se.setRenderState(rs);
    }

    @Override
    public void notifyDispose() {

        for(String animName : animationsList.keySet()) {
            animationsList.replace(animName,null);
        }
        animationsList = null;

        // meshes/sub-meshes are shared assets and may be in use by multiple
        // entities/sub-entities respectively, so do NOT destroy/dispose
        // mesh/sub-mesh instances here; the mesh manager takes care of those
        mesh = null;
        skeleton = null;
        animationsList.clear();
        animationsList = null;

        // we DO dispose of the sub-entities which are owned by this entity;
        // these include render states assigned to the sub-entities,
        // some of which may include communication with lower level systems
        // such as GPU texture buffers, depending on implementations, and
        // require clean-up
        for (SubEntity se : subEntityList)
            se.notifyDispose();

        subEntityList.clear();
        subEntityList = null;

        super.notifyDispose();
    }

    @Override
    public void setDepthShaderProgram(GpuShaderProgram prog) {
        for (SubEntity se : subEntityList)
            se.setDepthShaderProgram(prog);
    }

	@Override
	public void setCanReceiveShadows(boolean canReceiveShadows) {
		for (SubEntity se : subEntityList)
            se.setCanReceiveShadows(canReceiveShadows);
	}

}
