package com.ibm.hannover.development.tools.configurations;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;

import com.ibm.hannover.development.tools.Activator;

public class PropertiesUtils {
	
	private static final String CLAZZ = Properties.class.getName();
	private static Logger logger = Logger.getLogger(CLAZZ);
	private Map<String, String> properties = new Hashtable<String, String>();
	
	public PropertiesUtils(String configFile) throws IOException{
		this(FileLocator.openStream(Activator.getDefault().getBundle(), new Path(configFile),false));
	}
	
	public PropertiesUtils(InputStream inStream) throws IOException{
		initialize(inStream);
	}
	
	private void initialize(InputStream inStream) throws IOException{
		String method = "initialize";
		Properties prop = new Properties();
		try {
			prop.load(inStream);
			Iterator<Object> iter = prop.keySet().iterator();
			while(iter.hasNext()){
				Object obj = iter.next();
				properties.put((String)obj, (String)prop.get(obj));
			}
		} catch (IOException e) {
			logger.logp(Level.SEVERE, CLAZZ, method, "Fail to load config.properties file.", e);
		}finally{
			inStream.close();
		}
	}
	
	Map<String, String> getProperties(){
		return properties;
	}
	
	public String getProperty(String key){
		return (String)properties.get(key);
	}
}
