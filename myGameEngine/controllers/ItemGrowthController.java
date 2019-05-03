package myGameEngine.controllers;

import ray.rage.scene.Node;
import ray.rage.scene.controllers.AbstractController;

public class ItemGrowthController extends AbstractController {
	private float animationTime = 400f;
	private float totalTime = 0f;
	private final float BANANA_SCALE = 0.4f;

	@Override
	protected void updateImpl(float elapsedTimeMillis) {
		totalTime += elapsedTimeMillis;
		
		if (totalTime > animationTime) {
			this.removeAllNodes();
			this.setShouldDelete(true);
			return;
		}
		float percentageDone = (totalTime / animationTime);
		float scale = 0.0001f;
		if (percentageDone > 0f) {
			scale = percentageDone;
		}

		for (Node n : super.controlledNodesList) {
            n.setLocalScale(scale * BANANA_SCALE, scale * BANANA_SCALE, scale * BANANA_SCALE);
		}
	}
}
