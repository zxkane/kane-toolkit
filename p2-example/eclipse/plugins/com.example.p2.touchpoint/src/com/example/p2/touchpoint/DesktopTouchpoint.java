package com.example.p2.touchpoint;

import org.eclipse.equinox.internal.provisional.p2.core.Version;
import org.eclipse.equinox.internal.provisional.p2.engine.Touchpoint;
import org.eclipse.equinox.internal.provisional.p2.metadata.ITouchpointType;
import org.eclipse.equinox.internal.provisional.p2.metadata.MetadataFactory;

@SuppressWarnings("restriction")
public class DesktopTouchpoint extends Touchpoint {
	
	private static final String TOUCHPOINT_TYPE_NAME = "com.example.p2.touchpoint.desktop"; //$NON-NLS-1$
	public static final ITouchpointType TOUCHPOINT_TYPE = MetadataFactory.createTouchpointType(TOUCHPOINT_TYPE_NAME, Version.createOSGi(1, 0, 0));
	
}
