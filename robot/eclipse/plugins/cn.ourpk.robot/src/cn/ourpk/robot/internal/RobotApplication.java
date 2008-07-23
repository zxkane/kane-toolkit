package cn.ourpk.robot.blog;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;


public class BlogApplication implements IApplication {
	
	public Object start(IApplicationContext context) throws Exception {
		while(!Activator.getInstance().isLoadFinished()){
			Thread.sleep(500);
		}
		return EXIT_OK;
	}

	public void stop() {
		// TODO Auto-generated method stub

	}

}
