package com.ibm.hannover.development.tools.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.ibm.hannover.development.tools.Activator;


public class VMPreferencePage extends PreferencePage implements
IWorkbenchPreferencePage {

	// need to save state
	Button [] buttons = new Button[2];
	
	private static final String BUTTON_CONTENT_0= Messages.VMPreferencePage_BUTTON0TEXT; 
	private static final String BUTTON_CONTENT_1= Messages.VMPreferencePage_BUTTON1TEXT;
	public static final String PREFERENCE_KEY = "VMSetting";  //$NON-NLS-1$
	
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		FillLayout layout = new FillLayout();
		layout.type = SWT.HORIZONTAL;
		composite.setLayout(layout);

		Group group1 = new Group(composite, SWT.NONE);
		group1.setText(Messages.VMPreferencePage_NAME); 
		group1.setLayout(new GridLayout(1, true));
		
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		buttons[0] = new Button(group1, SWT.RADIO);
		buttons[0].setLayoutData(gridData);
		buttons[1] = new Button(group1, SWT.RADIO);
		
		buttons[0].setText(BUTTON_CONTENT_0);
		buttons[1].setText(BUTTON_CONTENT_1);
	
		IPreferenceStore st =  Activator.getDefault().getPreferenceStore();
		int select = st.getInt(PREFERENCE_KEY);
		if (buttons[select] != null)
			buttons[select].setSelection(true);
		return composite;
	}

	public String getDescription() {
		return Messages.VMPreferencePage_DESCRIPTION; 
	}
	
	protected void performDefaults() {
		if(buttons[0].getSelection()){
			buttons[0].setSelection(false);
			buttons[1].setSelection(true);
		}
	}
	
	protected void performApply() {
		// get choice number
		int i;
		for (i=0; i<2; ++i)
			if (buttons[i].getSelection())
				break;
		// save it		
		IPreferenceStore st = Activator.getDefault().getPreferenceStore();
		st.setValue(PREFERENCE_KEY, i);
	}

	public boolean performOk() {
		performApply();
		return true;
	}

	public void init(IWorkbench workbench) {
	}
}
