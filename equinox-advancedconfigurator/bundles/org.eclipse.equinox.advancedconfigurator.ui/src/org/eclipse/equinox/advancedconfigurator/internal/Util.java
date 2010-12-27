package org.eclipse.equinox.advancedconfigurator.internal;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class Util {
	public static void restartConfirm() {
		if (MessageDialog.openConfirm(Display.getDefault().getActiveShell(), Messages.Util_RestartDialogTitle, Messages.Util_RestartDialogMessage))
			Display.getDefault().asyncExec(new Runnable() {

				public void run() {
					PlatformUI.getWorkbench().restart();
				}
			});
	}
}
