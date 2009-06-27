package com.ibm.hannover.development.tools.configurations;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IConfigure {
	void configure(IProgressMonitor monitor) throws CoreException;

	String getName();
}
