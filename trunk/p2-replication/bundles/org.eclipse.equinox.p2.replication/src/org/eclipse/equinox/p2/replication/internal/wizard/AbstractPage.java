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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IContentProvider;
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
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Widget;
import org.osgi.util.tracker.ServiceTracker;
@SuppressWarnings("restriction")
public abstract class AbstractPage extends WizardPage implements Listener {

	protected class InstallationContentProvider implements IStructuredContentProvider {

		public void dispose() {
		}

		public Object[] getElements(Object inputElement) {
			return (Object[])inputElement;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
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

	protected String currentMessage;
	protected Button destinationBrowseButton;
	protected Combo destinationNameField;
	protected P2Replicator replicator = null;
	protected CheckboxTableViewer viewer = null;

	public AbstractPage(String pageName) {
		super(pageName);
	}

	public AbstractPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	private void createColumns(TableViewer viewer) {
		String[] titles = { Message.Column_Name, Message.Column_Version, Message.Column_Id};
		for (int i = 0; i < titles.length; i++) {
			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText(titles[i]);
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
		}

		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(false);
	}

	protected abstract void createContents(Composite composite);

	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		initializeReplicator();
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(1, true);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 5;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
				| GridData.HORIZONTAL_ALIGN_FILL));

		createContents(composite);

		// can not finish initially, but don't want to start with an error
		// message either
		if (!(validDestination() && validateOptionsGroup())) {
			setPageComplete(false);
		}

		setControl(composite);
		giveFocusToDestination();
		Dialog.applyDialogFont(composite);
	}

	protected void createDestinationGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.BORDER);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(
				GridData.FILL, GridData.FILL, true, false));

		Label label = new Label(composite, SWT.NONE);
		label.setText(getDestinationLabel());

		destinationNameField = new Combo(composite, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		destinationNameField.setLayoutData(data);
		destinationNameField.addListener(SWT.Modify | SWT.Selection, this);
		destinationBrowseButton = new Button(composite, SWT.PUSH);
		destinationBrowseButton.setText(Message.Page_BUTTON_BROWSER);
		destinationBrowseButton.addListener(SWT.Selection, this);
		destinationBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		new Label(composite, SWT.NONE); // vertical spacer
	}

	protected void createInstallationTable(final Composite parent) {
		viewer = CheckboxTableViewer.newCheckList(parent, SWT.MULTI | SWT.BORDER);
		final Table table = viewer.getTable();
		createColumns(viewer);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		viewer.getControl().setSize(300, 200);
		viewer.setContentProvider(getContentProvider());
		viewer.setLabelProvider(new InstallationLabelProvider());
		parent.addControlListener(new ControlAdapter() {
			private final int[] columnRate = new int[]{4, 2, 4};
			@Override
			public void controlResized(ControlEvent e) {
				Rectangle area = parent.getClientArea();
				Point size = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				ScrollBar vBar = table.getVerticalBar();
				int width = area.width - table.computeTrim(0,0,0,0).width - vBar.getSize().x;
				if (size.y > area.height + table.getHeaderHeight()) {
					// Subtract the scrollbar width from the total column width
					// if a vertical scrollbar will be required
					Point vBarSize = vBar.getSize();
					width -= vBarSize.x;
				}
				Point oldSize = table.getSize();
				TableColumn[] columns = table.getColumns();
				int hasUsed = 0, i = 0;
				if (oldSize.x > area.width) {
					// table is getting smaller so make the columns 
					// smaller first and then resize the table to
					// match the client area width
					for(; i < columns.length - 1; i++) {
						columns[i].setWidth(width*columnRate[i]/10);
						hasUsed += columns[i].getWidth();
					}
					columns[columns.length - 1].setWidth(width - hasUsed);
					table.setSize(area.width, area.height);
				} else {
					// table is getting bigger so make the table 
					// bigger first and then make the columns wider
					// to match the client area width
					table.setSize(area.width, area.height);
					for(; i < columns.length - 1; i++) {
						columns[i].setWidth(width*columnRate[i]/10);
						hasUsed += columns[i].getWidth();
					}
					columns[columns.length - 1].setWidth(width - hasUsed);
				}
			}
		});				
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updatePageCompletion();
			}
		});
		viewer.setInput(sortByName(getInput()));
	}

	protected IContentProvider getContentProvider() {
		return new InstallationContentProvider();
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

	protected abstract void doFinish();

	protected abstract int getBrowseDialogStyle();

	protected abstract String getDestinationLabel();

	/**
	 * Answer the contents of self's destination specification widget
	 * 
	 * @return java.lang.String
	 */
	protected String getDestinationValue() {
		return destinationNameField.getText().trim();
	}

	protected abstract String getFileDialogTitle();

	protected abstract IInstallableUnit[] getInput();

	protected abstract String getInvalidDestinationMessage();

	protected String getNoOptionsMessage() {
		return Message.PAGE_NOINSTALLTION_ERROR;
	}
	protected abstract void giveFocusToDestination();

	/**
	 * Open an appropriate destination browser so that the user can specify a
	 * source to import from
	 */
	protected void handleDestinationBrowseButtonPressed() {
		FileDialog dialog = new FileDialog(getContainer().getShell(),
				getBrowseDialogStyle() | SWT.SHEET);
		dialog.setText(getFileDialogTitle());
		dialog.setFilterPath(getDestinationValue());
		dialog.setFilterExtensions(new String[] {Message.EXTENSION_P2F, Message.EXTENSION_ALL});
		dialog.setFilterNames(new String[] {Message.EXTENSION_P2F_NAME, Message.EXTENSION_ALL_NAME});
		String selectedFileName = dialog.open();

		if (selectedFileName != null) {
			if(!selectedFileName.endsWith(Message.EXTENSION_P2F.substring(1)))
				selectedFileName += Message.EXTENSION_P2F.substring(1);
			setDestinationValue(selectedFileName);
		}
	}

	public void handleEvent(Event event) {
		Widget source = event.widget;

		if (source == destinationBrowseButton) {
			handleDestinationBrowseButtonPressed();
		}

		updatePageCompletion();
	}

	protected void initializeReplicator() {
		ServiceTracker tracker = new ServiceTracker(Platform.getBundle(Constants.Bundle_ID).getBundleContext(), 
				P2Replicator.class.getName(), null);
		tracker.open();
		replicator = (P2Replicator) tracker.getService();
		tracker.close();
	}

	protected void setDestinationValue(String selectedFileName) {
		destinationNameField.setText(selectedFileName);
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

	protected boolean validateOptionsGroup() {
		if(viewer.getCheckedElements().length > 0)
			return true;

		currentMessage = getNoOptionsMessage();
		return false;
	}

	protected boolean validDestination() {
		File file = new File(getDestinationValue());
		return !(file.getPath().length() <= 0 || file.isDirectory());
	}
}