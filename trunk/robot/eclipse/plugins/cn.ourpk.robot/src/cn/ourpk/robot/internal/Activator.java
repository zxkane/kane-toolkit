package cn.ourpk.robot.internal;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	public static final String ID = "cn.ourpk.robot";
	
	private static Activator instance;
	private boolean loaded = false;
	
	public Activator() {
		instance = this;
	}

	public void start(BundleContext context) throws Exception {
		Job job = new RssLoadJob("load rss job");
		job.addJobChangeListener(new JobChangeAdapter(){
			@Override
			public void done(IJobChangeEvent event) {
				super.done(event);
				loaded = true;
				System.out.println("Robot program is terminating.");
			}
		});
		job.schedule();
	}
	
	public boolean isLoadFinished(){
		return loaded;
	}

	public void stop(BundleContext context) throws Exception {
		instance = null;
	}

	public static Activator getInstance(){
		return instance;
	}
}
