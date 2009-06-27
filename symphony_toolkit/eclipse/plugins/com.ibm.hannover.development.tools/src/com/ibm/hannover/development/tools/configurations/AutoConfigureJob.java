package com.ibm.hannover.development.tools.configurations;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;

import com.ibm.hannover.development.tools.Activator;

public class AutoConfigureJob extends UIJob {
	private static final String ECLIPSE_HOME_612 = "/eclipse"; //$NON-NLS-1$
	private static final String PLUGINS = "/plugins"; //$NON-NLS-1$
	private static final String ECLIPSE_HOME_62 = "/rcp/eclipse"; //$NON-NLS-1$
	public static final String CLAZZ = AutoConfigureJob.class.getName();
	private static final Logger logger = Logger.getLogger(CLAZZ);

	private String name;
	private String variableFile, launchFile;
	private String installedPath;
	private String vmRootPath;
	private String eclipseHome = null;
	private String vmprovider;
	private String targetName;

	public void setVMProvider(String provider) {
		this.vmprovider = provider;
	}

	public AutoConfigureJob(String name) {
		super(name);
		this.name = name;
	}

	public void setVariableFile(String vfile) {
		variableFile = vfile;
	}

	public void setLaunchFile(String lfile) {
		launchFile = lfile;
	}

	public void setInstallPath(String path) {
		String err = "Can't locate the target platform."; //$NON-NLS-1$
		if (path == null || path.length() == 0) {
			logger.severe(err);
			Platform.getLog(Activator.getDefault().getBundle()).log(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, err, null));
		}
		installedPath = path;
		File f = new File(new Path(installedPath + ECLIPSE_HOME_612)
				.toOSString());
		File f1 = new File(new Path(installedPath + ECLIPSE_HOME_612 + PLUGINS)
				.toOSString());
		if (!f.exists() || !f1.exists()) {
			f = new File(new Path(installedPath + ECLIPSE_HOME_62).toOSString());
			if (f.exists())
				logger.info("XPD is 6.2.");
			else {
				logger.severe(err);
				Platform.getLog(Activator.getDefault().getBundle()).log(
						new Status(IStatus.ERROR, Activator.PLUGIN_ID, err,
								null));
			}
		} else
			logger.info("XPD is 6.1.2.");
		eclipseHome = f.getAbsolutePath();
	}

	public void setVMRootPath(String path) {
		vmRootPath = path;
	}

	public void setTargetPlatform(String name) {
		targetName = name;
	}

	public IStatus runInUIThread(IProgressMonitor monitor) {
		monitor.beginTask(name, 100);

		List<IConfigure> tasks = new ArrayList<IConfigure>(4);
		if (eclipseHome != null)
			tasks
					.add(new TargetPlatformConfiguration2(eclipseHome,
							targetName));

		VMConfiguration vmconf = null;
		if (vmRootPath != null && vmprovider != null) {
			ClassLoader loader = AutoConfigureJob.class.getClassLoader();
			try {
				Class provider = loader.loadClass(vmprovider);
				Constructor constructor = provider.getConstructor(String.class,
						boolean.class);
				vmconf = (VMConfiguration) constructor.newInstance(vmRootPath,
						false);
				tasks.add(vmconf);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

		}
		tasks.add(new StringVariableConfiguration(installedPath, variableFile));
		try {
			PropertiesUtils properties = new PropertiesUtils(launchFile);
			tasks.add(new LaunchConfiguration(properties,
					vmconf != null ? vmconf.getGeneratedVMName() : null));
		} catch (IOException e) {
			String err = String.format(
					"Fail to load {0}.", new Object[] { launchFile }); //$NON-NLS-1$
			logger.logp(Level.SEVERE, CLAZZ, "AutoConfigureJob", //$NON-NLS-1$
					err, e);
			Platform.getLog(Activator.getDefault().getBundle()).log(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, err, e));
			monitor.done();
			return Status.CANCEL_STATUS;
		}
		monitor.worked(20);
		try {
			Iterator<IConfigure> iter = tasks.iterator();
			while (iter.hasNext()) {
				IConfigure configuration = (IConfigure) iter.next();
				monitor.subTask(configuration.getName());
				configuration.configure(monitor);
				// monitor.worked(80 / tasks.size());
			}
		} catch (CoreException e) {
			String err = "Fail to configurate development environment."; //$NON-NLS-1$
			logger.logp(Level.SEVERE, CLAZZ, "AutoConfigureJob", //$NON-NLS-1$
					err, e);
			Platform.getLog(Activator.getDefault().getBundle()).log(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, err, e));
			return Status.CANCEL_STATUS;
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}
}
