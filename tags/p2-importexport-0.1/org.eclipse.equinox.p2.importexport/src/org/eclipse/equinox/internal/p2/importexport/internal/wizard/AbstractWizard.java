package org.eclipse.equinox.internal.p2.importexport.internal.wizard;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.importexport.Constants;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

public abstract class AbstractWizard extends Wizard {

	protected AbstractPage mainPage = null;

	public AbstractWizard() {
		super();
	}

	@Override
	public boolean performFinish() {
		try {
			mainPage.doFinish();
		} catch (Exception e) {
			Platform.getLog(Platform.getBundle(Constants.Bundle_ID)).log(
					new Status(IStatus.ERROR, Constants.Bundle_ID, e.getMessage(), e));
			MessageBox messageBox = new MessageBox(this.getShell(), SWT.ICON_ERROR);
			messageBox.setMessage(e.getMessage());
			messageBox.open();
			return false;
		}
		return true;
	}

}