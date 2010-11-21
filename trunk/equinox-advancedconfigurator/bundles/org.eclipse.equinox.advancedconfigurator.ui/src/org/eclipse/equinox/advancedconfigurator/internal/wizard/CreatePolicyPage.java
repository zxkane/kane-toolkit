package org.eclipse.equinox.advancedconfigurator.internal.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class CreatePolicyPage extends WizardPage {

	private Text name;

	protected CreatePolicyPage(String pageName) {
		super(pageName);
		setTitle(Messages.CreatePolicyPage_Title);
		setDescription(Messages.CreatePolicyPage_Description);
	}

	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(new GridLayout(2, false));
		Composite comp1 = new Composite(content, SWT.NONE);
		comp1.setLayoutData(new GridData(2, 1, true, false));
		comp1.setLayout(new FillLayout(SWT.HORIZONTAL));
		Label text = new Label(comp1, SWT.NONE);
		text.setText("Input the name of policy:   ");
		name = new Text(comp1, SWT.BORDER);
		Composite comp2 = new Composite(content, SWT.NONE);
		comp2.setLayoutData(new GridData(2, 1, true, true));
		setControl(content);
	}

}
