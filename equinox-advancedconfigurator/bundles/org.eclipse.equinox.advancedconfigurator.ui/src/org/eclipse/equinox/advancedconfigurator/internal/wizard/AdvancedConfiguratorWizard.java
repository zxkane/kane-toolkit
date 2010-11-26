package org.eclipse.equinox.advancedconfigurator.internal.wizard;

import org.eclipse.equinox.advancedconfigurator.Policy.Component;
import org.eclipse.equinox.advancedconfigurator.internal.Activator;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
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
			IInstallableUnit[] ius = configuratePage.getSelectedComponents();
			Component[] comps = new Component[ius.length];
			for (int i = 0; i < ius.length; i++) {
				Component comp = new Component();
				comp.id = ius[i].getId();
				comp.version = ius[i].getVersion().toString();
				comps[i] = comp;
			}
			Activator.getAdvancedManipulator().addPolicy(policyName, comps);
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
