package org.eclipse.equinox.internal.p2.importexport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;

public interface P2ImportExport {
	List<FeatureDetail> importP2F(InputStream input) throws IOException;
	IStatus exportP2F(OutputStream output, IInstallableUnit[] ius, IProgressMonitor monitor);
	IStatus exportP2F(OutputStream output, List<FeatureDetail> features, IProgressMonitor monitor);
}
