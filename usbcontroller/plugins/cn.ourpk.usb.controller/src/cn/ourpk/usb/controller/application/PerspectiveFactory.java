package cn.ourpk.usb.controller.application;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class PerspectiveFactory implements IPerspectiveFactory {

	public static final String ID = "cn.ourpk.usb.controller.usbperspective"; //$NON-NLS-1$
	
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		
		layout.addStandaloneView(USBViewPart.ID,  false, IPageLayout.LEFT, 1f, editorArea);	
		layout.getViewLayout(USBViewPart.ID).setCloseable(false);
	}

}
