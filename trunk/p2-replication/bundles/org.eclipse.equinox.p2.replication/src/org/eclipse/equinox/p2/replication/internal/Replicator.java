package org.eclipse.equinox.p2.replication.internal;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfileRegistry;
import org.eclipse.equinox.internal.provisional.p2.engine.IUProfilePropertyQuery;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.query.Collector;
import org.eclipse.equinox.p2.replication.P2Replicator;

@SuppressWarnings("restriction")
public class Replicator implements P2Replicator {

	private IProfileRegistry profileRegistry;
	private IProfile selfProfile = null;


	public void bindRegistry(IProfileRegistry registry) {
		profileRegistry = registry;
		selfProfile = registry.getProfile(IProfileRegistry.SELF);
	}

	public void unbindRegistry(IProfileRegistry registry) {
		if(profileRegistry == registry) {
			profileRegistry = null;
			selfProfile = null;
		}
	}

	public IInstallableUnit[] getRootIUs() {
		if(selfProfile != null) {
			Collector collector = new Collector();
			selfProfile.query(new IUProfilePropertyQuery(selfProfile, "org.eclipse.equinox.p2.type.root", "true"),  //$NON-NLS-1$ //$NON-NLS-2$
					collector, new NullProgressMonitor());
			return (IInstallableUnit[]) collector.toArray(IInstallableUnit.class);
		}
		return null;
	}

	public IProfile getSelfProfile() {
		return selfProfile;
	}

}
