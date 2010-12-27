package org.eclipse.equinox.advancedconfigurator.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.equinox.advancedconfigurator.internal.messages"; //$NON-NLS-1$
	public static String Util_RestartDialogMessage;
	public static String Util_RestartDialogTitle;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
