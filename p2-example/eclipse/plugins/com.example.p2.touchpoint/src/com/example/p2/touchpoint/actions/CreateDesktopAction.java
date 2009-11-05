package com.example.p2.touchpoint.actions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.provisional.p2.engine.ProvisioningAction;

@SuppressWarnings("restriction")
public class CreateDesktopAction extends ProvisioningAction {
	
	public static final String NAME = "createDesktop";
	public static final String KEY_NAME = "name";
	public static final String KEY_DISPLAY_NAME = "displayName";
	public static final String KEY_ICON = "icon";
	public static final String KEY_PATH = "path";

	@Override
	public IStatus execute(Map parameters) {
		if(Platform.OS_LINUX.equals(Platform.getOS()) || Platform.OS_SOLARIS.equals(Platform.getOS())) {
			String[] strings = 	{
					"[Desktop Entry]", //$NON-NLS-1$
					"Name=" + parameters.get(KEY_DISPLAY_NAME), //$NON-NLS-1$
					"Encoding=UTF-8", //$NON-NLS-1$
					"Terminal=false", //$NON-NLS-1$
					"Exec=" + parameters.get(KEY_PATH), //$NON-NLS-1$
					"Type=Application", //$NON-NLS-1$
					"Icon=" + parameters.get(KEY_ICON)}; //$NON-NLS-1$
				File parent = new File(System.getProperty("user.home"), "Desktop");
				File file = new File(parent, parameters.get(KEY_NAME) + ".desktop");
				FileWriter writer = null;
				try {
					writer = new FileWriter(file);
					for (int i=0; i<strings.length; i++)
						writer.write(strings[i] + "\n"); //$NON-NLS-1$
				} catch (IOException e){
					return new Status(IStatus.ERROR, "com.example.p2.touchpoint", "Exception occurred during creation of new .desktop file.",e); //$NON-NLS-1$
				} finally {
					try {
						if (writer != null)
							writer.close();
					} catch (IOException e) {
						/** At this point all human-possible was tried, silently ignore this */
					}
				}
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
