package org.eclipse.equinox.p2.replication;

import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;

@SuppressWarnings("restriction")
public interface P2Replicator {

	IProfile getSelfProfile();
	IInstallableUnit[] getRootIUs();

	void save(OutputStream output, IInstallableUnit[] ius, IProgressMonitor monitor);
}
