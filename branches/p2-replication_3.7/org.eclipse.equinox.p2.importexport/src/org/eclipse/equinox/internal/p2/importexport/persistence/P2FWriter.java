package org.eclipse.equinox.internal.p2.importexport.persistence;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.URIUtil;
import org.eclipse.equinox.internal.p2.importexport.FeatureDetail;
import org.eclipse.equinox.internal.p2.persistence.XMLWriter;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;

public class P2FWriter extends XMLWriter implements P2FConstants {

	public P2FWriter(OutputStream output, ProcessingInstruction[] piElements)
	throws UnsupportedEncodingException {
		super(output, piElements);
	}

	public void write(List<FeatureDetail> features) {
		start(P2F_ELEMENT);
		attribute(VERSION_ATTRIBUTE, CURRENT_VERSION);
		writeFeatures(features);
		end(P2F_ELEMENT);
		flush();
	}

	private void writeFeatures(List<FeatureDetail> features) {
		start(FEATURES_ELEMENT);
		attributeOptional(COLLECTION_SIZE_ATTRIBUTE, String.valueOf(features.size()));
		for (FeatureDetail feature : features)
			writeFeature(feature);
		end(FEATURES_ELEMENT);
	}

	private void writeFeature(FeatureDetail feature) {
		IInstallableUnit unit = feature.getTopIU();
		start(FEATURE_ELEMENT);
		attribute(ID_ATTRIBUTE, unit.getId());
		attribute(NAME_ATTRIBUTE, unit.getProperty(IInstallableUnit.PROP_NAME, Locale.getDefault().toString()));
		attribute(VERSION_ATTRIBUTE, unit.getVersion().toString());
		start(REPOSITORIES_ELEMENT);
		attribute(COLLECTION_SIZE_ATTRIBUTE, feature.getReferencedRepositories().size());
		for (URI uri : feature.getReferencedRepositories()) {
			start(REPOSITORY_ELEMENT);
			String unencoded = URIUtil.toUnencodedString(uri);
			attribute(LOCATION_ELEMENT, unencoded);
			end(REPOSITORY_ELEMENT);
		}
		end(REPOSITORIES_ELEMENT);
		end(FEATURE_ELEMENT);
	}
}
