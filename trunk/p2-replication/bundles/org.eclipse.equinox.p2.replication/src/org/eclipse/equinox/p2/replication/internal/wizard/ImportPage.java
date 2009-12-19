package org.eclipse.equinox.p2.replication.internal.wizard;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.replication.P2Replicator.InstallationConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings("restriction")
public class ImportPage extends AbstractPage {

	public ImportPage(String pageName) {
		super(pageName);
		setTitle(Message.ImportPage_TITLE);
		setDescription(Message.ImportPage_DESCRIPTION);
	}

	@Override
	protected void createContents(Composite composite) {
		createDestinationGroup(composite);
		createInstallationTable(composite);
	}

	@Override
	protected int getBrowseDialogStyle() {
		return SWT.OPEN;
	}

	@Override
	protected String getDestinationLabel() {
		return Message.ImportPage_DESTINATION_LABEL;
	}

	@Override
	protected String getFileDialogTitle() {
		return Message.ImportPage_FILEDIALOG_TITLE;
	}

	@Override
	protected IInstallableUnit[] getInput() {
		return new IInstallableUnit[0];
	}

	@Override
	protected String getInvalidDestinationMessage() {
		return Message.ImportPage_DEST_ERROR;
	}

	@Override
	protected void giveFocusToDestination() {
		destinationNameField.setFocus();
	}

	@Override
	protected void setDestinationValue(String selectedFileName) {		
		super.setDestinationValue(selectedFileName);
		if(validateDestinationGroup()) {
			InputStream input = null;
			try{
				input = new BufferedInputStream(new FileInputStream(getDestinationValue()));
				InstallationConfiguration conf = replicator.load(input);
				viewer.setInput(conf.getRootIUs());
				viewer.refresh();
			} catch(IOException e) {
				//TODO
				e.printStackTrace();
			}
		}
	}

	@Override
	protected boolean validDestination() {
		File target = new File(getDestinationValue());
		return super.validDestination() && target.exists() && target.canRead();
	}


}
