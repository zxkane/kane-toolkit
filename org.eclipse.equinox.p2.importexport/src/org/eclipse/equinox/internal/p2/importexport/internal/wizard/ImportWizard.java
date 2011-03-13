package org.eclipse.equinox.internal.p2.importexport.internal.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.importexport.internal.Constants;
import org.eclipse.equinox.internal.p2.importexport.internal.Messages;
import org.eclipse.equinox.internal.p2.ui.ProvUI;
import org.eclipse.equinox.internal.p2.ui.ProvUIMessages;
import org.eclipse.equinox.internal.p2.ui.dialogs.ISelectableIUsPage;
import org.eclipse.equinox.internal.p2.ui.dialogs.InstallWizard;
import org.eclipse.equinox.internal.p2.ui.model.IUElementListRoot;
import org.eclipse.equinox.p2.engine.ProvisioningContext;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.InstallOperation;
import org.eclipse.equinox.p2.ui.LoadMetadataRepositoryJob;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.statushandlers.StatusManager;

public class ImportWizard extends InstallWizard implements IImportWizard {

	public ImportWizard() {
		this(ProvisioningUI.getDefaultUI(), null, null, null);
	}

	public ImportWizard(ProvisioningUI ui, InstallOperation operation, Collection<IInstallableUnit> initialSelections, LoadMetadataRepositoryJob preloadJob) {
		super(ui, operation, initialSelections, preloadJob);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(Messages.ImportWizard_WINDOWTITLE);
		setDefaultPageImageDescriptor(ImageDescriptor.createFromURL(Platform.getBundle(Constants.Bundle_ID).getEntry("icons/ico_import_export.gif"))); //$NON-NLS-1$
		setNeedsProgressMonitor(true);
	}

	@Override
	protected ISelectableIUsPage createMainPage(IUElementListRoot input,
			Object[] selections) {
		return new ImportPage(ui, this);
	}

	@Override
	protected ProvisioningContext getProvisioningContext() {
		return ((ImportPage) mainPage).getProvisioningContext();
	}

	/**
	 * Recompute the provisioning plan based on the items in the IUElementListRoot and the given provisioning context.
	 * Report progress using the specified runnable context.  This method may be called before the page is created.
	 * 
	 * @param runnableContext
	 */
	@Override
	public void recomputePlan(IRunnableContext runnableContext) {
		if (((ImportPage) mainPage).hasUnLoadRepo()) {
			try {
				runnableContext.run(false, true, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InterruptedException {
						((ImportPage) mainPage).recompute(monitor);
					}
				});
				ProvisioningContext context = getProvisioningContext();
				initializeResolutionModelElements(getOperationSelections());
				if (planSelections.length == 0) {
					operation = null;
					couldNotResolve(ProvUIMessages.ResolutionWizardPage_NoSelections);
				} else {
					operation = getProfileChangeOperation(planSelections);
					operation.setProvisioningContext(context);
				}				
				runnableContext.run(true, true, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) {
						operation.resolveModal(monitor);
					}
				});
				planChanged();
			} catch (InterruptedException e) {
				// Nothing to report if thread was interrupted
			} catch (InvocationTargetException e) {
				ProvUI.handleException(e.getCause(), null, StatusManager.SHOW | StatusManager.LOG);
				couldNotResolve(null);
			}
		} else
			super.recomputePlan(runnableContext);
	}

	void couldNotResolve(String message) {
		IStatus couldNotResolveStatus;
		if (message != null) {
			couldNotResolveStatus = new Status(IStatus.ERROR, Constants.Bundle_ID, message, null);
		} else {
			couldNotResolveStatus = new Status(IStatus.ERROR, Constants.Bundle_ID, ProvUIMessages.ProvisioningOperationWizard_UnexpectedFailureToResolve, null);
		}
		StatusManager.getManager().handle(couldNotResolveStatus, StatusManager.LOG);
	}
}
