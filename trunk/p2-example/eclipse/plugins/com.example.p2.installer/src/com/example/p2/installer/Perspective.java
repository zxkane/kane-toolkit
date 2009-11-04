package com.example.p2.installer;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	/**
	 * The ID of the perspective as specified in the extension.
	 */
	public static final String ID = "com.example.p2.installer.perspective";

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		
		layout.addStandaloneView(InstallView.ID,  false, IPageLayout.LEFT, 1f, editorArea);
		
		layout.getViewLayout(InstallView.ID).setCloseable(false);
	}
}
