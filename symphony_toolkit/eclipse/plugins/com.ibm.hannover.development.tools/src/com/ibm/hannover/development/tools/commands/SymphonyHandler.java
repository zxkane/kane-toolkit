package com.ibm.hannover.development.tools.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.Platform;

import com.ibm.hannover.development.tools.configurations.SymphonyPolicy;

public class SymphonyHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		String launcher = "config/symphony/${os}/launcher.properties";
		launcher = launcher.replace("${os}", Platform.getOS());
		SymphonyPolicy policy = new SymphonyPolicy(launcher,
			"config/symphony/variable.properties");
		policy.configure();
		return null;
	}

}
