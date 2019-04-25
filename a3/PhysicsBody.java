package a3;

public class PhysicsBody {
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
}
