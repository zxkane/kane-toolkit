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
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.equinox.advancedconfigurator.Policy;
import org.eclipse.equinox.advancedconfigurator.Policy.Component;
import org.eclipse.equinox.advancedconfigurator.manipulator.AdvancedManipulator;
import org.eclipse.equinox.frameworkadmin.BundleInfo;
import org.eclipse.equinox.internal.advancedconfigurator.utils.AdvancedConfiguratorConstants;
import org.eclipse.equinox.internal.advancedconfigurator.utils.EquinoxUtils;
import org.eclipse.equinox.simpleconfigurator.manipulator.SimpleConfiguratorManipulator;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

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
			String line = null;
			while ((line = reader.readLine()) != null)
				bundleIDs.add(line);
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
				File bundleFile = new File(policy, BUNDLES_LIST);
				if (!bundleFile.exists())
					bundleFile.createNewFile();
				writer = new BufferedWriter(new FileWriter(bundleFile));
				Set<BundleInfo> policyBundles = new HashSet<BundleInfo>();
				for (Component comp : components) {
					policyBundles.addAll(Arrays.asList(comp.bundles));
					writer.write(comp.id + "," + comp.version);
					writer.write("\n");
				}

				ServiceTracker tracker = new ServiceTracker(Activator.getContext(), SimpleConfiguratorManipulator.class.getName(), null);
				ServiceTracker locationTracker = new ServiceTracker(Activator.getContext(), Activator.getContext().createFilter(Location.INSTALL_FILTER), null);
				try {
					tracker.open();
					SimpleConfiguratorManipulator manipulator = (SimpleConfiguratorManipulator) tracker.getService();
					BundleInfo[] totalBundleInfos = manipulator.loadConfiguration(Activator.getContext(),
							AdvancedConfiguratorConstants.SIMPLE_CONFIGURATOR_FOLDER + File.separator + AdvancedConfiguratorConstants.CONFIG_LIST);
					List<BundleInfo> policyBundleInfos = new ArrayList<BundleInfo>(policyBundles.size());
					for (BundleInfo bundle : totalBundleInfos) {
						if (policyBundles.contains(bundle))
							policyBundleInfos.add(bundle);
					}
					locationTracker.open();
					Location location = (Location) locationTracker.getService();
					manipulator.saveConfiguration(policyBundleInfos.toArray(new BundleInfo[policyBundleInfos.size()]), new File(policy,
							AdvancedConfiguratorConstants.CONFIG_LIST), location.getURL().toURI());

				} catch (URISyntaxException e) {
					// won't happen
				} finally {
					tracker.close();
					locationTracker.close();
				}
			} catch (IOException e) {

			} catch (InvalidSyntaxException e) {
				// won't happen
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
