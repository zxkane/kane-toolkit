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

import com.ibm.hannover.development.tools.Activator;

public class FinderUtility {
	private FinderUtility(){
		
	}
	
	private static final String CLAZZ = FinderUtility.class.getName();
	private static final Logger logger = Logger.getLogger(CLAZZ);
	private static final boolean isWindows = System.getProperty("os.name").
		toLowerCase().startsWith("windows");
	private static final String RCPLAUNCHER = "rcplauncher";
	
	public static String findNotesInstalledLocation(){
		String method = "findInstalledLocation";
		String notesPath = null;
		try{
			if(isWindows){
//				installedPath = RegistryHelper.queryValue(RegistryHelper.HKEY_LOCAL_MACHINE, 
//						"SOFTWARE\\IBM\\Lotus\\Expeditor\\{D8641E4B-77AF-4EAC-9137-8D4DCB1478E2}", 
//						"xpdInstallLocation");				
				notesPath = RegistryHelper.queryValue(RegistryHelper.HKEY_LOCAL_MACHINE, 
						"SOFTWARE\\IBM\\Lotus\\Expeditor\\Notes", 
						"launcher");
				notesPath = notesPath.substring(0, notesPath.indexOf("framework")+"framework".length());
			}else{
				Properties src = new Properties();
				InputStream in = new FileInputStream(new File("/etc/lotus/notes/notesrc"));
				src.load(in);
				notesPath = src.getProperty("RCPHOME");
				in.close();
			}
			if(notesPath == null || notesPath.trim().length() == 0)
				notesPath = null;
			else if(notesPath.endsWith(File.separator))
				notesPath = notesPath.substring(0, notesPath.length()-1);
		}catch(IOException e){
			logger.logp(Level.SEVERE, CLAZZ, method, 
					"Fail to get Hannover installation location.", e);
			Platform.getLog(Activator.getDefault().getBundle()).log(
					new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Fail to get Hannover installation location. It might affect vm configuration.", e));
		}
		return notesPath;
	}
	
	public static String findSymphonyInstalledLocation(){
		String method = "findInstalledLocation";
		String symphonyPath = null;
		try{
			/* initiate installedPath
			 * In windows, use JNI to call local API to get the installed path.
			 * In Linux, read the property file to get the installed path. 
			 */
			if(isWindows){
				symphonyPath = RegistryHelper.queryValue(RegistryHelper.HKEY_LOCAL_MACHINE, 
						"SOFTWARE\\Lotus\\Symphony", 
						"launcher");
			}else{
				Properties src = new Properties();
				InputStream in = new FileInputStream(new File("/etc/lotus/Symphony/Symphonyrc"));
				src.load(in);
				symphonyPath = src.getProperty("RCPHOME");
				in.close();
			}
			if(symphonyPath == null || symphonyPath.trim().length() == 0)
				symphonyPath = null;
			else{
				// remove last 2 directories from the installPath if it contain RCPLAUNCHER
				if(symphonyPath.contains(RCPLAUNCHER)){
					symphonyPath = symphonyPath.substring(0, symphonyPath.lastIndexOf(File.separatorChar));
					symphonyPath = symphonyPath.substring(0, symphonyPath.lastIndexOf(File.separatorChar));
				}
				// remove last character if it is a File.separator
				if(symphonyPath.endsWith(File.separator))
					symphonyPath = symphonyPath.substring(0, symphonyPath.length()-1);
			}
		}catch(IOException e){
			symphonyPath = null;
			logger.logp(Level.SEVERE, CLAZZ, method, 
					"Fail to get Lotus Symphony installation location.", e);
			Platform.getLog(Activator.getDefault().getBundle()).log(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Fail to get Lotus Symphony installation location.", e));
		}
		return symphonyPath;
	}
}
