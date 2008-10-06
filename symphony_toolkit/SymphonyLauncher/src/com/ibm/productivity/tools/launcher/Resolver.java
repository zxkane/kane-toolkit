package com.ibm.productivity.tools.launcher;

import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.core.runtime.internal.adaptor.EclipseAdaptorMsg;
import org.eclipse.core.runtime.internal.adaptor.MessageHelper;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.ResolverError;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.service.resolver.VersionConstraint;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Resolver {
	private PlatformAdmin platformAdmin;
	private BundleContext context;
	private String[] list;
	
	public Resolver(String[] list) {
		this.list = list;
	}
	
	private PlatformAdmin getPlatformAdmin() {
		if (platformAdmin == null) {
			ServiceReference platformAdminRef = context.getServiceReference(PlatformAdmin.class.getName());
			if (platformAdminRef == null) {
				return null;
			}
			platformAdmin = (PlatformAdmin) context.getService(platformAdminRef);
		}
		return platformAdmin;
	}
	
	private void ungetPlatformAdmin() {
		ServiceReference platformAdminRef = context.getServiceReference(PlatformAdmin.class.getName());
		context.ungetService(platformAdminRef);
	}

	public boolean resolveBundle(String bundleName){
		try{
			boolean resolve = false;
			Bundle[] bundles = context.getBundles();
			for(int i = 0; i < bundles.length; i++){
				if(bundles[i].getSymbolicName().equals(bundleName)){
					if((bundles[i].getState() & (Bundle.RESOLVED | Bundle.ACTIVE | Bundle.STARTING | Bundle.START_TRANSIENT)) > 0){
						resolve = true;
						return resolve;
					}else{
						System.out.println(bundleName + " is NOT resolved.:");
						PlatformAdmin platformAdmin = getPlatformAdmin();
						State systemState = platformAdmin.getState(false);
						BundleDescription bundle = systemState.getBundle(bundles[i].getBundleId());
						VersionConstraint[] unsatisfied = platformAdmin.getStateHelper().getUnsatisfiedConstraints(bundle);
						ResolverError[] resolverErrors = systemState.getResolverErrors(bundle);
						for (int j = 0; j < resolverErrors.length; j++) {
							if ((resolverErrors[j].getType() & (ResolverError.MISSING_FRAGMENT_HOST | ResolverError.MISSING_GENERIC_CAPABILITY | ResolverError.MISSING_IMPORT_PACKAGE | ResolverError.MISSING_REQUIRE_BUNDLE)) != 0)
								continue;
							System.out.print("  "); //$NON-NLS-1$
							System.out.println(resolverErrors[i].toString());
						}
	
						if (unsatisfied.length == 0 && resolverErrors.length == 0) {
							System.out.print("  "); //$NON-NLS-1$
							System.out.println(EclipseAdaptorMsg.ECLIPSE_CONSOLE_NO_CONSTRAINTS);
						}
						if (unsatisfied.length > 0) {
							System.out.print("  "); //$NON-NLS-1$
							System.out.println(EclipseAdaptorMsg.ECLIPSE_CONSOLE_DIRECT_CONSTRAINTS);
						}
						for (int j = 0; j < unsatisfied.length; j++) {
							System.out.print("    "); //$NON-NLS-1$
							System.out.println(MessageHelper.getResolutionFailureMessage(unsatisfied[j]));
						}
					}
					return resolve;
				}
			}
			System.out.println("Can't find bundle " + bundleName);
			return resolve;
		}finally{
			ungetPlatformAdmin();
		}
	}
	
	public Object run(String[] arguments) throws Exception{
		context = EclipseStarter.startup(arguments, null);
		for(int i = 0; i < list.length; i++){
			if(!resolveBundle(list[i]))
				break;
		}
		return EclipseStarter.run(null);
	}

}
