package org.eclipse.equinox.p2.replication.internal.wizard;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.InstallableUnitQuery;
import org.eclipse.equinox.internal.provisional.p2.query.Collector;
import org.eclipse.equinox.internal.provisional.p2.query.CompoundQuery;
import org.eclipse.equinox.p2.replication.P2Replicator.InstallationConfiguration;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MessageBox;

@SuppressWarnings("restriction")
public class ImportPage extends AbstractPage {

	private String[] repositories;

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

	private void disableInstalled() {
		IProfile profile = replicator.getSelfProfile();
		if(profile != null && viewer.getInput() != null) {
			IInstallableUnit[] units = (IInstallableUnit[])viewer.getInput();
			InstallableUnitQuery[] queries = new InstallableUnitQuery[units.length];
			for(int i = 0; i < units.length; i++)
				queries[i] = new InstallableUnitQuery(units[i].getId(), units[i].getVersion());
			Collector collector = profile.query(CompoundQuery.createCompoundQuery(queries, false), 
					new Collector(), new NullProgressMonitor());
			if(collector.size() > 0) {
				List<IInstallableUnit> shouldBeGrayed = new ArrayList<IInstallableUnit>();
				for(Object founded : collector.toArray(IInstallableUnit.class)) {
					IInstallableUnit installedIU = (IInstallableUnit)founded;
					for(IInstallableUnit unit : units) {
						if(installedIU.getId().equals(unit.getId()) &&
								installedIU.getVersion().compareTo(unit.getVersion()) >= 0)
							shouldBeGrayed.add(unit);
					}
				}
				viewer.setGrayedElements(shouldBeGrayed.toArray(
						new IInstallableUnit[shouldBeGrayed.size()]));
			}
		}
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
	public void handleEvent(Event event) {
		super.handleEvent(event);
		if(event.widget == viewer.getControl())
			updatePageCompletion();
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
				disableInstalled();
				viewer.refresh();
				repositories = conf.getRepositories();
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

	@Override
	protected boolean validateOptionsGroup() {
		Object[] checked = viewer.getCheckedElements();
		boolean checkGrayed = false;
		for(Object obj : checked) {
			if(viewer.getGrayed(obj)) {
				viewer.setChecked(obj, false);
				checkGrayed = true;
			}
		}
		if(checkGrayed)
			return false;
		return super.validateOptionsGroup();
	}

	@Override
	protected void doFinish() throws Exception{
		finishException = null;
		Object[] checked = viewer.getCheckedElements();
		final IInstallableUnit[] units = new IInstallableUnit[checked.length];
		for(int i = 0; i < checked.length; i++)
			units[i] = (IInstallableUnit) checked[i];
		getContainer().run(true, false, new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
				try {
					replicator.replicate(repositories, units, monitor);
				} catch (ProvisionException e) {
					finishException = e;
				}	
			}
		});
		if(finishException != null)
			throw finishException;
		MessageBox message = new MessageBox(getContainer().getShell(), SWT.ICON_INFORMATION);
		message.setMessage(Message.ImportPage_IMPORT_NOTIFICATION);
		message.open();
	}


}
