package cn.ourpk.robot.blog;

import org.eclipse.core.runtime.IConfigurationElement;

public class RssDescriptor {
	
	private static final String ATT_ID = "id";
	private static final String ATT_URL = "url";
	private static final String ATT_DESCRIPTION = "description";
	
	private String id, url, description;

	private Object configElement;
	
	public RssDescriptor(IConfigurationElement configElement) {
		this.configElement = configElement;
		this.id = configElement.getAttribute(ATT_ID);
		this.url = configElement.getAttribute(ATT_URL);
		this.description = configElement.getAttribute(ATT_DESCRIPTION);
	}
	
	public String getId() {
		return id;
	}

	public String getUrl() {
		return url;
	}

	public String getDescription() {
		return description;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(ATT_ID);
		sb.append(": ");
		sb.append(id);
		sb.append("\n");
		sb.append(ATT_URL);
		sb.append(": ");
		sb.append(url);
		sb.append("\n");
		sb.append(ATT_DESCRIPTION);
		sb.append(": ");
		sb.append(description);	
		return sb.toString();
	}
}
