package myGameEngine.controllers;

import ray.rage.scene.Node;
import ray.rage.scene.controllers.AbstractController;
import ray.rml.Vector3;

// Has the properties of both rubber and gum
public class BungeeController extends AbstractController {
	private Vector3 originalScale;
	private float originalY;
	private float periodLength = 2000f;
	private float totalTime = 0f;
	private boolean grow = true;
	
	@Override
	public void addNode(Node node) {
		super.addNode(node);
		originalScale = node.getLocalScale();
		originalY = node.getLocalPosition().y();
	}

	@Override
	protected void updateImpl(float elapsedTimeMillis) {
		if (grow) {
			totalTime += elapsedTimeMillis;
		}
		else {
			totalTime -= elapsedTimeMillis;
		}
		
		if (totalTime < 0) {
			grow = true;
			totalTime = 0;
		}
		else if (totalTime > periodLength) {
			grow = false;
			totalTime = periodLength;
		}

		float delta = 1.0f + totalTime / 1000;
		for (Node n : super.controlledNodesList) {
            n.setLocalScale(originalScale.x(), originalScale.y() * delta, originalScale.z());
            Vector3 op = n.getLocalPosition();
            n.setLocalPosition(op.x(), originalY * delta, op.z());
		}
	}
}
