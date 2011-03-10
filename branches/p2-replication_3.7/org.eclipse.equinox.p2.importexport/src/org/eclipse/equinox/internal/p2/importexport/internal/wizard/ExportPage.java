package org.eclipse.equinox.internal.p2.importexport.internal.wizard;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.internal.p2.importexport.internal.Message;
import org.eclipse.equinox.internal.p2.ui.ProvUI;
import org.eclipse.equinox.internal.p2.ui.model.ProfileElement;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

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
	public void doFinish() throws Exception {
		finishException = null;
		if(viewer == null)
			return;
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
						units[i] = ProvUI.getAdapter(checked[i], IInstallableUnit.class);
					IStatus status = replicator.exportP2F(out, units, monitor);
					if (status.isMultiStatus()) {
						final StringBuilder sb = new StringBuilder();
						for (IStatus child : status.getChildren()) {
							if (child.isMultiStatus()) {
								for (IStatus grandchild : child.getChildren())
									sb.append(grandchild.getMessage()).append("\n"); //$NON-NLS-1$
							} else if (child.isOK())
								sb.insert(0, Message.ExportPage_SuccessWithProblems);
							else {
								sb.insert(0, Message.ExportPage_Fail);
								sb.append(status.getMessage());
							}
						}
						Display.getDefault().asyncExec(new Runnable() {

							public void run() {
								MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_WARNING);
								messageBox.setMessage(sb.toString());
								messageBox.open();
							}
						});
					}
				}
			});
		} finally {
			if(stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					// do nothing
				}
			}
			if(finishException != null)
				throw finishException;
		}
	}

	@Override
	protected String getDialogTitle() {
		return Message.ExportPage_FILEDIALOG_TITLE;
	}

	@Override
	protected Object getInput() {
		//		return replicator.getRootIUs();
		ProfileElement element = new ProfileElement(null, replicator.getSelfProfile().getProfileId());
		return element;
	}


	@Override
	protected String getInvalidDestinationMessage() {
		return Message.ExportPage_DEST_ERRORMESSAGE;
	}

	@Override
	protected void giveFocusToDestination() {
		if(viewer != null)
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
