package myGameEngine;

import a3.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.input.action.Action;
import ray.rage.scene.SceneNode;

public class RotateNodeUpDownDeviceAction extends AbstractInputAction
{
	private SceneNode node;
	private Action rotateNodeUpAction;
	private Action rotateNodeDownAction;
	private float JOYSTICK_DEADZONE = 0.2f;
	
	public RotateNodeUpDownDeviceAction(SceneNode n, MyGame g) {
		this.node = n;
		this.rotateNodeUpAction = new RotateNodeUpAction(node, g);
		this.rotateNodeDownAction = new RotateNodeDownAction(node, g);
	}
	
	public void performAction(float time, Event e) {
		if (e.getValue() < -JOYSTICK_DEADZONE) {
			rotateNodeUpAction.performAction(Math.abs(e.getValue()) * time, e);
		}
		else if (e.getValue() > JOYSTICK_DEADZONE) {
			rotateNodeDownAction.performAction(Math.abs(e.getValue()) * time, e);
		}
	}
}
