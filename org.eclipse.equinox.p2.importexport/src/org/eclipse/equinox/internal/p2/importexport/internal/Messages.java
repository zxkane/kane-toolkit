package org.eclipse.equinox.internal.p2.importexport.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String Column_Id;
	public static String Column_Name;
	public static String Column_Version;
	public static String EXPORTPage_TITLE;
	public static String EXPORTPage_DESCRIPTION;
	public static String EXPORTPage_LABEL;
	public static String ExportPage_DEST_ERRORMESSAGE;
	public static String ExportPage_ERROR_CONFIG;
	public static String ExportPage_EXPORT_WARNING;
	public static String ExportPage_Fail;
	public static String ExportPage_FILEDIALOG_TITLE;
	public static String EXTENSION_ALL;
	public static String EXTENSION_ALL_NAME;
	public static String EXTENSION_P2F_NAME;
	public static String EXTENSION_P2F;
	public static String ExportPage_LABEL_EXPORTFILE;
	public static String ExportPage_SuccessWithProblems;
	public static String ImportPage_DESCRIPTION;
	public static String ImportPage_DEST_ERROR;
	public static String ImportPage_DESTINATION_LABEL;
	public static String ImportPage_FILEDIALOG_TITLE;
	public static String ImportPage_IMPORT_NOTIFICATION;
	public static String ImportPage_TITLE;
	public static String ImportWizard_WINDOWTITLE;
	public static String Page_BUTTON_BROWSER;
	public static String PAGE_NOINSTALLTION_ERROR;
	public static String ImportFromInstallationPage_DESTINATION_LABEL;
	public static String ImportFromInstallationPage_DIALOG_TITLE;
	public static String ImportFromInstallationPage_INVALID_DESTINATION;
	public static String ImportFromInstallationPage_DIALOG_DESCRIPTION;
	public static String ImportFromInstallationPage_SELECT_COMPONENT;

	public static String io_IncompatibleVersion;
	public static String io_parseError;
	public static String Replicator_ExportJobName;
	public static String Replicator_InstallFromLocal;
	public static String Replicator_NotFoundInRepository;
	public static String Replicator_SaveJobName;

	static {
		NLS.initializeMessages(
				"org.eclipse.equinox.internal.p2.importexport.internal.messages", Messages.class); //$NON-NLS-1$
	}
}
