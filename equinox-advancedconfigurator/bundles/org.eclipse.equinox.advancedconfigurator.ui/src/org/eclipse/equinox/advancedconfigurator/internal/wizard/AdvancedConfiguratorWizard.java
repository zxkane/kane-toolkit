package org.eclipse.equinox.advancedconfigurator.internal.wizard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.equinox.advancedconfigurator.Policy;
import org.eclipse.equinox.advancedconfigurator.Policy.Component;
import org.eclipse.equinox.advancedconfigurator.internal.Activator;
import org.eclipse.equinox.frameworkadmin.BundleInfo;
import org.eclipse.equinox.internal.p2.touchpoint.eclipse.Util;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IInstallableUnitFragment;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

@SuppressWarnings("restriction")
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
		Policy selectedPolicy = overviewPage.getSelectedPolicy();
		if (selectedPolicy == null) {
			String policyName = createPage.getPolicyName();
			boolean isDefault = createPage.isDefault();
			Component[] comps = getSelectedComponents();
			Activator.getAdvancedManipulator().addPolicy(policyName, isDefault, comps);
			if (isDefault)
				Display.getDefault().asyncExec(new Runnable() {

					public void run() {
						org.eclipse.equinox.advancedconfigurator.internal.Util.restartConfirm();
					}
				});
		} else {
			String policyName = createPage.getPolicyName();
			boolean isDefault = createPage.isDefault();
			Component[] comps = getSelectedComponents();
			Activator.getAdvancedManipulator().updatePolicy(selectedPolicy, policyName, isDefault, comps);
			if (selectedPolicy.isDefault() == false && isDefault == false)
				;
			else
				Display.getDefault().asyncExec(new Runnable() {

					public void run() {
						org.eclipse.equinox.advancedconfigurator.internal.Util.restartConfirm();
					}
				});
		}
		return true;
	}

	private Component[] getSelectedComponents() {
		IInstallableUnit[] ius = configuratePage.getSelectedComponents();
		Component[] comps = new Component[ius.length];
		for (int i = 0; i < ius.length; i++) {
			Component comp = new Component();
			comp.id = ius[i].getId();
			comp.version = ius[i].getVersion().toString();
			comp.bundles = getBundles(comp.id, comp.version);
			comps[i] = comp;
		}
		return comps;
	}

	private BundleInfo[] getBundles(String id, String version) {
		IProfile profile = configuratePage.getSelfProfile();
		IQueryResult<IInstallableUnit> root = profile.query(QueryUtil.createIUQuery(id, Version.create(version)), null);
		IQueryResult<IInstallableUnit> osgiBundles = profile.query(QueryUtil.createQuery(
				"select(iu | iu.providedCapabilities.exists(p | p.namespace == $0 && p.name == $1))", "org.eclipse.equinox.p2.eclipse.type", "bundle"), null);
		HashSet<IInstallableUnit> ius = new HashSet<IInstallableUnit>();
		for (IInstallableUnit iu : root.toSet()) {
			ius.add(iu);
			expandRequirement(ius, profile, iu);
		}
		ius.retainAll(osgiBundles.toSet());
		List<BundleInfo> bundles = new ArrayList<BundleInfo>(ius.size());
		for (IInstallableUnit iu : ius) {
			// By now we always have the manifest in the touchpoint data
			String manifest = Util.getManifest(iu.getTouchpointData());
			if (manifest == null) {
				System.out.println("IU " + iu.getId() + " doesn't have manifest.");
				continue;
			}
			BundleInfo bundleInfo = Util.createBundleInfo(null, manifest);
			bundles.add(bundleInfo);
		}
		return bundles.toArray(new BundleInfo[bundles.size()]);
	}

	private void expandRequirement(Set<IInstallableUnit> ius, IQueryable<IInstallableUnit> querable, IInstallableUnit iu) {
		if (iu instanceof IInstallableUnitFragment)
			return;
		for (IRequirement r : iu.getRequirements()) {
			for (IInstallableUnit req : querable.query(QueryUtil.createMatchQuery(r.getMatches()), null).toSet()) {
				if (ius.contains(req))
					continue;
				ius.add(req);
				expandRequirement(ius, querable, req);
			}
		}
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
