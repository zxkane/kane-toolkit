package com.ibm.hannover.development.tools.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.ibm.hannover.development.tools.preferences.messages"; //$NON-NLS-1$
	public static String VMPreferencePage_BUTTON0TEXT;
	public static String VMPreferencePage_BUTTON1TEXT;
	public static String VMPreferencePage_NAME;
	public static String VMPreferencePage_DESCRIPTION;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
