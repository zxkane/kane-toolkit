package org.eclipse.equinox.advancedconfigurator;


public interface Policy {
	public class Component {
		public String id;
		public String version;
	}

	public String getName();

	public Component[] getComponents();
}
