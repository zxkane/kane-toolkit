package com.ibm.hannover.development.tools.configurations;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;

import com.ibm.hannover.development.tools.Activator;

public class AutoConfigureJob extends UIJob{
	private static final String ECLIPSE_HOME_612 = "/eclipse";
	private static final String PLUGINS = "/plugins";
	private static final String ECLIPSE_HOME_62 = "/rcp/eclipse";
	public static final String CLAZZ = AutoConfigureJob.class.getName();
	private static final Logger logger = Logger.getLogger(CLAZZ);
	
	private final String J2SE_PLUGIN = "com.ibm.rcp.j2se";
	private final String DEE_PLUGIN = "com.ibm.rcp.dee";
	private String jrePath = null;
	private String name;
	private String variableFile, launchFile;
	private String installedPath;
	private String vmRootPath;
	private String eclipseHome = null;

	/**
	 * Add by Tang Qiao, 2008-09-18
	 */
	private boolean isNotesVM = true;
	
	/**
	 * Add by Tang Qiao, 2008-09-18
	 * @param isNotesVM
	 */
	public void setNotesVM(boolean isNotesVM) {
		this.isNotesVM = isNotesVM;
	}


	public AutoConfigureJob(String name) {
		super(name);
		this.name = name;
	}
	
	public void setVariableFile(String vfile){
		variableFile = vfile;
	}
	
	public void setLaunchFile(String lfile){
		launchFile = lfile;
	}
	
	public void setInstallPath(String path){
		if(path == null || path.length() == 0){
			logger.severe("Can't locate the target platform.");
			Platform.getLog(Activator.getDefault().getBundle()).log(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Can't locate the target platform.", null));
		}
		installedPath = path;
		File f = new File(new Path(installedPath + ECLIPSE_HOME_612).toOSString());
		File f1 = new File(new Path(installedPath + ECLIPSE_HOME_612 + PLUGINS).toOSString());
		if(!f.exists() || !f1.exists()){
			f = new File(new Path(installedPath + ECLIPSE_HOME_62).toOSString());
			if(f.exists())
				logger.info("XPD is 6.2.");
			else{
				logger.severe("Can't locate the target platform.");
				Platform.getLog(Activator.getDefault().getBundle()).log(
						new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Can't locate the target platform.", null));
			}
		}else
			logger.info("XPD is 6.1.2.");
		eclipseHome = f.getAbsolutePath();
	}
	
	public void setVMRootPath(String path){
		vmRootPath = path;
	}

	/** 
	 * add by Tang Qiao, 2008-09-18
	 * Find symphony own JVM instead of notes JVM 
	 */
	private void findSymphonyVMPath() {
		IPath pluginPath = new Path(vmRootPath + "/rcp/eclipse/plugins");
		File plugins = new File(pluginPath.toOSString());
		if (plugins.exists() && plugins.isDirectory()) {
			File[] entries = plugins.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					if (pathname.isDirectory()
							&& pathname.getPath().indexOf(DEE_PLUGIN) > -1)
						return true;
					return false;
				}
			});
			if (entries != null) {
				try {
					/**
					 * get the highest version JVM location. store it to jrePath
					 * variable.
					 */
					for (int i = 0; i < entries.length; i++) {
						if (jrePath == null)
							jrePath = entries[i].getCanonicalPath();
						else if (entries[i].getCanonicalPath()
								.compareToIgnoreCase(jrePath) > 0)
							jrePath = entries[i].getCanonicalPath();
					}
				} catch (IOException e) {
					logger.logp(Level.WARNING, CLAZZ, "findVMPath",
							"Fail to get jre path.", e);
				}
			}
		}
	}
	
	private void findVMPath(){
		IPath pluginPath = new Path(vmRootPath + "/rcp/eclipse/plugins");
		File plugins = new File(pluginPath.toOSString());
		if(plugins.exists() && plugins.isDirectory()){
			File[] entries = plugins.listFiles(new FileFilter(){
				public boolean accept(File pathname) {
					if(pathname.isDirectory() &&
							pathname.getPath().indexOf(J2SE_PLUGIN) > -1)
						return true;
					return false;
				}
			});
			if (entries != null) {
				try {
					/**
					 * get the highest version JVM location. store it to jrePath
					 * variable.
					 */
					for (int i = 0; i < entries.length; i++) {
						if (jrePath == null)
							jrePath = entries[i].getCanonicalPath();
						else if(entries[i].getCanonicalPath().compareToIgnoreCase(
								jrePath) > 0)
							jrePath = entries[i].getCanonicalPath();
					}
				}catch(IOException e){
					logger.logp(Level.WARNING, CLAZZ, "findVMPath", "Fail to get jre path.", e);
				}
			}
		}
	}

	public IStatus runInUIThread(IProgressMonitor monitor) {
		monitor.beginTask(name, 100);
		
		// modified by Tang Qiao, 2008-09-18
		if (isNotesVM) 
			findVMPath();  	
		else 
			findSymphonyVMPath();
		// end of modification
		
		List<IConfigure> tasks = new ArrayList<IConfigure>(4);
		if(eclipseHome != null)
			tasks.add(new TargetPlatformConfiguration(eclipseHome));
		if(jrePath != null)
			tasks.add(new VMConfiguration(jrePath, false));
			tasks.add(new StringVariableConfiguration(installedPath, variableFile));
		try{
			PropertiesUtils properties = new PropertiesUtils(launchFile);
			tasks.add(new LaunchConfiguration(properties));
		}catch(IOException e){
			logger.logp(Level.SEVERE, CLAZZ, "AutoConfigureJob", 
					String.format("Fail to load {0}.", new Object[]{launchFile}), e);
			monitor.done();
			return Status.CANCEL_STATUS;
		}
		monitor.worked(20);
		Iterator<IConfigure> iter = tasks.iterator();
		while(iter.hasNext()){
			IConfigure configuration = (IConfigure)iter.next();
			monitor.subTask(configuration.getName());
			configuration.configure();
			monitor.worked(80/tasks.size());
		}
		monitor.done();
		return Status.OK_STATUS;
	}
}
