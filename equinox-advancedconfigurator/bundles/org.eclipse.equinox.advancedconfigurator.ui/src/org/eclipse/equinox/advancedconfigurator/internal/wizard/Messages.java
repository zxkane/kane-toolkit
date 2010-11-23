package org.eclipse.equinox.advancedconfigurator.internal.wizard;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.equinox.advancedconfigurator.internal.wizard.messages"; //$NON-NLS-1$
	public static String AdvancedConfiguratorWizard_WizardTitle;
	public static String ConfiguratorPage_ColumnID;
	public static String ConfiguratorPage_ColumnName;
	public static String ConfiguratorPage_ColumnVersion;
	public static String ConfiguratorPage_ConfiguratorDescription;
	public static String ConfiguratorPage_Description;
	public static String ConfiguratorPage_NoSelection;
	public static String ConfiguratorPage_P2SelfNotFound;
	public static String ConfiguratorPage_Title;
	public static String CreatePolicyPage_Description;
	public static String CreatePolicyPage_Title;
	public static String OverviewPage_Description;
	public static String OverviewPage_Title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
