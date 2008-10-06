package com.ibm.hannover.development.tools.configurations;

import org.eclipse.ui.PlatformUI;

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
		job.setVMRootPath(installedPath);
		job.setUser(true);
		// add by Tang Qiao, 2008-09-18
		job.setNotesVM(true);
		// end of add
		PlatformUI.getWorkbench().getProgressService().showInDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), job);
		job.schedule();
	}
}
