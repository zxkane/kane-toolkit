package org.eclipse.equinox.advancedconfigurator.manipulator.internal;

import org.eclipse.equinox.advancedconfigurator.Policy;

public class PolicyImpl implements Policy {

	private String name;
	private Component[] topComponents;

	public String getName() {
		return name;
	}

	public Component[] getComponents() {
		return topComponents;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setComponents(Component[] topComponents) {
		this.topComponents = topComponents;
	}

}
