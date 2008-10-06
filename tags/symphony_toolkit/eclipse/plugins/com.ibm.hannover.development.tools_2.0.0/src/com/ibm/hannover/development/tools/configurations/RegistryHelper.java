package com.ibm.hannover.development.tools.configurations;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.ibm.hannover.development.tools.Activator;

public class RegistryHelper {
	
	public static String CLAZZ = RegistryHelper.class.getName();
	private static Logger logger = Logger.getLogger(CLAZZ);
	public static native String queryValue(int hkey, String path, String keyname);
	
	public static final int HKEY_CLASSES_ROOT = 0x80000000;
	public static final int HKEY_CURRENT_USER = 0x80000001;
	public static final int HKEY_LOCAL_MACHINE = 0x80000002;	
	public static final int HKEY_USERS = 0x80000003;
	
	static{
		try{
//			InputStream in = null;
//			OutputStream out = null;
//			
//			File tempDll = File.createTempFile("registry", "");
//			try{
//				in = FileLocator.openStream(Activator.getDefault().getBundle(), new Path("registry.dll"), false);					
//				out = new FileOutputStream(tempDll);
//				byte[] buff = new byte[1024];
//				int read;
//				while((read = in.read(buff)) != -1){
//					out.write(buff, 0, read);
//				}
//				out.flush();				
//			}finally{
//				if(in != null)
//					in.close();
//				if(out != null)
//					out.close();				
//			}
//			System.load(tempDll.getCanonicalPath());
			IPath lib = new Path(FileLocator.toFileURL(Activator.getDefault().getBundle().getEntry("/")).getPath()).append("registry.dll");
			System.load(lib.toOSString());
//			Library.loadLibrary("registry");
		}catch(UnsatisfiedLinkError e){
			logger.logp(Level.SEVERE, CLAZZ, "Initialize class", 
					"Fail to load library.", e);
		}
		catch(Exception e){
			logger.logp(Level.SEVERE, CLAZZ, "Initialize class", 
					"Fail to initialize RegisterHelper.", e);
		}
	}
	
	public static void main(String[] args){
		System.out.println(RegistryHelper.queryValue(RegistryHelper.HKEY_LOCAL_MACHINE, 
				"SOFTWARE\\IBM\\Lotus\\Expeditor\\{D8641E4B-77AF-4EAC-9137-8D4DCB1478E2}", 
		"xpdInstallLocation"));
	}
}
