package org.eclipse.equinox.p2.replication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;

@SuppressWarnings("restriction")
public interface P2Replicator {

	public interface InstallationConfiguration{
		public String[] getRepositories();
		public IInstallableUnit[] getRootIUs();
	};

	IProfile getSelfProfile();
	IInstallableUnit[] getRootIUs();

	/**
	 * 
	 * @param output
	 * @param ius
	 * @param monitor
	 * @return returns the IInstallableUnits that don't query their repository
	 * @throws ProvisionException
	 */
	IInstallableUnit[] save(OutputStream output, IInstallableUnit[] ius, IProgressMonitor monitor) throws ProvisionException;

	InstallationConfiguration load(InputStream input) throws IOException;

	void replicate(String[] repositories, IInstallableUnit[] rootIUs, IProgressMonitor monitor) throws ProvisionException;
}
