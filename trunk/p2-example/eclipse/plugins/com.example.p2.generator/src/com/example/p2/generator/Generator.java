package com.example.p2.generator;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.internal.p2.artifact.repository.ArtifactRepositoryManager;
import org.eclipse.equinox.internal.p2.metadata.repository.MetadataRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRepository;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepository;
import org.eclipse.equinox.internal.provisional.p2.repository.IRepository;
import org.eclipse.equinox.internal.provisional.spi.p2.artifact.repository.SimpleArtifactRepositoryFactory;
import org.eclipse.equinox.internal.provisional.spi.p2.metadata.repository.SimpleMetadataRepositoryFactory;
import org.eclipse.equinox.p2.publisher.IPublisherAction;
import org.eclipse.equinox.p2.publisher.IPublisherInfo;
import org.eclipse.equinox.p2.publisher.Publisher;
import org.eclipse.equinox.p2.publisher.PublisherInfo;
import org.eclipse.equinox.p2.publisher.eclipse.BundlesAction;
import org.eclipse.equinox.p2.publisher.eclipse.FeaturesAction;
import org.eclipse.osgi.service.resolver.BundleDescription;

@SuppressWarnings("restriction")
public class Generator implements IApplication {

	private static final String SOURCE = "-source";
	private static final String REPOSITORY = "-repository";
	private static final String HELP = "-help";
	private URI repositoryURI;
	private File source;
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		try{
			parse((String[])context.getArguments().get("application.args"));
		} catch (Exception e) {
			showUsage();
			return null;
		}
		IPublisherInfo info = createPublisherInfo();
		IPublisherAction[] actions = createActions();
		Publisher publisher = new Publisher(info);
		publisher.publish(actions, new NullProgressMonitor());
		return IApplication.EXIT_OK;
	}
	
	private void showUsage() {
		System.out.println("-----------------------------------------------------"); //$NON-NLS-1$
		System.out.println("-	              		Usage			            -"); //$NON-NLS-1$
		System.out.println("-----------------------------------------------------"); //$NON-NLS-1$
		System.out.println(HELP + "          print this help                    -"); //$NON-NLS-1$
		System.out.println(REPOSITORY + "      the location of outputing repository             -"); //$NON-NLS-1$
		System.out.println(SOURCE + "      the location of generating features and plugins      -"); //$NON-NLS-1$		
		System.out.println("-----------------------------------------------------"); //$NON-NLS-1$
	}
 
	private void parse(String[] args) {
		for(int i = 0; i < args.length; i++) {
			if(args[i].equalsIgnoreCase(HELP)) {
				throw new IllegalArgumentException();
			}else if(args[i].equalsIgnoreCase(REPOSITORY))
				repositoryURI = new File(args[++i]).toURI();
			else if(args[i].equalsIgnoreCase(SOURCE)) {
				source = new File(args[++i]);
				if(!source.isDirectory())
					throw new IllegalArgumentException();
			}
		}
		if(source == null || repositoryURI == null)
			throw new IllegalArgumentException();
	}

	@Override
	public void stop() {
 
	}
 
	public IPublisherInfo createPublisherInfo() throws ProvisionException, URISyntaxException {
		PublisherInfo result = new PublisherInfo();
 
		// Create the metadata repository.  This will fail if a repository already exists here
		IMetadataRepository metadataRepository = new SimpleMetadataRepositoryFactory().create(repositoryURI, "Sample Metadata Repository", MetadataRepositoryManager.TYPE_SIMPLE_REPOSITORY, Collections.EMPTY_MAP);
		metadataRepository.setProperty(IRepository.PROP_COMPRESSED, "true"); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Create the artifact repository.  This will fail if a repository already exists here
		IArtifactRepository artifactRepository = new SimpleArtifactRepositoryFactory().create(repositoryURI, "Sample Artifact Repository", ArtifactRepositoryManager.TYPE_SIMPLE_REPOSITORY, Collections.EMPTY_MAP);
		artifactRepository.setProperty(IRepository.PROP_COMPRESSED, "true"); //$NON-NLS-1$ //$NON-NLS-2$
		
		result.setMetadataRepository(metadataRepository);
		result.setArtifactRepository(artifactRepository);
		result.setArtifactOptions(IPublisherInfo.A_PUBLISH | IPublisherInfo.A_INDEX);
		result.addAdvice(new NativeLauncherTouchPoint());
		result.addAdvice(new PropertyAdvise());
		return result;
	}
 
 
	public IPublisherAction[] createActions() {
		IPublisherAction[] result = new IPublisherAction[0];
		File plugin = new File(source, "plugins");
		if(plugin.exists() && plugin.isDirectory()) {
			File[] plugins = plugin.listFiles();
			BundleDescription[] bundleDescriptions = new BundleDescription[plugins.length];
			for(int i = 0; i < plugins.length; i++) {
				bundleDescriptions[i] =  BundlesAction.createBundleDescription(plugins[i]);
			}
			BundlesAction bundlesAction = new BundlesAction(bundleDescriptions);
			result = mergeActions(result, bundlesAction);
		}
		File feature = new File(source, "features");
		if(feature.exists() && feature.isDirectory()) {
			File[] features = feature.listFiles();
			FeaturesAction featuresAction = new FeaturesAction(features);
			result = mergeActions(result, featuresAction);
		}
		
		return result;
	}

	private IPublisherAction[] mergeActions(IPublisherAction[] result,
			IPublisherAction action) {
		IPublisherAction[] tmp = new IPublisherAction[result.length + 1];
		tmp[0] = action;
		System.arraycopy(result, 0, tmp, 1, result.length);
		result = tmp;
		return result;
	}
}
