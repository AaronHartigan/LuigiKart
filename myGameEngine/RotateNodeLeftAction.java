package myGameEngine;

import a3.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rml.Degreef;
import ray.rml.Matrix3;
import ray.rml.Matrix3f;
import ray.rml.Vector3f;

public class RotateNodeLeftAction extends AbstractInputAction
{
	private SceneNode n;
	private MyGame g;
	
	public RotateNodeLeftAction(SceneNode n, MyGame g) { 
		this.n = n;
		this.g = g;
	}
	
	public void performAction(float time, Event e) {
		float toRotate = time * g.getRotateSpeed();
		
		n.yaw(Degreef.createFrom(time * g.getRotateSpeed()));
		
		/*
		Matrix3 oldRot = n.getLocalRotation();
		Matrix3 rotM = Matrix3f.createRotationFrom(Degreef.createFrom(toRotate), Vector3f.createFrom(0,1,0));
		//Matrix3 rotM = Matrix3f.createRotationFrom(Degreef.createFrom(toRotate), n.getLocalUpAxis());
		n.setLocalRotation(oldRot.mult(rotM));
		*/
	}
}