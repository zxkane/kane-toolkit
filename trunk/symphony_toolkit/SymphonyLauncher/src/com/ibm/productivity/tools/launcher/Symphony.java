package com.ibm.productivity.tools.launcher;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Symphony {

	private static final String SEPARATOR = File.separator;
	private static final String RESOLVER = "com.ibm.productivity.tools.launcher.Resolver"; //$NON-NLS-1$
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String symphonyPath = null;
		if(args != null && args.length >= 1){
			symphonyPath = args[0];
		}else{
			symphonyPath = System.getProperty("symphony.home");
		}
		if(symphonyPath == null){
			System.out.println("Symphony launcher 1.0. Usage:");
			System.out.println("java -Xbootclasspath/a:\"<symphony_home>/framework/rcp/eclipse/plugins/com.ibm.rcp.base_<version>/rcpbootcp.jar\" -jar launcher.jar <symphony_home>");
			System.out.println("java -Xbootclasspath/a:\"<symphony_home>/framework/rcp/eclipse/plugins/com.ibm.rcp.base_<version>/rcpbootcp.jar\" -jar launcher.jar -Dsymphony.home=<symphony_home>");
			System.out.println();
			return;
		}else{
			File root = new File(symphonyPath);
			if(!root.exists() || !root.isDirectory()){
				System.out.print("Symphony home is invalid.");
				System.out.println();
				return;
			}
		}
		
		if(symphonyPath.endsWith(File.separator))
			symphonyPath += "framework";
		else
			symphonyPath += File.separator + "framework";
		try {
			Symphony launcher = new Symphony(symphonyPath);
			Object result = launcher.run();
			if (result instanceof Integer)
				System.exit(((Integer) result).intValue());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}		
	}


	private static final String BASE = "com.ibm.rcp.base";
	private static final String OSGI = "org.eclipse.osgi";
	private static final String[] LAUNCHERARUGMENTS = new String[]{"-homepage", "-personality", "com.ibm.productivity.tools.standalone.personality", 
		"-product", "com.ibm.productivity.tools.standalone.branding.productivitytools"};
	private File data;
	private String rcphome;
	private URL framework;
	private File basefolder;
	
	public Symphony(String rcpHome) throws Exception {	
		this.rcphome = rcpHome;
		File installArea = new File(rcpHome + SEPARATOR + "rcp" + SEPARATOR + "eclipse");
		File plugins = new File(installArea.getAbsoluteFile() + SEPARATOR + "plugins");
		System.setProperty("rcp.home", rcpHome);
		System.setProperty("osgi.install.area", installArea.getAbsolutePath());
		System.setProperty("eclipse.registry.nulltoken", "true");
		System.setProperty("java.protocol.handler.pkgs", "com.ibm.net.ssl.www.protocol");
		System.setProperty("java.util.logging.config.class", "com.ibm.rcp.core.internal.logger.boot.LoggerConfig");
		System.setProperty("osgi.hook.configurators.exclude", "org.eclipse.core.runtime.internal.adaptor.EclipseLogHook");
		System.setProperty("osgi.framework.extensions", "com.ibm.rcp.core.logger.frameworkhook");
		try {
			framework = search(plugins, OSGI ).toURL();
			if(framework.toString().endsWith(".jar"))
				System.setProperty("osgi.framework.shape", "jar");
			else
				System.setProperty("osgi.framework.shape", "folder");
			System.setProperty("osgi.framework", framework.toString());	
		} catch (Exception e) {
			throw new Exception("Failed to find osgi framework.");
		}
		if(System.getProperty("os.name").indexOf("Windows") > -1){
			data = new File(System.getProperty("user.home") + SEPARATOR + "IBM" + SEPARATOR + "Lotus" + SEPARATOR + "Symphony");
		}else
			data = new File(System.getProperty("user.home") + SEPARATOR + ".lotus" + SEPARATOR + "symphony");
		File config = new File(data.getAbsolutePath() + SEPARATOR + ".config" + SEPARATOR);
		System.setProperty("osgi.configuration.area", config.toURI().toURL().toString());
		System.setProperty("osgi.bundles", "org.eclipse.equinox.common@2:start, org.eclipse.core.jobs@4:start,org.eclipse.equinox.registry@4:start,org.eclipse.core.runtime.compatibility.registry,org.eclipse.equinox.preferences@4,org.eclipse.core.contenttype@4,org.eclipse.core.runtime@4:start,org.eclipse.equinox.app@4:start,org.eclipse.update.configurator@3:start, com.ibm.rcp.lifecycle.platform@5:start");
		System.setProperty("osgi.bundles.defaultStartLevel", "10");
		System.setProperty("osgi.checkConfiguration", "true");

		basefolder = search(plugins, BASE);
		if(basefolder != null && basefolder.isDirectory()){
			File security = new File(basefolder.getAbsoluteFile() + SEPARATOR + "rcp.security.properties");
			System.setProperty("java.security.properties", security.toURI().toURL().toString());
		}		
		System.setProperty("com.ibm.rcp.aaf.emc.SaveTopologyFile", "true");
		System.setProperty("com.ibm.rcp.aaf.showAllComponents", "true");
		System.setProperty("eclipse.commands", assembleArguments(LAUNCHERARUGMENTS));
	}
	
	private String assembleArguments(String[] arguments) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < arguments.length; i++){
			sb.append(arguments[i]);
			sb.append(" ");
		}
		return sb.toString();
	}

	private File search(File installArea, String bundleName){
		if(installArea.isDirectory()){
			File[] files = installArea.listFiles();
			for(int i = 0; i < files.length; i++){
				if(files[i].getName().indexOf(bundleName + "_") > -1)
					return files[i];
			}
		}
		return null;
	}
	
	public Object run() throws Exception{
		List arguments = new ArrayList(LAUNCHERARUGMENTS.length + 4);
		Collections.addAll(arguments, LAUNCHERARUGMENTS);
		Collections.addAll(arguments, new String[]{"-data", data.getAbsolutePath(), 
				"-plugincustomization", 
				new File(rcphome + SEPARATOR + "rcp" + SEPARATOR + "plugin_customization.ini").getAbsolutePath()});

		URL[] bootPath = new URL[]{Symphony.class.getProtectionDomain().getCodeSource().getLocation(),
				new File(basefolder.getAbsolutePath() + File.separator + "rcpbootcp.jar").toURI().toURL(),
				framework,
				new File(framework.toURI()).getParentFile().toURI().toURL()};
		URLClassLoader loader = new StartupClassLoader(bootPath, null);
		Class clazz = loader.loadClass(RESOLVER);
		try {
			Constructor constructor = clazz.getConstructor(new Class[]{String[].class});
			Object resolver = constructor.newInstance(new Object[]{new String[]{"com.ibm.productivity.tools.standalone", "com.ibm.productivity.tools.standalone.branding"}});
			Method method = clazz.getDeclaredMethod("run", new Class[] {String[].class}); //$NON-NLS-1$
			return method.invoke(resolver, new Object[]{arguments.toArray(new String[arguments.size()])});
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof Error)
				throw (Error) e.getTargetException();
			else if (e.getTargetException() instanceof Exception)
				throw (Exception) e.getTargetException();
			else
				//could be a subclass of Throwable!
				throw e;
		}
	}
	
	private class StartupClassLoader extends URLClassLoader {	
		public StartupClassLoader(URL[] urls) {
			super(urls);
		}

		public StartupClassLoader(URL[] urls, ClassLoader parent) {
			super(urls, parent);
		}

		public StartupClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
			super(urls, parent, factory);
		}
	}
}
