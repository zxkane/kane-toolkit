package org.eclipse.equinox.advancedconfigurator.internal;

import org.eclipse.equinox.advancedconfigurator.manipulator.AdvancedManipulator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private static AdvancedManipulator manipulator;

	public static BundleContext getContext() {
		return context;
	}

	public Activator() {
		// TODO Auto-generated constructor stub
	}

	public void start(BundleContext context) throws Exception {
		Activator.context = context;
	}

	public void stop(BundleContext context) throws Exception {
		Activator.context = null;
	}

	public static AdvancedManipulator getAdvancedManipulator() {
		if (manipulator == null) {
			ServiceTracker tracker = new ServiceTracker(context, AdvancedManipulator.class.getName(), null);
			try {
				tracker.open();
				manipulator = (AdvancedManipulator) tracker.getService();
			} finally {
				tracker.close();
			}
		}
		return manipulator;
	}
}
