package cn.ourpk.bbs.robot.core.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.httpclient.methods.PostMethod;

import cn.ourpk.bbs.robot.core.internal.SiteDescriptor.PageDescriptor;
import cn.ourpk.bbs.robot.service.Page;
import cn.ourpk.bbs.robot.service.Site;

public class SiteImpl implements Site {
	
	SiteDescriptor siteDesc;
	HttpClient client;
	boolean hasLogin = false;
	private List<Page> pages = null;
	
	public SiteImpl(SiteDescriptor siteDesc) {
		this.siteDesc = siteDesc;
		client = new HttpClient();
	}

	public boolean login(String ... arguments) throws HttpException, IOException {
		if(hasLogin == true)
			return hasLogin;
		String[] properties = siteDesc.getSession().getProperties();
		if(arguments.length != properties.length)
			throw new IllegalArgumentException("Need pass in proper arguments.");
		client.getHostConfiguration().setHost(siteDesc.getSite(), siteDesc.getPort());
		NameValuePair[] params = new NameValuePair[arguments.length];
		for(int i = 0; i < params.length; i++)
			params[i] = new NameValuePair(properties[i], arguments[i]);
		HttpMethod method = getPostMethod(siteDesc.getSession().getUrl(), params);
		executeMethod(method);
		
		CookieSpec cookiespec = CookiePolicy.getDefaultSpec();
        Cookie[] cookies = cookiespec.match(siteDesc.getSite(), siteDesc.getPort(), "/", 
        		false, client.getState().getCookies());
		return (hasLogin = cookies.length != 0);
	}
	
    HttpMethod getPostMethod(String page, NameValuePair[] params){
        PostMethod post = new PostMethod(page);
        post.setRequestBody(params);
        return post;
    }
    
    int executeMethod(HttpMethod method) throws HttpException, IOException{
		int status = client.executeMethod(method);
//		methond.getResponseBodyAsStream();
		System.out.println(method.getResponseBodyAsString());
		method.releaseConnection();
		return status;
    }

	public String getId() {
		return siteDesc.getId();
	}

	public Page[] getPages() {
		if(pages == null){
			pages = new ArrayList<Page>();
			PageDescriptor[] descs = siteDesc.getPages();
			for(int i = 0; i < descs.length; i++)
				pages.add(createPage(descs[i]));
		}
		return (Page[]) pages.toArray(new Page[pages.size()]);
	}
	
	private Page createPage(PageDescriptor desc){
		if("post".equals(desc.getAction()))
			return new PostPageImpl(this, desc);
		else
			return new GetPageImp(this, desc);
	}
}
