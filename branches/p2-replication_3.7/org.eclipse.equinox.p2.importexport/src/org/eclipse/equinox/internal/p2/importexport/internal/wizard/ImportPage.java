package org.eclipse.equinox.internal.p2.importexport.internal.wizard;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.internal.p2.importexport.FeatureDetail;
import org.eclipse.equinox.internal.p2.importexport.internal.Messages;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MessageBox;

public class ImportPage extends AbstractImportPage {

	private class InstallationContentProvider implements IStructuredContentProvider {

		public void dispose() {
		}

		public Object[] getElements(Object inputElement) {
			return (Object[])inputElement;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	private class InstallationLabelProvider extends LabelProvider implements ITableLabelProvider{

		private IProfile profile = null;
		public InstallationLabelProvider() {
			profile = getSelfProfile();
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			IInstallableUnit iu = (IInstallableUnit) element;
			switch (columnIndex) {
			case 0:				
				return getIUNameWithDetail(iu);
			case 1:
				return iu.getVersion().toString();
			case 2:
				return iu.getId();
			default:
				throw new RuntimeException("Should not happen"); //$NON-NLS-1$
			}

		}
	}

	private String[] repositories;

	public ImportPage(String pageName) {
		super(pageName);
		setTitle(Messages.ImportPage_TITLE);
		setDescription(Messages.ImportPage_DESCRIPTION);
	}

	@Override
	protected void createContents(Composite composite) {
		createDestinationGroup(composite);
		createInstallationTable(composite);
	}

	@Override
	protected IContentProvider getContentProvider() {
		return new InstallationContentProvider();
	}

	@Override
	protected ITableLabelProvider getLabelProvider() {
		return new InstallationLabelProvider();
	}

	@Override
	protected int getBrowseDialogStyle() {
		return SWT.OPEN;
	}

	@Override
	protected String getDestinationLabel() {
		return Messages.ImportPage_DESTINATION_LABEL;
	}

	@Override
	protected String getDialogTitle() {
		return Messages.ImportPage_FILEDIALOG_TITLE;
	}

	@Override
	protected IInstallableUnit[] getInput() {
		return new IInstallableUnit[0];
	}

	@Override
	protected String getInvalidDestinationMessage() {
		return Messages.ImportPage_DEST_ERROR;
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
				List<FeatureDetail> features = importexportService.importP2F(input);
				List<IInstallableUnit> rootIUs = new ArrayList<IInstallableUnit>(features.size());
				List<String> repos = new ArrayList<String>(features.size());
				for (FeatureDetail feature : features) {
					rootIUs.add(feature.getTopIU());
					for (URI uri : feature.getReferencedRepositories())
						repos.add(uri.toString());
				}				
				viewer.setInput(rootIUs.toArray(new IInstallableUnit[rootIUs.size()]));
				repositories = repos.toArray(new String[repos.size()]);
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
					importexportService.replicate(repositories, units, monitor);
				} catch (ProvisionException e) {
					finishException = e;
				}	
			}
		});
		if(finishException != null)
			throw finishException;
		MessageBox message = new MessageBox(getContainer().getShell(), SWT.ICON_INFORMATION);
		message.setMessage(Messages.ImportPage_IMPORT_NOTIFICATION);
		message.open();
	}


}
