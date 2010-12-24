package org.eclipse.equinox.internal.advancedconfigurator;

import java.util.Hashtable;

import org.eclipse.equinox.advancedconfigurator.AdvancedConfigurator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.startlevel.StartLevel;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {
	public final static boolean DEBUG = false;
	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	private ServiceRegistration confReg;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		ServiceTracker tracker = new ServiceTracker(bundleContext, StartLevel.class.getName(), null);
		tracker.open();
		StartLevel startLevel = (StartLevel) tracker.getService();
		if (startLevel.getBundleStartLevel(bundleContext.getBundle()) == 1) {
			AdvancedConfiguratorImpl conf = new AdvancedConfiguratorImpl(bundleContext, bundleContext.getBundle());
			confReg = bundleContext.registerService(AdvancedConfigurator.class.getName(), conf, new Hashtable<String, String>());
			conf.applyConfiguration();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		if (confReg != null)
			confReg.unregister();
	}

}
