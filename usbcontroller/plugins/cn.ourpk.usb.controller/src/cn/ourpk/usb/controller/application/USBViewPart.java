package cn.ourpk.usb.controller.application;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

import cn.ourpk.usb.controller.internal.Des;

import com.ice.jni.registry.NoSuchKeyException;
import com.ice.jni.registry.NoSuchValueException;
import com.ice.jni.registry.RegDWordValue;
import com.ice.jni.registry.RegStringValue;
import com.ice.jni.registry.Registry;
import com.ice.jni.registry.RegistryException;
import com.ice.jni.registry.RegistryKey;
import com.ice.jni.registry.RegistryValue;

public class USBViewPart extends org.eclipse.ui.part.ViewPart {
	
	private static final RegistryKey GLOBAL = Registry.HKEY_CURRENT_USER;
	private static final String KEY_NAME = "Password"; //$NON-NLS-1$
	private static final String SUB_KEY = "SOFTWARE\\USBController"; //$NON-NLS-1$
	private static String USB_KEY_PATH = "SYSTEM\\CurrentControlSet\\Services\\UsbStor"; //$NON-NLS-1$
	private static final String USB_KEY = "Start"; //$NON-NLS-1$
	public static final String ID = "cn.ourpk.usb.controller.usbview"; //$NON-NLS-1$
	private Text password;
	private Des des;
	
	public USBViewPart() {
		try {
			des = new Des("USB Controller"); //$NON-NLS-1$
		} catch (Exception e) {
		}
		USB_KEY_PATH = SUB_KEY;
	}
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		RegistryKey key;
		try {
			key = GLOBAL.openSubKey(SUB_KEY);
			key.getValue(KEY_NAME);
			return;
		} catch (NoSuchKeyException e) {
		} catch (NoSuchValueException e) {
		} 
		catch (RegistryException e) {
		}
		InputDialog dialog = new InputDialog(site.getShell(), 
				Messages.PRODUCT_NAME, Messages.DIALOG_INPUT_MESSAGE, null,
				new IInputValidator(){
					public String isValid(String newText) {
						if(newText.trim().length() < 6 || !newText.matches("\\w{6,}?")) //$NON-NLS-1$
							return Messages.DIALOG_ERROR_MESSAGE;
						return null;							
					}
				}){ 
			@Override
			protected int getInputTextStyle() {
				return SWT.PASSWORD;
			}
			@Override
			protected Button getCancelButton() {
				return null;
			}
			
		};
		if(dialog.open() == InputDialog.OK){
			try {
				key = GLOBAL.createSubKey(SUB_KEY, ""); //$NON-NLS-1$
//				key = GLOBAL.openSubKey(SUB_KEY, RegistryKey.ACCESS_ALL);
				key.setValue(new RegStringValue(key, KEY_NAME, des.encrypt(dialog.getValue().trim())));
			} catch (NoSuchKeyException e) {
			} catch (NoSuchValueException e) {
			} catch (RegistryException e) {
			} catch (UnsupportedEncodingException e) {
			} catch (NoSuchAlgorithmException e) {
			} catch (Exception e) {
			} 
		}else
			site.getPage().hideView(this);
	}

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new FillLayout());
		final ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		scrolledComposite.setLayout(new FillLayout());
		final Composite composite = new Composite(scrolledComposite, SWT.NONE);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setContent(composite);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.makeColumnsEqualWidth = true;
		gridLayout.horizontalSpacing = 10;
		composite.setLayout(gridLayout);
		GridData gridData = new GridData();		
		gridData.horizontalAlignment = GridData.END;
		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.LABEL_PASSWORD);
		label.setLayoutData(gridData);
		final Composite right = new Composite(composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		right.setLayoutData(gridData);
		right.setLayout(new FillLayout());
		password = new Text(right, SWT.BORDER | SWT.PASSWORD);
		password.setSize(150, 20);
		final Button buttonPassword = new Button(right, SWT.BORDER);
		buttonPassword.setText(Messages.BUTTON_PASSWORD_TEXT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.CENTER;
		final Button enable = new Button(composite, SWT.BORDER);
		enable.setText(Messages.BUTTON_ENABLEUSB);
		enable.setLayoutData(gridData);
		enable.setEnabled(false);
		enable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setUSBKeyValue(parent, 0x04);
			}
		});
		final Button disable = new Button(composite, SWT.BORDER);
		disable.setText(Messages.BUTTON_DISABLEUSB);
		disable.setLayoutData(gridData);
		disable.setEnabled(false);
		disable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setUSBKeyValue(parent, 0x03);
			}
		});
		buttonPassword.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(verifyPassword(password.getText().trim())){
					enable.setEnabled(true);
					disable.setEnabled(true);
					password.setEnabled(false);
					buttonPassword.setEnabled(false);
				}else{
					MessageBox error = new MessageBox(parent.getShell(), SWT.ICON_ERROR);
					error.setText(Messages.DIALOG_TEXT);
					error.setMessage(Messages.DIALOG_MESSAGE);
					error.open();
				}
			}
		});
		scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	private boolean verifyPassword(final String password){
		if("".equals(password)) //$NON-NLS-1$
			return false;
		RegistryKey key;
		try {
			key = GLOBAL.openSubKey(SUB_KEY);
			RegistryValue value = key.getValue(KEY_NAME);
			return des.encrypt(password).equals(new String(value.getByteData()));
		} catch (NoSuchKeyException e) {
		} catch (RegistryException e) {
		} catch (UnsupportedEncodingException e) {
		} catch (NoSuchAlgorithmException e) {
		} catch (Exception e) {
		}

		return false;
	}

	@Override
	public void setFocus() {
		password.setFocus();
	}

	private void setUSBKeyValue(final Composite parent, int value) {
		boolean failed = false;
		try {
			RegistryKey key = GLOBAL.openSubKey(USB_KEY_PATH, RegistryKey.ACCESS_ALL);
			key.setValue(new RegDWordValue(key, USB_KEY, RegistryValue.REG_DWORD, value));
			GLOBAL.flushKey();
		} catch (NoSuchKeyException e1) {
			failed = true;
		} catch (RegistryException e1) {
			failed = true;
		}
		MessageBox message = null;
		if(failed == true){
			message = new MessageBox(parent.getShell(), SWT.ICON_ERROR);
			message.setMessage(Messages.MESSAGE_FAILED);
		}else{
			message = new MessageBox(parent.getShell(), SWT.ICON_INFORMATION);
			message.setMessage(Messages.MESSAGE_SUCCESSED);
		}
		message.open();
	}

}
