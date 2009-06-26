package com.ibm.hannover.development.tools.configurations;

import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;

import com.ibm.hannover.development.tools.Activator;
import com.ibm.hannover.development.tools.preferences.VMPreferencePage;

public class SymphonyPolicy {
	public static final String CLAZZ = NotesPolicy.class.getName();
	private static final Logger logger = Logger.getLogger(CLAZZ);
	private String installedPath, notesPath;
	private String launchFile, variableFile;
	
	public SymphonyPolicy(String launchFile, String variableFile){
		this.launchFile = launchFile;
		this.variableFile = variableFile;
	}
		

	
	public void configure(){
		// find symphony installed location. Return directly if it fails.
		try {
			installedPath = FinderUtility.findSymphonyInstalledLocation();
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "Can't find installed symphony."); //$NON-NLS-1$
			Platform.getLog(Activator.getDefault().getBundle()).log(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Can't find installed symphony.", null));
			return;
		}
		AutoConfigureJob job = new AutoConfigureJob("AutoConfigure"); //$NON-NLS-1$
		job.setLaunchFile(launchFile);
		job.setVariableFile(variableFile);
		job.setInstallPath(installedPath);
		// macosx use the default vm provided by System
		if(!Platform.OS_MACOSX.equals(Platform.getOS())){		
			// vm configuration
			IPreferenceStore st =  Activator.getDefault().getPreferenceStore();
			int select = st.getInt(VMPreferencePage.PREFERENCE_KEY);
			if (select == 1) {
//				job.setVMProvider("com.ibm.hannover.development.tools.configurations.EEVMConfiguration");
//				job.setVMRootPath(FinderUtility.findDEEVMPath(installedPath)); // new code
				// Use J2SE vm since Symphony 2.0
				job.setVMProvider("com.ibm.hannover.development.tools.configurations.StardVMConfiguration"); //$NON-NLS-1$
				job.setVMRootPath(FinderUtility.findStandardVMPath(installedPath));  
			}
		}
		
		job.setUser(true);
		PlatformUI.getWorkbench().getProgressService().showInDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), job);
		job.schedule();
	}
}