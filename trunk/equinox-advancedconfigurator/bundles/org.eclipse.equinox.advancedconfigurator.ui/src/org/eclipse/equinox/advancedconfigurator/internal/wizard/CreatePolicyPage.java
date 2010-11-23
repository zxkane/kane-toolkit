package org.eclipse.equinox.advancedconfigurator.internal.wizard;

import org.eclipse.equinox.advancedconfigurator.Policy;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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
		name.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (name.getText().trim().length() > 0)
					CreatePolicyPage.this.setPageComplete(true);
				else
					CreatePolicyPage.this.setPageComplete(false);
			}
		});
		Composite comp2 = new Composite(content, SWT.NONE);
		comp2.setLayoutData(new GridData(2, 1, true, true));
		setControl(content);
	}

	public String getPolicyName() {
		return name.getText().trim();
	}

	@Override
	public IWizardPage getNextPage() {
		return ((AdvancedConfiguratorWizard) getWizard()).configuratePage;
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			Policy policy = ((AdvancedConfiguratorWizard) getWizard()).overviewPage.getSelectedPolicy();
			if (policy != null)
				name.setText(policy.getName());
			name.setFocus();
		}
		super.setVisible(visible);
	}
}
