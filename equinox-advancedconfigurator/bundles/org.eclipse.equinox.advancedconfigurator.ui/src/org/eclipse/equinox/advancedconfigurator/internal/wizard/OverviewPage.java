package org.eclipse.equinox.advancedconfigurator.internal.wizard;

import org.eclipse.equinox.advancedconfigurator.Policy;
import org.eclipse.equinox.advancedconfigurator.internal.Activator;
import org.eclipse.equinox.advancedconfigurator.manipulator.ManipulatorEvent;
import org.eclipse.equinox.advancedconfigurator.manipulator.ManipulatorListener;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class OverviewPage extends WizardPage implements ManipulatorListener {

	class PolicyLabelProvider implements ITableLabelProvider {

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {

		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			Policy f = (Policy) element;
			switch (columnIndex) {
			case 0:
				return f.getName();
			case 1:
				return f.isDefault() ? Messages.OverviewPage_IsDefaultValue_Default : Messages.OverviewPage_IsDefaultValue_NonDefault;
			default:
				break;
			}
			return null;
		}
	}

	class PolicyContentProvider implements IStructuredContentProvider {

		public void dispose() {
			// TODO Auto-generated method stub

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub

		}

		public Object[] getElements(Object inputElement) {
			return (Policy[]) inputElement;
		}

	}

	private CheckboxTableViewer viewer;

	protected OverviewPage(String pageName) {
		super(pageName);
		setTitle(Messages.OverviewPage_Title);
		setDescription(Messages.OverviewPage_Description);
	}

	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, true);
		gridLayout.marginWidth = 0;
		content.setLayout(gridLayout);
		Composite list = new Composite(content, SWT.NONE);
		list.setLayout(new FillLayout(SWT.HORIZONTAL | SWT.VERTICAL));
		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer = CheckboxTableViewer.newCheckList(list, SWT.BORDER | SWT.SINGLE);
		viewer.setContentProvider(new PolicyContentProvider());
		viewer.setLabelProvider(new PolicyLabelProvider());
		viewer.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getChecked()) {
					Object[] objs = ((CheckboxTableViewer) event.getCheckable()).getCheckedElements();
					for (Object o : objs) {
						if (o != event.getElement())
							event.getCheckable().setChecked(o, false);
					}
				}
			}
		});
		Table policyTable = viewer.getTable();
		TableColumn policyCol = new TableColumn(policyTable, SWT.CENTER);
		policyCol.setText(Messages.OverviewPage_Column_Policy);
		TableColumn defaultCol = new TableColumn(policyTable, SWT.CENTER);
		defaultCol.setText(Messages.OverviewPage_Column_IsDefault);
		for (int i = 0, n = policyTable.getColumnCount(); i < n; i++) {
			policyTable.getColumn(i).pack();
		}
		policyTable.setHeaderVisible(true);
		policyTable.setLinesVisible(true);
		viewer.setInput(getInput());
		Composite compButtons = new Composite(content, SWT.NONE);
		compButtons.setLayoutData(new GridData(SWT.FILL, SWT.END, true, false));
		compButtons.setLayout(new FillLayout(SWT.HORIZONTAL));
		Group group = new Group(compButtons, SWT.BORDER);
		group.setLayout(new GridLayout(3, true));
		Button revertToSimple = new Button(group, SWT.PUSH);
		revertToSimple.setText(Messages.OverviewPage_ButtonRevert_Label);
		revertToSimple.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		revertToSimple.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Activator.getAdvancedManipulator().setDefault(null);
			}
		});
		Button makeDefaults = new Button(group, SWT.PUSH);
		makeDefaults.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		makeDefaults.setText(Messages.OverviewPage_ButtonDefault_Label);
		makeDefaults.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object[] elements = viewer.getCheckedElements();
				if (elements.length > 0)
					Activator.getAdvancedManipulator().setDefault((Policy) elements[0]);
			}
		});
		Button delete = new Button(group, SWT.PUSH);
		delete.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		delete.setText(Messages.OverviewPage_ButtonDelete_Label);
		delete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object[] elements = viewer.getCheckedElements();
				if (elements.length > 0)
					Activator.getAdvancedManipulator().removePolicy((Policy) elements[0]);
			}
		});
		setControl(content);
		Activator.getAdvancedManipulator().addManipulatorListener(this);
	}

	@Override
	public void dispose() {
		super.dispose();
		Activator.getAdvancedManipulator().removeManipulatorListener(this);
	}

	private Object getInput() {
		return Activator.getAdvancedManipulator().getPolicies();
	}

	@Override
	public IWizardPage getNextPage() {
		return ((AdvancedConfiguratorWizard) getWizard()).createPage;
	}

	public Policy getSelectedPolicy() {
		Object[] checked = viewer.getCheckedElements();
		if (checked.length > 0)
			return (Policy) viewer.getCheckedElements()[0];
		else
			return null;
	}

	public void notify(ManipulatorEvent e) {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				if (!viewer.getTable().isDisposed()) {
					viewer.setInput(getInput());
				}
			}
		});
	}
}
