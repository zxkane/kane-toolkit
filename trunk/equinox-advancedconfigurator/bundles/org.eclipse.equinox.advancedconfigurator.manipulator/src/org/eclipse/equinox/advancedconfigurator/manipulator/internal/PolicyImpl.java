package org.eclipse.equinox.advancedconfigurator.manipulator.internal;

import org.eclipse.equinox.advancedconfigurator.Policy;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;

public class PolicyImpl implements Policy {

	private String name;
	private IInstallableUnit[] topComponents;

	public String getName() {
		return name;
	}

	public IInstallableUnit[] getComponents() {
		return topComponents;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setComponents(IInstallableUnit[] topComponents) {
		this.topComponents = topComponents;
	}

}
