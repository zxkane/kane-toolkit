package cn.ourpk.bbs.robot.core.internal;

import org.eclipse.core.runtime.IConfigurationElement;

public class SiteDescriptor {
	
	class PageDescriptor{
		IConfigurationElement configurationElement;
		private String url, id;
		private String action;
		
		public PageDescriptor(IConfigurationElement configurationElement) {
			this.configurationElement = configurationElement;
			id = configurationElement.getAttribute(ATT_ID);
			url = configurationElement.getAttribute(ATT_URL);
			action = configurationElement.getAttribute(ATT_ACTION);
		}
		
		public String getId() {
			return id;
		}

		public String getUrl() {
			return url;
		}

		public String getAction() {
			return action;
		}
		
		public String[] getProperties(){
			return SiteDescriptor.this.getProperties(configurationElement);
		}
	}
	
	class SessionDescriptor{
		IConfigurationElement configElement;
		private String url;
				
		public SessionDescriptor() {
			IConfigurationElement[] elements = SiteDescriptor.this.configElement.getChildren(ATT_SESSION);
			if(elements.length == 0 || elements.length > 1)
				throw new IllegalArgumentException("Session element doesn't exist or exist more than one session element.");
			configElement = elements[0];
			url = configElement.getAttribute(ATT_URL);
		}
		
		public String getUrl() {
			return url;
		}
		
		public String[] getProperties(){		
			return SiteDescriptor.this.getProperties(configElement);
		}
	}

	private static final String ATT_ID = "id";
	private static final String ATT_NAME = "name";
	private static final String ATT_DESCRIPTION = "description";
	private static final String ATT_SITE = "site";
	private static final String ATT_PORT = "port";
	
	private static final String ATT_SESSION = "session";
	private static final String ATT_URL = "url";
	
	private static final String ATT_PROPERTY = "property";
	
	private static final String ATT_PAGE = "page";
	private static final String ATT_ACTION = "action";
	
	private IConfigurationElement configElement;
	private String id, name, description, site;
	private int port;
	private SessionDescriptor session;
	private PageDescriptor[] pages;
	
	public SiteDescriptor(IConfigurationElement configElement) {
		this.configElement = configElement;
		this.id = configElement.getAttribute(ATT_ID);
		this.name = configElement.getAttribute(ATT_NAME);
		this.description = configElement.getAttribute(ATT_DESCRIPTION);
		this.site = configElement.getAttribute(ATT_SITE);
		try{
			this.port = Integer.valueOf(configElement.getAttribute(ATT_PORT)).intValue();
			if(this.port < 0)
				throw new IllegalArgumentException("Port attribute is not non-negative integer.");
		}catch(NumberFormatException e){
			throw new IllegalArgumentException("Port attribute is not non-negative integer.");
		}
	}
	
	public String getId(){
		return id;
	}
	
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getSite() {
		return site;
	}

	public int getPort() {
		return port;
	}
	
	public SessionDescriptor getSession(){
		if(session == null)
			session = new SessionDescriptor();
		return session;
	}
	
	public PageDescriptor[] getPages(){
		if(pages == null){
			IConfigurationElement[] elements = configElement.getChildren(ATT_PAGE);
			pages = new PageDescriptor[elements.length];
			for(int i = 0; i < elements.length; i++)
				pages[i] = new PageDescriptor(elements[i]);
		}
		return pages;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(ATT_ID).append(": ").append(id).append("\n");
		sb.append(ATT_NAME).append(": ").append(name).append("\n");
		sb.append(ATT_DESCRIPTION).append(": ").append(description).append("\n");
		sb.append(ATT_SITE).append(": ").append(site).append("\n");
		sb.append(ATT_PORT).append(": ").append(port).append("\n");
		return sb.toString();
	}
	
	String[] getProperties(IConfigurationElement configElement){			
		IConfigurationElement[] elements = configElement.getChildren(ATT_PROPERTY);
		String[] properties = new String[elements.length];
		for(int i = 0; i < properties.length; i++)
			properties[i] = elements[i].getAttribute(ATT_NAME);
		return properties;
	}
}
