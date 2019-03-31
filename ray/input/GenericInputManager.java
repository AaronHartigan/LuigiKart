package ray.input;

import java.util.ArrayList;
import java.util.HashMap;

import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import net.java.games.input.Version;

import ray.input.ComponentStateInfo;
import ray.input.InputManager;
import ray.input.action.Action;

public class GenericInputManager implements InputManager{
	// this array holds the available "controllers" - the JInput term for a
	// single "device" such as a keyboard, mouse, or gamepad. These are
	// known as "Objects" in OIS, and as DirectInputDevice objects in DirectX
	private ArrayList<Controller> controllers;

	// this hashtable holds the state of each component in each controller
	// private HashMap<Component.Identifier, ComponentStateInfo> componentStates;
	
	private HashMap<Controller, HashMap<Component.Identifier,ComponentStateInfo>> controllerStates;
	private HashMap<Controller, HashMap<Component.Identifier,Action>> controllerActions;

	// this hashtable holds Component:AbstractInputAction pairs; when a given
	// component is
	// activated, this table defines the corresponding action which should
	// occur.
	// (That is, it manages the implementation of the "Command" design pattern.)
	// It is the responsibility of the application (game) to install
	// Component:AbstractInputAction pairs into the actions table, by calling
	// method
	// associateAction().

	private int verbosity;

	public GenericInputManager() {
		this(0);
	}
	
	public GenericInputManager(int verbosity) {

		this.verbosity = verbosity;

		// list of known controllers
		controllers = new ArrayList<Controller>();

		// table to hold the state of each component
		controllerStates = new HashMap<Controller, HashMap<Component.Identifier,ComponentStateInfo>>();

		// table of component:action pairs
		controllerActions = new HashMap<Controller, HashMap<Component.Identifier,Action>> ();

		// get an array of the currently available controllers from JInput
		Controller[] jinput_controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();

		parseControllersList();
		
		//create a hashtable entry for each controller to hold the controller's component's states
		for (Controller c : controllers) {
			controllerStates.put(c, new HashMap<Component.Identifier,ComponentStateInfo>());
		}
		
		//create a hashtable entry for each controller to hold actions associated with the controller's components
		for (Controller c : controllers) {
			controllerActions.put(c, new HashMap<Component.Identifier,Action>());
		}
		
		if (verbosity > 0) {
			System.out.println(this.getClass().getName()
					+ ": using JInput version " + Version.getVersion());
		}
		if (verbosity > 1) {
			if (jinput_controllers.length > 0) {
				System.out.println(this.getClass().getName()
						+ ": found the following controllers:");
				for (int i = 0; i < jinput_controllers.length; i++) {
					Controller c = jinput_controllers[i];
					Controller[] subControllers = c.getControllers();
					Component[] components = c.getComponents();
					System.out.println("  " + c + "  (name='" + c.getName()
							+ "'" + "  type=" + c.getType() + "  port="
							+ c.getPortType() + "  num components="
							+ components.length + "  num subcontrollers="
							+ subControllers.length + ")");
					if (verbosity > 2) {
						if (components.length > 0) {
							for (int j = 0; j < components.length; j++) {
								System.out.println("    Component " + j
										+ " name= '" + components[j].getName()
										+ "'" + " identifier= '"
										+ components[j].getIdentifier() + "'");
							}// end for each component of this controller
						} else {
							System.out
									.println("   No components found for the above controller");
						}
					}
				}// end for each controller
			} else {
				System.out.println(this.getClass().getName()
						+ ": no controllers found during initialization");
			}
		}

	}
	
