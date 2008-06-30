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

public class NotesPolicy {
	
	public static final String CLAZZ = NotesPolicy.class.getName();
	private static final Logger logger = Logger.getLogger(CLAZZ);
	private String installedPath;
	private String launchFile, variableFile;
	private boolean isWindows = System.getProperty("os.name").
		toLowerCase().startsWith("windows");
	
	public NotesPolicy(String launchFile, String variableFile){
		this.launchFile = launchFile;
		this.variableFile = variableFile;
	}
	
	private boolean findInstalledLocation(){
		String method = "findInstalledLocation";
		boolean rt = true;
		try{
			if(isWindows){
				installedPath = RegistryHelper.queryValue(RegistryHelper.HKEY_LOCAL_MACHINE, 
						"SOFTWARE\\IBM\\Lotus\\Expeditor\\{D8641E4B-77AF-4EAC-9137-8D4DCB1478E2}", 
						"xpdInstallLocation");
			}else{
				Properties src = new Properties();
				InputStream in = new FileInputStream(new File("/etc/lotus/notes/notesrc"));
				src.load(in);
				installedPath = src.getProperty("RCPHOME");
				in.close();
			}
			if(installedPath == null || installedPath.trim().length() == 0)
				rt = false;
			if(installedPath.endsWith(File.separator))
				installedPath = installedPath.substring(0, installedPath.length()-1);
		}catch(IOException e){
			logger.logp(Level.SEVERE, CLAZZ, method, 
					"Fail to get Hannover installation location.", e);
			Platform.getLog(Activator.getDefault().getBundle()).log(
					new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Fail to get Hannover installation location. It might affect vm configuration.", e));
		}
		return rt;
	}
	
	public void configure(){
		findInstalledLocation();
		AutoConfigureJob job = new AutoConfigureJob("AutoConfigure");
		job.setLaunchFile(launchFile);
		job.setVariableFile(variableFile);
		job.setInstallPath(installedPath);
		job.setVMRootPath(installedPath);
		job.setUser(true);
		PlatformUI.getWorkbench().getProgressService().showInDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), job);
		job.schedule();
	}
}
