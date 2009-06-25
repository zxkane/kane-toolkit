package com.ibm.hannover.development.tools.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.Platform;

import com.ibm.hannover.development.tools.configurations.NotesPolicy;

public class HannoverHandler extends AbstractHandler implements IHandler {

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		String launcher = "config/notes/${os}/launcher.properties";
		launcher = launcher.replace("${os}", Platform.getOS());
		NotesPolicy policy = new NotesPolicy(launcher,
				"config/notes/variable.properties");
		policy.configure();
		return null;
	}

}
