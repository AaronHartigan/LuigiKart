package ray.physics.JBullet;

import ray.physics.PhysicsConstraint;
import ray.physics.PhysicsObject;
import ray.physics.JBullet.JBulletPhysicsObject;

public abstract class JBulletConstraint implements PhysicsConstraint {
	private int uid;
	private JBulletPhysicsObject bodyA;
	private JBulletPhysicsObject bodyB;
	
	public JBulletConstraint(int uid, JBulletPhysicsObject bodyA, JBulletPhysicsObject bodyB){
		this.uid=uid;
		this.bodyA=bodyA;
		this.bodyB=bodyB;
	}
	@Override
	public PhysicsObject getBodyA() {
		return bodyA;
	}

	@Override
	public PhysicsObject getBodyB() {
		return bodyB;
	}
	@Override
	public int getUID() {
		return uid;
	}

}
