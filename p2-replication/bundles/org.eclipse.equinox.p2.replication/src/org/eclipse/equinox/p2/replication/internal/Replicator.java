package org.eclipse.equinox.p2.replication.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
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
import org.eclipse.equinox.internal.provisional.p2.director.ProfileChangeRequest;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IEngine;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.engine.IProvisioningPlan;
import org.eclipse.equinox.p2.engine.PhaseSetFactory;
import org.eclipse.equinox.p2.engine.ProvisioningContext;
import org.eclipse.equinox.p2.engine.query.UserVisibleRootQuery;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.planner.IPlanner;
import org.eclipse.equinox.p2.planner.IProfileChangeRequest;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.replication.Constants;
import org.eclipse.equinox.p2.replication.P2Replicator;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;

@SuppressWarnings("restriction")
public class Replicator implements P2Replicator { 

	private class ReplicateJob extends Job {

		private final Collection<IInstallableUnit> toBeInstalled;
		private final URI[] uris;

		public ReplicateJob(String name, URI[] uris, Collection<IInstallableUnit> units) {
			super(name);
			this.uris = uris;
			toBeInstalled = units;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			SubMonitor progress = SubMonitor.convert(monitor, 
					"P2 installation replication", 1000); //$NON-NLS-1$
			try {
				IPlanner planner = (IPlanner) agent.getService(IPlanner.SERVICE_NAME);
				IProfileChangeRequest request = ProfileChangeRequest.createByProfileId(agent, getSelfProfile().getProfileId());
				//				IProfileChangeRequest request = planner.createChangeRequest(getSelfProfile()); 
				request.addAll(toBeInstalled);
				for(IInstallableUnit unit : toBeInstalled)
					request.setInstallableUnitProfileProperty(unit, "org.eclipse.equinox.p2.type.root", "true"); //$NON-NLS-1$ //$NON-NLS-2$
				ProvisioningContext context = new ProvisioningContext(agent);
				context.setMetadataRepositories(uris);
				context.setArtifactRepositories(uris);
				IProvisioningPlan plan = planner.getProvisioningPlan(request, context, progress.newChild(100));
				IStatus result = plan.getStatus();
				if(!result.isOK())
					return result;
				IEngine engine = (IEngine) agent.getService(IEngine.SERVICE_NAME);
				result = engine.perform(plan, PhaseSetFactory.createDefaultPhaseSet(), progress.newChild(900));
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

	private IProfile selfProfile = null;
	private IProvisioningAgent agent = null;

	public void bind(IProvisioningAgent agent) {
		this.agent = agent;
		IProfileRegistry registry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
		if(registry != null) {
			String selfID = System.getProperty("eclipse.p2.profile"); //$NON-NLS-1$
			if(selfID == null)
				selfID = IProfileRegistry.SELF;
			selfProfile = registry.getProfile(selfID);
		}
	}

	public void unbind(IProvisioningAgent agent) {
		if(this.agent == agent) {
			this.agent = null;
			selfProfile = null;
		}
	}

	public IInstallableUnit[] getRootIUs() {
		if(selfProfile != null) {
			IQueryResult<IInstallableUnit> resutl = selfProfile.query(new UserVisibleRootQuery(), new NullProgressMonitor());
			return resutl.toArray(IInstallableUnit.class);
		}
		return null;
	}

	public IProfile getSelfProfile() {
		return selfProfile;
	}

	public IInstallableUnit[] save(OutputStream output, IInstallableUnit[] ius, IProgressMonitor monitor) throws ProvisionException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Save p2 installation", 1000); //$NON-NLS-1$
		try {
			List<URI> repositories = new ArrayList<URI>();
			IMetadataRepositoryManager repoManager = (IMetadataRepositoryManager) agent.getService(IMetadataRepositoryManager.SERVICE_NAME);
			URI[] uris = repoManager.getKnownRepositories(IMetadataRepositoryManager.REPOSITORIES_ALL);
			final Map<IInstallableUnit, IQuery<IInstallableUnit>> queries = new HashMap<IInstallableUnit, IQuery<IInstallableUnit>>(ius.length, 1);
			for(int i = 0; i < ius.length; i++)
				queries.put(ius[i], QueryUtil.createIUQuery(ius[i].getId(), ius[i].getVersion()));
			int elapsed = 0;
			for(URI uri : uris) {
				if(queries.isEmpty())
					break;
				try{
					IMetadataRepository repo = repoManager.loadRepository(uri, subMonitor.newChild(500/uris.length));
					IQueryResult<IInstallableUnit> result = repo.query(QueryUtil.createCompoundQuery(queries.values(), false), 
							subMonitor.newChild(400/uris.length));
					if(!result.isEmpty()) {
						repositories.add(uri);
						for(IInstallableUnit unit : result.toSet())
							queries.remove(unit);
					}
				} catch(ProvisionException e) {
					// ignore the provision exception when loadRepository, it might be caused by network issue.
				} finally {
					elapsed += 900/uris.length;
					subMonitor.setWorkRemaining(1000 - elapsed);
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

			return queries.keySet().toArray(new IInstallableUnit[queries.size()]);
		} catch (UnsupportedEncodingException e) {
			// should not happen
			return new IInstallableUnit[0];
		} finally {
			subMonitor.done();
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

	public void replicate(String[] repositories, IInstallableUnit[] rootIUs, IProgressMonitor monitor) throws ProvisionException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Import p2 installation", 1000); //$NON-NLS-1$

		try{
			List<URI> uris = new ArrayList<URI>(repositories.length);
			IMetadataRepositoryManager repoManager = (IMetadataRepositoryManager) agent.getService(IMetadataRepositoryManager.SERVICE_NAME);
			for(String repository : repositories) {
				URI uri = URI.create(repository);
				if(!repoManager.contains(uri)) {
					repoManager.addRepository(uri);
					repoManager.loadRepository(uri, subMonitor.newChild(900/repositories.length));
				}
				uris.add(uri);
			}
			subMonitor.setWorkRemaining(100);
			List<IInstallableUnit> list = new ArrayList<IInstallableUnit>();
			for (IInstallableUnit iu : rootIUs)
				list.add(iu);
			ReplicateJob job = new ReplicateJob("Install", uris.toArray(new URI[uris.size()]), list); //$NON-NLS-1$
			job.belongsTo(this);
			job.schedule();
		} finally {
			subMonitor.done();
		}
	}
}
