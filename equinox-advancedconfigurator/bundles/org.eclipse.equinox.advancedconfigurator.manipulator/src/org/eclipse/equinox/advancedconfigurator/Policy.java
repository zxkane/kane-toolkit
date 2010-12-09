package org.eclipse.equinox.advancedconfigurator;

import org.eclipse.equinox.frameworkadmin.BundleInfo;

public interface Policy {
	public class Component {
		public String id;
		public String version;

		public BundleInfo[] bundles = null;
	}

	public String getName();

	public Component[] getComponents();

	public boolean isDefault();
}
