package myGameEngine.controllers;

import a3.MyGame;
import ray.rage.scene.Node;
import ray.rage.scene.SceneNode;
import ray.rage.scene.controllers.AbstractController;
import ray.rml.Vector3;

public class BananaDeathAnimationController extends AbstractController {
	private MyGame g;
	private SceneNode banana;
	private float originalHeight;
	private Vector3 hitForce;
	private float animationTime = 600f;
	private float totalTime = 0f;
	private final float BANANA_SCALE = 0.4f;
	
	
	public BananaDeathAnimationController(MyGame g, SceneNode banana, Vector3 hitForce) {
		this.g = g;
		this.banana = banana;
		this.originalHeight = banana.getLocalPosition().y();
		this.hitForce = hitForce;
	}

	@Override
	protected void updateImpl(float elapsedTimeMillis) {
		totalTime += elapsedTimeMillis;
		float elapsedSec = elapsedTimeMillis / 1000;
		
		if (totalTime > animationTime) {
			this.removeAllNodes();
			this.setShouldDelete(true);
			g.getEngine().getSceneManager().destroySceneNode(banana);
		}
		
		float extraHeight = (float) Math.sin((totalTime / animationTime) * Math.PI) * 2f;
		float percentageDone = (totalTime / animationTime);
		float scale = 1f;
		if (percentageDone > 0.5f) {
			scale = (-8f / 5f) * percentageDone + 1.8f;
		}

		for (Node n : super.controlledNodesList) {
			Vector3 lp = n.getLocalPosition();
            n.setLocalPosition(
            	lp.x() + hitForce.x() * elapsedSec,
            	originalHeight + extraHeight,
            	lp.z() + hitForce.z() * elapsedSec
            );
            n.setLocalScale(scale * BANANA_SCALE, scale * BANANA_SCALE, scale * BANANA_SCALE);
		}
	}
}
