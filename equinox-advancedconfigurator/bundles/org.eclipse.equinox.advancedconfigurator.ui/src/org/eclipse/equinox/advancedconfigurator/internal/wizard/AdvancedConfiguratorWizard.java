package org.eclipse.equinox.advancedconfigurator.internal.wizard;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import org.eclipse.equinox.advancedconfigurator.internal.Activator;
import org.eclipse.equinox.internal.advancedconfigurator.utils.AdvancedConfiguratorConstants;
import org.eclipse.equinox.internal.advancedconfigurator.utils.EquinoxUtils;
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
		String policyName = createPage.getPolicyName();
		URL[] configs = EquinoxUtils.getConfigAreaURL(Activator.getContext());
		if (configs != null) {
			File policy = new File(configs[0].getFile(), AdvancedConfiguratorConstants.CONFIGURATOR_FOLDER + File.separator + policyName);
			policy.mkdirs();
			BufferedWriter writer = null;
			try {
				File policyInfo = new File(configs[0].getFile(), AdvancedConfiguratorConstants.CONFIGURATOR_FOLDER + File.separator
						+ AdvancedConfiguratorConstants.POLICY_LIST);
				if (!policyInfo.exists())
					policyInfo.createNewFile();
				writer = new BufferedWriter(new FileWriter(policyInfo));
				writer.write(policyName);
				writer.close();
				writer = null;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (writer != null)
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}

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
