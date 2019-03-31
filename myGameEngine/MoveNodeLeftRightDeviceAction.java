package myGameEngine;

import a3.MyGame;
import myGameEngine.MoveNodeLeftAction;
import myGameEngine.MoveNodeRightAction;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.input.action.Action;
import ray.rage.scene.SceneNode;

public class MoveNodeLeftRightDeviceAction extends AbstractInputAction
{
	private SceneNode node;
	private Action moveLeftAction;
	private Action moveRightAction;
	private float JOYSTICK_DEADZONE = 0.2f;

	public MoveNodeLeftRightDeviceAction(SceneNode n, MyGame g) {
		this.node = n;
		this.moveLeftAction = new MoveNodeLeftAction(node, g);
		this.moveRightAction = new MoveNodeRightAction(node, g);
	}
	
	public void performAction(float time, Event e) {
		if (e.getValue() < -JOYSTICK_DEADZONE) {
			moveLeftAction.performAction(Math.abs(e.getValue()) * time, e);
		}
		else if (e.getValue() > JOYSTICK_DEADZONE) {
			moveRightAction.performAction(Math.abs(e.getValue()) * time, e);
		}
	}
}
