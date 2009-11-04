package com.example.p2.installer;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper;
import org.eclipse.equinox.internal.p2.engine.SimpleProfileRegistry;
import org.eclipse.equinox.internal.p2.engine.SurrogateProfileHandler;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.core.VersionRange;
import org.eclipse.equinox.internal.provisional.p2.director.IPlanner;
import org.eclipse.equinox.internal.provisional.p2.director.ProfileChangeRequest;
import org.eclipse.equinox.internal.provisional.p2.director.ProvisioningPlan;
import org.eclipse.equinox.internal.provisional.p2.engine.DefaultPhaseSet;
import org.eclipse.equinox.internal.provisional.p2.engine.IEngine;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfileRegistry;
import org.eclipse.equinox.internal.provisional.p2.engine.ProvisioningContext;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.InstallableUnitQuery;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepository;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.query.Collector;
import org.eclipse.equinox.internal.provisional.p2.query.ContextQuery;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;


@SuppressWarnings("restriction")
public class ProvisioningJob extends UIJob {

	private class LatestNoninstalledIUCollector extends Collector {
		/** Remember the profile this collector is operating on */
		private IProfile fProfile = null;
		public LatestNoninstalledIUCollector(IProfile profile){
			fProfile = profile;
		}
		@SuppressWarnings("unchecked")
		@Override
		public boolean accept(Object match) {
			if (! (match instanceof IInstallableUnit) )
				return super.accept(match);
			IInstallableUnit unit = (IInstallableUnit)match;
			Collector result = fProfile.query(new InstallableUnitQuery(unit.getId(),VersionRange.emptyRange), new Collector(), null);
			/** We found a unit that is not in the profile */
			if (result.size() == 0)
				return super.accept(unit);
			for (IInstallableUnit oldUnit: (Collection<IInstallableUnit>)result.toCollection()){
				if (unit.getUpdateDescriptor().isUpdateOf(oldUnit))
					return super.accept(unit);
			}
			return true;
		}
	}
	private class AccpetQuery extends ContextQuery{

		@Override
		public Collector perform(Iterator iterator, Collector result) {
			while(iterator.hasNext()){
				Object obj = iterator.next();
				if(obj instanceof IInstallableUnit) {
					String filterStr = ((IInstallableUnit)obj).getFilter();
					if(filterStr != null) {
						try {
							Filter filter = Activator.getDefault().getBundle().getBundleContext().createFilter(filterStr);
							Dictionary<String, String> environment = new Hashtable<String, String>(3);
							environment.put("osgi.ws", Platform.getWS()); //$NON-NLS-1$
							environment.put("osgi.os", Platform.getOS()); //$NON-NLS-1$
							environment.put("osgi.arch", Platform.getOSArch()); //$NON-NLS-1$
							if(filter.match(environment))
								result.accept(obj);
						} catch (InvalidSyntaxException e) {
							// ignore
						}
					} else
						result.accept(obj);
				}
			}
			return result;
		}
	}

	private final File installLocation;
	private static final String PROFILE_ID = "installedrcp";
	private final URI[] uris;
	private ServiceRegistration profileRegistryRegistration = null;
	
	
	public ProvisioningJob(String name, URI repository, File location) {
		super(name);
		installLocation = location;
		uris = new URI[] {repository};
	}
		
	private void registerProfileRegistry(){
		File loc = new File(installLocation, "p2");
		if(!loc.exists())
			loc.mkdirs();
		/** The surrogate handler will take of moving the installer profile for us here */
		IProfileRegistry profileRegistry = new SimpleProfileRegistry(loc, new SurrogateProfileHandler(), true);
		/** Make sure this service takes precedence */
		Dictionary<String, Object> props = new Hashtable<String, Object>(1, 1);
		props.put(Constants.SERVICE_RANKING, new Integer(Integer.MAX_VALUE));
		profileRegistryRegistration = Activator.getDefault().getBundle().getBundleContext().registerService(IProfileRegistry.class.getName(), profileRegistry, props);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		IStatus result = null;
		IProgressMonitor progress = SubMonitor.convert(monitor, "Installation", 1000);
		try{
			registerProfileRegistry();
			IProfileRegistry registry = 
				(IProfileRegistry) ServiceHelper.getService(Activator.getDefault().getBundle().getBundleContext(), IProfileRegistry.class.getName());
			IProfile[] profiles = registry.getProfiles();
			IProfile currentProfile = null;
			for(IProfile profile : profiles){
				if(PROFILE_ID.equals(profile.getProfileId())){
					currentProfile = profile;
					break;
				}
			}
			if(currentProfile == null){
				try {
					Map<String, String> profileProperties = new HashMap<String, String>();
					profileProperties.put(IProfile.PROP_INSTALL_FOLDER, installLocation.getAbsolutePath());
					profileProperties.put(IProfile.PROP_FLAVOR, "tooling"); //$NON-NLS-1$
					profileProperties.put(IProfile.PROP_ENVIRONMENTS, "osgi.os=" + Platform.getOS() + ",osgi.ws=" + Platform.getWS() + ",osgi.arch=" + Platform.getOSArch()); //$NON-NLS-1$;
					profileProperties.put(IProfile.PROP_NL, "en_US"); //$NON-NLS-1$
					profileProperties.put(IProfile.PROP_INSTALL_FEATURES, "true");
					profileProperties.put(IProfile.PROP_CONFIGURATION_FOLDER, new File(installLocation, "configuration").getAbsolutePath());
					profileProperties.put(IProfile.PROP_ROAMING, "true");
					profileProperties.put(IProfile.PROP_CACHE, installLocation.getAbsolutePath());
					currentProfile = registry.addProfile(PROFILE_ID, profileProperties);
				} catch (ProvisionException e) {
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to create profile.");
				}
			}
			progress.worked(2);

			ArrayList<IInstallableUnit> ius = new ArrayList<IInstallableUnit>();
			IMetadataRepositoryManager repositoryManager = (IMetadataRepositoryManager) ServiceHelper.getService(Activator.getDefault().getBundle().getBundleContext(),
					IMetadataRepositoryManager.class.getName()); 
			if (repositoryManager == null) 
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to get IMetadataRepositoryManager.");    
			try {
				for (URI uri : uris) {
					IMetadataRepository metaRepo = repositoryManager.loadRepository(uri, progress);
					Collector collector = metaRepo.query(new AccpetQuery(), new LatestNoninstalledIUCollector(currentProfile), null);
					ius.addAll(collector.toCollection());
				}
			} catch (ProvisionException e) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to get IMetadataRepository.");
			}
			ProfileChangeRequest request = ProfileChangeRequest.createByProfileId(currentProfile.getProfileId());  
			request.addInstallableUnits(ius.toArray(new IInstallableUnit[ius.size()]));
			IPlanner planner = (IPlanner) ServiceHelper.getService(Activator.getDefault().getBundle().getBundleContext(), 
					IPlanner.class.getName());
			ProvisioningContext context = new ProvisioningContext(uris);
			context.setArtifactRepositories(uris);
			ProvisioningPlan plan = planner.getProvisioningPlan(request, context, progress);
			result = plan.getStatus();
			if(!result.isOK())
				return result;

			IEngine engine = (IEngine) ServiceHelper.getService(Activator.getDefault().getBundle().getBundleContext(), 
					IEngine.SERVICE_NAME);
			result = engine.perform(currentProfile, new DefaultPhaseSet(), plan.getOperands(), context, progress);
		}finally{
			if(profileRegistryRegistration != null){
				profileRegistryRegistration.unregister();
				profileRegistryRegistration = null;
			}
			progress.done();
		}
		return result;
	}

}