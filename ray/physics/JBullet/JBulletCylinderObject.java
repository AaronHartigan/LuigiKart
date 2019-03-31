package ray.physics.JBullet;

import com.bulletphysics.collision.shapes.CylinderShape;

import ray.physics.JBullet.JBulletPhysicsObject;

import javax.vecmath.Vector3f;

public class JBulletCylinderObject extends JBulletPhysicsObject {
	
    private float[] halfExtents;

    public JBulletCylinderObject(int uid, float mass, double[] transform, float[] halfExtents) {

        super(uid, mass, transform, new CylinderShape(new Vector3f(halfExtents)));
        this.halfExtents = halfExtents;
    }

}
