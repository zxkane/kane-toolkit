package org.eclipse.equinox.internal.p2.importexport;

import java.net.URI;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;

public class FeatureDetail implements IAdaptable{

	private final IInstallableUnit iu;
	private final List<URI> referredRepo;

	public FeatureDetail(IInstallableUnit iu, List<URI> uris) {
		this.iu = iu;
		referredRepo = uris;
	}

	public IInstallableUnit getTopIU() {
		return iu;
	}

	public List<URI> getReferencedRepositories() {
		return referredRepo;
	}

	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (IInstallableUnit.class.equals(adapter))
			return iu;
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof FeatureDetail) {
			if (iu.equals(((FeatureDetail)obj).getTopIU()))
				return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return iu.hashCode();
	}
}
