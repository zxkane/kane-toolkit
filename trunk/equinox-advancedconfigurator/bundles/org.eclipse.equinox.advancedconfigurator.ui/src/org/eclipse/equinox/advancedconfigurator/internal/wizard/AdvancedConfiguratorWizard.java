package org.eclipse.equinox.advancedconfigurator.internal.wizard;

import org.eclipse.equinox.advancedconfigurator.internal.Activator;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;

public class AdvancedConfiguratorWizard extends Wizard {

	CreatePolicyPage createPage = null;
	ConfiguratorPage configuratePage = null;
	OverviewPage overviewPage = null;

	@Override
	public String getWindowTitle() {
		return Messages.AdvancedConfiguratorWizard_WizardTitle;
	}

	@Override
	public boolean performFinish() {
		if (overviewPage.getSelectedPolicy() == null) {
			String policyName = createPage.getPolicyName();
			Activator.getAdvancedManipulator().addPolicy(policyName, configuratePage.getSelectedComponents());
		}
		return true;
	}

	@Override
	public void addPages() {
		super.addPages();
		overviewPage = new OverviewPage("overview");
		addPage(overviewPage);
		createPage = new CreatePolicyPage("createPolicy");
		addPage(createPage);
		configuratePage = new ConfiguratorPage("configurator");
		addPage(configuratePage);
	}

	@Override
	public void createPageControls(Composite pageContainer) {
		if (getContainer().getCurrentPage() != null)
			getContainer().getCurrentPage().createControl(pageContainer);
	}

	@Override
	public boolean canFinish() {
		return getContainer().getCurrentPage() == configuratePage && configuratePage.isPageComplete();
	}
}
