package cn.ourpk.bbs.robot.core;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import cn.ourpk.bbs.robot.core.internal.RobotImpl;
import cn.ourpk.bbs.robot.service.RobotService;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "cn.ourpk.robot.core";

	// The shared instance
	private static Activator plugin;

	private BundleContext context;
	private ServiceReference robotService;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		plugin = this;
		this.context = context;
		ServiceRegistration sr = context.registerService(RobotService.class.getName(), new RobotImpl(), new Hashtable());
		robotService = sr.getReference();
		getRobotService().getSites();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		context.ungetService(robotService);
		plugin = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public RobotService getRobotService(){
		return (RobotService) context.getService(robotService);
	}
}
