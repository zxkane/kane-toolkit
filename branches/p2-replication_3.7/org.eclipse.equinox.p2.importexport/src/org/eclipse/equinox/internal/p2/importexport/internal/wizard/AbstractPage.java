package org.eclipse.equinox.internal.p2.importexport.internal.wizard;

import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.internal.p2.importexport.P2ImportExport;
import org.eclipse.equinox.internal.p2.importexport.internal.Constants;
import org.eclipse.equinox.internal.p2.importexport.internal.Messages;
import org.eclipse.equinox.internal.p2.ui.ProvUIMessages;
import org.eclipse.equinox.internal.p2.ui.dialogs.ILayoutConstants;
import org.eclipse.equinox.internal.p2.ui.viewers.DeferredQueryContentProvider;
import org.eclipse.equinox.internal.p2.ui.viewers.IUColumnConfig;
import org.eclipse.equinox.internal.p2.ui.viewers.IUDetailsLabelProvider;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
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
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public abstract class AbstractPage extends WizardPage implements Listener {

	protected String currentMessage;
	protected Button destinationBrowseButton;
	protected Combo destinationNameField;
	protected P2ImportExport importexportService = null;
	protected CheckboxTableViewer viewer = null;
	protected Exception finishException;
	protected static IProfileRegistry profileRegistry = null;
	protected static IProvisioningAgent agent = null;

	static {
		BundleContext context = Platform.getBundle(Constants.Bundle_ID).getBundleContext();
		ServiceTracker<IProvisioningAgent, IProvisioningAgent> tracker = new ServiceTracker<IProvisioningAgent, IProvisioningAgent>(context, IProvisioningAgent.class, null);
		tracker.open();
		agent = tracker.getService();
		tracker.close();
		if (agent != null)
			profileRegistry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
	}

	public AbstractPage(String pageName) {
		super(pageName);
	}

	public AbstractPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	protected IProfile getSelfProfile() {
		if (profileRegistry != null) { 
			String selfID = System.getProperty("eclipse.p2.profile"); //$NON-NLS-1$
			if(selfID == null)
				selfID = IProfileRegistry.SELF;
			return profileRegistry.getProfile(selfID);
		}
		return null;
	}

	private void createColumns(TableViewer viewer) {
		String[] titles = { Messages.Column_Name, Messages.Column_Version, Messages.Column_Id};
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
		initializeService();
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
		destinationBrowseButton.setText(Messages.Page_BUTTON_BROWSER);
		destinationBrowseButton.addListener(SWT.Selection, this);
		destinationBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
	}

	protected IUColumnConfig[] getColumnConfig() {
		return new IUColumnConfig[] {new IUColumnConfig(ProvUIMessages.ProvUI_NameColumnTitle, IUColumnConfig.COLUMN_NAME, ILayoutConstants.DEFAULT_PRIMARY_COLUMN_WIDTH), 
				new IUColumnConfig(ProvUIMessages.ProvUI_VersionColumnTitle, IUColumnConfig.COLUMN_VERSION, ILayoutConstants.DEFAULT_SMALL_COLUMN_WIDTH), 
				new IUColumnConfig(ProvUIMessages.ProvUI_IdColumnTitle, IUColumnConfig.COLUMN_ID, ILayoutConstants.DEFAULT_COLUMN_WIDTH)};
	}

	protected void createInstallationTable(final Composite parent) {
		viewer = CheckboxTableViewer.newCheckList(parent, SWT.MULTI | SWT.BORDER);
		final Table table = viewer.getTable();
		createColumns(viewer);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		viewer.getControl().setSize(300, 200);
		viewer.setContentProvider(getContentProvider());
		viewer.setLabelProvider(getLabelProvider());
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
		ICheckStateProvider provider = getViewerDefaultState();
		if (provider != null)
			viewer.setCheckStateProvider(provider);
		viewer.setInput(getInput());
	}

	protected ICheckStateProvider getViewerDefaultState() {
		return null;
	}

	protected ITableLabelProvider getLabelProvider() {
		return new IUDetailsLabelProvider(null, getColumnConfig(), null);
	}

	protected IContentProvider getContentProvider() {
		return new DeferredQueryContentProvider();
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

	protected abstract void doFinish() throws Exception;

	protected int getBrowseDialogStyle() {
		return SWT.OPEN;
	}

	/**
	 * returns the destination label
	 * @return non null string
	 */
	protected abstract String getDestinationLabel();

	/**
	 * Answer the contents of self's destination specification widget
	 * 
	 * @return java.lang.String
	 */
	protected String getDestinationValue() {
		return destinationNameField.getText().trim();
	}

	/**
	 * return the title of dialog
	 * @return non null string
	 */
	protected abstract String getDialogTitle();

	protected abstract Object getInput();

	protected abstract String getInvalidDestinationMessage();

	protected String getNoOptionsMessage() {
		return Messages.PAGE_NOINSTALLTION_ERROR;
	}
	protected abstract void giveFocusToDestination();

	/**
	 * Open an appropriate destination browser so that the user can specify a
	 * source to import from
	 */
	protected void handleDestinationBrowseButtonPressed() {
		FileDialog dialog = new FileDialog(getContainer().getShell(),
				getBrowseDialogStyle() | SWT.SHEET);
		dialog.setText(getDialogTitle());
		dialog.setFilterPath(getDestinationValue());
		dialog.setFilterExtensions(new String[] {Messages.EXTENSION_P2F, Messages.EXTENSION_ALL});
		dialog.setFilterNames(new String[] {Messages.EXTENSION_P2F_NAME, Messages.EXTENSION_ALL_NAME});
		String selectedFileName = dialog.open();

		if (selectedFileName != null) {
			if(!selectedFileName.endsWith(Messages.EXTENSION_P2F.substring(1)))
				selectedFileName += Messages.EXTENSION_P2F.substring(1);
			setDestinationValue(selectedFileName);
		}
	}

	public void handleEvent(Event event) {
		Widget source = event.widget;

		if (source == destinationBrowseButton) {
			handleDestinationBrowseButtonPressed();
		}
	}

	protected void initializeService() {
		ServiceTracker<P2ImportExport, P2ImportExport> tracker = new ServiceTracker<P2ImportExport, P2ImportExport>(Platform.getBundle(Constants.Bundle_ID).getBundleContext(), 
				P2ImportExport.class.getName(), null);
		tracker.open();
		importexportService = tracker.getService();
		tracker.close();
	}

	protected void setDestinationValue(String selectedFileName) {
		destinationNameField.setText(selectedFileName);
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
		if(viewer == null || viewer.getCheckedElements().length > 0)
			return true;

		currentMessage = getNoOptionsMessage();
		return false;
	}

	protected boolean validDestination() {
		if (this.destinationNameField == null)
			return true;
		File file = new File(getDestinationValue());
		return !(file.getPath().length() <= 0 || file.isDirectory());
	}
}