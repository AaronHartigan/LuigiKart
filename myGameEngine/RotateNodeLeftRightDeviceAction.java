package myGameEngine;

import a3.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.input.action.Action;
import ray.rage.scene.SceneNode;

public class RotateNodeLeftRightDeviceAction extends AbstractInputAction
{
	private SceneNode node;
	private Action rotateNodeLeftAction;
	private Action moveNodeBackwardAction;
	private float JOYSTICK_DEADZONE = 0.2f;
	
	public RotateNodeLeftRightDeviceAction(SceneNode n, MyGame g) {
		this.node = n;
		this.rotateNodeLeftAction = new RotateNodeLeftAction(node, g);
		this.moveNodeBackwardAction = new RotateNodeRightAction(node, g);
	}
	
	public void performAction(float time, Event e) {
		if (e.getValue() < -JOYSTICK_DEADZONE) {
			rotateNodeLeftAction.performAction(Math.abs(e.getValue()) * time, e);
		}
		else if (e.getValue() > JOYSTICK_DEADZONE) {
			moveNodeBackwardAction.performAction(Math.abs(e.getValue()) * time, e);
		}
	}
}
