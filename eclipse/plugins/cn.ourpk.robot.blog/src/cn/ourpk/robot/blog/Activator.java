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

	public Activator() {
		instance = this;
	}

	public void start(BundleContext context) throws Exception {
		this.context = context;
		robotRef = context.getServiceReference(RobotService.class.getName());
		
		RssDescriptor[] descs = RssFactory.getInstance().getRssItems();
		for (RssDescriptor rssDescriptor : descs) {
			RssFactory.getInstance().printNewsItem(rssDescriptor);
		}
//		RobotBlog blog = new RobotBlog();
//		System.out.println("Log in:");
//		blog.doLogin();
//		blog.post("博客转贴机器人", 
//				"为了提高本博客人气，决定开发一个博客自动转贴的机器人程序。\n此程序自动抓取各位同学其他博客上的新帖，然后转贴到本群体博客上。\n此程序使用Java开发，有兴趣的同学可以一同参与。\np.s:本文由此机器人发布。");
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
