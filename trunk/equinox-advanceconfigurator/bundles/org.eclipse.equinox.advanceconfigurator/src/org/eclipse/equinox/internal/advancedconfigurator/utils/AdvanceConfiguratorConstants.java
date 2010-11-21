package org.eclipse.equinox.internal.advanceconfigurator.utils;

public class AdvanceConfiguratorConstants {

	/**
	 * If BundleContext#getProperty(PROP_KEY_USE_REFERENCE) does not equal "false" ignoring case, 
	 * when a SimpleConfigurator installs a bundle, "reference:" is added to its bundle location in order to avoid
	 * caching its bundle jar.  Otherwise, it will add nothing to any bundle location.
	 * 	 
	 * Default: true
	 */
	public static final String PROP_KEY_USE_REFERENCE = "org.eclipse.equinox.advanceconfigurator.useReference"; //$NON-NLS-1$

	/**
	 * BundleContext#getProperty(PROP_KEY_CONFIGURL) is used for SimpleConfigurator to do life cycle control of bundles.
	 * The file specified by the returned url is read by AdvanceConfigurator and do life cycle control according to it.
	 * If improper value or null is returned, AdvanceConfigurator doesn't do it.
	 * 
	 * Default: null
	 */
	public static final String PROP_KEY_CONFIGURL = "org.eclipse.equinox.advanceconfigurator.configUrl"; //$NON-NLS-1$

	public static final String POLICY_LIST = "policy.info"; //$NON-NLS-1$
	
	public static final String CONFIG_LIST = "bundles.info"; //$NON-NLS-1$
	
	public static final String CONFIGURATOR_FOLDER = "org.eclipse.equinox.advanceconfigurator"; //$NON-NLS-1$

	public static final String TARGET_CONFIGURATOR_NAME = "org.eclipse.equinox.advanceconfigurator"; //$NON-NLS-1$

}
