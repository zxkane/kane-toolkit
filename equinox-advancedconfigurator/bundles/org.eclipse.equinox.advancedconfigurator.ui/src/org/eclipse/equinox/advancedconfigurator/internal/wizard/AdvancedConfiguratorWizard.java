package org.eclipse.equinox.advancedconfigurator.internal.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;

public class AdvancedConfiguratorWizard extends Wizard {

	CreatePolicyPage createPage = null;

	@Override
	public String getWindowTitle() {
		return Messages.AdvancedConfiguratorWizard_WizardTitle;
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(new OverviewPage("overview"));
		createPage = new CreatePolicyPage("createPolicy");
		addPage(createPage);
	}

	@Override
	public void createPageControls(Composite pageContainer) {
		if (getContainer().getCurrentPage() != null)
			getContainer().getCurrentPage().createControl(pageContainer);
	}

	@Override
	public boolean canFinish() {
		return false;
	}
}
