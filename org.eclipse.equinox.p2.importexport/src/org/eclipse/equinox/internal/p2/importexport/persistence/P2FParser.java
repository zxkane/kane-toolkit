package org.eclipse.equinox.internal.p2.importexport.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.equinox.internal.p2.importexport.P2Replicator.InstallationConfiguration;
import org.eclipse.equinox.internal.p2.importexport.internal.Replicator.Configuriation;
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
	private InstallationConfiguration features;

	protected class RepositoryHandler extends AbstractHandler {

		private final String[] required = new String[] {LOCATION_ELEMENT};
		private final String[] optional = new String[] {};

		public RepositoryHandler(AbstractHandler parentHandler, Attributes attributes, Collection<String> uri) {
			super(parentHandler, REPOSITORY_ELEMENT);
			String[] values = parseAttributes(attributes, required, optional);
			//skip entire subrepository if the location is missing
			if (values[0] == null)
				return;
			uri.add(values[0]);
		}

		@Override
		public void startElement(String name, Attributes attributes)
		throws SAXException {
			checkCancel();
		}
	}

	protected class RepositoriesHandler extends AbstractHandler {

		Collection<String> uris;

		public RepositoriesHandler(AbstractHandler parentHandler, Attributes attributes) {
			super(parentHandler, REPOSITORIES_ELEMENT);
			String size = parseOptionalAttribute(attributes, COLLECTION_SIZE_ATTRIBUTE);
			uris = (size == null) ? new ArrayList<String>() : new ArrayList<String>(new Integer(size).intValue());
		}

		@Override
		public void startElement(String name, Attributes attributes)
		throws SAXException {
			if (name.equals(REPOSITORY_ELEMENT)) {
				new RepositoryHandler(this, attributes, uris);
			}
		}

		public String[] getRepositories() {
			return uris.toArray(new String[uris.size()]);
		}
	}

	protected class FeatureHandler extends AbstractHandler {
		private final String[] required = new String[] {ID_ATTRIBUTE, NAME_ATTRIBUTE, VERSION_ATTRIBUTE};
		private final String[] optional = new String[] {};

		IInstallableUnit iu = null;
		private RepositoriesHandler repositoriesHandler;

		public FeatureHandler(AbstractHandler parentHandler, Attributes attributes, Collection<IInstallableUnit> ius) {
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
			ius.add(iu);
		}

		@Override
		public void startElement(String name, Attributes attributes) {
			if (name.equals(REPOSITORIES_ELEMENT)) {
				repositoriesHandler = new RepositoriesHandler(this, attributes);
			}
		}
	}

	protected class FeaturesHanlder extends AbstractHandler {

		private FeatureHandler featureHandler = null;
		private final Collection<IInstallableUnit> ius;

		public FeaturesHanlder(ContentHandler parentHandler, Attributes attributes) {
			super(parentHandler, FEATURES_ELEMENT);
			String size = parseOptionalAttribute(attributes, COLLECTION_SIZE_ATTRIBUTE);
			ius = (size != null ? new ArrayList<IInstallableUnit>(new Integer(size).intValue()) : new ArrayList<IInstallableUnit>());
		}

		@Override
		public void startElement(String name, Attributes attributes) {
			if (name.equals(FEATURE_ELEMENT)) {
				featureHandler = new FeatureHandler(this, attributes, ius);
			} else {
				invalidElement(name, attributes);
			}
		}

		public IInstallableUnit[] getInstallableUnits() {
			return ius.toArray(new IInstallableUnit[ius.size()]);
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

		private InstallationConfiguration features = null;
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

		public InstallationConfiguration getFeatures() {
			return features;
		}

		/*
		 * If we parsed valid XML then fill in our installation configuration object with the parsed data.
		 */
		@Override
		protected void finished() {
			if (isValidXML()) {
				features = new Configuriation(featuresHanlder.getInstallableUnits(), 
						featuresHanlder.featureHandler.repositoriesHandler.getRepositories());
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
			if (isValidXML()) {
				features = p2fHandler.getFeatures();
			}
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		} catch (ParserConfigurationException e) {
			throw new IOException(e.getMessage());
		} finally {
			stream.close();
		}
	}

	public InstallationConfiguration getFeatures() {
		return features;
	}

	@Override
	protected Object getRootObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getErrorMessage() {
		return Messages.io_parseError;
	}

}
