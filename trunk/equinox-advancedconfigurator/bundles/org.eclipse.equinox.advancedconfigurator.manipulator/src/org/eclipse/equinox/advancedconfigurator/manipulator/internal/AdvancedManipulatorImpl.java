package org.eclipse.equinox.advancedconfigurator.manipulator.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.equinox.advancedconfigurator.Policy;
import org.eclipse.equinox.advancedconfigurator.Policy.Component;
import org.eclipse.equinox.advancedconfigurator.manipulator.AdvancedManipulator;
import org.eclipse.equinox.internal.advancedconfigurator.utils.AdvancedConfiguratorConstants;
import org.eclipse.equinox.internal.advancedconfigurator.utils.EquinoxUtils;

public class AdvancedManipulatorImpl implements AdvancedManipulator {

	private static final String BUNDLES_LIST = "bundles.list";

	public Policy[] getPolicies() {
		URL[] configs = EquinoxUtils.getConfigAreaURL(Activator.getContext());
		if (configs != null) {
			File policy = new File(configs[0].getFile(), AdvancedConfiguratorConstants.CONFIGURATOR_FOLDER);
			if (policy.exists() && policy.isDirectory()) {
				File[] candinates = policy.listFiles(new FileFilter() {

					public boolean accept(File pathname) {
						if (pathname.isDirectory())
							return true;
						return false;
					}
				});
				List<Policy> policies = new ArrayList<Policy>(candinates.length);
				for (File c : candinates)
					policies.add(buildPolicy(c));
				return policies.toArray(new Policy[policies.size()]);
			}
		}
		return new Policy[0];
	}

	private Policy buildPolicy(File c) {
		PolicyImpl p = new PolicyImpl();
		try {
			p.setName(URLDecoder.decode(c.getName(), "utf-8"));
		} catch (UnsupportedEncodingException e) {
			// won't happen
		}
		File bundles = new File(c, BUNDLES_LIST);
		List<String> bundleIDs = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(bundles));
			bundleIDs.add(reader.readLine());
		} catch (FileNotFoundException e) {

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		List<Component> comps = new ArrayList<Policy.Component>(bundleIDs.size());
		for (String bundle : bundleIDs) {
			String[] strs = bundle.split(",");
			if (strs.length == 2) {
				Component comp = new Component();
				comp.id = strs[0];
				comp.version = strs[1];
				comps.add(comp);
			}
		}
		p.setComponents(comps.toArray(new Component[comps.size()]));

		return p;
	}

	public void updatePolicy(Policy oldPolicy, Policy newPolicy) {
		// TODO Auto-generated method stub

	}

	public void addPolicy(String policyName, Component[] components) {
		URL[] configs = EquinoxUtils.getConfigAreaURL(Activator.getContext());
		if (configs != null) {
			File policy = new File(configs[0].getFile(), AdvancedConfiguratorConstants.CONFIGURATOR_FOLDER + File.separator + policyName);
			policy.mkdirs();
			BufferedWriter writer = null;
			try {
				File bundles = new File(policy, BUNDLES_LIST);
				if (!bundles.exists())
					bundles.createNewFile();
				writer = new BufferedWriter(new FileWriter(bundles));
				for (Component comp : components) {
					writer.write(comp.id + "," + comp.version);
					writer.write("\n");
				}
			} catch (IOException e) {

			} finally {
				if (writer != null)
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
	}

}
