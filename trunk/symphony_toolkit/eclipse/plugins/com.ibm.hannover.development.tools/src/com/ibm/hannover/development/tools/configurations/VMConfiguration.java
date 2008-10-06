package com.ibm.hannover.development.tools.configurations;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.launching.EEVMInstall;
import org.eclipse.jdt.internal.launching.EEVMType;
import org.eclipse.jdt.internal.launching.VMDefinitionsContainer;
import org.eclipse.jdt.launching.AbstractVMInstallType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.osgi.service.environment.Constants;

public class VMConfiguration implements IConfigure {

	private static String CLAZZ = VMConfiguration.class.getName();
	private static Logger logger = Logger.getLogger(CLAZZ);
	private static final String StandardVM = "org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType";
	private static final String EEVM = "org.eclipse.jdt.launching.EEVMType";
	private String jrePath;
	private String vmArgs;
	private IVMInstallType fSelectedVMType;
	private IVMInstall newVM = null;
	private boolean isDefault = true;

	/**
	 * Add by Tang Qiao, 2008-09-18
	 */
	private boolean isNotesVM = true;

	/**
	 * Add by Tang Qiao, 2008-09-18
	 * 
	 * @param isNotesVM
	 */
	public void setNotesVM(boolean isNotesVM) {
		this.isNotesVM = isNotesVM;
		if (!isNotesVM)
			fSelectedVMType = JavaRuntime.getVMInstallType(EEVM);
		else
			fSelectedVMType = JavaRuntime.getVMInstallType(StandardVM);
	}

	/**
	 * VMs being displayed
	 */
	private Map<IVMInstall, String> fVMs = new HashMap<IVMInstall, String>(2);

	public VMConfiguration(String path, boolean isDefault) {
		initialize(path);
		this.isDefault = isDefault;
	}
	
	public VMConfiguration(String path, String vmArgs, boolean isDefault){		
		this(path, isDefault);
		this.vmArgs = vmArgs;
	}
	
	public void configure(){
		String method = "configure";
		logger.entering(CLAZZ, method);
		if (isNotesVM) {
			if (!fVMs.containsValue(jrePath)) {
				logger.logp(Level.FINE, CLAZZ, method, "Create new vm.");
				newVM = new VMStandin(fSelectedVMType,
						createUniqueId(fSelectedVMType));
				setFieldValuesToVM(newVM);
				fVMs.put(newVM, newVM.getInstallLocation().getAbsolutePath());
				save();
			}
		} else {
			String dir = jrePath + File.separatorChar + "jre";
			if (!fVMs.containsValue(dir)) {
				logger.logp(Level.FINE, CLAZZ, method, "Create new vm.");

				newVM = new VMStandin(fSelectedVMType,
						createUniqueId(fSelectedVMType));
				setFieldValueToDEEVM(newVM);
				fVMs.put(newVM, newVM.getInstallLocation().getAbsolutePath());
				save();
			}
		}
		logger.exiting(CLAZZ, method);
	}

	private void initialize(String path) {
		this.jrePath = path;

		// put standard vm
		fSelectedVMType = JavaRuntime.getVMInstallType(StandardVM);
		IVMInstallType[] fVMTypes = JavaRuntime.getVMInstallTypes();
		for (int i = 0; i < fVMTypes.length; i++) {
			IVMInstallType type = fVMTypes[i];
			IVMInstall[] installs = type.getVMInstalls();
			for (int j = 0; j < installs.length; j++) {
				IVMInstall install = installs[j];
				fVMs.put(new VMStandin(install), install.getInstallLocation()
						.getAbsolutePath());
			}
		}
	}

	private IVMInstall getCurrentDefaultVM() {
		// modified by Tang Qiao, 2008-09-27
		// do not change the default VM
		if (isDefault)
			return newVM;		
	
		return JavaRuntime.getDefaultVMInstall();
	}
	
