package com.ibm.hannover.development.tools.configurations;

import java.io.File;
import java.io.IOException;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;

public class StardVMConfiguration extends VMConfiguration {

	private static final String AUTO_GENERATED_NAME = "AutoGeneratedJ2SE";
	private static final String StandardVM = "org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType";
	
	public StardVMConfiguration(String path, boolean isDefault) {
		super(path, isDefault, StandardVM);
	}

	protected final void fillVMFields(IVMInstall vm) {
		File dir = new File(jrePath);
		try {
			vm.setInstallLocation(dir.getCanonicalFile());
		} 
		catch (IOException e) {
			vm.setInstallLocation(dir.getAbsoluteFile());
		}
		vm.setName(AUTO_GENERATED_NAME);
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

	public final String getGeneratedVMName() {
		return AUTO_GENERATED_NAME;
	}

}
