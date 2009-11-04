package com.example.p2.generator;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.equinox.internal.provisional.p2.metadata.ITouchpointData;
import org.eclipse.equinox.internal.provisional.p2.metadata.MetadataFactory;
import org.eclipse.equinox.internal.provisional.p2.metadata.MetadataFactory.InstallableUnitDescription;
import org.eclipse.equinox.p2.publisher.IPublisherInfo;
import org.eclipse.equinox.p2.publisher.actions.ITouchpointAdvice;
import org.eclipse.equinox.p2.publisher.eclipse.BundlesAction;
import org.eclipse.osgi.service.resolver.BundleDescription;

import com.example.p2.touchpoint.DesktopTouchpoint;

@SuppressWarnings("restriction")
public class CustomBundlesAction extends BundlesAction {

	public CustomBundlesAction(BundleDescription[] bundles) {
		super(bundles);
	}

	protected static void processTouchpointAdvice(InstallableUnitDescription iu, Map currentInstructions, IPublisherInfo info) {
		Collection advice = info.getAdvice(null, false, iu.getId(), iu.getVersion(), ITouchpointAdvice.class);
		if (currentInstructions == null) {
			if (advice.isEmpty())
				return;

			currentInstructions = Collections.EMPTY_MAP;
		}

		ITouchpointData result = MetadataFactory.createTouchpointData(currentInstructions);
		for (Iterator i = advice.iterator(); i.hasNext();) {
			ITouchpointAdvice entry = (ITouchpointAdvice) i.next();
			result = entry.getTouchpointData(result);
		}
		iu.addTouchpointData(result);
		if("com.example.mail.desktop".equals(iu.getId()))
			iu.setTouchpointType(DesktopTouchpoint.TOUCHPOINT_TYPE);
	}
}
