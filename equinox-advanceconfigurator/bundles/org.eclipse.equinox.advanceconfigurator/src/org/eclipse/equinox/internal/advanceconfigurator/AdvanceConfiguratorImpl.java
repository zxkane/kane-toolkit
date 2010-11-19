package org.eclipse.equinox.internal.advanceconfigurator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.eclipse.equinox.internal.advanceconfigurator.utils.AdvanceConfiguratorConstants;
import org.eclipse.equinox.internal.provisional.configurator.Configurator;
import org.eclipse.equinox.internal.simpleconfigurator.utils.EquinoxUtils;
import org.eclipse.equinox.internal.simpleconfigurator.utils.SimpleConfiguratorUtils;
import org.eclipse.equinox.internal.simpleconfigurator.utils.Utils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class AdvanceConfiguratorImpl implements Configurator {
	
	private static URL configurationURL = null;
	private static Object configurationLock = new Object();
	
	private final BundleContext context;
	private final Bundle bundle;

	public AdvanceConfiguratorImpl(BundleContext context, Bundle bundle) {
		this.context = context;
		this.bundle = bundle;
	}

	public void applyConfiguration(URL url) throws IOException {
		// TODO Auto-generated method stub

	}

	public void applyConfiguration() throws IOException {
		synchronized (configurationLock) {
			configurationURL = getConfigurationURL();
			applyConfiguration(configurationURL);
		}
	}
	
	public URL getConfigurationURL() throws IOException {
		String specifiedURL = context.getProperty(AdvanceConfiguratorConstants.PROP_KEY_CONFIGURL);
		if (specifiedURL == null)
			specifiedURL = "file:" + AdvanceConfiguratorConstants.CONFIGURATOR_FOLDER + "/" + AdvanceConfiguratorConstants.POLICY_LIST; //$NON-NLS-1$ //$NON-NLS-2$

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
				if (configURL.length == 1)
					return userConfig.exists() ? userConfig.toURL() : null;

				File sharedConfig = new File(configURL[1].getFile(), url.getFile());
				if (!userConfig.exists())
					return sharedConfig.exists() ? sharedConfig.toURL() : null;

				if (!sharedConfig.exists())
					return userConfig.toURL();

				URI base = EquinoxUtils.getInstallLocationURI(context);

				URL sharedConfigURL = sharedConfig.toURL();
				List sharedBundles = SimpleConfiguratorUtils.readConfiguration(sharedConfigURL, base);

				URL userConfigURL = userConfig.toURL();
				List userBundles = SimpleConfiguratorUtils.readConfiguration(userConfigURL, base);

				return (userBundles.containsAll(sharedBundles)) ? userConfigURL : sharedConfigURL;
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
