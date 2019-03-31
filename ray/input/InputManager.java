package ray.input;

import java.util.ArrayList;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import ray.input.action.Action;

public interface InputManager {
	
	//TODO docs
	public enum INPUT_ACTION_TYPE {
		ON_PRESS_ONLY, ON_PRESS_AND_RELEASE, REPEAT_WHILE_DOWN
	}
	
	/**
	 * <code>update(float time)</code> instructs the <code>IInputManager</code>
	 * object to update its notion of the state of all input devices about which
	 * it knows, and then to invoke any {@link IAction}s associated with device
	 * components based on the {@link IInputManager.INPUT_ACTION_TYPE} of the
	 * {@link IAction}.
	 * 
	 * @param time
	 *            - the amount of time, normally given in milliseconds, which has 
	 *            	passed since the last call to update().
	 */
	public void update(float time);
	
	/**
	 * <code>associateAction()</code> causes the <code>IInputManager</code>
	 * object to associate an {@link IAction} (for example, "walk forward",
	 * "fire weapon", etc) with the activation of a specific
	 * component <I>of a specific controller</i>. 
	 * Associating an
	 * {@link IAction} with a controller and component causes
	 * {@link IInputManager#update(float)} to automatically invoke that
	 * {@link IAction}, by calling its
	 * {@link IAction#performAction(float,Event)} method, based on the
	 * associated {@link IInputManager.INPUT_ACTION_TYPE} (for example, each time the
	 * specified component on the specified controller changes state).
	 * 
	 * @param controllerName
	 * 				- the name of the controller, as returned by {@link Controller#getName()}
	 * 					 with which the action is to be associated
	 * @param component
	 *            - identifier of the component with which the action is to be
	 *            associated
	 * @param action
	 *            - the action which is to be associated with the specified
	 *            component
	 * @param actionType
	 *            -- indicates when the action is to be invoked in relation to
	 *            the occurrence of component events
	 * 
	 * @return the action which was previously associated with the specified
	 *         component, or null if there was no previously associated action.
	 */
	public Action associateAction(String controllerName, Component.Identifier component,
			Action action, INPUT_ACTION_TYPE actionType);
	
	public Action associateAction(Controller controller, Component.Identifier component,
			Action action, INPUT_ACTION_TYPE actionType);
	
	
	/**
	 * <code>getControllers()</code> returns an {@link ArrayList} of the
	 * controllers currently recognized as attached to the execution
	 * environment. The controllers are returned in the order in which the
	 * underlying implementation (currently JInput) returns them.
	 * 
	 * @return a list of the available device controllers
	 */
	public ArrayList<Controller> getControllers();
	
	/**
	 * Returns the name string for the Keyboard controller (if more than one keyboard is present,
	 * the first one is returned).  If no keyboard controller is found, null is returned.
	 */
	public String getKeyboardName();
	
	/**
	 * Returns the name string for the Mouse controller (if more than one mouse is present,
	 * the first one is returned).  If no mouse controller is found, null is returned.
	 */
	public String getMouseName();
	
	/**
	 * Returns the name string for the first gamepad controller.  If no gamepad controller is found, 
	 * null is returned.
	 */
	public String getFirstGamepadName();
	
	/**
	 * Returns the name string for the second gamepad controller (if less than two gamepad controllers
	 * are present, null is returned).
	 */
	public String getSecondGamepadName();
	
	/**
	 * Returns the {@link net.java.games.input.Controller} with the specified name, or null if
	 * no controller of the specified name is installed.
	 */
	public Controller getControllerByName(String name);
	
	
	/* NEW CODE */
	
	/**
	 * Returns the Keyboard controller (if more than one keyboard is present,
	 * the first one is returned).  If no keyboard controller is found, null is returned.
	 */
	public Controller getKeyboardController();
	
