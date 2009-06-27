package com.ibm.hannover.development.tools.commands;

import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import com.ibm.hannover.development.tools.configurations.TargetPlatformConfiguration2;

public class ResetCommand extends AbstractHandler implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		URL installURL = Platform.getInstallLocation().getURL();
		IPath path = new Path(installURL.getFile()).removeTrailingSeparator();
		// TargetPlatformConfiguration conf = new
		// TargetPlatformConfiguration(path.toOSString());
		// conf.configure();
		TargetPlatformConfiguration2 conf = new TargetPlatformConfiguration2(
				path.toOSString(), "Running Platform"); //$NON-NLS-1$
		try {
			conf.configure(new NullProgressMonitor());
		} catch (CoreException e) {
			throw new ExecutionException(e.getMessage(), e);
		}
		return null;
	}
}