	public void update(float time) {
		if (verbosity > 3) {
			System.out.println("...start InputManager.update() event processing...");
		}

		// process each controller
		for (Controller c : controllers) {

			/* poll the controller to update its event queue */
			c.poll();

			/* Get the controller's event queue */
			EventQueue queue = c.getEventQueue();

			/* Create an event object for the underlying plugin to populate */
			Event event = new Event();

			/*
			 * As long as there's more Events in the queue, copy the next Event into "event" and
			 * then process it
			 */
			while (queue.getNextEvent(event)) { // get an event

				// PROCESS THE EVENT:

				// get the identifier of the component associated with the event
				Identifier componentID = event.getComponent().getIdentifier();

				if (verbosity > 3) {
					System.out.println(System.nanoTime()
							+ ": InputManager.update(): found input event from: " + " controller='"
							+ c.getName() + "'" + " componentID='" + componentID + "'"
							+ ", componentName='" + componentID.getName() + "'" + ", isRelative='"
							+ event.getComponent().isRelative() + "'" + ", componentValue="
							+ event.getValue() + ", time=" + event.getNanos());
				}

				// get ComponentStateInfo object associated with the controller and
				// component which generated the current event
				ComponentStateInfo componentState = controllerStates.get(c).get(componentID);

				// make sure there IS a ComponentStateInfo for the component (only components for
				// which actions have been registered will have ComponentStateInfo objects
				if (componentState != null) {

					// check whether the component which generated the event is an
					// "axis" or a "button" (which includes "keys")
					if (event.getComponent().getIdentifier() instanceof Component.Identifier.Axis) {

						// HANDLE AXIS EVENT

						// update the event for the axis component
						componentState.updateEvent(event);

						// see if there's an action associated with the axis component
						Action action = controllerActions.get(c).get(componentID);
						if (action != null) {
							// the axis has an action attached; execute the action
							// and pass the event (which includes the axis value)
							action.performAction(time, event);
						} else {
							// action == null, meaning no action was ever associated
							// with this axis; silently ignore the axis event
						}

					} else {

						// HANDLE BUTTON/KEY EVENT

						// the component is not an "Axis", so it's a button or a key (since those
						// are the other two subtypes of Identifier in addition to Axis).

						// update the event for the button/key component
						componentState.updateEvent(event);

						// determine whether this is a "pressed" or "released" event
						if (prettyCloseTo(Math.abs(event.getValue()), 1.0f)) {

							// HANDLE BUTTON "PRESSED" EVENT

							// mark the button state as 'pressed'
							componentState.markAsPressed();

							// check if component actions should be invoked on press
							if (componentState.getActionType() == INPUT_ACTION_TYPE.ON_PRESS_ONLY
									|| componentState.getActionType() == INPUT_ACTION_TYPE.ON_PRESS_AND_RELEASE) {

								// yes it's "invoke on press"; see if it has an action
								Action action = (Action) controllerActions.get(c)
										.get(componentID);
								if (action != null) {

									if (verbosity > 3) {
										System.out.println(System.nanoTime() + ": invoking action "
												+ action + ".performAction() with elapsed time "
												+ time + "and event " + event);
									}
									// it has an action; execute it for the press
									action.performAction(time, event);
								}
							}

						} else {

							// HANDLE BUTTON "RELEASED" EVENT

							// mark the button state as 'released'
							componentState.markAsReleased();

							// check if component actions should be invoked "ON_RELEASED"
							if (componentState.getActionType() == INPUT_ACTION_TYPE.ON_PRESS_AND_RELEASE) {

								// yes it's "invoke on release"; see if it has an action
								Action action = (Action) controllerActions.get(c)
										.get(componentID);
								if (action != null) {

									// it has an action; execute it for the release
									action.performAction(time, event);
								}
							}
						} // end handle "released" event

					} // end handle button/key event

				} else {
					// the ComponentState for the component is null;
					// do nothing -- no ComponentState means there's no action registered for the
					// component which generated this event
				}

			}// end while(there are events in the controller's EventQueue)

		}// end for(each controller)

		// invoke actions for all button/key components which are currently pressed and
		// which are marked as to be invoked "repeatedly while down", and also for
		// all axis components which are not at their neutral (zero) position
		for (Controller c : controllers) {

			for (Component.Identifier component : controllerStates.get(c).keySet()) {

				// get the state of the next component
				ComponentStateInfo componentState = controllerStates.get(c).get(component);

				// check if the event in the componentState is "new". It will be new if it was just
				// added by the event loop (above).
				if (componentState.isNewEvent()) {
					// the event is new and therefore was processed above; don't process it again
					// here
					// but mark it old so we'll process it next time if necessary
					componentState.markEventAsNew(false);
				} else {
					// event is not new (didn't just arrive), so consider component for
					// repeat-processing
					// (note that the component might not even have an event at all...)

					// check if the component both is pressed and is repeatable (only buttons and
					// keys
					// are ever in the "pressed" state, and then only when they have generated a
					// prior event)
					if (componentState.isPressed()
							&& componentState.getActionType() == INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN) {

						// this component is pressed and repeatable; see if it has an Action
						Action action = (Action) controllerActions.get(c).get(component);
						if (action != null) {
							if (verbosity > 3) {
								System.out.println(System.nanoTime()
										+ ": InputManager: invoking repeatable pressed action "
								// + action + "with elapsed time " + time
								// + "and event " + componentState.getEvent()
										);
							}
							// yes it has an Action; invoke the Action
							action.performAction(time, componentState.getEvent());
						}
					} else {
						// check for repeatable axis actions.
						// first, make sure the component has at some time in the past generated an
						// event
						Event e = componentState.getEvent();
						if (e != null) {
							// yes, the component has been activated in the past;
							// see if it's an absolute axis which is still at non-zero and is
							// repeatable
							Component comp = e.getComponent();
							Component.Identifier id = comp.getIdentifier();
							if ((id instanceof Component.Identifier.Axis) // check if it's an axis
									// check if it is marked as repeatable
									&& (componentState.getActionType() == INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN)
									// only consider absolute axes
									&& (!comp.isRelative())
									// only consider repeating if the axis is NOT at zero
									&& (!prettyCloseTo(componentState.getEvent().getValue(), 0))) {
								// yes, axis is being held but not moved (so, not generating any new
								// events)
								// but has generated events in the past and is repeatable;
								// we want to repeat processing of the old event
								// while the axis is held). First, see if it has an Action
								Action action = (Action) controllerActions.get(c).get(component);
								if (action != null) {
									if (verbosity > 3) {
										System.out
												.println(System.nanoTime()
														+ ": InputManager: invoking repeatable axis action "
												// + action + "with elapsed time " + time
												// + "and event " + componentState.getEvent()
												);
									}
									// yes it has an Action; invoke the Action
									action.performAction(time, componentState.getEvent());
								}
							}// end it's an axis which is not at zero
						}// end event is not null
						else {
							// this component has never generated an event; do nothing
						}
					}// end else check for for repeatable axis actions
				}// end else consider component for repeat processing

			}// end for each component
			if (verbosity > 3) {
				System.out.println("...end InputManager.update() event processing...");
			}
		}

	}// end update()
	
