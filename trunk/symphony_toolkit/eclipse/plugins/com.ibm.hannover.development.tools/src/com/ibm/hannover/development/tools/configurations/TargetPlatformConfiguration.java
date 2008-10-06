package com.ibm.hannover.development.tools.configurations;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PDEState;
import org.eclipse.pde.internal.core.PluginPathFinder;
import org.eclipse.pde.internal.core.TargetPlatformResetJob;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressConstants;

public class TargetPlatformConfiguration implements IConfigure{
	
	class ReloadOperation implements IRunnableWithProgress {
		private String location;
		
		public ReloadOperation(String platformPath) {
			this.location = platformPath;
		}
		
		/**
		 * the PluginPathFinder.getPluginPaths function will read the link file to 
		 * find plug-ins in other directories.
		 * Sometimes the link file will be lost, and the other directories are known for us.
		 * So we add them directly.
		 * location:
		 * C:\Program Files\IBM\Lotus\Symphony\framework\eclipse
		 * rcp:
		 * C:\Program Files\IBM\Lotus\Symphony\framework\rcp\eclipse
		 * shared:
		 * C:\Program Files\IBM\Lotus\Symphony\framework\shared\eclipse
		 */
		private URL[] computePluginURLs() {
			URL[] base  = PluginPathFinder.getPluginPaths(location);
			// add by Tang Qiao at 2008-09-17
			String baseDir = location.substring(0, location.lastIndexOf("framework")+"framework".length());			
			URL[] rcpPlugins = PluginPathFinder.getPluginPaths(baseDir+File.separatorChar+"rcp"+File.separatorChar+"eclipse");
			URL[] sharedPlugins = PluginPathFinder.getPluginPaths(baseDir+File.separatorChar+"shared"+File.separatorChar+"eclipse");
			// merge URLs
			Set<URL> allPlugins = new HashSet<URL>(base.length+rcpPlugins.length+sharedPlugins.length);
			for (int i=0; i<base.length; ++i)
				allPlugins.add(base[i]);
			for (int i=0; i<rcpPlugins.length; ++i)
				allPlugins.add(rcpPlugins[i]);
			for (int i=0; i<sharedPlugins.length; ++i)
				allPlugins.add(sharedPlugins[i]);
			base = allPlugins.toArray(base);
			// end of add by Tang Qiao at 2008-09-17
			return base;
		}
			
		public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {	
			monitor.beginTask(PDEUIMessages.TargetPluginsTab_readingPlatform, 10);
			SubProgressMonitor parsePluginMonitor = new SubProgressMonitor(monitor, 9);
			fCurrentState = new PDEState(computePluginURLs(), true, parsePluginMonitor);
			loadFeatures(new SubProgressMonitor(monitor, 1));
			monitor.done();
		}
		
		private void loadFeatures(IProgressMonitor monitor) {
			IFeatureModel[] workspaceModels = PDECore.getDefault().getFeatureModelManager().getWorkspaceModels();
			int numFeatures = workspaceModels.length;
			fCurrentFeatures = new HashMap((4/3) * (numFeatures) + 1);
			for (int i = 0; i < workspaceModels.length; i++) {
				String id = workspaceModels[i].getFeature().getId();
				if (id != null)
					fCurrentFeatures.put(id, workspaceModels[i]);
			}
			monitor.done();
		}
		
	}
	
	private String targetPlatform;
	private PDEState fCurrentState;
	private Map fCurrentFeatures;
	private HashSet fChangedModels = new HashSet();
	
	public TargetPlatformConfiguration(String targetPlatform){
		this.targetPlatform = targetPlatform;
	}
	
	public void configure(){
		handleReload();
		savePreferences();
		updateModels();
		Job job = new TargetPlatformResetJob(fCurrentState);
		job.schedule();
		job.setProperty(IProgressConstants.ICON_PROPERTY, PDEPluginImages.DESC_PLUGIN_OBJ);
	}

	private void savePreferences() {
		Preferences preferences = PDECore.getDefault().getPluginPreferences();
		IPath newPath = new Path(targetPlatform);
		URL installURL = Platform.getInstallLocation().getURL();
		IPath defaultPath = new Path(installURL.getFile()).removeTrailingSeparator();
		String mode =
			newPath.equals(defaultPath)
				? ICoreConstants.VALUE_USE_THIS
				: ICoreConstants.VALUE_USE_OTHER;
		preferences.setValue(ICoreConstants.TARGET_MODE, mode);
		preferences.setValue(ICoreConstants.PLATFORM_PATH, targetPlatform);
		preferences.setValue(ICoreConstants.CHECKED_PLUGINS, ICoreConstants.VALUE_SAVED_ALL);		
		preferences.setValue(ICoreConstants.GROUP_PLUGINS_VIEW, false);		
		StringBuffer buffer = new StringBuffer();
		preferences.setValue(ICoreConstants.ADDITIONAL_LOCATIONS, buffer.toString());
		PDECore.getDefault().savePluginPreferences();
	}

	public String getName() {
		return "Configure target platform";
	}
	
	protected void handleReload() {
		if (targetPlatform != null && targetPlatform.length() > 0) {
			ReloadOperation op = new ReloadOperation(targetPlatform);
			try {
				PlatformUI.getWorkbench().getProgressService().run(true, false, op);
			} catch (InvocationTargetException e) {
			} catch (InterruptedException e) {
			}
			fChangedModels.clear();
			handleSelectAll(true);
		}
	}	
	
	public void handleSelectAll(boolean selected) {
		IPluginModelBase[] allModels = getCurrentModels();
		for (int i = 0; i < allModels.length; i++) {
			IPluginModelBase model = allModels[i];
			if (model.isEnabled() != selected) {
				fChangedModels.add(model);
			} else if (fChangedModels.contains(model) && model.isEnabled() == selected) {
				fChangedModels.remove(model);
			}
		}
	}
	
	protected IPluginModelBase[] getCurrentModels() {
		if (fCurrentState != null)
			return fCurrentState.getTargetModels();
		return PDECore.getDefault().getModelManager().getExternalModels();
	}
	
	private void updateModels() {
		Iterator iter = fChangedModels.iterator();
		while (iter.hasNext()) {
			IPluginModelBase model = (IPluginModelBase) iter.next();
			model.setEnabled(true);
		}
	}
}
