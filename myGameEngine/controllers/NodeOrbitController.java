package myGameEngine.controllers;

import java.util.ArrayList;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import ray.input.InputManager;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.scene.Node;
import ray.rage.scene.SceneNode;
import ray.rage.scene.controllers.AbstractController;
import ray.rml.Matrix3;
import ray.rml.Matrix4f;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class NodeOrbitController extends AbstractController
{
	private SceneNode orbitTarget;
	private float MAX_DISTANCE = 10f;
	private float MIN_DISTANCE = 0.5f;
	private float orbitDistance = (MAX_DISTANCE + MIN_DISTANCE) / 2;
	private float azimuth = 0f; //rotation of camera around Y axis
	private float elevation = 15f; //elevation of camera above target
	private float MAX_ELEVATION = 85f;
	private float MIN_ELEVATION = 1f;
	private Vector3 worldUpVec = Vector3f.createUnitVectorY();
	private InputManager im;
	private GL4RenderSystem rs;
	private final float ROTATE_AMOUNT = 0.2f;
	private final float ZOOM_AMOUNT = 0.02f;

	public NodeOrbitController(SceneNode orbitTarget, GL4RenderSystem rs, InputManager im) {
		super();
		this.orbitTarget = orbitTarget;
		this.im = im;
		this.rs = rs;
		setupInputs();
	}
	
	public float getOrbitDistance() {
		return this.orbitDistance;
	}
	
	public void setOrbitDistance(float distance) {
		if (distance < MIN_DISTANCE) {
			this.orbitDistance = MIN_DISTANCE;
		}
		else if (distance > MAX_DISTANCE) {
			this.orbitDistance = MAX_DISTANCE;
		}
		else {
			this.orbitDistance = distance;
		}
	}
	
	public void setAzimuth(float azimuth) {
		this.azimuth = azimuth;
		if (this.azimuth < 0) {
			this.azimuth += 360;
		}
		else if (this.azimuth > 360) {
			this.azimuth -= 360;
		}
	}
	
	public float getAzimuth() {
		return this.azimuth;
	}
	
	public float getElevation() {
		return this.elevation;
	}
	
	public void setElevation(float elevation) {
		if (elevation > MAX_ELEVATION) {
			this.elevation = MAX_ELEVATION;
		}
		else if (elevation < MIN_ELEVATION) {
			this.elevation = MIN_ELEVATION;
		}
		else {
			this.elevation = elevation;
		}
	}


	private void setupInputs() {
		ArrayList<Controller> controllers = im.getControllers();
		for (Controller c : controllers) {
			if (c.getType() == Controller.Type.KEYBOARD) {
				im.associateAction(
					c,
					Component.Identifier.Key.UP,
					new ElevateNodeUpAction(this, ROTATE_AMOUNT),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
				im.associateAction(
					c,
					Component.Identifier.Key.DOWN,
					new ElevateNodeDownAction(this, ROTATE_AMOUNT),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
				im.associateAction(
					c,
					Component.Identifier.Key.LEFT,
					new OrbitNodeLeftAction(this, ROTATE_AMOUNT),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
				im.associateAction(
					c,
					Component.Identifier.Key.RIGHT,
					new OrbitNodeRightAction(this, ROTATE_AMOUNT),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);

			}
			else if (c.getType() == Controller.Type.MOUSE) {
				im.associateAction(
					c,
					Component.Identifier.Axis.Z,
					new OrbitZoomInOutAction(this, ZOOM_AMOUNT, rs),
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
			}
		}
	}

	@Override
	protected void updateImpl(float elapsedTimeMillis) {
		final int nodeCount = super.controlledNodesList.size();
		for (int i = 0; i < nodeCount; ++i) {
			Node n = super.controlledNodesList.get(i);
			double theta = Math.toRadians(azimuth); // rot around target
			double phi = Math.toRadians(elevation); // altitude angle
			double x = orbitDistance * Math.cos(phi) * Math.sin(theta);
			double y = orbitDistance * Math.sin(phi);
			double z = orbitDistance * Math.cos(phi) * Math.cos(theta);
			n.setLocalPosition(Vector3f.createFrom((float)x, (float)y, (float)-z));
			Matrix3 lookAt = Matrix4f.createLookAtMatrix(n.getWorldPosition(), orbitTarget.getWorldPosition(), worldUpVec).toMatrix3();
			Matrix3 parentRotInv = n.getParent().getLocalRotation().inverse();
			n.setLocalRotation(parentRotInv.mult(lookAt));
		}
	}
}
