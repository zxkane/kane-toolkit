package org.eclipse.equinox.internal.p2.importexport.persistence;

import org.eclipse.equinox.p2.metadata.Version;

public interface P2FConstants {

	public static final Version CURRENT_VERSION = Version.createOSGi(1, 0, 0);

	public static final String P2F_ELEMENT = "p2f"; //$NON-NLS-1$
	public static final String FEATURES_ELEMENT = "features"; //$NON-NLS-1$
	public static final String FEATURE_ELEMENT = "feature"; //$NON-NLS-1$

	public static final String REPOSITORIES_ELEMENT = "repositories"; //$NON-NLS-1$
	public static final String REPOSITORY_ELEMENT = "repository"; //$NON-NLS-1$
	public static final String P2FURI_ATTRIBUTE = "uri"; //$NON-NLS-1$

}