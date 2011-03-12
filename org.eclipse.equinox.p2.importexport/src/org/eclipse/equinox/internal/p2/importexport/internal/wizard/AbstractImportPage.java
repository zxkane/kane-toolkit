package org.eclipse.equinox.internal.p2.importexport.internal.wizard;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.importexport.internal.Messages;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.VersionRange;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.osgi.util.NLS;

public abstract class AbstractImportPage extends AbstractPage {

	IProfile profile = null;

	public AbstractImportPage(String pageName) {
		super(pageName);
		profile = getSelfProfile();
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
				if (profile != null) {
					IInstallableUnit iu = (IInstallableUnit) element;
					IQueryResult<IInstallableUnit> collector = profile.query(QueryUtil.createIUQuery(iu.getId(), new VersionRange(iu.getVersion(), true, null, false)), new NullProgressMonitor());
					if(!collector.isEmpty()) {
						return true;
					}
				}
				return false;
			}

			public boolean isChecked(Object element) {
				if (profile != null) {
					IInstallableUnit iu = (IInstallableUnit) element;
					IQueryResult<IInstallableUnit> collector = profile.query(QueryUtil.createIUQuery(iu.getId(), new VersionRange(iu.getVersion(), true, null, false)), new NullProgressMonitor());
					if(collector.isEmpty()) {
						return true;
					}
				}
				return false;
			}
		};
	}
}