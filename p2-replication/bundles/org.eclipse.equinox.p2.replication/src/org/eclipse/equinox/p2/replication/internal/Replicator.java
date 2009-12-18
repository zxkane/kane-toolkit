package org.eclipse.equinox.p2.replication.internal;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
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

	public void save(OutputStream output, IInstallableUnit[] ius, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Save p2 installation", 1000); //$NON-NLS-1$
		try {
			P2FWriter writer = new P2FWriter(output, null);
			writer.start(P2FWriter.P2F_ELEMENT);

			int percent = 1000/ius.length;
			for(IInstallableUnit unit : ius) { 
				writer.writeIU(unit);
				subMonitor.worked(percent);
			}

			writer.end(P2FWriter.P2F_ELEMENT);
			writer.flush();
		} catch (UnsupportedEncodingException e) {
			// should not happen
		} finally {
			subMonitor.done();
		}
	}

}
