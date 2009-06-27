package com.ibm.hannover.development.tools.configurations;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.VariablesPlugin;

public class StringVariableConfiguration implements IConfigure {

	public static final String CLAZZ = StringVariableConfiguration.class
			.getName();
	private static Logger logger = Logger.getLogger(CLAZZ);
	private final String RCP_HOME = "${rcp_home}";
	private final String RCP_VERSION = "${rcp_version}";
	private final String SYMPHONY_VERSION = "${symphony_version}";
	private final String RCP_BASE = "com.ibm.rcp.base_";
	private final String SYMPHONY_SYSTEM_PLUGIN = "com.ibm.symphony.base.app."
			+ Platform.getOS() + "_";
	private String rcpHome;
	private String config;

	public StringVariableConfiguration(String rcp, String config) {
		rcpHome = rcp;
		this.config = config;
	}

	public void configure(IProgressMonitor monitor) {
		List<IValueVariable> variables = new ArrayList<IValueVariable>();
		IStringVariableManager stringVariableManager = VariablesPlugin
				.getDefault().getStringVariableManager();
		IValueVariable variable;

		try {
			PropertiesUtils prop = new PropertiesUtils(config);
			Map<String, String> props = prop.getProperties();
			for (Iterator<String> iter = props.keySet().iterator(); iter
					.hasNext();) {
				String key = iter.next();
				if ((variable = stringVariableManager.getValueVariable(key)) == null) {
					variable = stringVariableManager.newValueVariable(key, key);
					variables.add(variable);
				}
				variable.setValue(getKeyValue(props.get(key)));
			}
			monitor.worked(5);
			if (variables.size() > 0)
				try {
					stringVariableManager
							.addVariables((IValueVariable[]) variables
									.toArray(new IValueVariable[variables
											.size()]));
				} catch (CoreException e) {
					logger.logp(Level.SEVERE, CLAZZ, "updateVariable",
							"Fail to update variables.", e);
				}
			monitor.worked(10);
		} catch (IOException e1) {
			logger.logp(Level.WARNING, CLAZZ, "configure",
					"Fail to load configuration.", e1);
		}

	}

	private String getKeyValue(String key) {
		if (RCP_HOME.equals(key))
			return rcpHome;
		else if (RCP_VERSION.equals(key))
			return getRCPVersion();
		else if (SYMPHONY_VERSION.equals(key))
			return getSymphonyVersion();
		else
			return key;
	}

	private String getSymphonyVersion() {
		IPath rcp = new Path(rcpHome + "/shared/eclipse/plugins");
		File plugins = new File(rcp.toOSString());
		String systemlinux = null;
		if (plugins.exists() && plugins.isDirectory()) {
			File[] entries = plugins.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					if (pathname.isDirectory()
							&& pathname.getPath().indexOf(
									SYMPHONY_SYSTEM_PLUGIN) > -1)
						return true;
					return false;
				}
			});
			if (entries != null) {
				try {
					for (int i = 0; i < entries.length; i++) {
						String fullpath = entries[i].getCanonicalPath();
						String version = fullpath.substring(fullpath
								.indexOf(SYMPHONY_SYSTEM_PLUGIN)
								+ SYMPHONY_SYSTEM_PLUGIN.length());
						if (systemlinux == null)
							systemlinux = version;
						else if (version.compareToIgnoreCase(systemlinux) > 0)
							systemlinux = version;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return systemlinux;
	}

	private String getRCPVersion() {
		IPath rcp = new Path(rcpHome + "/rcp/eclipse/plugins");
		File plugins = new File(rcp.toOSString());
		if (plugins.exists() && plugins.isDirectory()) {
			File[] entries = plugins.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					if (pathname.isDirectory()
							&& pathname.getPath().indexOf(RCP_BASE) > -1)
						return true;
					return false;
				}
			});
			String rcpBase = null;
			if (entries != null) {
				try {
					for (int i = 0; i < entries.length; i++) {
						if (rcpBase == null)
							rcpBase = entries[i].getCanonicalPath();
						else if (entries[i].getCanonicalPath()
								.compareToIgnoreCase(rcpBase) > 0)
							rcpBase = entries[i].getCanonicalPath();
					}
					return rcpBase.substring(rcpBase.indexOf(RCP_BASE)
							+ RCP_BASE.length());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public String getName() {
		return "Configure string variable";
	}
}
