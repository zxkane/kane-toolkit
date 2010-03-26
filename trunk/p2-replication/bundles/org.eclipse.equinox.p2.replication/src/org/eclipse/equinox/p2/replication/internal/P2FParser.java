package org.eclipse.equinox.p2.replication.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.equinox.internal.p2.metadata.repository.io.MetadataParser;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.osgi.framework.BundleContext;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
@SuppressWarnings("restriction")
public class P2FParser extends MetadataParser implements P2FConstants{

	private String[] repositories;
	private IInstallableUnit[] ius;

	public P2FParser(BundleContext context, String bundleId) {
		super(context, bundleId);
	}

	public synchronized void parse(InputStream stream) throws IOException {
		this.status = null;
		try {
			getParser();
			P2FHandler p2fHandler = new P2FHandler();
			xmlReader.setContentHandler(new P2FDocHandler(P2F_ELEMENT, p2fHandler));
			xmlReader.parse(new InputSource(stream));
			repositories = p2fHandler.getRepositories();
			ius = p2fHandler.getInstallableUnits();
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		} catch (ParserConfigurationException e) {
			throw new IOException(e.getMessage());
		} finally {
			stream.close();
		}
	}

	public String[] getRepositories() {
		return repositories;
	}

	public IInstallableUnit[] getIUs() {
		return ius;
	}

	@Override
	protected String getErrorMessage() {
		return "Error parsing the eclipse installation cofiguration file."; //$NON-NLS-1$
	}

	@Override
	protected Object getRootObject() {
		return this;
	}

	private final class P2FDocHandler extends DocHandler {

		public P2FDocHandler(String rootName, RootHandler rootHandler) {
			super(rootName, rootHandler);
		}

		@Override
		public void processingInstruction(String target, String data) throws SAXException {
			// do nothing
		}
	}

	private class P2FHandler extends RootHandler {

		private InstallableUnitsHandler unitsHandler;
		private RepositoriesHandler repositoriesHandler;

		public P2FHandler() {
			// default
		}

		@Override
		protected void handleRootAttributes(Attributes attributes) {
			// default
		}

		@Override
		public void startElement(String name, Attributes attributes) {
			if (REPOSITORIES_ELEMENT.equals(name)) {
				if (repositoriesHandler == null) {
					repositoriesHandler = new RepositoriesHandler(this, attributes);
				} else {
					duplicateElement(this, name, attributes);
				}
			} else if (INSTALLABLE_UNITS_ELEMENT.equals(name)) {
				if (unitsHandler == null) {
					unitsHandler = new InstallableUnitsHandler(this, attributes);
				} else {
					duplicateElement(this, name, attributes);
				}
			} else {
				invalidElement(name, attributes);
			}
		}

		public IInstallableUnit[] getInstallableUnits() {
			if (unitsHandler == null)
				return null;
			return unitsHandler.getUnits();
		}

		public String[] getRepositories() {
			if (repositoriesHandler == null)
				return null;
			return repositoriesHandler.getRepositories();
		}
	}

	private class RepositoriesHandler extends AbstractHandler {

		private final List<String> urls;

		public RepositoriesHandler(AbstractHandler p2fHandler, Attributes attributes) {
			super(p2fHandler, REPOSITORIES_ELEMENT);
			String size = parseOptionalAttribute(attributes, COLLECTION_SIZE_ATTRIBUTE);
			urls = (size != null ? new ArrayList<String>(new Integer(size).intValue()) : new ArrayList<String>(4));
		}

		@Override
		public void startElement(String name, Attributes attributes)
		throws SAXException {
			if (name.equals(REPOSITORY_ELEMENT)) {
				new RepositoryHandler(this, attributes, urls);
			} else {
				invalidElement(name, attributes);
			}
		}

		public String[] getRepositories() {
			return urls.toArray(new String[urls.size()]);
		}
	}

	private class RepositoryHandler extends AbstractHandler {

		private final List<String> urls = null;

		public RepositoryHandler(AbstractHandler parentHandler, Attributes attributes, List<String> urls) {
			super(parentHandler, REPOSITORY_ELEMENT);
			String[] values = parseRequiredAttributes(attributes, new String[]{P2FURI_ATTRIBUTE});
			urls.add(values[0]);
		}

		@Override
		public void startElement(String name, Attributes attributes)
		throws SAXException {
			invalidElement(name, attributes);
		}
	}
}
