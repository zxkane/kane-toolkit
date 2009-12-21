package org.eclipse.equinox.p2.replication.internal.wizard;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
@SuppressWarnings("restriction")
public class ExportPage extends AbstractPage {

	public ExportPage(String pageName) {
		super(pageName);
		setTitle(Message.EXPORTPage_TITLE);
		setDescription(Message.EXPORTPage_DESCRIPTION);
	}

	@Override
	protected void createContents(Composite composite) {
		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		if(replicator.getSelfProfile() == null){			
			label.setText(Message.ExportPage_ERROR_CONFIG);
		}else {
			label.setText(Message.EXPORTPage_LABEL);

			createInstallationTable(composite);
			createDestinationGroup(composite);
		}
	}

	@Override
	public void doFinish() {
		final Object[] checked = viewer.getCheckedElements();
		OutputStream stream = null;
		try {
			File target = new File(ExportPage.this.destinationNameField.getText());
			if(!target.exists())
				target.createNewFile();
			stream = new BufferedOutputStream(new FileOutputStream(target));
			final OutputStream out = stream;
			getContainer().run(true, false, new IRunnableWithProgress() {

				public void run(IProgressMonitor monitor) throws InvocationTargetException,
				InterruptedException {
					IInstallableUnit[] units = new IInstallableUnit[checked.length];
					for(int i = 0; i < units.length; i++)
						units[i] = (IInstallableUnit)checked[i];
					try {
						replicator.save(out, units, monitor);
					} catch (ProvisionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(stream != null)
				try {
					stream.close();
				} catch (IOException e) {
					// do nothing
				}
		}
	}

	@Override
	protected String getFileDialogTitle() {
		return Message.ExportPage_FILEDIALOG_TITLE;
	}

	@Override
	protected IInstallableUnit[] getInput() {
		return replicator.getRootIUs();
	}


	@Override
	protected String getInvalidDestinationMessage() {
		return Message.ExportPage_DEST_ERRORMESSAGE;
	}

	@Override
	protected void giveFocusToDestination() {
		viewer.getControl().setFocus();
	}

	@Override
	protected String getDestinationLabel() {
		return Message.ExportPage_LABEL_EXPORTFILE;
	}

	@Override
	protected int getBrowseDialogStyle() {
		return SWT.SAVE;
	}

	@Override
	public void handleEvent(Event event) {
		super.handleEvent(event);

		updatePageCompletion();
	}
}
