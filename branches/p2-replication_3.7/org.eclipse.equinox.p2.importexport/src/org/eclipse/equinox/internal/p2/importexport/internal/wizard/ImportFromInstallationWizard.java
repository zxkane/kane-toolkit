package org.eclipse.equinox.internal.p2.importexport.internal.wizard;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.internal.p2.importexport.internal.Constants;
import org.eclipse.equinox.internal.p2.importexport.internal.Messages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class ImportFromInstallationWizard extends AbstractWizard implements IImportWizard {

	public ImportFromInstallationWizard() {
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(Messages.ImportWizard_WINDOWTITLE);
		setDefaultPageImageDescriptor(ImageDescriptor.createFromURL(Platform.getBundle(Constants.Bundle_ID).getEntry("icons/ico_import_export.gif"))); //$NON-NLS-1$
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		super.addPages();
		mainPage = new ImportFromInstallationPage("mainPage"); //$NON-NLS-1$
		addPage(mainPage);
	}
}
