package ray.input.action;

import net.java.games.input.Event;

public abstract class AbstractInputAction implements Action{
	
		// speed at which the action can occur
		private float speed = 1; 

		/** Sets the speed at which the Action occurs (default = 1). 
		 * The interpretation of speed units is
		 * application (game) dependent.
		 */
		public void setSpeed(float newSpeed) {
			speed = newSpeed;
		}

		/** Returns the current speed setting for the Action */
		public float getSpeed() {
			return speed;
		}
		
		/** <code> performAction() </code> is invoked to execute this input action.
		 * It must be implemented by all concrete subclasses. 
		 * @param time -- the time at which the action is invoked
		 * @param evt -- the event associated with invoking this action
		 */
		public abstract void performAction(float time, Event evt);
	

}
