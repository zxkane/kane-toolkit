package org.eclipse.equinox.p2.replication.internal.wizard;

import org.eclipse.osgi.util.NLS;

public class Message extends NLS {
	public static String Column_Id;
	public static String Column_Name;
	public static String Column_Version;
	public static String EXPORTPage_TITLE;
	public static String EXPORTPage_DESCRIPTION;
	public static String EXPORTPage_LABEL;
	public static String ExportPage_ERROR_CONFIG;

	static {
		NLS.initializeMessages(
				"org.eclipse.equinox.p2.replication.internal.wizard.message", Message.class); //$NON-NLS-1$
	}
}
