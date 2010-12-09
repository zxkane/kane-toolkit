package org.eclipse.equinox.advancedconfigurator.manipulator;

import org.eclipse.equinox.advancedconfigurator.Policy;
import org.eclipse.equinox.advancedconfigurator.Policy.Component;

public interface AdvancedManipulator {
	public Policy[] getPolicies();

	public void addPolicy(String policyName, boolean isDefault, Component[] components);

	public void updatePolicy(Policy oldPolicy, Policy newPolicy);
}
