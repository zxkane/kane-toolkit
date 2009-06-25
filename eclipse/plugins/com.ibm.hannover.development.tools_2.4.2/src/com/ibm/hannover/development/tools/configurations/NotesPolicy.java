package com.ibm.hannover.development.tools.configurations;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;

import com.ibm.hannover.development.tools.Activator;
import com.ibm.hannover.development.tools.preferences.VMPreferencePage;

public class NotesPolicy {
	
	public static final String CLAZZ = NotesPolicy.class.getName();
	private String installedPath;
	private String launchFile, variableFile;
	
	public NotesPolicy(String launchFile, String variableFile){
		this.launchFile = launchFile;
		this.variableFile = variableFile;
	}
	
	public void configure(){
		installedPath = FinderUtility.findNotesInstalledLocation();
		AutoConfigureJob job = new AutoConfigureJob("AutoConfigure"); //$NON-NLS-1$
		job.setLaunchFile(launchFile);
		job.setVariableFile(variableFile);
		job.setInstallPath(installedPath);
		job.setUser(true);
		if(!Platform.OS_MACOSX.equals(Platform.getOS())){
			IPreferenceStore st =  Activator.getDefault().getPreferenceStore();
			int select = st.getInt(VMPreferencePage.PREFERENCE_KEY);
			if(select == 1){
				job.setVMRootPath(FinderUtility.findStandardVMPath(installedPath));  
				job.setVMProvider("com.ibm.hannover.development.tools.configurations.StardVMConfiguration"); //$NON-NLS-1$
			}
		}
		PlatformUI.getWorkbench().getProgressService().showInDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), job);
		job.schedule();
	}
}