	private void save(){
		final String method = "save";
		logger.entering(CLAZZ, method);
		IVMInstall defaultVM = getCurrentDefaultVM();
		IVMInstall[] vms = (IVMInstall[]) fVMs.keySet().toArray(
				new IVMInstall[fVMs.size()]);
		// JREsUpdater updater = new JREsUpdater();
		// if (!updater.updateJRESettings(vms, defaultVM))
		//			
		try {
			// Create a VM definition container
			VMDefinitionsContainer vmContainer = new VMDefinitionsContainer();
			// Set the default VM Id on the container
			String defaultVMId = JavaRuntime.getCompositeIdFromVM(defaultVM);
			vmContainer.setDefaultVMInstallCompositeID(defaultVMId);
			// Set the VMs on the container
			for (int i = 0; i < vms.length; i++) {
				vmContainer.addVM(vms[i]);
			}
			// Generate XML for the VM defs and save it as the new value of the
			// VM preference
			String vmDefXML = vmContainer.getAsXML();
			JavaRuntime.getPreferences().setValue(JavaRuntime.PREF_VM_XML,
					vmDefXML);
			JavaRuntime.savePreferences();
		}catch (CoreException e) {
			log(method, e);
		}
		logger.exiting(CLAZZ, method);
	}
	
	private void log(String method, Exception e){
		logger.logp(Level.SEVERE, CLAZZ, method,
				"SEVERE: Fail to save jre configuration.", e);
	}
	
	/**
	 * Creates a unique name for the VMInstallType
	 * 
	 * @param vmType
	 *            the vm install type
	 * @return a unique name
	 */
	private String createUniqueId(IVMInstallType vmType) {
		String id = null;
		do {
			id = String.valueOf(System.currentTimeMillis());
		} while (vmType.findVMInstall(id) != null);
		return id;
	}

	protected void setFieldValueToDEEVM(IVMInstall vm) {
		File dir = new File(jrePath + File.separatorChar + "jre"
				+ File.separatorChar + "default.ee");
		try {
			vm.setInstallLocation(dir.getCanonicalFile());
		} catch (IOException e) {
			vm.setInstallLocation(dir.getAbsoluteFile());
		}
		vm.setName("AutoGeneratedDEE");
		vm.setJavadocLocation(detectJavadocLocation());
		try {
			vm = JavaRuntime.createVMFromDefinitionFile(dir, newVM.getName(),
					newVM.getId());

		} catch (CoreException e) {
			e.printStackTrace();
		}
		// set properties
		VMStandin vv = (VMStandin) vm;
		File eeFile = dir;
		File home = null;
		if (eeFile != null) {
			vv.setAttribute(EEVMInstall.ATTR_DEFINITION_FILE, eeFile.getPath());
			String homePath = EEVMType.getProperty(EEVMType.PROP_JAVA_HOME,
					eeFile);
			if (homePath != null) {
				home = new File(homePath);
				try {
					home = home.getCanonicalFile();
				} catch (IOException e) {
				}
			}
		}
		vv.setInstallLocation(home);
		vv.setName(newVM.getName());
//		vm.setLibraryLocations(null);
		newVM = vm;

	}

	/**
	 * init fields to the specified VM
	 * 
	 * @param vm
	 *            the VM to init from
	 */
	protected void setFieldValuesToVM(IVMInstall vm) {
		File dir = new File(jrePath);
		try {
			vm.setInstallLocation(dir.getCanonicalFile());
		} 
		catch (IOException e) {
			vm.setInstallLocation(dir.getAbsoluteFile());
		}
		vm.setName("AutoGeneratedJ2SE");
		vm.setJavadocLocation(detectJavadocLocation());
		
		String argString = vmArgs;
		if (vm instanceof IVMInstall2) {
			IVMInstall2 vm2 = (IVMInstall2) vm;
			if (argString != null && argString.length() > 0) {
				vm2.setVMArgs(argString);
			} else {
				vm2.setVMArgs(null);
			}
		} 
		else {
			if (argString != null && argString.length() > 0) {
				vm.setVMArguments(VMConfiguration.parseArguments(argString));			
			} 
			else {
				vm.setVMArguments(null);
			}			
		}
		vm.setLibraryLocations(null);
	}
	
