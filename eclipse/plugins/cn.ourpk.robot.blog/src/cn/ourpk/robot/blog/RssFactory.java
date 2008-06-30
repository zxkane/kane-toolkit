package cn.ourpk.robot.blog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.rssowl.dao.NewsfeedFactoryException;
import net.sourceforge.rssowl.model.NewsItem;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.jdom.JDOMException;

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
	
	public void printNewsItem(RssDescriptor rss){
		Rssloader loader = new Rssloader(rss.getUrl());
		try {
			loader.load();
			NewsItem[] items = loader.getItems();
			for(int i = 0; i < items.length; i++){
				System.out.println(items[i].getTitle());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NewsfeedFactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
