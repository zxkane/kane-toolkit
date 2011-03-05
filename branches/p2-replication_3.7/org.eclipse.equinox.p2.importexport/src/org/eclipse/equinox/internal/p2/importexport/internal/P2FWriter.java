package org.eclipse.equinox.internal.p2.importexport.internal;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.equinox.internal.p2.metadata.repository.io.MetadataWriter;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
@SuppressWarnings("restriction")
public class P2FWriter extends MetadataWriter implements P2FConstants {

	public P2FWriter(OutputStream output, ProcessingInstruction[] piElements)
	throws UnsupportedEncodingException {
		super(output, piElements);
	}

	public void writeIU(IInstallableUnit unit) {
		writeInstallableUnit(unit);
	}
}
