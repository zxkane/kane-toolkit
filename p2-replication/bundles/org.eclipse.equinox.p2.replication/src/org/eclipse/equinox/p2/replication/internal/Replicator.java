package org.eclipse.equinox.p2.replication.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.internal.p2.metadata.repository.io.XMLConstants;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.director.IPlanner;
import org.eclipse.equinox.internal.provisional.p2.director.ProfileChangeRequest;
import org.eclipse.equinox.internal.provisional.p2.director.ProvisioningPlan;
import org.eclipse.equinox.internal.provisional.p2.engine.DefaultPhaseSet;
import org.eclipse.equinox.internal.provisional.p2.engine.IEngine;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfileRegistry;
import org.eclipse.equinox.internal.provisional.p2.engine.IUProfilePropertyQuery;
import org.eclipse.equinox.internal.provisional.p2.engine.ProvisioningContext;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.InstallableUnitQuery;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepository;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.query.Collector;
import org.eclipse.equinox.internal.provisional.p2.query.CompoundQuery;
import org.eclipse.equinox.p2.replication.Constants;
import org.eclipse.equinox.p2.replication.P2Replicator;
import org.osgi.util.tracker.ServiceTracker;

@SuppressWarnings("restriction")
public class Replicator implements P2Replicator { 

	private class ReplicateJob extends Job {

		private final IInstallableUnit[] toBeInstalled;
		private final URI[] uris;

		public ReplicateJob(String name, URI[] uris, IInstallableUnit[] units) {
			super(name);
			this.uris = uris;
			toBeInstalled = units;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			SubMonitor progress = SubMonitor.convert(monitor, 
					"P2 installation replication", 1000); //$NON-NLS-1$
			try {
				ProfileChangeRequest request = ProfileChangeRequest.createByProfileId(getSelfProfile().getProfileId());
				request.addInstallableUnits(toBeInstalled);
				for(IInstallableUnit unit : toBeInstalled)
					request.setInstallableUnitProfileProperty(unit, "org.eclipse.equinox.p2.type.root", "true"); //$NON-NLS-1$ //$NON-NLS-2$
				IPlanner planner = getService(IPlanner.class);
				ProvisioningContext context = new ProvisioningContext(uris);
				context.setArtifactRepositories(uris);
				ProvisioningPlan plan = planner.getProvisioningPlan(request, context, progress.newChild(100));
				IStatus result = plan.getStatus();
				if(!result.isOK())
					return result;
				IEngine engine = getService(IEngine.class);
				result = engine.perform(getSelfProfile(), new DefaultPhaseSet(), plan.getOperands(), context, progress.newChild(900));
				return result;
			} finally {
				progress.done();
			}
		}

	}

	private final class Configuriation implements InstallationConfiguration{
		String[] repoStrings;
		IInstallableUnit[] ius;
		public String[] getRepositories() {
			return repoStrings;
		}

		public IInstallableUnit[] getRootIUs() {
			return ius;
		}
	}

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

	public void save(OutputStream output, IInstallableUnit[] ius, IProgressMonitor monitor) throws ProvisionException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Save p2 installation", 1000); //$NON-NLS-1$
		try {
			List<URI> repositories = new ArrayList<URI>();
			IMetadataRepositoryManager repoManager = getService(IMetadataRepositoryManager.class);
			URI[] uris = repoManager.getKnownRepositories(IMetadataRepositoryManager.REPOSITORIES_ALL);
			final Map<IInstallableUnit, InstallableUnitQuery> queries = new HashMap<IInstallableUnit, InstallableUnitQuery>(ius.length, 1);
			for(int i = 0; i < ius.length; i++)
				queries.put(ius[i], new InstallableUnitQuery(ius[i].getId(), ius[i].getVersion()));
			for(URI uri : uris) {
				if(queries.isEmpty())
					break;
				IMetadataRepository repo = repoManager.loadRepository(uri, subMonitor.newChild(500/uris.length));
				Collector result = repo.query(CompoundQuery.createCompoundQuery(queries.values().toArray(new InstallableUnitQuery[queries.size()]), false), 
						new Collector(), subMonitor.newChild(400/uris.length));
				if(result.size() > 0) {
					repositories.add(uri);
					for(Object unit : result.toCollection())
						queries.remove(unit);
				}
			}
			subMonitor.setWorkRemaining(100);
			P2FWriter writer = new P2FWriter(output, null);
			writer.start(P2FConstants.P2F_ELEMENT);
			writer.start(XMLConstants.INSTALLABLE_UNITS_ELEMENT);
			int percent = 100/ius.length;
			for(IInstallableUnit unit : ius) { 
				writer.writeIU(unit);
				subMonitor.worked(percent);
			}
			writer.end(XMLConstants.INSTALLABLE_UNITS_ELEMENT);
			writer.start(P2FConstants.REPOSITORIES_ELEMENT);
			for(URI uri : repositories) {
				writer.start(P2FConstants.REPOSITORY_ELEMENT);
				writer.attribute(P2FConstants.P2FURI_ATTRIBUTE, uri.toString());
				writer.end(P2FConstants.REPOSITORY_ELEMENT);
			}
			writer.end(P2FConstants.REPOSITORIES_ELEMENT);
			writer.end(P2FConstants.P2F_ELEMENT);
			writer.flush();
		} catch (UnsupportedEncodingException e) {
			// should not happen
		} finally {
			subMonitor.done();
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T getService(Class<T> clazz) {
		ServiceTracker serviceTracker = new ServiceTracker(Platform.getBundle(Constants.Bundle_ID).getBundleContext(), 
				clazz.getName(), null);
		serviceTracker.open();
		try {
			return (T)serviceTracker.getService();
		} finally {
			serviceTracker.close();
		}
	}

	public InstallationConfiguration load(InputStream input) throws IOException {
		P2FParser parser = new P2FParser(Platform.getBundle(Constants.Bundle_ID).getBundleContext(), 
				Constants.Bundle_ID);
		parser.parse(input);
		Configuriation conf = new Configuriation();
		conf.ius = parser.getIUs();
		conf.repoStrings = parser.getRepositories();
		return conf;
	}

	public void replicate(String[] repositories, IInstallableUnit[] rootIUs, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Import p2 installation", 1000); //$NON-NLS-1$

		try{
			List<URI> uris = new ArrayList<URI>(repositories.length);
			for(String repository : repositories) {
				URI uri = URI.create(repository);
				IMetadataRepositoryManager repoManager = getService(IMetadataRepositoryManager.class);
				if(!repoManager.contains(uri)) {
					repoManager.addRepository(uri);
				}
				uris.add(uri);
				subMonitor.worked(900/repositories.length);
			}
			subMonitor.setWorkRemaining(100);
			ReplicateJob job = new ReplicateJob("Install", uris.toArray(new URI[uris.size()]), rootIUs); //$NON-NLS-1$
			job.belongsTo(this);
			job.schedule();
		} finally {
			subMonitor.done();
		}
	}
}
