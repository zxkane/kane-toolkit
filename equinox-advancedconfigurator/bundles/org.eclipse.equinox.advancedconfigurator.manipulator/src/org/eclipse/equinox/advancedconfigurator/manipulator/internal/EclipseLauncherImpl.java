/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.advancedconfigurator.manipulator.internal;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.equinox.internal.frameworkadmin.equinox.EquinoxConstants;
import org.eclipse.equinox.internal.frameworkadmin.equinox.Messages;
import org.eclipse.equinox.internal.frameworkadmin.utils.SimpleBundlesState;
import org.eclipse.equinox.internal.frameworkadmin.utils.Utils;
import org.eclipse.equinox.internal.provisional.frameworkadmin.FrameworkAdminRuntimeException;
import org.eclipse.equinox.internal.provisional.frameworkadmin.LauncherData;
import org.eclipse.equinox.internal.provisional.frameworkadmin.Manipulator;

@SuppressWarnings("restriction")
public class EclipseLauncherImpl {
	static String getStringOfCmd(String[] cmdarray) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < cmdarray.length; i++) {
			sb.append(cmdarray[i]);
			sb.append(" "); //$NON-NLS-1$
		}
		return sb.toString();
	}

	EquinoxFwAdminImpl fwAdmin = null;

	EclipseLauncherImpl(EquinoxFwAdminImpl fwAdmin) {
		this.fwAdmin = fwAdmin;
	}

	public Process launch(Manipulator manipulator, File cwd) throws IllegalArgumentException, IOException, FrameworkAdminRuntimeException {
		SimpleBundlesState.checkAvailability(fwAdmin);
		LauncherData launcherData = manipulator.getLauncherData();
		if (launcherData.getLauncher() == null)
			return launchInMemory(manipulator, cwd);
		return launchByLauncher(manipulator, cwd);
	}

	private Process launchByLauncher(Manipulator manipulator, File cwd) throws IOException {
		LauncherData launcherData = manipulator.getLauncherData();

		if (launcherData.getLauncher() == null)
			throw new IllegalStateException(Messages.exception_launcherLocationNotSet);
		String[] cmdarray = new String[] { launcherData.getLauncher().getAbsolutePath() };
		if (cwd == null)
			cwd = launcherData.getLauncher().getParentFile();
		Process process = Runtime.getRuntime().exec(cmdarray, null, cwd);
		return process;
	}

	private Process launchInMemory(Manipulator manipulator, File cwd) throws IOException {
		LauncherData launcherData = manipulator.getLauncherData();
		Utils.checkAbsoluteFile(launcherData.getFwJar(), "fwJar"); //$NON-NLS-1$
		Utils.checkAbsoluteDir(cwd, "cwd"); //$NON-NLS-1$

		List cmdList = new LinkedList();
		if (launcherData.getJvm() != null)
			cmdList.add(launcherData.getJvm().getAbsolutePath());
		else
			cmdList.add("java"); //$NON-NLS-1$

		if (launcherData.getJvmArgs() != null)
			for (int i = 0; i < launcherData.getJvmArgs().length; i++)
				cmdList.add(launcherData.getJvmArgs()[i]);

		cmdList.add("-jar"); //$NON-NLS-1$
		cmdList.add(Utils.getRelativePath(launcherData.getFwJar(), cwd));

		AdvancedEquinoxManipulatorImpl.checkConsistencyOfFwConfigLocAndFwPersistentDataLoc(launcherData);
		cmdList.add(EquinoxConstants.OPTION_CONFIGURATION);
		cmdList.add(Utils.getRelativePath(launcherData.getFwPersistentDataLocation(), cwd));

		if (launcherData.isClean())
			cmdList.add(EquinoxConstants.OPTION_CLEAN);

		String[] cmdarray = new String[cmdList.size()];
		cmdList.toArray(cmdarray);
		Process process = Runtime.getRuntime().exec(cmdarray, null, cwd);
		return process;
	}
}
