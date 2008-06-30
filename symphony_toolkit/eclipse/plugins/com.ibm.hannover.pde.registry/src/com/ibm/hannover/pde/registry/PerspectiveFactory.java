package com.ibm.hannover.pde.registry;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class PerspectiveFactory implements IPerspectiveFactory {
	public static final String VIEW_ID_PDE_REGISTRY = "org.eclipse.pde.runtime.RegistryBrowser";
	public static final String PERSPECTIVE_ID = "com.ibm.hannover.pde.registry.perspective";
	
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		layout.addStandaloneView(VIEW_ID_PDE_REGISTRY, true, IPageLayout.TOP, 1, 
				IPageLayout.ID_EDITOR_AREA);
	}

}
