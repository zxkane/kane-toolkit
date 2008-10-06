package cn.ourpk.bbs.robot.service;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;

public interface Site {

	abstract String getId();
	abstract boolean login(String ... arguments) throws HttpException, IOException;
	abstract Page[] getPages();
}
