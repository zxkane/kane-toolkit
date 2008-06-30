package com.ibm.hannover.pde.registry.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.util.Util;

import com.ibm.hannover.pde.registry.PerspectiveFactory;

public class PDERegistryHandler extends AbstractHandler implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		try {
			if(page == null)
				HandlerUtil.getActiveWorkbenchWindow(event).openPage(PerspectiveFactory.PERSPECTIVE_ID, null);
			else{
				IPerspectiveRegistry reg = PlatformUI.getWorkbench().getPerspectiveRegistry();
				IPerspectiveDescriptor perDesc = reg.findPerspectiveWithId(PerspectiveFactory.PERSPECTIVE_ID);
				if(Util.equals(page.getPerspective(), perDesc))
					page.showView(PerspectiveFactory.VIEW_ID_PDE_REGISTRY);
				else
					page.setPerspective(perDesc);
			}
		} catch (WorkbenchException e) {
			throw new ExecutionException("Error while opening view.", e);
		}
		
		return null;
	}

    public static boolean equals(Object left, Object right) {
        return left == null ? right == null : ((right != null) && left
                .equals(right));
    }
}
