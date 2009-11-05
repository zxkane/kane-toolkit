package com.example.p2.touchpoint;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.equinox.internal.provisional.p2.core.Version;
import org.eclipse.equinox.internal.provisional.p2.engine.Touchpoint;
import org.eclipse.equinox.internal.provisional.p2.metadata.ITouchpointType;
import org.eclipse.equinox.internal.provisional.p2.metadata.MetadataFactory;

import com.example.p2.touchpoint.actions.CreateDesktopAction;
import com.example.p2.touchpoint.actions.DeleteDesktopAction;

@SuppressWarnings("restriction")
public class DesktopTouchpoint extends Touchpoint {
	
	private static final String TOUCHPOINT_TYPE_NAME = "com.example.p2.touchpoint.desktop"; //$NON-NLS-1$
	public static final ITouchpointType TOUCHPOINT_TYPE = MetadataFactory.createTouchpointType(TOUCHPOINT_TYPE_NAME, Version.createOSGi(1, 0, 0));
	private List<String> actions = null;
	
	public DesktopTouchpoint() {
		actions = new ArrayList<String>();
		actions.add(CreateDesktopAction.NAME);
		actions.add(DeleteDesktopAction.NAME);
	}
	
	@Override
	public String qualifyAction(String actionId) {
		if(actions.contains(actionId))
			return "com.example.p2.touchpoint." + actionId;
		return "org.eclipse.equinox.p2.touchpoint.eclipse." + actionId;
	}
}
