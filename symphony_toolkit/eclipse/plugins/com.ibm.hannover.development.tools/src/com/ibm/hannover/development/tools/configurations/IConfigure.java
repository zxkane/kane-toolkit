package com.ibm.hannover.development.tools.configurations;

import org.eclipse.core.runtime.CoreException;

public interface IConfigure {
	void configure() throws CoreException;

	String getName();
}
