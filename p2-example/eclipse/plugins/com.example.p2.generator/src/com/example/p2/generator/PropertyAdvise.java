package com.example.p2.generator;

import java.util.Properties;

import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactDescriptor;
import org.eclipse.equinox.internal.provisional.p2.core.Version;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.MetadataFactory.InstallableUnitDescription;
import org.eclipse.equinox.p2.publisher.AbstractAdvice;
import org.eclipse.equinox.p2.publisher.actions.IPropertyAdvice;

import com.example.p2.touchpoint.DesktopTouchpoint;

@SuppressWarnings("restriction")
public class PropertyAdvise extends AbstractAdvice implements IPropertyAdvice {
	
	@Override
	public boolean isApplicable(String configSpec, boolean includeDefault,
			String id, Version version) {
		return true;
	}

	@Override
	public Properties getArtifactProperties(IInstallableUnit iu,
			IArtifactDescriptor descriptor) {
		return null;
	}

	@Override
	public Properties getInstallableUnitProperties(InstallableUnitDescription iu) {
		/**
		 * so trick
		 */
		if("com.example.mail.desktop".equals(iu.getId()))
			iu.setTouchpointType(DesktopTouchpoint.TOUCHPOINT_TYPE);
		return null;
	}

}
