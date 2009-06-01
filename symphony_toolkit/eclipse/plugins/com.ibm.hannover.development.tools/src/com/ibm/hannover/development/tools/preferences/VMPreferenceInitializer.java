package com.ibm.hannover.development.tools.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.ibm.hannover.development.tools.Activator;


public class VMPreferenceInitializer extends AbstractPreferenceInitializer{
		@Override
		public void initializeDefaultPreferences() {
			IPreferenceStore st = Activator.getDefault().getPreferenceStore();
			st.setDefault(VMPreferencePage.PREFERENCE_KEY, 1);			
		}	
}
