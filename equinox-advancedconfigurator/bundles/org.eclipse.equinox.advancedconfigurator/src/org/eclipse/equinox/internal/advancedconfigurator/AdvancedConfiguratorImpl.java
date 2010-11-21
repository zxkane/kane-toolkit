package org.eclipse.equinox.internal.advancedconfigurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Properties;

import org.eclipse.equinox.internal.advancedconfigurator.utils.AdvanceConfiguratorConstants;
import org.eclipse.equinox.internal.advancedconfigurator.utils.EquinoxUtils;
import org.eclipse.equinox.internal.advancedconfigurator.utils.Utils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class AdvancedConfiguratorImpl {
	
	private static URL configurationURL = null;
	private static Object configurationLock = new Object();
	
	private final BundleContext context;
	private final Bundle bundle;
	private ConfigApplier configApplier;

	public AdvancedConfiguratorImpl(BundleContext context, Bundle bundle) {
		this.context = context;
		this.bundle = bundle;
	}

	public void applyConfiguration(URL url) throws IOException {
		synchronized (configurationLock) {
			if (Activator.DEBUG)
				System.out.println("applyConfiguration() URL=" + url);
			if (url == null)
				return;
			configurationURL = url;

			if (this.configApplier == null)
				configApplier = new ConfigApplier(context, bundle);
			configApplier.install(url, isExclusiveInstallation());
		}
	}

	public void applyConfiguration() throws IOException {
		synchronized (configurationLock) {
			configurationURL = getConfigurationURL();
			applyConfiguration(configurationURL);
		}
	}

	private boolean isExclusiveInstallation() {
		String value = context.getProperty(AdvanceConfiguratorConstants.PROP_KEY_EXCLUSIVE_INSTALLATION);
		if (value == null || value.trim().length() == 0)
			value = "true";
		return Boolean.valueOf(value).booleanValue();
	}

	/**
	 * find the url of bundle info, 
	 * 1. it's given by system property <code>AdvanceConfiguratorConstants.PROP_KEY_CONFIGURL</code> 
	 * 2. it's record in AdvanceConfiguratorConstants.CONFIGURATOR_FOLDER/AdvanceConfiguratorConstants.POLICY_LIST
	 * 3. use the given by system property "org.eclipse.equinox.simpleconfigurator.configUrl"
	 * 4. use the default one in configuration folder of "org.eclipse.equinox.simpleconfigurator"
	 * 
	 * @return
	 * @throws IOException
	 */
	public URL getConfigurationURL() throws IOException {
		String specifiedURL = context.getProperty(AdvanceConfiguratorConstants.PROP_KEY_CONFIGURL);
		if (specifiedURL == null) {
			URL[] configURL = EquinoxUtils.getConfigAreaURL(context);
			if (configURL != null) {
				String relative = AdvanceConfiguratorConstants.CONFIGURATOR_FOLDER + "/" + AdvanceConfiguratorConstants.POLICY_LIST; //$NON-NLS-1$
				File userConfig = new File(configURL[0].getFile(), relative);
				InputStream input = null;
				try {
					Properties prop = new Properties();
					input = new FileInputStream(userConfig);
					prop.load(input);
					String name = prop.getProperty(AdvanceConfiguratorConstants.POLICY_NAME);
					if (name != null)
						specifiedURL = "file:" + AdvanceConfiguratorConstants.CONFIGURATOR_FOLDER + "/" + URLDecoder.decode(name, "UTF-8") + "/" + AdvanceConfiguratorConstants.CONFIG_LIST; //$NON-NLS-1$
				} catch (IOException e) {
					//ignore
				} finally {
					if (input != null)
						input.close();
				}				
			}
			if (specifiedURL == null) {
				specifiedURL = context.getProperty(AdvanceConfiguratorConstants.PROP_SIMPLE_KEY_CONFIGURL);
				if (specifiedURL == null)
					specifiedURL = "file:" + AdvanceConfiguratorConstants.SIMPLE_CONFIGURATOR_FOLDER + "/" + AdvanceConfiguratorConstants.CONFIG_LIST;
			}
		}

		try {
			// if it is an absolute file URL, use it as is
			boolean done = false;
			URL url = null;
			String file = specifiedURL;
			while (!done) {
				// TODO what is this while loop for?  nested file:file:file: urls?
				try {
					url = Utils.buildURL(file);
					file = url.getFile();
				} catch (java.net.MalformedURLException e) {
					done = true;
				}
			}
			if (url != null && new File(url.getFile()).isAbsolute())
				return url;

			//if it is an relative file URL, then resolve it against the configuration area
			// TODO Support relative file URLs when not on Equinox
			URL[] configURL = EquinoxUtils.getConfigAreaURL(context);
			if (configURL != null) {
				File userConfig = new File(configURL[0].getFile(), url.getFile());
				return userConfig.toURI().toURL();
			}
		} catch (MalformedURLException e) {
			return null;
		}

		//Last resort
		try {
			return Utils.buildURL(specifiedURL);
		} catch (MalformedURLException e) {
			//Ignore
		}

		return null;
	}

	public URL getUrlInUse() {
		synchronized (configurationLock) {
			return configurationURL;
		}
	}

}
