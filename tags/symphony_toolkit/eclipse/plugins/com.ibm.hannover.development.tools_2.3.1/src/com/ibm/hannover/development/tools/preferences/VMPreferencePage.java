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
	Button [] buttons = new Button[3];
	
	private static final String BUTTON_CONTENT_0= "Do not set runtime JRE.";
	private static final String BUTTON_CONTENT_1= "Set product JRE as its runtime JRE.";
	private static final String BUTTON_CONTENT_2= "Set Notes JRE as its runtime JRE.";
	public static final String PREFERENCE_KEY = "Symphony Runtime JRE Setting";
	
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		FillLayout layout = new FillLayout();
		layout.type = SWT.HORIZONTAL;
		composite.setLayout(layout);

		Group group1 = new Group(composite, SWT.NONE);
		group1.setText("Config Runtime JRE");
		group1.setLayout(new GridLayout(1, true));
		
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		buttons[0] = new Button(group1, SWT.RADIO);
		buttons[0].setLayoutData(gridData);
		buttons[1] = new Button(group1, SWT.RADIO);
		buttons[2] = new Button(group1, SWT.RADIO);
		
		buttons[0].setText(BUTTON_CONTENT_0);
		buttons[1].setText(BUTTON_CONTENT_1);
		buttons[2].setText(BUTTON_CONTENT_2);
	
		IPreferenceStore st =  Activator.getDefault().getPreferenceStore();
		int select = st.getInt(PREFERENCE_KEY);
		if (select == 0)
			select = 3;
		select --;
		if (buttons[select]!=null)
			buttons[select].setSelection(true);
		return composite;
	}

	public String getDescription() {
		return "The setting of JRE of auto generated launch configuration.\n\n";
	}
	
	protected void performDefaults() {
		if (buttons[2]!=null)
			buttons[2].setSelection(true);		
	}
	
	protected void performApply() {
		// get choice number
		int i;
		for (i=0; i<3; ++i)
			if (buttons[i].getSelection())
				break;
		if (i==3) i=2;
		// save it		
		IPreferenceStore st = Activator.getDefault().getPreferenceStore();
		st.setValue(PREFERENCE_KEY, i+1);
	}

	public boolean performOk() {
		performApply();
		return true;
	}
	
	public void init(IWorkbench workbench) {
		// do nothing 
	}

}
