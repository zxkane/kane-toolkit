package org.eclipse.equinox.internal.p2.replication.internal.wizard;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.internal.p2.replication.Constants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;


public class ExportWizard extends AbstractWizard implements IExportWizard {

	public ExportWizard() {
	}

	@Override
	public void addPages() {
		super.addPages();
		mainPage = new ExportPage("mainPage"); //$NON-NLS-1$
		addPage(mainPage);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(Message.EXPORTPage_TITLE);
		setDefaultPageImageDescriptor(ImageDescriptor.createFromURL(Platform.getBundle(Constants.Bundle_ID).getEntry("icons/ico_import_export.gif"))); //$NON-NLS-1$
		setNeedsProgressMonitor(true);
	}

}
