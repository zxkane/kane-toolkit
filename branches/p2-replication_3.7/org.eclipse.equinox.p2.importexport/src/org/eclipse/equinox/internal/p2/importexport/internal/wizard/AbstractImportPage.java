package org.eclipse.equinox.internal.p2.importexport.internal.wizard;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.importexport.internal.Messages;
import org.eclipse.equinox.internal.p2.ui.ProvUI;
import org.eclipse.equinox.internal.p2.ui.dialogs.ProvisioningOperationWizard;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.VersionRange;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

public abstract class AbstractImportPage extends AbstractPage {

	IProfile profile = null;
	private final ProvisioningOperationWizard wizard;
	private final ProvisioningUI ui;

	public AbstractImportPage(String pageName, ProvisioningUI ui, ProvisioningOperationWizard wizard) {
		super(pageName);
		this.wizard = wizard;
		this.ui = ui;
		profile = getSelfProfile();
	}

	protected ProvisioningOperationWizard getProvisioningWizard() {
		return wizard;
	}

	protected ProvisioningUI getProvisioningUI() {
		return ui;
	}

	@Override
	protected void createInstallationTable(Composite parent) {
		super.createInstallationTable(parent);
		viewer.getTable().addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event event) {
				if( event.detail == SWT.CHECK ) {
					if (hasInstalled(ProvUI.getAdapter(event.item.getData(), IInstallableUnit.class))) {
						viewer.getTable().setRedraw(false);
						((TableItem)event.item).setChecked(false);
						viewer.getTable().setRedraw(true);
					}
				}				
			}
		});
	}

	public boolean hasInstalled(IInstallableUnit iu) {
		IQueryResult<IInstallableUnit> results = profile.query(QueryUtil.createIUQuery(iu.getId(), 
				new VersionRange(iu.getVersion(), true, null, false)), null);
		return !results.isEmpty();
	}

	public String getIUNameWithDetail(IInstallableUnit iu) {
		IQueryResult<IInstallableUnit> results = profile.query(QueryUtil.createIUQuery(iu.getId(), 
				new VersionRange(iu.getVersion(), true, null, false)), null);
		final String text = iu.getProperty(IProfile.PROP_NAME, null);
		if (!results.isEmpty()) {
			boolean hasHigherVersion = false;
			boolean hasEqualVersion = false;
			for (IInstallableUnit installedIU : results.toSet()) {
				int compareValue = installedIU.getVersion().compareTo(iu.getVersion()); 
				if (compareValue > 0) {
					hasHigherVersion = true;
					break;
				} else if (compareValue == 0)
					hasEqualVersion = true;
			}
			if (hasHigherVersion)
				return NLS.bind(Messages.AbstractImportPage_HigherVersionInstalled, text);
			else if (hasEqualVersion)
				return NLS.bind(Messages.AbstractImportPage_SameVersionInstalled, text);
		}
		return text;
	}

	@Override
	protected ICheckStateProvider getViewerDefaultState() {
		return new ICheckStateProvider() {

			public boolean isGrayed(Object element) {
				return false;
			}

			public boolean isChecked(Object element) {
				if (profile != null) {
					IInstallableUnit iu = ProvUI.getAdapter(element, IInstallableUnit.class);
					IQueryResult<IInstallableUnit> collector = profile.query(QueryUtil.createIUQuery(iu.getId(), new VersionRange(iu.getVersion(), true, null, false)), new NullProgressMonitor());
					if(collector.isEmpty()) {
						return true;
					}
				}
				return false;
			}
		};
	}

	@Override
	protected void doFinish() throws Exception {
		// do nothing
	}


	@Override
	public boolean canFlipToNextPage() {
		return isPageComplete();
	}
}