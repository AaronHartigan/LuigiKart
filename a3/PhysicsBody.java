package a3;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import ray.rage.asset.texture.Texture;
import ray.rage.asset.texture.TextureManager;
import ray.rage.asset.texture.loaders.RgbaTextureLoader;
import ray.rage.util.Configuration;
import ray.rml.Degreef;
import ray.rml.Matrix3;
import ray.rml.Matrix3f;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class PhysicsBody {
	private Vector3 position = Vector3f.createFrom(0f, 0f, 0f);
	private Matrix3 direction = Matrix3f.createIdentityMatrix();
	private Matrix3 rotation = Matrix3f.createIdentityMatrix();
	private Matrix3 spinRotation = Matrix3f.createIdentityMatrix();
	private boolean isOnGround = true;
	private float gravity = -10f;
	private boolean isAccelerating = false;
	private boolean isDeccelerating = false;
	private boolean isBraking = false;
	private boolean isOnSpeedBoost = false;
	private float accelerationRate = 20f;
	private float deccelerationRate = -15f;
	private float TURN_RATE = 50f;
	private float spinDirection = 0f;
	private float spinoutTimer = 0f;
	private float SPINOUT_DURATION = 1500f;
	private float speedBoostTimer = 0f;
	private float SPEED_BOOST_DURATION = 1000f;
	private float DRIFTING_TURN_RATE = 75f;
	private boolean isDrifting = false;
	private float driftingDirection = 0f;
	private float MAX_BASE_SPEED = 20f;
	private float roadFriction = 10f;
	private float desiredTurn = 0f;
	private float desiredTurnRollingAverage = 0f;
	private float actualTurn = 0f;
	private float vForward = 0f; // forward velicty
	private float vUp = 0f; // upward velocity
	private float currentPitch = 0f;
	private float currentRoll = 0f;
	private Texture heightMap;
	private TextureManager textureManager;
	
	public PhysicsBody(Vector3 position, Matrix3 rotation) {
		textureManager = new TextureManager();
        textureManager.addAssetLoader(new RgbaTextureLoader());
        Configuration conf = new Configuration();
        try {
			conf.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
        textureManager.setBaseDirectoryPath(conf.valueOf("assets.textures.path"));
		try {
			heightMap = textureManager.getAssetByPath("height_map.png");
		}
		catch (IOException e) {
			e.printStackTrace(); return;
		}
		this.position = position;
		this.rotation = rotation;
	}
	
	public void updatePhysics(float elapsedMS) {
		updateTimers(elapsedMS);
		float elapsedSec = elapsedMS / 1000;
		Vector3 fv = direction.mult(rotation).column(2);
		
		// Handle speed boost
		setOnSpeedBoost(getIsSpeedBoost(position.x(), position.z(), fv, 1.5f));
		
		// Handle collision spinning
		if (isSpinning()) {
			spinRotation = Matrix3f.createIdentityMatrix();
			spinRotation = spinRotation.rotate(Degreef.createFrom(getSpinDirection()), Vector3f.createUnitVectorY());
		}
		else {
			spinRotation = Matrix3f.createIdentityMatrix();
		}
		
		// Handle turning
		desiredTurnRollingAverage = desiredTurnRollingAverage * 0.9f + getDesiredTurn() * 0.1f;
		float dTurn = desiredTurnRollingAverage - actualTurn;
		actualTurn += dTurn / 6;
		float turnDegrees = actualTurn * elapsedSec * getTurnRate();
		// Reduce turn rate if car is moving slowly
		if (Math.abs(vForward) <= 1f) {
			if (Math.abs(vForward) <= 0.05f) {
				turnDegrees = 0;
			}
			else {
				turnDegrees *= ((Math.abs(vForward) * 16 / 19) + 3/19);	
			}
		}
		if (isOnGround) {
			setDirection(direction.rotate(Degreef.createFrom(turnDegrees), Vector3f.createUnitVectorY()));
		}

		// Handle forward movement
		if (speedBoostTimer > 0f) {
			vForward = getMaxSpeed();
		}
		else if (isOnGround) {
			// adjust forward velocity
			if (isAccelerating()) {
				vForward += accelerationRate * elapsedSec;
			}
			if (isDeccelerating()) {
				vForward += deccelerationRate * elapsedSec;
			}
			if (
				(!isAccelerating() && !isDeccelerating()
				|| isAccelerating() && isDeccelerating())
				&& Math.abs(vForward) < 0.05f
			) {
				// Clamp to prevent small movement when player should be stationary
				vForward = 0f;
			}
			else {
				float frictionDirection = vForward > 0f ? -1f : 1f;
				vForward += roadFriction * frictionDirection * elapsedSec;
				vForward = Math.max(vForward, getMaxReverseSpeed());
				vForward = Math.min(vForward, getMaxSpeed());
			}
		}
		// apply forward velocity
		fv = fv.mult((vForward + getGravityForce()) * elapsedSec);
		
		// Check if player is running into a wall by checking the angle of the avatar
		float currentHeight = position.y();
		float tCAR_LENGTH = vForward > 0 ? 0.5f : -0.5f;
		position = position.add(rotation.column(2).mult(tCAR_LENGTH)); // Move forward
		Vector3 newLP = position;
		position = position.add(rotation.column(2).mult(-tCAR_LENGTH)); // Move backward
		float heightDifferential = (getGroundHeight(newLP.x(), newLP.z())) - currentHeight;
		float tpitchAngle = (float) Math.toDegrees(Math.atan(heightDifferential / Math.abs(tCAR_LENGTH)));
		if (tpitchAngle > 45f) {
			// Calculate angle and set new angle and velocity
			vForward = 0f;
		}
		else {
			// Move forward
			position = position.add(fv);
		}
		
		// Handle new location's height
		float groundHeight = getGroundHeight(position.x(), position.z());
		currentHeight = position.y();
		// If avatar was already on the ground, and the ground is near, "snap" the avatar to the ground
		if (isOnGround && (currentHeight - groundHeight) < 0.2f) {
			currentHeight = groundHeight;
		}
		// If falling, update currentHeight with gravityd
		if (currentHeight > groundHeight) {
			isOnGround = false;
			vUp += gravity * elapsedSec;
			currentHeight += ((gravity * elapsedSec * elapsedSec / 2) + vUp * elapsedSec);
		}
		// Must check if gravity has put us below ground (or going uphill)
		if (currentHeight < groundHeight) {
			// if we were falling with enough speed, bounce
			if (!isOnGround && vUp < -5f) {
				vUp = Math.abs(vUp) / 8;
			}
			// If not enough downward velocity, just be on the ground
			else {
				vUp = 0;
				isOnGround = true;
			}
			currentHeight = groundHeight;
		}
		position = Vector3f.createFrom(position.x(), currentHeight, position.z());
		
		// Change the avatar's angle
		if (isOnGround) {
			Vector3 heading;
			float heightChange;
			Matrix3 newRotation = Matrix3f.createIdentityMatrix();
			// Calculate pitch
			float CAR_LENGTH = 0.5f;
			position = position.add(direction.column(2).mult(CAR_LENGTH)); // Move forward
			heading = position;
			position = position.add(direction.column(2).mult(-CAR_LENGTH)); // Move backward
			heightChange = (getGroundHeight(heading.x(), heading.z())) - currentHeight;
			if (heightChange < -1f) {
				// if falling, don't correct pitch much
				float pitchAngle = (float) Math.toDegrees(Math.atan(heightChange / CAR_LENGTH));
				float newPitch = (pitchAngle - currentPitch) * elapsedSec / 3 + currentPitch;
				rotation = newRotation.rotate(Degreef.createFrom(-newPitch), Vector3f.createUnitVectorX());
				currentPitch = newPitch;
			}
			else {
				float pitchAngle = (float) Math.toDegrees(Math.atan(heightChange / CAR_LENGTH));
				float newPitch = (pitchAngle - currentPitch) * elapsedSec * 3 + currentPitch;
				rotation = newRotation.rotate(Degreef.createFrom(-newPitch), Vector3f.createUnitVectorX());
				currentPitch = newPitch;
			}
			
			// Calculate roll
			float CAR_WIDTH = 0.25f;
			position = position.add(direction.column(0).mult(CAR_LENGTH)); // Move right
			heading = position;
			position = position.add(direction.column(0).mult(-CAR_LENGTH)); // Move left
			heightChange = (getGroundHeight(heading.x(), heading.z())) - currentHeight;
			float rollAngle = (float) Math.toDegrees(Math.atan(heightChange / CAR_WIDTH));
			float newRoll = (rollAngle - currentRoll) * elapsedSec * 5 + currentRoll;
			rotation = rotation.rotate(Degreef.createFrom(newRoll), Vector3f.createUnitVectorZ());
			currentRoll = newRoll;
		}
		// If in air, straighten car out
		else {
			Matrix3 newRotation = Matrix3f.createIdentityMatrix();
			float newPitch = -currentPitch * elapsedSec + currentPitch;
			rotation = newRotation.rotate(Degreef.createFrom(-newPitch), Vector3f.createUnitVectorX());
			currentPitch = newPitch;

			float newRoll = -currentRoll * elapsedSec + currentRoll;
			rotation = rotation.rotate(Degreef.createFrom(newRoll), Vector3f.createUnitVectorZ());
			currentRoll = newRoll;
		}
	}
	

	public boolean isAccelerating() {
		return isAccelerating;
	}

	public void setAccelerating(boolean isAccelerating) {
		this.isAccelerating = isAccelerating;
	}

	public boolean isBraking() {
		return isBraking;
	}

	public void setBraking(boolean isBraking) {
		this.isBraking = isBraking;
	}

	public boolean isDeccelerating() {
		return isDeccelerating;
	}

	public void setDeccelerating(boolean isDeccelerating) {
		this.isDeccelerating = isDeccelerating;
	}
	
	protected float getGravityForce() {
		return (float) Math.sin(Math.toRadians(currentPitch)) * gravity;
	}
	
	protected float getMaxSpeed() {
		return (MAX_BASE_SPEED + getGravityForce()) * getMaxSpeedModifier();
	}
	
	protected float getMaxReverseSpeed() {
		return -getMaxSpeed() / 4;
	}
	
	protected float getTurnRate() {
		float direction = 1f;
		if (vForward < 0) {
			direction = -1f;
		}
		if (isDrifting()) {
			return direction * DRIFTING_TURN_RATE;
		}
		return direction * TURN_RATE;
	}

	public float getDesiredTurn() {
		return desiredTurn;
	}

	public void setDesiredTurn(float desiredTurn) {
		this.desiredTurn = desiredTurn;
	}

	public float getActualTurn() {
		return actualTurn;
	}

	public void setActualTurn(float actualTurn) {
		this.actualTurn = actualTurn;
	}
	
	public float getDriftingDirection() {
		return driftingDirection;
	}

	public void setDriftingDirection(float driftingDirection) {
		this.driftingDirection = driftingDirection;
	}

	public boolean isDrifting() {
		return isDrifting;
	}

	public void setDrifting(boolean isDrifting) {
		this.isDrifting = isDrifting;
	}
	
	public boolean isSpinning() {
		return spinoutTimer > 0;
	}

    public void handleCollision() {
    	setSpinoutTimer(SPINOUT_DURATION);
    }

    private void setSpinoutTimer(float f) {
		this.spinoutTimer = f;
	}
    
    private float getSpinDirection() {
    	return spinDirection;
    }
    
    private void calculateSpinDirection() {
    	float timer = spinoutTimer;
    	if (spinoutTimer > 750) {
    		timer -= 750;
    	}
    	setSpinDirection(timer * 360 / 750);
    }
    
    private void setSpinDirection(float direction) {
    	spinDirection = direction;
    }

	protected float getMaxSpeedModifier() {
    	if (spinoutTimer <= 0f) {
    		if (speedBoostTimer > 0f) {
    			return 1.2f;
    		}
    		return 1f;
    	}
    	return ((spinoutTimer / SPINOUT_DURATION) * 0.3f ) + 0.4f;
    }

	public void updateTimers(float elapsedMS) {
		setSpinoutTimer(Math.max(0, spinoutTimer - elapsedMS));
		setSpeedBoostTimer(Math.max(0, speedBoostTimer - elapsedMS));
		calculateSpinDirection();
	}

	private void setSpeedBoostTimer(float time) {
		this.speedBoostTimer = time;
	}
	

	public boolean isOnSpeedBoost() {
		return isOnSpeedBoost;
	}

	public void setOnSpeedBoost(boolean isOnSpeedBoost) {
		if (isOnSpeedBoost && !isSpinning()) {
			setSpeedBoostTimer(SPEED_BOOST_DURATION);
		}
		this.isOnSpeedBoost = isOnSpeedBoost;
	}
	
	public float getVForward() {
		return vForward;
	}

	public Vector3 getPosition() {
		return position;
	}


	public void setPosition(Vector3 position) {
		this.position = position;
	}


	public Matrix3 getRotation() {
		return rotation;
	}


	public void setRotation(Matrix3 rotation) {
		this.rotation = rotation;
	}

	public Matrix3 getSpinRotation() {
		return spinRotation;
	}

	public void setSpinRotation(Matrix3 spinRotation) {
		this.spinRotation = spinRotation;
	}

	public Matrix3 getDirection() {
		return direction;
	}

	public void setDirection(Matrix3 direction) {
		this.direction = direction;
	}

	protected float getGroundHeight(float x, float z) {
		return getWorldHeight(x, z);
	}

	protected boolean getIsSpeedBoost(float x, float z, Vector3 facing, float scale) {
		return (
			getIsSpeedBoost(x + facing.x() * scale, z + facing.z() * scale) ||
			getIsSpeedBoost(x, z) ||
			getIsSpeedBoost(x - facing.x() * scale, z - facing.z() * scale)
		);
	}
	
	public float getWorldHeight(float globalX, float globalZ) {
		float scale = 250f;

		// Get size bounds
		float targetX;
		float targetZ;
		
		// Normalize the target values
		targetX = globalX / scale + 0.5f;
		targetZ = 1 - (globalZ / scale + 0.5f);
		// Now that the user's provided coordinates have been converted to local coordinates, perform the height estimation.
		return getAverageHeight(targetX, targetZ);
	}
	
	public boolean getIsSpeedBoost(float globalX, float globalZ) {
		float scale = 250f;

		// Get size bounds
		float targetX;
		float targetZ;
		
		// Normalize the target values
		targetX = globalX / scale + 0.5f;
		targetZ = 1 - (globalZ / scale + 0.5f);

		switch (8) {
		case 5:
			targetZ += 0.030f;
			break;
		case 6:
			targetZ += 0.020f;
			break;
		case 7:
			targetZ += 0.010f;
			break;
		case 8:
			targetZ += 0.006f;
			break;
		case 9:
			targetZ += 0.004f;
			break;
		case 10:
			targetZ += 0.002f;
			break;
		case 11:
			targetZ += 0.001f;
			break;
		default: // 12+
			break;
		}
		
		// Normalize parameters (and constrain invalid parameters between 0.0 and 1.0)
		while (targetX < 0.0f) {targetX += 1.0f;}
		while (targetX > 1.0f) {targetX -= 1.0f;}
		while (targetZ < 0.0f) {targetZ += 1.0f;}
		while (targetZ > 1.0f) {targetZ -= 1.0f;}

		// Obtain the buffered image
		BufferedImage img = heightMap.getImage();
		
		// Estimate the closest HeightMap pixel
		float xPixel  = (img.getWidth()  - 1) * targetX;
		float zPixel  = (img.getHeight() - 1) * targetZ;
		int   xPixelT = (int) xPixel;
		int   zPixelT = (int) zPixel;

		float blue = new Color(img.getRGB(xPixelT, zPixelT)).getBlue() / 255.0f;
		return (blue > 0.9f);
	}

    public float getAverageHeight(float xPercent, float zPercent) {
    	float amount = 0f;

		// Z Hack, since the tessellation shaders are apparently not perfectly centered. Depends on Patch Sizes
		switch (8) {
		case 5:
			zPercent += 0.030f;
			break;
		case 6:
			zPercent += 0.020f;
			break;
		case 7:
			zPercent += 0.010f;
			break;
		case 8:
			zPercent += 0.006f;
			break;
		case 9:
			zPercent += 0.004f;
			break;
		case 10:
			zPercent += 0.002f;
			break;
		case 11:
			zPercent += 0.001f;
			break;
		default: // 12+
			break;
		}

		// Normalize parameters (and constrain invalid parameters between 0.0 and 1.0)
		while (xPercent < 0.0f) {xPercent += 1.0f;}
		while (xPercent > 1.0f) {xPercent -= 1.0f;}
		while (zPercent < 0.0f) {zPercent += 1.0f;}
		while (zPercent > 1.0f) {zPercent -= 1.0f;}

		// Obtain the buffered image
		BufferedImage img = heightMap.getImage();
		
		// Estimate the closest HeightMap pixel
		float xPixel  = (img.getWidth()  - 1) * xPercent;
		float zPixel  = (img.getHeight() - 1) * zPercent;
		int   xPixelT = (int) xPixel;
		int   zPixelT = (int) zPixel;

		// Estimate how exact the position is
		float xDepth = xPixel % 1.0f;
		float zDepth = zPixel % 1.0f;
		
		// Discover how much weight to put on the targeted pixel
		float xWeightT = getWeight(xDepth);
		float zWeightT = getWeight(zDepth);
		
		// Discover nearest neighbors
		int xPixelN;
		int zPixelN;
		if      (xDepth <  0.5f)  xPixelN = (int) xPixelT - 1;
		else if (xDepth == 0.5f)  xPixelN = (int) xPixelT    ;
		else                      xPixelN = (int) xPixelT + 1;
		
		if      (zDepth <  0.5f)  zPixelN = (int) zPixelT - 1;
		else if (zDepth == 0.5f)  zPixelN = (int) zPixelT    ;
		else                      zPixelN = (int) zPixelT + 1;
		
		// Verify neighboring pixels exist, if they don't, wrap around
		if      (xPixelN < 0)                xPixelN = img.getWidth()  - 1;
		else if (xPixelN >= img.getWidth() ) xPixelN = 0;
		if      (zPixelN < 0)                zPixelN = img.getHeight() - 1;
		else if (zPixelN >= img.getHeight()) zPixelN = 0;
		
		// Average the weighted heights
		{
			// Calculate target pixel's height
			float total0 = new Color(img.getRGB(xPixelT, zPixelT)).getRed() / 255.0f;
			float total1 = new Color(img.getRGB(xPixelN, zPixelT)).getRed() / 255.0f;
			float total2 = new Color(img.getRGB(xPixelT, zPixelN)).getRed() / 255.0f;
			float total3 = new Color(img.getRGB(xPixelN, zPixelN)).getRed() / 255.0f;
			
			// Weight the heights of the target and neighboring pixels
			float weightXZ = (xWeightT + zWeightT) / 2;
			float avgX     = ((xWeightT * total0) + ((1.0f - xWeightT) * total1));
			float avgZ     = ((zWeightT * total0) + ((1.0f - zWeightT) * total2));
			float avgXZ    = ((weightXZ * total0) + ((1.0f - weightXZ) * total3));
			
			// Get the final total
			amount = (avgX + avgZ + avgXZ) / (3.0f);
		}
    	
    	return (amount * 10);
    }
    
    private float getWeight(float depth) {
    	return 1.0f - Math.abs(depth - 0.5f);
    }
    
    public void resetInputs() {
		setAccelerating(false);
		setDeccelerating(false);
		setDrifting(false);
		setDesiredTurn(0f);
	}
}