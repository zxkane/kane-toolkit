package org.eclipse.equinox.advancedconfigurator.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.equinox.advancedconfigurator.internal.wizard.AdvancedConfiguratorWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;

public class ConfiguratorWizardHanlder extends AbstractHandler {


	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Instantiates and initializes the wizard
		AdvancedConfiguratorWizard wizard = new AdvancedConfiguratorWizard();
		// Instantiates the wizard container with the wizard and opens it
		WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		dialog.create();
		dialog.open();
		return null;
	}
}
