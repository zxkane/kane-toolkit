package com.ibm.hannover.development.tools.configurations;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PlatformUI;

import com.ibm.hannover.development.tools.Activator;

public class SymphonyPolicy {
	public static final String CLAZZ = NotesPolicy.class.getName();
	private static final Logger logger = Logger.getLogger(CLAZZ);
	private String installedPath, notesPath;
	private String launchFile, variableFile;
	private boolean isWindows = System.getProperty("os.name").
		toLowerCase().startsWith("windows");
	static final String RCPLAUNCHER = "rcplauncher";
	
	public SymphonyPolicy(String launchFile, String variableFile){
		this.launchFile = launchFile;
		this.variableFile = variableFile;
	}
	
	private boolean findInstalledNotesLocation(){
		String method = "findInstalledNotesLocation";
		boolean rt = true;
		try{
			if(isWindows){
				notesPath = RegistryHelper.queryValue(RegistryHelper.HKEY_LOCAL_MACHINE, 
						"SOFTWARE\\IBM\\Lotus\\Expeditor\\Notes", 
						"launcher");
			}else{
				Properties src = new Properties();
				InputStream in = new FileInputStream(new File("/etc/lotus/notes/notesrc"));
				src.load(in);
				notesPath = src.getProperty("RCPHOME");
				in.close();
			}
			if(notesPath == null || notesPath.trim().length() == 0)
				rt = false;
			else{
				if(notesPath.contains(RCPLAUNCHER)){
					notesPath = notesPath.substring(0, notesPath.lastIndexOf(File.separatorChar));
					notesPath = notesPath.substring(0, notesPath.lastIndexOf(File.separatorChar));
				}
				if(notesPath.endsWith(File.separator))
					notesPath = notesPath.substring(0, notesPath.length()-1);
			}
		}catch(IOException e){
			rt = false;
			logger.logp(Level.SEVERE, CLAZZ, method, 
					"Fail to get Hannover installation location.", e);
			Platform.getLog(Activator.getDefault().getBundle()).log(
					new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Fail to get Hannover installation location. It might affect vm configuration.", e));
		}
		return rt;
	}
	
	private boolean findInstalledLocation(){
		String method = "findInstalledLocation";
		boolean rt = true;
		try{
			if(isWindows){
				installedPath = RegistryHelper.queryValue(RegistryHelper.HKEY_LOCAL_MACHINE, 
						"SOFTWARE\\Lotus\\Symphony", 
						"launcher");
			}else{
				Properties src = new Properties();
				InputStream in = new FileInputStream(new File("/etc/lotus/Symphony/Symphonyrc"));
				src.load(in);
				installedPath = src.getProperty("RCPHOME");
				in.close();
			}
			if(installedPath == null || installedPath.trim().length() == 0)
				rt = false;
			else{
				if(installedPath.contains(RCPLAUNCHER)){
					installedPath = installedPath.substring(0, installedPath.lastIndexOf(File.separatorChar));
					installedPath = installedPath.substring(0, installedPath.lastIndexOf(File.separatorChar));
				}
				if(installedPath.endsWith(File.separator))
					installedPath = installedPath.substring(0, installedPath.length()-1);
			}
		}catch(IOException e){
			rt = false;
			logger.logp(Level.SEVERE, CLAZZ, method, 
					"Fail to get Lotus Symphony installation location.", e);
			Platform.getLog(Activator.getDefault().getBundle()).log(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Fail to get Lotus Symphony installation location.", e));
		}
		return rt;
	}
	
	public void configure(){
		findInstalledNotesLocation();
		if(!findInstalledLocation()){
			logger.log(Level.SEVERE, "Can't find installed symphony.");
			Platform.getLog(Activator.getDefault().getBundle()).log(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Can't find installed symphony.", null));
			return;
		}
		AutoConfigureJob job = new AutoConfigureJob("AutoConfigure");
		job.setLaunchFile(launchFile);
		job.setVariableFile(variableFile);
		job.setInstallPath(installedPath);
		// use ibm j2se as vm
		job.setVMRootPath(notesPath);
		job.setUser(true);
		PlatformUI.getWorkbench().getProgressService().showInDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), job);
		job.schedule();
	}
}
