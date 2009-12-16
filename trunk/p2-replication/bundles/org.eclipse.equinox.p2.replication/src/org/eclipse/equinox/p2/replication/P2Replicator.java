package org.eclipse.equinox.p2.replication;

import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;

@SuppressWarnings("restriction")
public interface P2Replicator {

	IProfile getSelfProfile();
	IInstallableUnit[] getRootIUs();
}
