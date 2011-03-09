package org.eclipse.equinox.internal.p2.importexport.persistence;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import org.eclipse.core.runtime.URIUtil;
import org.eclipse.equinox.internal.p2.importexport.P2Replicator.InstallationConfiguration;
import org.eclipse.equinox.internal.p2.persistence.XMLWriter;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;

public class P2FWriter extends XMLWriter implements P2FConstants {

	public P2FWriter(OutputStream output, ProcessingInstruction[] piElements)
	throws UnsupportedEncodingException {
		super(output, piElements);
	}

	public void write(InstallationConfiguration features) {
		start(P2F_ELEMENT);
		attribute(VERSION_ATTRIBUTE, CURRENT_VERSION);
		writeFeatures(features);
		end(P2F_ELEMENT);
		flush();
	}

	public void writeFeatures(InstallationConfiguration features) {
		start(FEATURES_ELEMENT);
		attributeOptional(COLLECTION_SIZE_ATTRIBUTE, String.valueOf(features.getRootIUs().length));
		writeFeature(features);
		end(FEATURES_ELEMENT);
	}

	public void writeFeature(InstallationConfiguration feature) {
		for (IInstallableUnit unit : feature.getRootIUs()) {
			start(FEATURE_ELEMENT);
			attribute(ID_ATTRIBUTE, unit.getId());
			attribute(NAME_ATTRIBUTE, unit.getProperty(IInstallableUnit.PROP_NAME, Locale.getDefault().toString()));
			attribute(VERSION_ATTRIBUTE, unit.getVersion().toString());
			start(REPOSITORIES_ELEMENT);
			attribute(COLLECTION_SIZE_ATTRIBUTE, feature.getRepositories().length);
			for (String uri : feature.getRepositories()) {
				start(REPOSITORY_ELEMENT);
				String unencoded = uri;
				try {
					unencoded = URIUtil.toUnencodedString(new URI(uri));
				} catch (URISyntaxException e) {					
				}
				attribute(LOCATION_ELEMENT, unencoded);
				end(REPOSITORY_ELEMENT);
			}
			end(REPOSITORIES_ELEMENT);
			end(FEATURE_ELEMENT);
		}		
	}
}
