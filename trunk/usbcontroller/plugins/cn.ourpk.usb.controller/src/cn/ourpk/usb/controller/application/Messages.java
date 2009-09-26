package cn.ourpk.usb.controller.application;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "cn.ourpk.usb.controller.application.messages"; //$NON-NLS-1$
	public static String BUTTON_DISABLEUSB;
	public static String BUTTON_ENABLEUSB;
	public static String BUTTON_PASSWORD_TEXT;
	public static String DIALOG_ERROR_MESSAGE;
	public static String DIALOG_INPUT_MESSAGE;
	public static String DIALOG_MESSAGE;
	public static String DIALOG_TEXT;
	public static String LABEL_PASSWORD;
	public static String MESSAGE_FAILED;
	public static String MESSAGE_SUCCESSED;
	public static String PRODUCT_NAME;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
