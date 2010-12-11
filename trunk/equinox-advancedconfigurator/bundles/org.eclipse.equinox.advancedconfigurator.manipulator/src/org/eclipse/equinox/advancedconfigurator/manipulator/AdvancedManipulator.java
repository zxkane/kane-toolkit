package org.eclipse.equinox.advancedconfigurator.manipulator;

import org.eclipse.equinox.advancedconfigurator.Policy;
import org.eclipse.equinox.advancedconfigurator.Policy.Component;

public interface AdvancedManipulator {
	public Policy[] getPolicies();

	public void addPolicy(String policyName, boolean isDefault, Component[] components);

	public boolean setDefault(Policy policy);

	public void addManipulatorListener(ManipulatorListener l);

	public void removeManipulatorListener(ManipulatorListener l);

	public void updatePolicy(Policy oldPolicy, Policy newPolicy);
}
