package cn.ourpk.robot.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import cn.ourpk.robot.PostProvider;

public class ProviderFactory {
	private static final String EXTENSION_NAME = "provider";
	private static PostProvider provider = null;
	
	public static PostProvider getProvider(){
		if(provider == null){
			IConfigurationElement[] configurationElements = Platform.getExtensionRegistry().
			getExtensionPoint(Activator.ID, EXTENSION_NAME).getConfigurationElements();
			IConfigurationElement element = null;
			int highestRank = 0;
			for(int i = 0; i < configurationElements.length; i++){
				if(element == null)
					element = configurationElements[i];
				else{
					int rank = Integer.valueOf(configurationElements[i].getAttribute("rank")).intValue();
					if(rank > highestRank){
						highestRank = rank;
						element = configurationElements[i];
					}
				}
			}
			if(element != null)
				try {
					provider = (PostProvider) element.createExecutableExtension("class");
				} catch (CoreException e) {
					e.printStackTrace();
				}
		}
		return provider;
	}
}
