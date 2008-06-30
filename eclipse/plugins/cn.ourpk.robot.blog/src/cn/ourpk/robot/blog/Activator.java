package cn.ourpk.robot.blog;

import java.util.Date;

import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import cn.ourpk.bbs.robot.service.RobotService;

public class Activator implements BundleActivator {

	public static final String ID = "cn.ourpk.robot.blog";
	
	private ServiceReference robotRef;
	private BundleContext context;
	private static Activator instance;

	public Activator() {
		instance = this;
	}

	public void start(BundleContext context) throws Exception {
		this.context = context;
		robotRef = context.getServiceReference(RobotService.class.getName());
//		Job job = new RssLoadJob("load rss job");
//		job.schedule();
		RobotBlog blog = new RobotBlog();
		blog.doLogin();
		blog.post("test", "test publish date", new Date());
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
