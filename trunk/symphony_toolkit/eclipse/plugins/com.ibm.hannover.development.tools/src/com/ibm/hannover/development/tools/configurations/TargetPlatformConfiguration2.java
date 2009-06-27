/**
 * 
 */
package com.ibm.hannover.development.tools.configurations;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.target.DirectoryBundleContainer;
import org.eclipse.pde.internal.core.target.TargetDefinition;
import org.eclipse.pde.internal.core.target.provisional.IBundleContainer;
import org.eclipse.pde.internal.core.target.provisional.ITargetDefinition;
import org.eclipse.pde.internal.core.target.provisional.ITargetHandle;
import org.eclipse.pde.internal.core.target.provisional.ITargetPlatformService;
import org.eclipse.pde.internal.core.target.provisional.LoadTargetDefinitionJob;
import org.eclipse.pde.internal.ui.shared.target.Messages;

import com.ibm.hannover.development.tools.Activator;

/**
 * @since 3.0
 * @author kane
 * @see
 *      <code>org.eclipse.pde.internal.ui.shared.target.EditDirectoryContainerPage</code>
 */
public class TargetPlatformConfiguration2 implements IConfigure {

	private static final String CLAZZ = TargetPlatformConfiguration2.class
			.getName();
	private static final Logger logger = Logger.getLogger(CLAZZ);

	/**
	 * @see
	 *      <code>org.eclipse.pde.internal.ui.shared.target.EditDirectoryContainerPage</code>
	 * @author kane
	 * 
	 */

	private static ITargetPlatformService fTargetService = null;
	protected IBundleContainer fContainer;
	private String targetPath;
	private String targetName;

	/**
	 * 
	 */
	public TargetPlatformConfiguration2(String targetPlatform, String name) {
		this.targetPath = targetPlatform;
		this.targetName = name;
	}

	/**
	 * Validate the input fields before a container is created/edited. The
	 * page's enablement, message and completion should be updated.
	 * 
	 * @return whether the finish button should be enabled and container
	 *         creation should continue
	 */
	protected boolean validateInput(String loc) {
		final String method = "validateInput"; //$NON-NLS-1$
		// Check if the text field is blank
		if (loc.trim().length() == 0) {
			logger.logp(Level.INFO, CLAZZ, method,
					Messages.AddDirectoryContainerPage_1);
			return false;
		}

		// Resolve any variables
		String locationString = null;
		try {
			locationString = VariablesPlugin.getDefault()
					.getStringVariableManager().performStringSubstitution(
							loc.trim());
		} catch (CoreException e) {
			logger.logp(Level.WARNING, CLAZZ, method, e.getMessage());
			return true;
		}
		File location = new File(locationString);

		// Check if directory exists
		if (!location.isDirectory()) {
			logger.logp(Level.WARNING, CLAZZ, method,
					Messages.AddDirectoryContainerPage_6);
		} else {
			logger.logp(Level.INFO, CLAZZ, method,
					Messages.AddDirectoryContainerPage_1);
		}
		return true;
	}

	/**
	 * Returns a bundle container based on the current inputs to the wizard. If
	 * a previous container is supplied, any relevant information it stores
	 * should be copied to the new container.
	 * 
	 * @param previous
	 *            previous container to grab information from or
	 *            <code>null</code> if a new container should be created
	 * @return a new or modified bundle container
	 * @throws CoreException
	 */
	protected IBundleContainer createContainer(IBundleContainer previous,
			String location) throws CoreException {
		IBundleContainer container = getTargetPlatformService()
				.newDirectoryContainer(location);
		if (previous instanceof DirectoryBundleContainer) {
			container.setIncludedBundles(previous.getIncludedBundles());
			container.setOptionalBundles(previous.getOptionalBundles());
		}
		return container;
	}

