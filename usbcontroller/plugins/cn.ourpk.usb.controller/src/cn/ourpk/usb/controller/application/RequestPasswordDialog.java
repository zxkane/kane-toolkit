package cn.ourpk.usb.controller.application;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;

public class RequestPasswordDialog extends InputDialog {

	public RequestPasswordDialog(Shell parentShell, String dialogTitle,
			String dialogMessage, String initialValue, IInputValidator validator) {
		super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
	}
	
	@Override
	protected int getInputTextStyle() {
		return SWT.PASSWORD;
	}
}
