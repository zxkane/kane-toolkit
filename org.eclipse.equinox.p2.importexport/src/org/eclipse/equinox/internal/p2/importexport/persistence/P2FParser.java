package org.eclipse.equinox.internal.p2.importexport.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.equinox.internal.p2.importexport.FeatureDetail;
import org.eclipse.equinox.internal.p2.importexport.internal.Message;
import org.eclipse.equinox.internal.p2.persistence.Messages;
import org.eclipse.equinox.internal.p2.persistence.XMLParser;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.MetadataFactory;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.metadata.VersionRange;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class P2FParser extends XMLParser implements P2FConstants {

	static final VersionRange XML_TOLERANCE = new VersionRange(CURRENT_VERSION, true, Version.createOSGi(2, 0, 0), false);
	private List<FeatureDetail> features;

	protected class RepositoryHandler extends AbstractHandler {

		private final String[] required = new String[] {LOCATION_ELEMENT};
		private final String[] optional = new String[] {};
		private URI referredRepo;
		private List<URI> uri;

		public RepositoryHandler(AbstractHandler parentHandler, Attributes attributes, List<URI> uri) {
			super(parentHandler, REPOSITORY_ELEMENT);
			String[] values = parseAttributes(attributes, required, optional);
			//skip entire subrepository if the location is missing
			if (values[0] == null)
				return;
			this.uri = uri;
			referredRepo = checkURI(REPOSITORY_ELEMENT, LOCATION_ELEMENT, values[0]);
		}

		@Override
		public void startElement(String name, Attributes attributes)
		throws SAXException {
			checkCancel();
		}

		@Override
		protected void finished() {
			if (referredRepo != null)
				uri.add(referredRepo);
		}
	}

	protected class RepositoriesHandler extends AbstractHandler {

		List<URI> uris;

		public RepositoriesHandler(AbstractHandler parentHandler, Attributes attributes) {
			super(parentHandler, REPOSITORIES_ELEMENT);
			String size = parseOptionalAttribute(attributes, COLLECTION_SIZE_ATTRIBUTE);
			uris = (size == null) ? new ArrayList<URI>() : new ArrayList<URI>(new Integer(size).intValue());
		}

		@Override
		public void startElement(String name, Attributes attributes)
		throws SAXException {
			if (name.equals(REPOSITORY_ELEMENT)) {
				new RepositoryHandler(this, attributes, uris);
			}
		}

		public List<URI> getRepositories() {
			return uris;
		}
	}

	protected class FeatureHandler extends AbstractHandler {
		private final String[] required = new String[] {ID_ATTRIBUTE, NAME_ATTRIBUTE, VERSION_ATTRIBUTE};
		private final String[] optional = new String[] {};

		IInstallableUnit iu = null;
		private RepositoriesHandler repositoriesHandler;
		private List<FeatureDetail> features;

		public FeatureHandler(AbstractHandler parentHandler, Attributes attributes, List<FeatureDetail> features) {
			super(parentHandler, FEATURE_ELEMENT);
			String[] values = parseAttributes(attributes, required, optional);
			//skip entire record if the id is missing
			if (values[0] == null)
				return;
			MetadataFactory.InstallableUnitDescription desc = new MetadataFactory.InstallableUnitDescription();
			desc.setId(values[0]);
			desc.setProperty(IInstallableUnit.PROP_NAME, values[1]);
			desc.setVersion(Version.create(values[2]));
			iu = MetadataFactory.createInstallableUnit(desc);
			this.features = features;
		}

		@Override
		public void startElement(String name, Attributes attributes) {
			if (name.equals(REPOSITORIES_ELEMENT)) {
				repositoriesHandler = new RepositoriesHandler(this, attributes);
			}
		}

		@Override
		protected void finished() {
			if (isValidXML()) {
				FeatureDetail feature = new FeatureDetail(iu, repositoriesHandler.getRepositories());
				features.add(feature);
			}
		}
	}

	protected class FeaturesHanlder extends AbstractHandler {

		private final List<FeatureDetail> features;

		public FeaturesHanlder(ContentHandler parentHandler, Attributes attributes) {
			super(parentHandler, FEATURES_ELEMENT);
			String size = parseOptionalAttribute(attributes, COLLECTION_SIZE_ATTRIBUTE);
			features = (size != null ? new ArrayList<FeatureDetail>(new Integer(size).intValue()) : new ArrayList<FeatureDetail>());
		}

		@Override
		public void startElement(String name, Attributes attributes) {
			if (name.equals(FEATURE_ELEMENT)) {
				new FeatureHandler(this, attributes, features);
			} else {
				invalidElement(name, attributes);
			}
		}

		public List<FeatureDetail> getFeatureDetails() {
			return features;
		}
	}

	private final class P2FDocHandler extends DocHandler {

		public P2FDocHandler(String rootName, RootHandler rootHandler) {
			super(rootName, rootHandler);
		}

		@Override
		public void processingInstruction(String target, String data) throws SAXException {
			Version repositoryVersion = extractPIVersion(target, data);
			if (!XML_TOLERANCE.isIncluded(repositoryVersion)) {
				throw new SAXException(NLS.bind(Messages.io_IncompatibleVersion, repositoryVersion, XML_TOLERANCE));
			}
		}
	}

	private final class P2FHandler extends RootHandler {
		private final String[] required = new String[] {VERSION_ATTRIBUTE};
		private final String[] optional = new String[] {};
		private String[] attrValues = new String[required.length + optional.length];

		private FeaturesHanlder featuresHanlder;


		@Override
		protected void handleRootAttributes(Attributes attributes) {
			attrValues = parseAttributes(attributes, required, optional);
			attrValues[0] = checkVersion(P2F_ELEMENT, VERSION_ATTRIBUTE, attrValues[0]).toString();
		}

		@Override
		public void startElement(String name, Attributes attributes)
		throws SAXException {
			if (FEATURES_ELEMENT.equals(name)) {
				if (featuresHanlder == null) {
					featuresHanlder = new FeaturesHanlder(this, attributes);
				} else {
					duplicateElement(this, name, attributes);
				}
			} else {
				invalidElement(name, attributes);
			}
		}

		@Override
		protected void finished() {
			if (isValidXML()) {
				features = featuresHanlder.getFeatureDetails();
			}
		}
	}

	public P2FParser(BundleContext context, String pluginId) {
		super(context, pluginId);
	}

	public void parse(File file) throws IOException {
		// don't overwrite if we already have a filename/location
		if (errorContext == null)
			setErrorContext(file.getAbsolutePath());
		parse(new FileInputStream(file));
	}

	public synchronized void parse(InputStream stream) throws IOException {
		this.status = null;
		try {
			// TODO: currently not caching the parser since we make no assumptions
			//		 or restrictions on concurrent parsing
			getParser();
			P2FHandler p2fHandler = new P2FHandler();
			xmlReader.setContentHandler(new P2FDocHandler(P2F_ELEMENT, p2fHandler));
			xmlReader.parse(new InputSource(stream));
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		} catch (ParserConfigurationException e) {
			throw new IOException(e.getMessage());
		} finally {
			stream.close();
		}
	}

	public List<FeatureDetail> getFeatures() {
		return features;
	}

	@Override
	protected Object getRootObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getErrorMessage() {
		return Message.io_parseError;
	}

}