	/**
	 * Auto-detects the default javadoc location
	 */
	private URL detectJavadocLocation() {
		if (fSelectedVMType instanceof AbstractVMInstallType) {
			AbstractVMInstallType type = (AbstractVMInstallType) fSelectedVMType;
			return type.getDefaultJavadocLocation(new File(jrePath));
		}else
			return null;			
	}
	
	/**
	 * Utility class to parse command line arguments.
	 * 
	 * @since 3.1
	 */
	private static class ArgumentParser {
		private String fArgs;
		private int fIndex= 0;
		private int ch= -1;
		
		public ArgumentParser(String args) {
			fArgs= args;
		}
		
		public String[] parseArguments() {
			List v= new ArrayList();
			
			ch= getNext();
			while (ch > 0) {
				if (Character.isWhitespace((char)ch)) {
					ch= getNext();	
				} else {
					if (ch == '"') {
					    StringBuffer buf = new StringBuffer();
						buf.append(parseString());
						if (buf.length() == 0 && Platform.getOS().equals(Constants.OS_WIN32)) {
							// empty string on windows platform
							buf.append("\"\""); //$NON-NLS-1$
						}
						v.add(buf.toString());
					} else {
						v.add(parseToken());
					}
				}
			}
	
			String[] result= new String[v.size()];
			v.toArray(result);
			return result;
		}
		
		private int getNext() {
			if (fIndex < fArgs.length())
				return fArgs.charAt(fIndex++);
			return -1;
		}
		
		private String parseString() {
			ch= getNext();
			if (ch == '"') {
				ch= getNext();
				return ""; //$NON-NLS-1$
			}
			StringBuffer buf= new StringBuffer();
			while (ch > 0 && ch != '"') {
				if (ch == '\\') {
					ch= getNext();
					if (ch != '"') {           // Only escape double quotes
						buf.append('\\');
					} else {
						if (Platform.getOS().equals(Constants.OS_WIN32)) {
							// @see Bug 26870. Windows requires an extra escape for embedded strings
							buf.append('\\');
						}
					}
				}
				if (ch > 0) {
					buf.append((char)ch);
					ch= getNext();
				}
			}
			ch= getNext();
			return buf.toString();
		}
		
		private String parseToken() {
			StringBuffer buf= new StringBuffer();
			
			while (ch > 0 && !Character.isWhitespace((char)ch)) {
				if (ch == '\\') {
					ch= getNext();
					if (Character.isWhitespace((char)ch)) {
						// end of token, don't lose trailing backslash
						buf.append('\\');
						return buf.toString();
					}
					if (ch > 0) {
						if (ch != '"') {           // Only escape double quotes
							buf.append('\\');
						} else {
							if (Platform.getOS().equals(Constants.OS_WIN32)) {
								// @see Bug 26870. Windows requires an extra escape for embedded strings
								buf.append('\\');
							}
						}
						buf.append((char)ch);
						ch= getNext();
					} else if (ch == -1) {     // Don't lose a trailing backslash
						buf.append('\\');
					}
				} else if (ch == '"') {
					buf.append(parseString());
				} else {
					buf.append((char)ch);
					ch= getNext();
				}
			}
			return buf.toString();
		}
	}
	
	/**
	 * Parses the given command line into separate arguments that can be passed to
	 * <code>DebugPlugin.exec(String[], File)</code>. Embedded quotes and slashes
	 * are escaped.
	 * 
	 * @param args command line arguments as a single string
	 * @return individual arguments
	 * @since 3.1
	 */
	public static String[] parseArguments(String args) {
		if (args == null)
			return new String[0];
		ArgumentParser parser= new ArgumentParser(args);
		String[] res= parser.parseArguments();
		
		return res;
	}

	public String getName() {
		return "Configure vm";
	}	
}
