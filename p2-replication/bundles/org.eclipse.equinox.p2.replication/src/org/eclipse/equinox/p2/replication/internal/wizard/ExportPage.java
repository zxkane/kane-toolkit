package org.eclipse.equinox.p2.replication.internal.wizard;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.ui.IUPropertyUtils;
import org.eclipse.equinox.p2.replication.Constants;
import org.eclipse.equinox.p2.replication.P2Replicator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Widget;
import org.osgi.util.tracker.ServiceTracker;
@SuppressWarnings("restriction")
public class ExportPage extends WizardPage implements Listener{

	private class InstallationContentProvider implements IStructuredContentProvider {

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getElements(Object inputElement) {
			return (Object[])inputElement;
		}

	}

	private class InstallationLabelProvider extends LabelProvider implements ITableLabelProvider{

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			IInstallableUnit iu = (IInstallableUnit) element;
			switch (columnIndex) {
			case 0:
				return IUPropertyUtils.getIUProperty(iu, IInstallableUnit.PROP_NAME);
			case 1:
				return iu.getVersion().toString();
			case 2:
				return iu.getId();
			default:
				throw new RuntimeException("Should not happen"); //$NON-NLS-1$
			}

		}
	}

	private CheckboxTableViewer viewer = null;
	private P2Replicator replicator = null;
	private Combo destinationNameField;
	private Button destinationBrowseButton;
	private String currentMessage;

	public ExportPage(String pageName) {
		super(pageName);
		setTitle(Message.EXPORTPage_TITLE);
		setDescription(Message.EXPORTPage_DESCRIPTION);
	}


	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		initializeReplicator();
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 10;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
				| GridData.HORIZONTAL_ALIGN_FILL));

		Label label = new Label(composite, SWT.NONE);
		if(replicator.getSelfProfile() == null){			
			label.setText(Message.ExportPage_ERROR_CONFIG);
		}else {
			label.setText(Message.EXPORTPage_LABEL);

			createInstallationTable(composite);
			createDestinationGroup(composite);
		}

		// can not finish initially, but don't want to start with an error
		// message either
		if (!(validDestination() && validateOptionsGroup())) {
			setPageComplete(false);
		}

		setControl(composite);

		giveFocusToDestination();
		Dialog.applyDialogFont(composite);
	}

	private void createDestinationGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));

		Label label = new Label(composite, SWT.NONE);
		label.setText(Message.ExportPage_LABEL_EXPORTFILE);

		destinationNameField = new Combo(composite, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		destinationNameField.setLayoutData(data);
		destinationNameField.addListener(SWT.Modify | SWT.Selection, this);
		destinationBrowseButton = new Button(composite, SWT.PUSH);
		destinationBrowseButton.setText(Message.ExportPage_BUTTON_BROWSER);
		destinationBrowseButton.addListener(SWT.Selection, this);
		destinationBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		new Label(parent, SWT.NONE); // vertical spacer
	}

	/**
	 * Open an appropriate destination browser so that the user can specify a
	 * source to import from
	 */
	protected void handleDestinationBrowseButtonPressed() {
		FileDialog dialog = new FileDialog(getContainer().getShell(),
				SWT.SAVE | SWT.SHEET);
		dialog.setText(getFileDialogTitle());
		dialog.setFilterPath(getDestinationValue());
		dialog.setFilterExtensions(new String[] {Message.EXTENSION_P2F, Message.EXTENSION_ALL});
		dialog.setFilterNames(new String[] {Message.EXTENSION_P2F_NAME, Message.EXTENSION_ALL_NAME});
		String selectedFileName = dialog.open();

		if (selectedFileName != null) {
			setDestinationValue(selectedFileName);
		}
	}

	protected String getFileDialogTitle() {
		return Message.ExportPage_FILEDIALOG_TITLE;
	}

	/**
	 * Answer the contents of self's destination specification widget
	 * 
	 * @return java.lang.String
	 */
	protected String getDestinationValue() {
		return destinationNameField.getText().trim();
	}

	protected void setDestinationValue(String selectedFileName) {
		destinationNameField.setText(selectedFileName);
	}


	private void initializeReplicator() {
		ServiceTracker tracker = new ServiceTracker(Platform.getBundle(Constants.Bundle_ID).getBundleContext(), 
				P2Replicator.class.getName(), null);
		tracker.open();
		replicator = (P2Replicator) tracker.getService();
		tracker.close();
	}

	private void giveFocusToDestination() {
		viewer.getControl().setFocus();
	}

	private void createInstallationTable(Composite parent) {
		viewer = CheckboxTableViewer.newCheckList(parent,  SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns(viewer);
		viewer.setContentProvider(new InstallationContentProvider());
		viewer.setLabelProvider(new InstallationLabelProvider());
		viewer.setInput(sortByName(replicator.getRootIUs()));
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updatePageCompletion();
			}
		});
	}

	private IInstallableUnit[] sortByName(IInstallableUnit[] rootIUs) {
		Arrays.sort(rootIUs, new Comparator<IInstallableUnit>(){

			public int compare(IInstallableUnit iu1, IInstallableUnit iu2) {
				return IUPropertyUtils.getIUProperty(iu1, IInstallableUnit.PROP_NAME).compareTo(
						IUPropertyUtils.getIUProperty(iu2, IInstallableUnit.PROP_NAME));
			}

		});
		return rootIUs;
	}


	private void createColumns(TableViewer viewer2) {
		String[] titles = { Message.Column_Name, Message.Column_Version, Message.Column_Id};
		int[] bounds = { 200, 100, 200 };

		for (int i = 0; i < titles.length; i++) {
			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText(titles[i]);
			column.getColumn().setWidth(bounds[i]);
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
		}

		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(false);
	}

	public void handleEvent(Event event) {
		Widget source = event.widget;

		if (source == destinationBrowseButton) {
			handleDestinationBrowseButtonPressed();
		}

		updatePageCompletion();
	}

	/**
	 * Determine if the page is complete and update the page appropriately.
	 */
	protected void updatePageCompletion() {
		boolean pageComplete = determinePageCompletion();
		setPageComplete(pageComplete);
		if (pageComplete) {
			setMessage(null);
		}
	}

	protected boolean determinePageCompletion() {
		// validate groups in order of priority so error message is the most important one
		boolean complete = validateDestinationGroup() && validateOptionsGroup();

		// Avoid draw flicker by not clearing the error
		// message unless all is valid.
		if (complete) {
			setErrorMessage(null);
		} else {
			setErrorMessage(currentMessage);
		}

		return complete;
	}

	protected boolean validateOptionsGroup() {
		if(viewer.getCheckedElements().length > 0)
			return true;

		currentMessage = getNoOptionsMessage();

		return false;
	}


	protected String getNoOptionsMessage() {
		return Message.ExportPage_NOINSTALLTION_ERROR;
	}


	/**
	 * Validate the destination group.
	 * @return <code>true</code> if the group is valid. If
	 * not set the error message and return <code>false</code>.
	 */
	protected boolean validateDestinationGroup() {
		if (!validDestination()) {
			currentMessage = getInvalidDestinationMessage();
			return false;
		}

		return true;
	}

	protected boolean validDestination() {
		File file = new File(getDestinationValue());
		return !(file.getPath().length() <= 0 || file.isDirectory());
	}

	protected String getInvalidDestinationMessage() {
		return Message.ExportPage_DEST_ERRORMESSAGE;
	}
}
