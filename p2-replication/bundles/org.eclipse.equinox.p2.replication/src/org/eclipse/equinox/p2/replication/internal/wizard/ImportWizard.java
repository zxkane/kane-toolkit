package org.eclipse.equinox.p2.replication.internal.wizard;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.p2.replication.Constants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class ImportWizard extends Wizard implements IImportWizard {

	private ImportPage mainPage;

	public ImportWizard() {
	}

	@Override
	public boolean performFinish() {
		mainPage.doFinish();
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(Message.ImportWizard_WINDOWTITLE);
		setDefaultPageImageDescriptor(ImageDescriptor.createFromURL(Platform.getBundle(Constants.Bundle_ID).getEntry("icons/ico_import_export.gif"))); //$NON-NLS-1$
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		super.addPages();
		mainPage = new ImportPage("mainPage"); //$NON-NLS-1$
		addPage(mainPage);
	}
}
