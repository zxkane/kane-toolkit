package cn.ourpk.robot;

import java.util.Date;

public interface PostProvider {
	void doLogin();
	void post(String title, String content, Date pubDate);
}
