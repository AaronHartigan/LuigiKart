package myGameEngine;

import myGameEngine.MoveNodeForwardAction;
import a3.MyGame;
import myGameEngine.MoveNodeBackwardAction;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.input.action.Action;
import ray.rage.scene.SceneNode;

public class MoveNodeForwardBackwardDeviceAction extends AbstractInputAction
{
	private SceneNode node;
	private Action moveNodeForwardAction;
	private Action moveNodeBackwardAction;
	private float JOYSTICK_DEADZONE = 0.2f;

	public MoveNodeForwardBackwardDeviceAction(SceneNode n, MyGame g) {
		this.node = n;
		this.moveNodeForwardAction = new MoveNodeForwardAction(node, g);
		this.moveNodeBackwardAction = new MoveNodeBackwardAction(node, g);
	}
	
	public void performAction(float time, Event e) {
		if (e.getValue() < -JOYSTICK_DEADZONE) {
			moveNodeForwardAction.performAction(Math.abs(e.getValue()) * time, e);
		}
		else if (e.getValue() > JOYSTICK_DEADZONE) {
			moveNodeBackwardAction.performAction(Math.abs(e.getValue()) * time, e);
		}
	}
}
