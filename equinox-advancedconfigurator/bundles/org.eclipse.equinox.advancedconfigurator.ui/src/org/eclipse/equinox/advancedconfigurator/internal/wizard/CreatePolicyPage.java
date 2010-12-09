package org.eclipse.equinox.advancedconfigurator.internal.wizard;

import org.eclipse.equinox.advancedconfigurator.Policy;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class CreatePolicyPage extends WizardPage {

	private Text name;
	private Button makeDefault;

	protected CreatePolicyPage(String pageName) {
		super(pageName);
		setTitle(Messages.CreatePolicyPage_Title);
		setDescription(Messages.CreatePolicyPage_Description);
	}

	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 30;
		gridLayout.marginWidth = 20;
		content.setLayout(gridLayout);
		Label text = new Label(content, SWT.NONE);
		text.setText(Messages.CreatePolicyPage_Label_InputName);
		text.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		name = new Text(content, SWT.BORDER);
		name.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (name.getText().trim().length() > 0)
					CreatePolicyPage.this.setPageComplete(true);
				else
					CreatePolicyPage.this.setPageComplete(false);
			}
		});
		name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		makeDefault = new Button(content, SWT.CHECK);
		makeDefault.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		makeDefault.setText(Messages.CreatePolicyPage_Button_MakeDefault);
		Composite comp2 = new Composite(content, SWT.NONE);
		comp2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		setControl(content);
	}

	public String getPolicyName() {
		return name.getText().trim();
	}

	public boolean isDefault() {
		return makeDefault.getSelection();
	}

	@Override
	public IWizardPage getNextPage() {
		return ((AdvancedConfiguratorWizard) getWizard()).configuratePage;
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			Policy policy = ((AdvancedConfiguratorWizard) getWizard()).overviewPage.getSelectedPolicy();
			if (policy != null) {
				name.setText(policy.getName());
				makeDefault.setSelection(policy.isDefault());
			}
			name.setFocus();
		}
		super.setVisible(visible);
	}
}
