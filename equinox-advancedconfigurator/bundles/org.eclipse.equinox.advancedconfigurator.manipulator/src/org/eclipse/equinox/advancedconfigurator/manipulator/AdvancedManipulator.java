package org.eclipse.equinox.advancedconfigurator.manipulator;

import org.eclipse.equinox.advancedconfigurator.Policy;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;


public interface AdvancedManipulator {
	public Policy[] getPolicies();

	public void addPolicy(String policyName, IInstallableUnit[] components);

	public void updatePolicy(Policy oldPolicy, Policy newPolicy);
}
