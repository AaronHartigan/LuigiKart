package ray.input.action;

import net.java.games.input.Event;

public interface Action {
	
	/** <code>performAction()</code> is invoked to perform the action 
	 * defined by this <code>IAction</code> object.
	 * @param time -- the elapsed time for which the <code>IAction</code> is being invoked
	 * @param evt -- the event associated with invoking this <code>IAction</code>
	 */
	
	public void performAction(float time, Event evt);

}
