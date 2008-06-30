package cn.ourpk.robot.blog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class RssFactory {
	private static final String EXTENSION_NAME = "rss";
	private static final String ATT_ITEM = "item";
	private static RssFactory factory = new RssFactory();
	private List<RssDescriptor> items = null;
	
	private RssFactory(){
	}
	
	public static RssFactory getInstance(){
		return factory;
	}
	
	public RssDescriptor[] getRssItems(){
		if(items == null){
			items = new ArrayList<RssDescriptor>();
			IConfigurationElement[] configurationElements = Platform.getExtensionRegistry().
			getExtensionPoint(Activator.ID, EXTENSION_NAME).getConfigurationElements();
			for(int i = 0; i < configurationElements.length; i++){
				if(ATT_ITEM.equals(configurationElements[i].getName()))
					items.add(new RssDescriptor(configurationElements[i]));
			}
		}
		
		return (RssDescriptor[]) items.toArray(new RssDescriptor[items.size()]);
	}
}