	@Override
	public Action associateAction(String controllerName, Component.Identifier component,
			Action action, INPUT_ACTION_TYPE actionType) {
		//find the controller with the specified ID
		for (Controller c : controllers ) {
			if (c.getName().equalsIgnoreCase(controllerName)) {
				//put a new ComponentState record in the component entry for the controller
				controllerStates.get(c).put(component, new ComponentStateInfo(false, actionType));
				//put a new Action in the component entry for the controller
				return controllerActions.get(c).put(component, action);
			}
		}
		throw new RuntimeException("InputManager.associateAction(): controller '"
							+ controllerName + "' not found");
	}
	
	@Override
	public Action associateAction(Controller controller, Component.Identifier component,
			Action action, INPUT_ACTION_TYPE actionType)
	{
		if(controller == null)
			throw new RuntimeException("InputManager.associateAction: controller is NULL!");
		
		//find the controller with the specified ID
		for (Controller c : controllers ) 
		{
			if (c == controller) 
			{
				//put a new ComponentState record in the component entry for the controller
				controllerStates.get(c).put(component, new ComponentStateInfo(false, actionType));
				
				//put a new Action in the component entry for the controller
				return controllerActions.get(c).put(component, action);
			}
		}
		
		throw new RuntimeException("InputManager.associateAction(): controller '" + controller.getName() + "' not found");		
	}
	
