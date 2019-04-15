package myGameEngine;

import a3.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rml.Degreef;
import ray.rml.Matrix3;
import ray.rml.Matrix3f;
import ray.rml.Vector3f;

public class RotateNodeRightAction extends AbstractInputAction
{
	private SceneNode n;
	private MyGame g;
	
	public RotateNodeRightAction(SceneNode n, MyGame g) { 
		this.n = n;
		this.g = g;
	}
	
	public void performAction(float time, Event e) {
		float toRotate = time * g.getRotateSpeed();
		
		Matrix3 oldRot = n.getLocalRotation();
		Matrix3 rotM = Matrix3f.createRotationFrom(Degreef.createFrom(-toRotate), Vector3f.createFrom(0,1,0));
		n.setLocalRotation(oldRot.mult(rotM));
	}
}