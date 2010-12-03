package org.eclipse.equinox.advancedconfigurator.internal.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.equinox.advancedconfigurator.internal.Activator;
import org.eclipse.equinox.internal.p2.ui.dialogs.ILayoutConstants;
import org.eclipse.equinox.internal.p2.ui.model.InstalledIUElement;
import org.eclipse.equinox.internal.p2.ui.model.ProfileElement;
import org.eclipse.equinox.internal.p2.ui.viewers.DeferredQueryContentProvider;
import org.eclipse.equinox.internal.p2.ui.viewers.IUColumnConfig;
import org.eclipse.equinox.internal.p2.ui.viewers.IUDetailsLabelProvider;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.osgi.util.tracker.ServiceTracker;

@SuppressWarnings("restriction")
public class ConfiguratorPage extends WizardPage {

	protected CheckboxTableViewer viewer = null;
	protected String currentMessage;
	private IProfile selfProfile;
	private List<String> initialComps;

	public ConfiguratorPage(String pageName) {
		super(pageName);
		setTitle(Messages.ConfiguratorPage_Title);
		setDescription(Messages.ConfiguratorPage_Description);
	}

	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(1, true);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 5;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

		createContents(composite);

		// can not finish initially, but don't want to start with an error
		// message either
		if (!validateOptionsGroup()) {
			setPageComplete(false);
		}

		setControl(composite);
		Dialog.applyDialogFont(composite);
	}

	protected boolean validateOptionsGroup() {
		if (viewer == null || viewer.getCheckedElements().length > 0)
			return true;

		currentMessage = getNoOptionsMessage();
		return false;
	}

	protected String getNoOptionsMessage() {
		return Messages.ConfiguratorPage_NoSelection;
	}

	protected void createContents(Composite composite) {
		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		if (getSelfProfile() == null) {
			label.setText(Messages.ConfiguratorPage_P2SelfNotFound);
		} else {
			label.setText(Messages.ConfiguratorPage_ConfiguratorDescription);

			createInstallationTable(composite);
		}
	}

	IProfile getSelfProfile() {
		if (selfProfile == null) {
			ServiceTracker tracker = new ServiceTracker(Activator.getContext(), IProvisioningAgent.class.getName(), null);
			try {
				tracker.open();
				IProvisioningAgent agent = (IProvisioningAgent) tracker.getService();
				IProfileRegistry registry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
				selfProfile = registry.getProfile(IProfileRegistry.SELF);
				if (selfProfile != null) {
					long[] timestamps = registry.listProfileTimestamps(selfProfile.getProfileId());
					for (long timestamp : timestamps) {
						IProfile profile = registry.getProfile(selfProfile.getProfileId(), timestamp);
						IQueryResult<IInstallableUnit> result = profile.query(QueryUtil.createPipeQuery(QueryUtil.createIUGroupQuery(), QueryUtil
								.createQuery("select(iu2 | !exists(iu | iu.requirements.exists(r | iu2 ~= r)))"))
						/* QueryUtil.createIUPropertyQuery(IProfile.PROP_PROFILE_ROOT_IU, Boolean.TRUE.toString()) */, null);
						if (!result.isEmpty()) {
							initialComps = new ArrayList<String>();
							for (IInstallableUnit iu : result.toSet())
								initialComps.add(iu.getId());
							break;
						}
					}
				}
			} finally {
				tracker.close();
			}
		}
		return selfProfile;
	}

	protected void createInstallationTable(final Composite parent) {
		viewer = CheckboxTableViewer.newCheckList(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		final Table table = viewer.getTable();
		createColumns(viewer);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		viewer.getControl().setSize(300, 200);
		viewer.setContentProvider(new DeferredQueryContentProvider());
		viewer.setLabelProvider(new IUDetailsLabelProvider(null, getColumnConfig(), null));
		parent.addControlListener(new ControlAdapter() {
			private final int[] columnRate = new int[] { 4, 2, 4 };

			@Override
			public void controlResized(ControlEvent e) {
				Rectangle area = parent.getClientArea();
				Point size = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				ScrollBar vBar = table.getVerticalBar();
				int width = area.width - table.computeTrim(0, 0, 0, 0).width - vBar.getSize().x;
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
					for (; i < columns.length - 1; i++) {
						columns[i].setWidth(width * columnRate[i] / 10);
						hasUsed += columns[i].getWidth();
					}
					columns[columns.length - 1].setWidth(width - hasUsed);
					table.setSize(area.width, area.height);
				} else {
					// table is getting bigger so make the table
					// bigger first and then make the columns wider
					// to match the client area width
					table.setSize(area.width, area.height);
					for (; i < columns.length - 1; i++) {
						columns[i].setWidth(width * columnRate[i] / 10);
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
		viewer.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(CheckStateChangedEvent event) {
				InstalledIUElement e = (InstalledIUElement) event.getElement();
				if (initialComps.contains(e.getIU().getId())) {
					event.getCheckable().setChecked(e, true);
				}
			}
		});
		viewer.setCheckStateProvider(new ICheckStateProvider() {

			public boolean isGrayed(Object element) {
				InstalledIUElement e = (InstalledIUElement) element;
				if (initialComps.contains(e.getIU().getId())) {
					return true;
				}
				return false;
			}

			public boolean isChecked(Object element) {
				InstalledIUElement e = (InstalledIUElement) element;
				if (initialComps.contains(e.getIU().getId())) {
					return true;
				}
				return false;
			}
		});
		viewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				InstalledIUElement iu1 = (InstalledIUElement) e1;
				InstalledIUElement iu2 = (InstalledIUElement) e2;
				return iu1.getIU().getProperty(IInstallableUnit.PROP_NAME, null).compareTo(iu2.getIU().getProperty(IInstallableUnit.PROP_NAME, null));
			}
		});
		viewer.setInput(getInput());
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible == true) {
			// TODO recheck the components for existing policy
			viewer.getTable().setFocus();
		}
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
		boolean complete = validateOptionsGroup();

		// Avoid draw flicker by not clearing the error
		// message unless all is valid.
		if (complete) {
			setErrorMessage(null);
		} else {
			setErrorMessage(currentMessage);
		}

		return complete;
	}

	private IUColumnConfig[] getColumnConfig() {
		return new IUColumnConfig[] { new IUColumnConfig("", IUColumnConfig.COLUMN_NAME, ILayoutConstants.DEFAULT_PRIMARY_COLUMN_WIDTH),
				new IUColumnConfig("", IUColumnConfig.COLUMN_VERSION, ILayoutConstants.DEFAULT_SMALL_COLUMN_WIDTH),
				new IUColumnConfig("", IUColumnConfig.COLUMN_ID, ILayoutConstants.DEFAULT_COLUMN_WIDTH) };
	}

	private void createColumns(TableViewer viewer) {
		String[] titles = { Messages.ConfiguratorPage_ColumnName, Messages.ConfiguratorPage_ColumnVersion, Messages.ConfiguratorPage_ColumnID };
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

	protected Object getInput() {
		// return replicator.getRootIUs();
		ProfileElement element = new ProfileElement(null, getSelfProfile().getProfileId());
		return element;
	}

	public void handleEvent(Event event) {
		updatePageCompletion();
	}

	public IInstallableUnit[] getSelectedComponents() {
		Object[] objs = viewer.getCheckedElements();
		IInstallableUnit[] ius = new IInstallableUnit[objs.length];
		for (int i = 0; i < objs.length; i++) {
			ius[i] = ((InstalledIUElement) objs[i]).getIU();
		}
		return ius;
	}
}
