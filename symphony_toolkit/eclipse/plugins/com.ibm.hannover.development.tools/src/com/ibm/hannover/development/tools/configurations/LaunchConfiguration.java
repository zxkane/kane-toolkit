package com.ibm.hannover.development.tools.configurations;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.pde.internal.ui.launcher.VMHelper;
import org.eclipse.pde.ui.launcher.IPDELauncherConstants;

import com.ibm.hannover.development.tools.Activator;
import com.ibm.hannover.development.tools.preferences.VMPreferencePage;

/**
 * Refer to package 'org.eclipse.pde.ui.launcher' and class 'org.eclipse.debug.ui.EnvironmentTab'
 * @author kane
 */
public class LaunchConfiguration implements IConfigure{
		
	private static final String CLAZZ = LaunchConfiguration.class.getName();
	private static Logger logger = Logger.getLogger(CLAZZ);
	public static final String LAUNCH_NAME = "launchName";
	public static final String ENV_PREFIX = "env.";
	private static final String ECLIPSE_APPLICATION = "org.eclipse.pde.ui.RuntimeWorkbench";
	private PropertiesUtils properties;
	private boolean isNotes;
	
	public LaunchConfiguration(PropertiesUtils properties){
		this(properties, true);
	}
	
	public LaunchConfiguration(PropertiesUtils properties, boolean isNotes){
		this.properties = properties;
		this.isNotes = isNotes;
	}

	public void configure(){
		String method = "autoGenerateLaunch";
		
		try {
			ILaunchConfigurationType eclipseType = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(ECLIPSE_APPLICATION);
			if (eclipseType == null) {
				logger.logp(Level.SEVERE, CLAZZ, method,
						"Can't find Eclipse Application Launch.");
				return;
			}
			ILaunchConfigurationWorkingCopy wc = eclipseType.newInstance(null, properties.getProperty(LAUNCH_NAME));
			setWorkspaceConfiguration(wc);
			setProgram(wc);
			setArguments(wc);
			setWorkingDirectory(wc);
			setConfiguration(wc);
			setEnvironmentVariables(wc);
			setVM(wc);
			wc.doSave();
		} catch (CoreException e) {
			logger.logp(Level.SEVERE, CLAZZ, method, "Fail to generate launch configuration.", e);
		}
	}

	private void setEnvironmentVariables(ILaunchConfigurationWorkingCopy wc) {
		Map<String, String> map = getEnvironmentVariables();
		if(map != null){
			wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, map);
			// set the flag to append the value
			wc.setAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
		}
	}

	private Map<String, String> getEnvironmentVariables() {
		Map<String, String> map = null;
		int suffix = 0;
		while(true){
			String value = properties.getProperty(ENV_PREFIX + suffix++);
			if(value == null)
				break;
			else{
				if(map == null)
					map = new HashMap<String, String>(1);
				String[] arrays = value.split(";");
				map.put(arrays[0], arrays[1]);
			}
		}
		return map;
	}

	private void setConfiguration(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IPDELauncherConstants.CONFIG_CLEAR_AREA, 
				Boolean.valueOf((properties.getProperty(IPDELauncherConstants.CONFIG_CLEAR_AREA))).booleanValue());
	}

	private void setWorkingDirectory(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, 
				properties.getProperty("WORKING_DIRECTORY"));	//$NON-NLS-1$		
	}

	private void setArguments(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, 
				properties.getProperty("PROGRAM_ARGUMENTS"));	//$NON-NLS-1$
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, 
				properties.getProperty("VM_ARGUMENTS"));	//$NON-NLS-1$		
	}

	private void setProgram(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IPDELauncherConstants.USE_PRODUCT, true);
		config.setAttribute(IPDELauncherConstants.PRODUCT, 
				properties.getProperty(IPDELauncherConstants.PRODUCT));
	}

	private void setWorkspaceConfiguration(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IPDELauncherConstants.LOCATION, properties.getProperty(IPDELauncherConstants.LOCATION));
		config.setAttribute(IPDELauncherConstants.DOCLEAR, Boolean.valueOf((properties.getProperty(IPDELauncherConstants.DOCLEAR))).booleanValue());
		config.setAttribute(IPDELauncherConstants.ASKCLEAR, Boolean.valueOf((properties.getProperty(IPDELauncherConstants.ASKCLEAR))).booleanValue());
	}
	
	// add vm configuration code here, Tang Qiao 2008-09-23
	private void setVM(ILaunchConfigurationWorkingCopy config) {
		IPreferenceStore st =  Activator.getDefault().getPreferenceStore();
		int select = st.getInt(VMPreferencePage.PREFERENCE_KEY);
		if (select == 1) {
			// do nothing, use default VM
		}
		else if (select == 2) {
			IVMInstall install;
			if (isNotes)
				install = VMHelper.getVMInstall("AutoGeneratedJ2SE");
			else 
				install = VMHelper.getVMInstall("AutoGeneratedDEE");				
			IPath jrePath = JavaRuntime.newJREContainerPath(install);
			String attr = null;
			if (jrePath != null) 
				attr = jrePath.toPortableString();
			// remove old attribute first
			config.removeAttribute(IPDELauncherConstants.VMINSTALL);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, attr);
		}
		else {
			IVMInstall install = VMHelper.getVMInstall("AutoGeneratedJ2SE");				
			IPath jrePath = JavaRuntime.newJREContainerPath(install);
			String attr = null;
			if (jrePath != null) 
				attr = jrePath.toPortableString();
			// remove old attribute first
			config.removeAttribute(IPDELauncherConstants.VMINSTALL);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, attr);
	
		}
	}
	
	public String getName() {
		return "Configure launch";
	}
}
