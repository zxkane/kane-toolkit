package org.eclipse.equinox.advancedconfigurator.internal.wizard;

import org.eclipse.equinox.advancedconfigurator.Policy;
import org.eclipse.equinox.advancedconfigurator.internal.Activator;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class OverviewPage extends WizardPage {

	class PolicyLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			Policy f = (Policy) element;
			return f.getName();
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
		content.setLayout(new FillLayout(SWT.HORIZONTAL | SWT.VERTICAL));
		viewer = CheckboxTableViewer.newCheckList(content, SWT.BORDER | SWT.SINGLE);
		viewer.setContentProvider(new PolicyContentProvider());
		viewer.setLabelProvider(new PolicyLabelProvider());
		viewer.setInput(getInput());
		setControl(content);
	}

	private Object getInput() {
		return Activator.getAdvancedManipulator().getPolicies();
	}

	@Override
	public IWizardPage getNextPage() {
		if (viewer.getCheckedElements().length == 0)
			return ((AdvancedConfiguratorWizard) getWizard()).createPage;
		return super.getNextPage();
	}

	public Policy getSelectedPolicy() {
		Object[] checked = viewer.getCheckedElements();
		if (checked.length > 0)
			return (Policy) viewer.getCheckedElements()[0];
		else
			return null;
	}
}