	private boolean prettyCloseTo(float actualValue, float referenceValue) {
		double delta = Math.abs(Math.abs(actualValue) - Math.abs(referenceValue));
		if (delta > 0.01) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Returns an {@link ArrayList} containing the currently attached device
	 * controllers.
	 * 
	 */
	@Override
	public ArrayList<Controller> getControllers() {
		return controllers;
	}

	/**
	 * Returns the name string for the Keyboard controller (if more than one keyboard is present,
	 * the first one is returned).  If no keyboard controller is found, null is returned.
	 */
	public String getKeyboardName() {
		for (Controller c : controllers) {
			if (c.getType().equals(Controller.Type.KEYBOARD)) {
				return c.getName();
			}
		}
		return null ;
	}
	
	/**
	 * Returns the name string for the Mouse controller (if more than one mouse is present,
	 * the first one is returned).  If no mouse controller is found, null is returned.
	 */
	public String getMouseName() {
		for (Controller c : controllers) {
			if (c.getType().equals(Controller.Type.MOUSE)) {
				return c.getName();
			}
		}
		return null ;
	}
	
	/**
	 * Returns the name string for the first gamepad controller.  If no gamepad controller is found, 
	 * null is returned.
	 */
	public String getFirstGamepadName() {
		for (Controller c : controllers) {
			if ((c.getType().equals(Controller.Type.GAMEPAD))||(c.getType().equals(Controller.Type.STICK))) {
				return c.getName();
			}
		}
		return null ;		
	}
	
	/**
	 * Returns the name string for the second gamepad controller (if less than two gamepad controllers
	 * are present, null is returned).
	 */
	public String getSecondGamepadName() {
		boolean foundFirst = false;
		for (Controller c : controllers) {
			if (((c.getType().equals(Controller.Type.GAMEPAD))||(c.getType().equals(Controller.Type.STICK))) && !foundFirst) {
				foundFirst = true;
				continue;
			}
			if (((c.getType().equals(Controller.Type.GAMEPAD))||(c.getType().equals(Controller.Type.STICK))) && foundFirst) {
				return c.getName();
			}
		}
		return null ;		
		
	}
	
	/**
	 * Returns the {@link net.java.games.input.Controller} with the specified name, or null if
	 * no controller of the specified name is installed.
	 */
	public Controller getControllerByName(String name) {
		for (Controller c : controllers) {
			if (c.getName().equalsIgnoreCase(name)) {
				return c ;
			}
		}
		//no controller by the specified name found
		return null ;
	}
	
	
	/* NEW CODE */
	
	/**
	 * Returns the Keyboard controller (if more than one keyboard is present,
	 * the first one is returned).  If no keyboard controller is found, null is returned.
	 */
	public Controller getKeyboardController()
	{
		return getNthOccurrenceController(1, Controller.Type.KEYBOARD);
	}
	
	/**
	 * Returns the <code>nth</code> occurrence of a keyboard {@link net.java.games.input.Controller}.
	 * If no keyboard controller is found, null is returned.
	 * 
	 * @param n Occurrence number, such as 1 for first occurrence of a keyboard, or 2 for second occurrence of a keyboard.
	 */
	public Controller getKeyboardController(int n)
	{
		return getNthOccurrenceController(n, Controller.Type.KEYBOARD);
	}
	
	/**
	 * Returns the first keyboard controller with <code>n</code> or more components. 
	 * If no such keyboard controller is found, null is returned.
	 * 
	 * @param n The minimum number of components the keyboard should have.
	 */
	public Controller getKeyboardControllerWithNComponents(int n)
	{
		for (Controller c : controllers)
		{
			if (c.getType().equals(Controller.Type.KEYBOARD) &&
				c.getComponents().length >= n)
			{
				return c;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the Mouse controller (if more than one mouse is present,
	 * the first one is returned).  If no mouse controller is found, null is returned.
	 */
	public Controller getMouseController()
	{
		return getNthOccurrenceController(1, Controller.Type.MOUSE);
	}
	
	/**
	 * Returns the <code>nth</code> occurrence of a Mouse {@link net.java.games.input.Controller}.
	 * If no mouse controller is found, null is returned.
	 * 
	 * @param n Occurrence number, such as 1 for first occurrence of a mouse, or 2 for second occurrence of a mouse.
	 */
	public Controller getMouseController(int n)
	{
		return getNthOccurrenceController(n, Controller.Type.MOUSE);
	}
	
	/**
	 * Returns the <code>nth</code> occurrence of a Gamepad {@link net.java.games.input.Controller}. 
	 * 
	 * @param n Occurrence number, such as 1 for first occurrence of a gamepad, or 2 for second occurrence of a gamepad. 
	 */
	public Controller getGamepadController(int n)
	{
		return getNthOccurrenceController(n, Controller.Type.GAMEPAD);
	}
	
	/**
	 * Returns the <code>nth</code> occurrence of a Joystick {@link net.java.games.input.Controller}. 
	 * 
	 * @param n Occurrence number, such as 1 for first occurrence of a joystick, or 2 for second occurrence of a joystick. 
	 */
	public Controller getJoystickController(int n)
	{
		return getNthOccurrenceController(n, Controller.Type.STICK);
	}
	
	/**
	 * Returns the <code>nth</code> occurrence of a Fingerstick {@link net.java.games.input.Controller}. 
	 * 
	 * @param n Occurrence number, such as 1 for first occurrence of a Fingerstick, or 2 for second occurrence of a Fingerstick. 
	 */
	public Controller getFingerstickController(int n)
	{
		return getNthOccurrenceController(n, Controller.Type.FINGERSTICK);
	}
	
	/**
	 * Returns the <code>nth</code> occurrence of a Headtracker {@link net.java.games.input.Controller}. 
	 * 
	 * @param n Occurrence number, such as 1 for first occurrence of a Headtracker, or 2 for second occurrence of a Headtracker. 
	 */
	public Controller getHeadtrackerController(int n)
	{
		return getNthOccurrenceController(n, Controller.Type.HEADTRACKER);
	}
	
	/**
	 * Returns the <code>nth</code> occurrence of a Rudder {@link net.java.games.input.Controller}. 
	 * 
	 * @param n Occurrence number, such as 1 for first occurrence of a Rudder, or 2 for second occurrence of a Rudder. 
	 */
	public Controller getRudderController(int n)
	{
		return getNthOccurrenceController(n, Controller.Type.RUDDER);
	}
	
	/**
	 * Returns the <code>nth</code> occurrence of a Trackball {@link net.java.games.input.Controller}. 
	 * 
	 * @param n Occurrence number, such as 1 for first occurrence of a Trackball, or 2 for second occurrence of a Trackball. 
	 */
	public Controller getTrackballController(int n)
	{
		return getNthOccurrenceController(n, Controller.Type.TRACKBALL);
	}
	
	/**
	 * Returns the <code>nth</code> occurrence of a Trackpad {@link net.java.games.input.Controller}. 
	 * 
	 * @param n Occurrence number, such as 1 for first occurrence of a Trackpad, or 2 for second occurrence of a Trackpad. 
	 */
	public Controller getTrackpadController(int n)
	{
		return getNthOccurrenceController(n, Controller.Type.TRACKPAD);
	}
	
	/**
	 * Returns the <code>nth</code> occurrence of a Wheel {@link net.java.games.input.Controller}. 
	 * 
	 * @param n Occurrence number, such as 1 for first occurrence of a Wheel, or 2 for second occurrence of a Wheel. 
	 */
	public Controller getWheelController(int n)
	{
		return getNthOccurrenceController(n, Controller.Type.WHEEL);
	}
	
	/**
	 * Returns the <code>nth</code> occurrence of a {@link net.java.games.input.Controller} with the 
	 * specified <code>type</code>. 
	 * 
	 * @param n Occurrence number, such as 1 for first occurrence of a gamepad, or 2 for second occurrence of a gamepad. 
	 */
	private Controller getNthOccurrenceController(int x, Controller.Type type)
	{
		int occurrence = 1;
		
		for (Controller c : controllers)
		{
			if (c.getType().equals(type) && occurrence != x)
			{
				occurrence++;
				continue;
			}
			
			if (c.getType().equals(type) && occurrence == x)
			{
				return c;
			}
		}
		
		return null;
	}
	
	/**
	 * Parses the list of Controllers returned by JInput to only include Controllers that have a known
	 * controller Type and have a least 1 Component. 
	 */
	private void parseControllersList()
	{
		// get an array of the currently available controllers from JInput
		Controller[] jinput_controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
				
		// copy the controllers from the fixed-size array to an ArrayList
		// for processing by the update() loop
		for (Controller c : jinput_controllers) 
		{
			// Only add the controller if it is of a known controller type and it has at least 1 component
			if(!c.getType().equals(Controller.Type.UNKNOWN) && c.getComponents().length > 0)
			{
				c.setEventQueueSize(128);
				controllers.add(c);
			}
		}
	}
	
	/**
	 * Prints a list of all connected controllers to the console.
	 * @param printComponents <code>TRUE</code> if all the components of the controller should also be printed. 
	 * <code>FALSE</code> if not. 
	 */
	public void printControllers(boolean printComponents)
	{
		for (Controller c : controllers)
		{
			listComponents(c, printComponents);
		}
	}
	
	/**
	 * Prints a specific controllers information to the console.
	 * @param c The controller whose information is to be printed. 
	 * @param printComponents <code>TRUE</code> if all the components of the controller should also be printed. 
	 * <code>FALSE</code> if not.
	 */
	public void printController(Controller c, boolean printComponents)
	{
		listComponents(c, printComponents);
	}
	
	/**
	 * Lists the individual components of a controller. 
	 * @param c The controller whose information is to be printed.
	 * @param printComponents <code>TRUE</code> if all the components of the controller should also be printed. 
	 * <code>FALSE</code> if not.
	 */
	private void listComponents(Controller c, boolean printComponents)
	{
		System.out.println("Name: " + c.getName() + ". Type: " + c.getType());
		
		if(printComponents)
		{
			for (Component comp : c.getComponents())
			{
				System.out.println("\tName: " + comp.getName() + ". ID: " + comp.getIdentifier());
			}
		}
		
		int i = 1; 
		for (Controller subC : c.getControllers())
		{
			System.out.println(" " + c.getName() + " subcontroller #" + i);
			listComponents(subC, printComponents);
		}
	}

}
