package cn.ourpk.bbs.robot.service;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;

public interface Page {
	abstract String getId();
	abstract String getURL();
	abstract String getAction();
	abstract boolean execute(String ... arguments) throws HttpException, IOException;
}