	/**
	 * Returns the <code>nth</code> occurrence of a keyboard {@link net.java.games.input.Controller}.
	 * If no keyboard controller is found, null is returned.
	 * 
	 * @param n Occurrence number, such as 1 for first occurrence of a keyboard, or 2 for second occurrence of a keyboard.
	 */
	public Controller getKeyboardController(int n);
	
	/**
	 * Returns the first keyboard controller with <code>n</code> or more components. 
	 * If no such keyboard controller is found, null is returned.
	 * 
	 * @param n The minimum number of components the keyboard should have. 
	 */
	public Controller getKeyboardControllerWithNComponents(int n);
	
	/**
	 * Returns the Mouse controller (if more than one mouse is present,
	 * the first one is returned).  If no mouse controller is found, null is returned.
	 */
	public Controller getMouseController();
	
	/**
	 * Returns the <code>nth</code> occurrence of a Mouse {@link net.java.games.input.Controller}.
	 * If no mouse controller is found, null is returned.
	 * 
	 * @param n Occurrence number, such as 1 for first occurrence of a mouse, or 2 for second occurrence of a mouse.
	 */
	public Controller getMouseController(int n);
	
	/**
	 * Returns the <code>nth</code> occurrence of a Gamepad {@link net.java.games.input.Controller}. 
	 * 
	 * @param n Occurrence number, such as 1 for first occurrence of a gamepad, or 2 for second occurrence of a gamepad. 
	 */
	public Controller getGamepadController(int n);
	
	/**
	 * Returns the <code>nth</code> occurrence of a joystick {@link net.java.games.input.Controller}. 
	 * 
	 * @param n Occurrence number, such as 1 for first occurrence of a joystick, or 2 for second occurrence of a joystick. 
	 */
	public Controller getJoystickController(int n);
	
	/**
	 * Returns the <code>nth</code> occurrence of a Fingerstick {@link net.java.games.input.Controller}. 
	 * 
	 * @param n Occurrence number, such as 1 for first occurrence of a Fingerstick, or 2 for second occurrence of a Fingerstick. 
	 */
	public Controller getFingerstickController(int n);
	
	/**
	 * Returns the <code>nth</code> occurrence of a Headtracker {@link net.java.games.input.Controller}. 
	 * 
	 * @param n Occurrence number, such as 1 for first occurrence of a Headtracker, or 2 for second occurrence of a Headtracker. 
	 */
	public Controller getHeadtrackerController(int n);
	
	/**
	 * Returns the <code>nth</code> occurrence of a Rudder {@link net.java.games.input.Controller}. 
	 * 
	 * @param n Occurrence number, such as 1 for first occurrence of a Rudder, or 2 for second occurrence of a Rudder. 
	 */
	public Controller getRudderController(int n);
	
	/**
	 * Returns the <code>nth</code> occurrence of a Trackball {@link net.java.games.input.Controller}. 
	 * 
	 * @param n Occurrence number, such as 1 for first occurrence of a Trackball, or 2 for second occurrence of a Trackball. 
	 */
	public Controller getTrackballController(int n);
	
	/**
	 * Returns the <code>nth</code> occurrence of a Trackpad {@link net.java.games.input.Controller}. 
	 * 
	 * @param n Occurrence number, such as 1 for first occurrence of a Trackpad, or 2 for second occurrence of a Trackpad. 
	 */
	public Controller getTrackpadController(int n);
	
	/**
	 * Returns the <code>nth</code> occurrence of a Wheel {@link net.java.games.input.Controller}. 
	 * 
	 * @param n Occurrence number, such as 1 for first occurrence of a Wheel, or 2 for second occurrence of a Wheel. 
	 */
	public Controller getWheelController(int n);
	
	/**
	 * Prints a list of all connected controllers to the console.
	 * @param printComponents <code>TRUE</code> if all the components of the controller should also be printed. 
	 * <code>FALSE</code> if not. 
	 */
	public void printControllers(boolean printComponents);
	
	//TODO add printcontroller function (depends on controllers)

}
