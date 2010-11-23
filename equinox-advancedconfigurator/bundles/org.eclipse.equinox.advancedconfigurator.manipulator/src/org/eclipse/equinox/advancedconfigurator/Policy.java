package org.eclipse.equinox.advancedconfigurator;

import org.eclipse.equinox.p2.metadata.IInstallableUnit;

public interface Policy {
	public String getName();

	public IInstallableUnit[] getComponents();
}
