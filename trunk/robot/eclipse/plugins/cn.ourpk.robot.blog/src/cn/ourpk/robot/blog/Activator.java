package cn.ourpk.robot.blog;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import cn.ourpk.bbs.robot.service.RobotService;

public class Activator implements BundleActivator {

	public static final String ID = "cn.ourpk.robot.blog";
	
	private ServiceReference robotRef;
	private BundleContext context;
	private static Activator instance;
	private boolean loaded = false;
	
	public Activator() {
		instance = this;
	}

	public void start(BundleContext context) throws Exception {
		this.context = context;
		robotRef = context.getServiceReference(RobotService.class.getName());
	}
	
	public boolean isLoadFinished(){
		return loaded;
	}

	public void stop(BundleContext context) throws Exception {
		context.ungetService(robotRef);
		this.context = null;
		instance = null;
	}

	public static Activator getInstance(){
		return instance;
	}
	
	public RobotService getRobotService(){
		return (RobotService) context.getService(robotRef);
	}
}
