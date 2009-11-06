package com.example.p2.touchpoint.actions;

import java.io.File;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.provisional.p2.engine.ProvisioningAction;

@SuppressWarnings("restriction")
public class DeleteDesktopAction extends ProvisioningAction {
	
	public static final String NAME = "deleteDesktop";
	public static final String KEY_NAME = "name";

	@Override
	public IStatus execute(Map parameters) {
		if(Platform.OS_LINUX.equals(Platform.getOS()) || Platform.OS_SOLARIS.equals(Platform.getOS())) {
			String name = (String) parameters.get(KEY_NAME);
			File desktop = new File(System.getProperty("user.home"), "Desktop/" + name);
			desktop.delete();
		} else if(Platform.OS_WIN32.equals(Platform.getOS())) {
			// TODO implement it
			System.out.println("Not implement yet.");
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(Map parameters) {
		// TODO Auto-generated method stub
		return null;
	}
}
