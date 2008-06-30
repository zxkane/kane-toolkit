package cn.ourpk.robot.blog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import net.sourceforge.rssowl.dao.NewsfeedFactoryException;
import net.sourceforge.rssowl.model.NewsItem;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.jdom.JDOMException;

public class RssLoadJob extends Job {

	public RssLoadJob(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		NewsItem[] items = getUnreadItem();
		RobotBlog blog = new RobotBlog();
		blog.doLogin();
		for(NewsItem item : items){
			StringBuffer sb = new StringBuffer();
			sb.append("<a href=\"" + item.getLink() + "\">原文链接</a>");
			sb.append("<br>");
			sb.append("原文作者：" + item.getAuthor());
			sb.append("<br><br>");
			sb.append(item.getDescription());
			blog.post(item.getTitle(), sb.toString(), item.getPubDateParsed());
		}
		return new Status(IStatus.OK, Activator.ID, "loading rss successfully.");
	}

	NewsItem[] getUnreadItem(){
		List<NewsItem> items = new ArrayList<NewsItem>();
		RssDescriptor[] descs = RssFactory.getInstance().getRssItems();
		for (RssDescriptor rssDescriptor : descs) {
			Rssloader loader = new Rssloader(rssDescriptor.getUrl());
			try {
				loader.load();
				NewsItem[] newsItems = loader.getItems();
				for(int i = 0; i < newsItems.length; i++){
					if(isUnread(newsItems[i]))
						items.add(newsItems[i]);
					else
						break;
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
		
		return items.toArray(new NewsItem[items.size()]);
	}
	
	private static final Date DCUT = new GregorianCalendar(2008, 5, 15).getTime();
	boolean isUnread(NewsItem item){
		return item.getPubDateParsed().after(DCUT);
	}
}
