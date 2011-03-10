package org.eclipse.equinox.internal.p2.importexport;

import java.net.URI;
import java.util.List;

import org.eclipse.equinox.p2.metadata.IInstallableUnit;

public class FeatureDetail {

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
}
