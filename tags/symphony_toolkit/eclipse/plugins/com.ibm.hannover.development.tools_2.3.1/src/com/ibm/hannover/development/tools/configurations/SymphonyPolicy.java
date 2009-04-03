package com.ibm.hannover.development.tools.configurations;

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
		if((installedPath = FinderUtility.findSymphonyInstalledLocation()) == null){
			logger.log(Level.SEVERE, "Can't find installed symphony.");
			Platform.getLog(Activator.getDefault().getBundle()).log(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Can't find installed symphony.", null));
			return;
		}
		AutoConfigureJob job = new AutoConfigureJob("AutoConfigure");
		job.setLaunchFile(launchFile);
		job.setVariableFile(variableFile);
		job.setInstallPath(installedPath);
		// macosx use the default vm provided by System
		if(!Platform.OS_MACOSX.equals(Platform.getOS())){		
			// add by Tang Qiao, 2008-09-18
			// vm configuration
			IPreferenceStore st =  Activator.getDefault().getPreferenceStore();
			int select = st.getInt(VMPreferencePage.PREFERENCE_KEY);
			if (select == 2) {
				job.setVMProvider("com.ibm.hannover.development.tools.configurations.EEVMConfiguration");
				job.setVMRootPath(FinderUtility.findDEEVMPath(installedPath)); // new code
			}
			else if(select == 3){
				// find notes location
				notesPath = FinderUtility.findNotesInstalledLocation();
				job.setVMRootPath(FinderUtility.findStandardVMPath(notesPath));  
				job.setVMProvider("com.ibm.hannover.development.tools.configurations.StardVMConfiguration");
			}
		}
		// end of add
		
		job.setUser(true);
		PlatformUI.getWorkbench().getProgressService().showInDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), job);
		job.schedule();
	}
}