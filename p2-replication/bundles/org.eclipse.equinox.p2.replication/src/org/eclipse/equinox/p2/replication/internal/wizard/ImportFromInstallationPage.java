package org.eclipse.equinox.p2.replication.internal.wizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.internal.p2.extensionlocation.ExtensionLocationArtifactRepository;
import org.eclipse.equinox.internal.p2.ui.model.ProfileElement;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
@SuppressWarnings("restriction")
public class ImportFromInstallationPage extends AbstractPage {

	public ImportFromInstallationPage(String pageName) {
		super(pageName);
		setTitle(Message.ImportFromInstallationPage_DIALOG_TITLE);
		setDescription(Message.ImportFromInstallationPage_DIALOG_DESCRIPTION);
	}

	@Override
	protected void createContents(Composite composite) {
		createDestinationGroup(composite);
		createInstallationTable(composite);
	}

	@Override
	protected void doFinish() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getDestinationLabel() {
		return Message.ImportFromInstallationPage_DESTINATION_LABEL;
	}

	@Override
	protected String getDialogTitle() {
		return Message.ImportFromInstallationPage_DIALOG_TITLE;
	}

	@Override
	protected Object getInput() {
		return new IInstallableUnit[0];
	}

	@Override
	protected String getInvalidDestinationMessage() {
		return Message.ImportFromInstallationPage_INVALID_DESTINATION;
	}

	@Override
	public void handleEvent(Event event) {
		super.handleEvent(event);
		if(event.widget == viewer.getControl() || event.widget == destinationBrowseButton)
			updatePageCompletion();
	}

	@Override
	protected boolean validateDestinationGroup() {
		boolean rt = super.validateDestinationGroup();
		if (rt) {
			try {
				ExtensionLocationArtifactRepository.validate(new File(getDestinationValue()).toURI(), null);
			} catch (ProvisionException e) {
				currentMessage = getInvalidDestinationMessage();
				rt = false;
			}
		}
		return rt;
	}

	@Override
	protected void giveFocusToDestination() {
		destinationBrowseButton.setFocus();
	}

	@Override
	protected void handleDestinationBrowseButtonPressed() {
		DirectoryDialog dialog = new DirectoryDialog(getContainer().getShell());
		dialog.setText(getDialogTitle());
		dialog.setFilterPath(getDestinationValue());
		final String selectedFileName = dialog.open();

		if (selectedFileName != null) {
			setDestinationValue(selectedFileName);
			try {
				getContainer().run(true, false, new IRunnableWithProgress() {

					public void run(IProgressMonitor monitor) throws InvocationTargetException,
					InterruptedException {
						try {
							IProvisioningAgent agent = replicator.getAgentProvider().createAgent(new File(selectedFileName, "p2").toURI()); //$NON-NLS-1$
							IProfileRegistry registry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
							final IProfile profile = registry.getProfiles()[0];
							final ProfileElement element = new ProfileElement(null, profile.getProfileId()) {
								@Override
								public org.eclipse.equinox.p2.query.IQueryable<?> getQueryable() {
									return profile;
								};
							};
							element.setQueryable(profile);
							Display.getDefault().asyncExec(new Runnable() {								
								public void run() {
									viewer.setInput(element);
								}
							});							
						} catch (ProvisionException e) {
							throw new InvocationTargetException(e);
						}
					}
				});
			} catch (InvocationTargetException e) {
				setErrorMessage(e.getLocalizedMessage());
			} catch (InterruptedException e) {

			}
		}
	}

	@Override
	protected boolean validDestination() {
		if (this.destinationNameField == null)
			return true;
		File file = new File(getDestinationValue());
		return file.exists() && file.isDirectory();
	}
}