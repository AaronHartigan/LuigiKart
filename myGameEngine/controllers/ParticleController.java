package myGameEngine.controllers;

import a3.MyGame;
import ray.rage.scene.Node;
import ray.rage.scene.SceneNode;
import ray.rage.scene.controllers.AbstractController;
import ray.rml.Degreef;
import ray.rml.Vector3;

public class ParticleController extends AbstractController {
	private MyGame g;
	private SceneNode particle;
	private float originalHeight;
	private Vector3 hitForce;
	private float animationTime = 500f;
	private float totalTime = 0f;
	private float initialRoll = (float) Math.random() * 180f - 90;
	private float initialPitch = (float) Math.random() * 180f - 90;
	private float roll = (float) Math.random() * 20;
	private float pitch = (float) Math.random() * 20;
	private float xOffset = (float) (Math.random() - 0.5f) * 2;
	private float yOffset = (float) (Math.random() - 0.5f) * 2;
	private float zOffset = (float) (Math.random() - 0.5f) * 2;
	private float heightOffset = (float) Math.random() + 0.7f;
	private float xForce = (float) (Math.random() * 10) - 5f;
	private float yForce = (float) (Math.random() * 5) - 2.5f;
	private float zForce = (float) (Math.random() * 10) - 5f;
	private float size = (float) ((Math.random() * 0.9f) + 0.1f) / 6;
	
	public ParticleController(MyGame g, SceneNode particle, Vector3 hitForce) {
		this.g = g;
		this.particle = particle;
		this.originalHeight = particle.getLocalPosition().y();
		this.hitForce = hitForce;
		particle.translate(xOffset, yOffset, zOffset);
		particle.pitch(Degreef.createFrom(initialPitch));
		particle.roll(Degreef.createFrom(initialRoll));
	}

	@Override
	protected void updateImpl(float elapsedTimeMillis) {
		totalTime += elapsedTimeMillis;
		float elapsedSec = elapsedTimeMillis / 1000;
		
		if (totalTime > animationTime) {
			this.removeAllNodes();
			this.setShouldDelete(true);
			g.getEngine().getSceneManager().destroyEntity(particle.getName());
			g.getEngine().getSceneManager().destroySceneNode(particle);
		}
		
		particle.pitch(Degreef.createFrom(pitch));
		particle.roll(Degreef.createFrom(roll));
		
		float extraHeight = (float) Math.sin((totalTime / animationTime) * Math.PI) * 1.5f * heightOffset;
		float percentageDone = (totalTime / animationTime);
		float scale = size;
		if (percentageDone > 0.5f) {
			scale = ((-8f / 5f) * percentageDone + 1.8f) * size;
		}

		for (Node n : super.controlledNodesList) {
			Vector3 lp = n.getLocalPosition();
            n.setLocalPosition(
            	lp.x() + hitForce.x() * elapsedSec + xForce * elapsedSec,
            	originalHeight + extraHeight + hitForce.y() * elapsedSec + yForce * elapsedSec,
            	lp.z() + hitForce.z() * elapsedSec + zForce * elapsedSec
            );
            n.setLocalScale(scale, scale, scale);
		}
	}
}
