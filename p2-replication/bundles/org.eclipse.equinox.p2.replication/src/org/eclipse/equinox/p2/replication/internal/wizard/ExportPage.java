package org.eclipse.equinox.p2.replication.internal.wizard;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.ui.IUPropertyUtils;
import org.eclipse.equinox.p2.replication.Constants;
import org.eclipse.equinox.p2.replication.P2Replicator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.osgi.util.tracker.ServiceTracker;
@SuppressWarnings("restriction")
public class ExportPage extends WizardPage {

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

	public ExportPage(String pageName) {
		super(pageName);
		setTitle(Message.EXPORTPage_TITLE);
		setDescription(Message.EXPORTPage_DESCRIPTION);
	}


	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		initializeReplicator();
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
				| GridData.HORIZONTAL_ALIGN_FILL));

		Label label = new Label(composite, SWT.NONE);
		if(replicator.getSelfProfile() == null){			
			label.setText(Message.ExportPage_ERROR_CONFIG);
		}else {
			label.setText(Message.EXPORTPage_LABEL);

			createInstallationTable(composite);
		}
		setControl(composite);

		giveFocusToDestination();
		Dialog.applyDialogFont(composite);
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
				| SWT.V_SCROLL | SWT.FULL_SELECTION);
		createColumns(viewer);
		viewer.setContentProvider(new InstallationContentProvider());
		viewer.setLabelProvider(new InstallationLabelProvider());
		viewer.setInput(sortByName(replicator.getRootIUs()));
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
		int[] bounds = { 400, 200, 400 };

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
}
