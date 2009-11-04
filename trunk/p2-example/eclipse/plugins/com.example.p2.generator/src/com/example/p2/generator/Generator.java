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
import org.eclipse.osgi.service.resolver.BundleDescription;

@SuppressWarnings("restriction")
public class Generator implements IApplication {

	private URI repositoryURI;
	private File source;
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		try{
			parse((String[])context.getArguments().get("application.args"));
		} catch (Exception e) {
			System.out.println("Invalid arguments");
			return null;
		}
		IPublisherInfo info = createPublisherInfo();
		IPublisherAction[] actions = createActions();
		Publisher publisher = new Publisher(info);
		publisher.publish(actions, new NullProgressMonitor());
		return IApplication.EXIT_OK;
	}
 
	private void parse(String[] args) {
		for(int i = 0; i < args.length; i++) {
			if(args[i].equals("-repository"))
				repositoryURI = new File(args[++i]).toURI();
			else if(args[i].equals("-source"))
				source = new File(args[++i]);
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
		return result;
	}
 
 
	public IPublisherAction[] createActions() {
		IPublisherAction[] result = new IPublisherAction[1];
		BundleDescription[] bundleDescriptions = new BundleDescription[1];
		bundleDescriptions[0] =  BundlesAction.createBundleDescription(source);
		BundlesAction bundlesAction = new BundlesAction(bundleDescriptions);
		result[0] = bundlesAction;
		return result;
	}
}
