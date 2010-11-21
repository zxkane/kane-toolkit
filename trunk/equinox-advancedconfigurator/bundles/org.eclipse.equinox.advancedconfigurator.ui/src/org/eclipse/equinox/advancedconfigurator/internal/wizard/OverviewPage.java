package org.eclipse.equinox.advancedconfigurator.internal.wizard;

import java.io.File;
import java.io.FileFilter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import org.eclipse.equinox.advancedconfigurator.internal.Activator;
import org.eclipse.equinox.internal.advancedconfigurator.utils.AdvanceConfiguratorConstants;
import org.eclipse.equinox.internal.advancedconfigurator.utils.EquinoxUtils;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class OverviewPage extends WizardPage {

	class PolicyLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			File f = (File) element;
			try {
				return URLDecoder.decode(f.getName(), "utf-8");
			} catch (UnsupportedEncodingException e) {
				// won't happen
			}
			return f.getName();
		}
	}

	class PolicyContentProvider implements IStructuredContentProvider {

		public void dispose() {
			// TODO Auto-generated method stub

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub

		}

		public Object[] getElements(Object inputElement) {
			// TODO Auto-generated method stub
			return null;
		}

	}

	private CheckboxTableViewer viewer;

	protected OverviewPage(String pageName) {
		super(pageName);
		setTitle(Messages.OverviewPage_Title);
		setDescription(Messages.OverviewPage_Description);
	}

	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(new FillLayout(SWT.HORIZONTAL | SWT.VERTICAL));
		viewer = CheckboxTableViewer.newCheckList(content, SWT.BORDER | SWT.SINGLE);
		viewer.setContentProvider(new PolicyContentProvider());
		viewer.setLabelProvider(new PolicyLabelProvider());
		viewer.setInput(getInput());
		setControl(content);
	}

	private Object getInput() {
		URL[] configURL = EquinoxUtils.getConfigAreaURL(Activator.getContext());
		if (configURL != null) {
			File f = new File(configURL[0].getFile(), AdvanceConfiguratorConstants.CONFIGURATOR_FOLDER);
			return f.listFiles(new FileFilter() {

				public boolean accept(File pathname) {
					if (pathname.isDirectory())
						return true;
					return false;
				}
			});
		}
		return null;
	}

	@Override
	public IWizardPage getNextPage() {
		if (viewer.getCheckedElements().length == 0)
			return ((AdvancedConfiguratorWizard) getWizard()).createPage;
		return super.getNextPage();
	}
}
