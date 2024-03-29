package org.eclipse.equinox.internal.advancedconfigurator.utils;

public class AdvancedConfiguratorConstants {

	public static final String PROP_KEY_EXCLUSIVE_INSTALLATION = "org.eclipse.equinox.simpleconfigurator.exclusiveInstallation"; //$NON-NLS-1$
	/**
	 * If BundleContext#getProperty(PROP_KEY_USE_REFERENCE) does not equal "false" ignoring case, 
	 * when a SimpleConfigurator installs a bundle, "reference:" is added to its bundle location in order to avoid
	 * caching its bundle jar.  Otherwise, it will add nothing to any bundle location.
	 * 	 
	 * Default: true
	 */
	public static final String PROP_KEY_USE_REFERENCE = "org.eclipse.equinox.advancedconfigurator.useReference"; //$NON-NLS-1$

	/**
	 * BundleContext#getProperty(PROP_KEY_CONFIGURL) is used for SimpleConfigurator to do life cycle control of bundles.
	 * The file specified by the returned url is read by AdvancedConfigurator and do life cycle control according to it.
	 * If improper value or null is returned, AdvancedConfigurator doesn't do it.
	 * 
	 * Default: null
	 */
	public static final String PROP_KEY_CONFIGURL = "org.eclipse.equinox.advancedconfigurator.configUrl"; //$NON-NLS-1$

	public static final String POLICY_LIST = "policy.info"; //$NON-NLS-1$
	
	public static final String DEFAULT_POLICY_KEY = "defaultPolicy"; //$NON-NLS-1$

	public static final String CONFIG_LIST = "bundles.info"; //$NON-NLS-1$
	
	public static final String CONFIGURATOR_FOLDER = "org.eclipse.equinox.advancedconfigurator"; //$NON-NLS-1$

	public static final String SIMPLE_CONFIGURATOR_FOLDER = "org.eclipse.equinox.simpleconfigurator"; //$NON-NLS-1$

	public static final String PROP_SIMPLE_KEY_CONFIGURL = "org.eclipse.equinox.simpleconfigurator.configUrl"; //$NON-NLS-1$

	public static final String TARGET_CONFIGURATOR_NAME = "org.eclipse.equinox.advancedconfigurator"; //$NON-NLS-1$

}
