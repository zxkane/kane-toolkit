package com.example.p2.generator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.equinox.internal.provisional.p2.core.Version;
import org.eclipse.equinox.internal.provisional.p2.metadata.ITouchpointData;
import org.eclipse.equinox.internal.provisional.p2.metadata.ITouchpointInstruction;
import org.eclipse.equinox.internal.provisional.p2.metadata.MetadataFactory;
import org.eclipse.equinox.p2.publisher.AbstractAdvice;
import org.eclipse.equinox.p2.publisher.actions.ITouchpointAdvice;

import com.example.p2.touchpoint.actions.CreateDesktopAction;
import com.example.p2.touchpoint.actions.DeleteDesktopAction;

@SuppressWarnings("restriction")
public class NativeLauncherTouchPoint extends AbstractAdvice implements
		ITouchpointAdvice {
	
	private final Map<String, ITouchpointInstruction> touchpointInstructions = new HashMap<String, ITouchpointInstruction>(2, 1);


	public NativeLauncherTouchPoint() {
		touchpointInstructions.put("configure", MetadataFactory.createTouchpointInstruction(getConfigureBody(), null)); //$NON-NLS-1$
		/** For now, set the same data for the unconfigure phase */
		touchpointInstructions.put("unconfigure", MetadataFactory.createTouchpointInstruction(getUnConfigureBody(), null)); //$NON-NLS-1$
	}
	
	protected String getConfigureBody(){
		StringBuilder sb = new StringBuilder();
		sb.append(CreateDesktopAction.NAME);
		sb.append("(");
		sb.append(CreateDesktopAction.KEY_NAME);
		sb.append(":mail,");
		sb.append(CreateDesktopAction.KEY_DISPLAY_NAME);
		sb.append(":Mail App,");
		sb.append(CreateDesktopAction.KEY_PATH);
		sb.append(":${installFolder}/mail,");
		sb.append(CreateDesktopAction.KEY_ICON);
		sb.append(":)");
		return sb.toString();
	}
	
	protected String getUnConfigureBody() {
		StringBuilder sb = new StringBuilder();
		sb.append(DeleteDesktopAction.NAME);
		sb.append("(");
		sb.append(DeleteDesktopAction.KEY_NAME);
		sb.append(":mail.desktop)");
		return sb.toString();		
	}
	
	@Override
	public boolean isApplicable(String configSpec, boolean includeDefault,
			String id, Version version) {
		if("com.example.mail.desktop".equals(id))
			return true;
		else
			return super.isApplicable(configSpec, includeDefault, id, version);
	}
	

	@Override
	public ITouchpointData getTouchpointData(ITouchpointData existingData) {
		Map<String, ITouchpointInstruction> resultInstructions = new HashMap<String, ITouchpointInstruction>(existingData.getInstructions());
		for (Iterator<String> iterator = touchpointInstructions .keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			ITouchpointInstruction instruction = touchpointInstructions.get(key);
			ITouchpointInstruction existingInstruction = resultInstructions.get(key);

			if (existingInstruction != null) {
				String body = existingInstruction.getBody();
				if (body == null || body.length() == 0)
					body = instruction.getBody();
				else if (instruction.getBody() != null) {
					if (!body.endsWith(";")) //$NON-NLS-1$
						body += ';';
					body += instruction.getBody();
				}

				String importAttribute = existingInstruction.getImportAttribute();
				if (importAttribute == null || importAttribute.length() == 0)
					importAttribute = instruction.getImportAttribute();
				else if (instruction.getImportAttribute() != null) {
					if (!importAttribute.endsWith(",")) //$NON-NLS-1$
						importAttribute += ',';
					importAttribute += instruction.getBody();
				}
				instruction = MetadataFactory.createTouchpointInstruction(body, importAttribute);
			}
			resultInstructions.put(key, instruction);
		}
		return MetadataFactory.createTouchpointData(resultInstructions);
	}

}
