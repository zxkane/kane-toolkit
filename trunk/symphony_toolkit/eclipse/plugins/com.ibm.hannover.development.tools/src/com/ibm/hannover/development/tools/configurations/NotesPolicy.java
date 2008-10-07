package com.ibm.hannover.development.tools.configurations;

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
		AutoConfigureJob job = new AutoConfigureJob("AutoConfigure");
		job.setLaunchFile(launchFile);
		job.setVariableFile(variableFile);
		job.setInstallPath(installedPath);
		job.setUser(true);
		IPreferenceStore st =  Activator.getDefault().getPreferenceStore();
		int select = st.getInt(VMPreferencePage.PREFERENCE_KEY);
		if(select != 1){
			job.setVMRootPath(FinderUtility.findStandardVMPath(installedPath));  
			job.setVMProvider("com.ibm.hannover.development.tools.configurations.StardVMConfiguration");
		}
		PlatformUI.getWorkbench().getProgressService().showInDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), job);
		job.schedule();
	}
}
