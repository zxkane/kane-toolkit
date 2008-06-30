package cn.ourpk.bbs.robot.core.internal;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;

import cn.ourpk.bbs.robot.core.internal.SiteDescriptor.PageDescriptor;
import cn.ourpk.bbs.robot.service.Page;

public abstract class PageImpl implements Page {
	
	protected SiteImpl site; 
	protected PageDescriptor pageDesc;
	
	public PageImpl(SiteImpl site, PageDescriptor desc) {
		this.site = site;
		this.pageDesc = desc;
	}

	public boolean execute(String... arguments) throws HttpException, IOException {
		if(!site.hasLogin)
			throw new IllegalStateException("Login site firstly.");
		if(arguments.length != pageDesc.getProperties().length)
			throw new IllegalArgumentException("Need pass in proper arguments.");
		return true;
	}

	public String getAction() {
		return pageDesc.getAction();
	}

	public String getId() {
		return pageDesc.getId();
	}

	public String getURL() {
		return pageDesc.getUrl();
	}

}
