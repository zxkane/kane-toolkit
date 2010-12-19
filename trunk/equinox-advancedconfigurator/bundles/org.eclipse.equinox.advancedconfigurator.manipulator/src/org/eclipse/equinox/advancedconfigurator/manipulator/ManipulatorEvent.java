package org.eclipse.equinox.advancedconfigurator.manipulator;

import org.eclipse.equinox.advancedconfigurator.Policy;

public class ManipulatorEvent {
	public static final int ADD_POLICY = 1;
	public static final int REMOVE_POLICY = 2;
	public static final int Update_POLICY = 4;
	public static final int CHANGE_DEFAULT_POLICY = 8;

	private int eventType = 0;
	private Policy oldStatePolicy = null;
	private Policy newStatePolicy = null;

	public ManipulatorEvent(int type, Policy oldState, Policy newState) {
		eventType = type;
		this.oldStatePolicy = oldState;
		this.newStatePolicy = newState;
	}

	public Policy getOldStatePolicy() {
		return oldStatePolicy;
	}

	public Policy getNewStatePolicy() {
		return newStatePolicy;
	}

	public int getEventType() {
		return eventType;
	}
}
