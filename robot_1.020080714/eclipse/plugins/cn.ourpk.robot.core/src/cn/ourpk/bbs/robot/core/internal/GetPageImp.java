package cn.ourpk.bbs.robot.core.internal;

import cn.ourpk.bbs.robot.core.internal.SiteDescriptor.PageDescriptor;

public class GetPageImp extends PageImpl {

	public GetPageImp(SiteImpl site, PageDescriptor desc) {
		super(site, desc);
	}

	@Override
	public boolean execute(String... arguments) {
		throw new UnsupportedOperationException("No implementation.");
	}
}