	/**
	 * Gets the target platform service provided by PDE Core
	 * 
	 * @return the target platform service
	 * @throws CoreException
	 *             if unable to acquire the service
	 */
	protected static ITargetPlatformService getTargetPlatformService()
			throws CoreException {
		if (fTargetService == null) {
			fTargetService = (ITargetPlatformService) PDECore.getDefault()
					.acquireService(ITargetPlatformService.class.getName());
			if (fTargetService == null) {
				throw new CoreException(new Status(IStatus.ERROR,
						Activator.PLUGIN_ID,
						Messages.AddDirectoryContainerPage_9));
			}
		}
		return fTargetService;
	}

	protected ITargetDefinition createTarget() throws CoreException {
		ITargetPlatformService service = getTargetPlatformService();
		if (service != null) {
			ITargetDefinition definition = service.newTarget();
			definition.setName(this.targetName);
			return definition;
		}
		return null;
	}

	/**
	 * the PluginPathFinder.getPluginPaths function will read the link file to
	 * find plug-ins in other directories. Sometimes the link file will be lost,
	 * and the other directories are known for us. So we add them directly.
	 * location: C:\Program Files\IBM\Lotus\Symphony\framework\eclipse rcp:
	 * C:\Program Files\IBM\Lotus\Symphony\framework\rcp\eclipse shared:
	 * C:\Program Files\IBM\Lotus\Symphony\framework\shared\eclipse
	 */
	private String[] computeTargets() {
		String baseDir = targetPath.substring(0, targetPath
				.lastIndexOf("framework") //$NON-NLS-1$
				+ "framework".length()); //$NON-NLS-1$
		String targetRCP = baseDir + File.separatorChar + "rcp" //$NON-NLS-1$
				+ File.separatorChar + "eclipse"; //$NON-NLS-1$
		String targetShared = baseDir + File.separatorChar + "shared" //$NON-NLS-1$
				+ File.separatorChar + "eclipse"; //$NON-NLS-1$
		return new String[] { targetRCP, targetShared };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.hannover.development.tools.configurations.IConfigure#configure()
	 */
	public void configure(IProgressMonitor monitor) throws CoreException {
		final ITargetPlatformService targetService = getTargetPlatformService();
		ITargetHandle[] handles = targetService.getTargets(monitor);
		ITargetDefinition fTarget = null;
		for (int i = 0; i < handles.length; i++) {
			ITargetDefinition definition = handles[i].getTargetDefinition();
			if (definition.getName().equals(this.targetName)) {
				fTarget = definition;
				break;
			}
		}
		if (fTarget == null) {
			fTarget = createNewTargetDefinition();
			monitor.worked(10);
			targetService.saveTargetDefinition(fTarget);
			monitor.worked(10);
		}
		// set it as active
		ITargetHandle activeHandle = targetService.getWorkspaceTargetHandle();
		if (activeHandle == null
				|| !activeHandle.equals(fTarget.getHandle())
				|| !((TargetDefinition) activeHandle.getTargetDefinition())
						.isContentEquivalent(fTarget)) {
			LoadTargetDefinitionJob.load(fTarget);
			monitor.worked(30);
		}
	}

	private ITargetDefinition createNewTargetDefinition() throws CoreException {
		ITargetDefinition fTarget = createTarget();
		final String[] locations = computeTargets();
		for (int i = 0; i < locations.length; i++) {
			// Validate the location and any other text fields
			if (validateInput(locations[i])) {
				fContainer = createContainer(fContainer, locations[i]);
				addToDefinition(fTarget, fContainer);
			} else {
				fContainer = null;
			}
		}
		return fTarget;
	}

	private void addToDefinition(ITargetDefinition fTarget,
			IBundleContainer container) {
		IBundleContainer[] oldContainers = fTarget.getBundleContainers();
		if (oldContainers == null) {
			fTarget.setBundleContainers(new IBundleContainer[] { container });
		} else {
			IBundleContainer[] newContainers = new IBundleContainer[oldContainers.length + 1];
			System.arraycopy(oldContainers, 0, newContainers, 0,
					oldContainers.length);
			newContainers[newContainers.length - 1] = container;
			fTarget.setBundleContainers(newContainers);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.hannover.development.tools.configurations.IConfigure#getName()
	 */
	public String getName() {
		return "Configure target platform"; //$NON-NLS-1$
	}

}
