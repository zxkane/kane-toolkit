package com.example.p2.installer;


import java.io.File;
import java.net.URI;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class InstallView extends ViewPart {
	
	class SelectionListener extends MouseAdapter {
		private final Text text;

		public SelectionListener(Text text) {
			this.text = text;
		}
		
		@Override
		public void mouseDown(MouseEvent e) {
			DirectoryDialog dialog = new DirectoryDialog(InstallView.this.getViewSite().getShell());
			String input = dialog.open();
			if(input != null)
				text.setText(input);
		}
	}
	
	public static final String ID = "com.example.p2.installer.installView";
	private Button install;

	/**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.horizontalSpacing = 10;
		parent.setLayout(layout);
			
		Label label = new Label(parent, SWT.NONE);
		label.setText("Input repository location");
		final Text text = new Text(parent, SWT.BORDER);
		GridData griddata = new GridData();
		griddata.grabExcessHorizontalSpace = true;
		griddata.horizontalAlignment = GridData.FILL;
		text.setLayoutData(griddata);
		text.setSize(600, 20);
		text.addMouseListener(new SelectionListener(text));
		
		label = new Label(parent, SWT.NONE);
		label.setText("Input install location");
		final Text text2 = new Text(parent, SWT.BORDER);
		text2.setLayoutData(griddata);
		text2.setSize(600, 20);
		text2.addMouseListener(new SelectionListener(text2));
		
		install = new Button(parent, SWT.BORDER);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.CENTER;
		data.horizontalSpan = 2;
		install.setLayoutData(data);
		install.setText("Click to install");
		
		install.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				String repository = text.getText();
				String location = text2.getText();
				String error = "Invalid repository or install location.";
				
				if(repository == null || location == null || "".equals(repository.trim())
						|| "".equals(location.trim())) {
					popMessage(error);
					return;
				}
				try {
						File f = new File(location);
						URI repositoryURI = new File(repository).toURI();
						try {
							IRunnableWithProgress job = new ProvisioningJob(repositoryURI, f);
							PlatformUI.getWorkbench().getProgressService().run(true, false, job);
						} catch (InterruptedException e) {
							popMessage(e.getMessage());
						}
				} catch(Exception excption) {
					popMessage(error);
				}
			}
			
			private void popMessage(String message){
				MessageDialog.openError(InstallView.this.getViewSite().getShell(), "ERROR", message);				
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		install.setFocus();
	}
}