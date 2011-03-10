package org.eclipse.equinox.internal.p2.importexport.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.internal.p2.importexport.FeatureDetail;
import org.eclipse.equinox.internal.p2.importexport.P2ImportExport;
import org.eclipse.equinox.internal.p2.importexport.persistence.P2FParser;
import org.eclipse.equinox.internal.p2.importexport.persistence.P2FWriter;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IEngine;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.engine.IProvisioningPlan;
import org.eclipse.equinox.p2.engine.PhaseSetFactory;
import org.eclipse.equinox.p2.engine.ProvisioningContext;
import org.eclipse.equinox.p2.engine.query.UserVisibleRootQuery;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.VersionRange;
import org.eclipse.equinox.p2.planner.IPlanner;
import org.eclipse.equinox.p2.planner.IProfileChangeRequest;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.osgi.util.NLS;

public class ImportExportImpl implements P2ImportExport { 

	private static final String SCHEME_FILE = "file"; //$NON-NLS-1$

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
				IProfileChangeRequest request = planner.createChangeRequest(getSelfProfile()); 
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

	private IProfile selfProfile = null;
	private IProvisioningAgent agent = null;
	private IProvisioningAgentProvider provider;

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

	public void bindProvider(IProvisioningAgentProvider provider) {
		this.provider = provider;
	}

	public IProvisioningAgentProvider getAgentProvider() {
		return provider;
	}

	public IProvisioningAgent getAgent() {
		return agent;
	}

	public List<FeatureDetail> importP2F(InputStream input) throws IOException {
		P2FParser parser = new P2FParser(Platform.getBundle(Constants.Bundle_ID).getBundleContext(), 
				Constants.Bundle_ID);
		parser.parse(input);
		return parser.getFeatures();
	}

	public IStatus exportP2F(OutputStream output, IInstallableUnit[] ius,
			IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, Messages.Replicator_ExportJobName, 1000);
		IMetadataRepositoryManager repoManager = (IMetadataRepositoryManager) agent.getService(IMetadataRepositoryManager.SERVICE_NAME);
		URI[] uris = repoManager.getKnownRepositories(IMetadataRepositoryManager.REPOSITORIES_ALL);
		Arrays.sort(uris, new Comparator<URI>() {
			public int compare(URI o1, URI o2) {
				String scheme1 = o1.getScheme();
				String scheme2 = o2.getScheme();
				if (scheme1.equals(scheme2))
					return 0;
				if(SCHEME_FILE.equals(scheme1)) {
					return 1;
				} else if (SCHEME_FILE.equals(scheme2)) {
					return -1;
				}
				return 0;
			}
		});
		List<IMetadataRepository> repos = new ArrayList<IMetadataRepository>(uris.length);
		for (URI uri : uris) {
			try {
				IMetadataRepository repo = repoManager.loadRepository(uri, subMonitor.newChild(500/uris.length, SubMonitor.SUPPRESS_ALL_LABELS));
				repos.add(repo);
			} catch (ProvisionException e) {
				// ignore
			}
		}
		subMonitor.setWorkRemaining(500);
		List<FeatureDetail> features = new ArrayList<FeatureDetail>(ius.length);
		SubMonitor sub2 = subMonitor.newChild(450, SubMonitor.SUPPRESS_ALL_LABELS);
		sub2.setWorkRemaining(ius.length * 100);
		MultiStatus queryRepoResult = new MultiStatus(Constants.Bundle_ID, 0, null, null);
		for (IInstallableUnit iu : ius) {
			List<URI> referredRepos = new ArrayList<URI>(1);
			SubMonitor sub3 = sub2.newChild(100);
			sub3.setWorkRemaining(repos.size() * 100);
			for (IMetadataRepository repo : repos) {
				if (SCHEME_FILE.equals(repo.getLocation().getScheme()) && referredRepos.size() > 0)
					break;
				IQueryResult<IInstallableUnit> result = repo.query(QueryUtil.createIUQuery(iu.getId(), new VersionRange(iu.getVersion(), true, null, true)), sub3.newChild(100));
				if (!result.isEmpty())
					referredRepos.add(repo.getLocation());
			}
			sub3.setWorkRemaining(1).worked(1);
			if (referredRepos.size() == 0) {
				queryRepoResult.add(new Status(IStatus.WARNING, Constants.Bundle_ID, NLS.bind(Messages.Replicator_NotFoundInRepository, 
						iu.getProperty(IInstallableUnit.PROP_NAME, Locale.getDefault().toString()))));
			} else {
				if (SCHEME_FILE.equals(referredRepos.get(0).getScheme()))
					queryRepoResult.add(new Status(IStatus.INFO, Constants.Bundle_ID, NLS.bind(Messages.Replicator_InstallFromLocal,
							iu.getProperty(IInstallableUnit.PROP_NAME, Locale.getDefault().toString()))));
				else {
					FeatureDetail feature = new FeatureDetail(iu, referredRepos);
					features.add(feature);
				}
			}
		}
		subMonitor.setWorkRemaining(50);
		IStatus status = exportP2F(output, features, subMonitor);
		if (status.isOK() && queryRepoResult.isOK())
			return status;
		MultiStatus rt = new MultiStatus(Constants.Bundle_ID, 0, new IStatus[]{queryRepoResult, status}, null, null);
		return rt;
	}

	public IStatus exportP2F(OutputStream output, List<FeatureDetail> features,
			IProgressMonitor monitor) {
		SubMonitor sub = SubMonitor.convert(monitor, Messages.Replicator_SaveJobName, 100);
		try {
			P2FWriter writer = new P2FWriter(output, null);
			writer.write(features);
			return Status.OK_STATUS;
		} catch (UnsupportedEncodingException e) {
			return new Status(IStatus.ERROR, Constants.Bundle_ID, e.getMessage(), e);
		} finally {
			sub.worked(100);
		}

	}
}
