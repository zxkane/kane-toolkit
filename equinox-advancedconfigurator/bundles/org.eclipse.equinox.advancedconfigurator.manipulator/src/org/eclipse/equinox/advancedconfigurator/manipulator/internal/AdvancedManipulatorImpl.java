package org.eclipse.equinox.advancedconfigurator.manipulator.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.advancedconfigurator.Policy;
import org.eclipse.equinox.advancedconfigurator.Policy.Component;
import org.eclipse.equinox.advancedconfigurator.manipulator.AdvancedManipulator;
import org.eclipse.equinox.advancedconfigurator.manipulator.ManipulatorEvent;
import org.eclipse.equinox.advancedconfigurator.manipulator.ManipulatorListener;
import org.eclipse.equinox.frameworkadmin.BundleInfo;
import org.eclipse.equinox.internal.advancedconfigurator.utils.AdvancedConfiguratorConstants;
import org.eclipse.equinox.internal.advancedconfigurator.utils.EquinoxUtils;
import org.eclipse.equinox.internal.frameworkadmin.equinox.EquinoxFwConfigFileParser;
import org.eclipse.equinox.internal.provisional.frameworkadmin.FrameworkAdmin;
import org.eclipse.equinox.internal.provisional.frameworkadmin.Manipulator;
import org.eclipse.equinox.simpleconfigurator.manipulator.SimpleConfiguratorManipulator;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.Bundle;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

@SuppressWarnings("restriction")
public class AdvancedManipulatorImpl implements AdvancedManipulator {

	private static final String BUNDLES_LIST = "bundles.list";
	private final List<ManipulatorListener> listeners = new ArrayList<ManipulatorListener>();

	public Policy[] getPolicies() {
		URL[] configs = EquinoxUtils.getConfigAreaURL(Activator.getContext());
		if (configs != null) {
			File policy = new File(configs[0].getFile(), AdvancedConfiguratorConstants.CONFIGURATOR_FOLDER);
			File defaultPolicyList = new File(policy, AdvancedConfiguratorConstants.POLICY_LIST);
			File defaultPolicy = null;
			InputStream input = null;

			try {
				input = new FileInputStream(defaultPolicyList);
				Properties prop = new Properties();
				prop.load(input);
				String file = prop.getProperty(AdvancedConfiguratorConstants.DEFAULT_POLICY_KEY);
				if (file != null)
					defaultPolicy = new File(file);
			} catch (FileNotFoundException e) {
				// do nothing
			} catch (IOException e) {
			} finally {
				if (input != null)
					try {
						input.close();
					} catch (IOException e) {
					}
			}
			if (policy.exists() && policy.isDirectory()) {
				File[] candinates = policy.listFiles(new FileFilter() {

					public boolean accept(File pathname) {
						if (pathname.isDirectory())
							return true;
						return false;
					}
				});
				List<Policy> policies = new ArrayList<Policy>(candinates.length);
				for (File c : candinates) {
					policies.add(buildPolicy(c));
					if (c.equals(defaultPolicy))
						((PolicyImpl) policies.get(policies.size() - 1)).setDefault(true);
				}
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

	public void addPolicy(String policyName, boolean isDefault, Component[] components) {
		Policy newPolicy = addInternal(policyName, isDefault, components);
		if (newPolicy != null)
			fireEvent(ManipulatorEvent.ADD_POLICY, null, newPolicy);
	}

	private Policy addInternal(String policyName, boolean isDefault, Component[] components) {
		Policy newPolicy = buildPolicy(policyName, components);
		try {
			if (isDefault && newPolicy != null) {
				setDefaultInternal(newPolicy);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return newPolicy;
	}

	private Policy buildPolicy(String policyName, Component[] components) {
		URL[] configs = EquinoxUtils.getConfigAreaURL(Activator.getContext());
		if (configs != null) {
			BufferedWriter writer = null;
			try {
				File policy = new File(configs[0].getFile(), AdvancedConfiguratorConstants.CONFIGURATOR_FOLDER + File.separator + encodePolicyName(policyName));
				policy.mkdirs();
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
						// prevent simpleconfigurator to take effect
						if ("org.eclipse.equinox.simpleconfigurator".equals(bundle.getSymbolicName())) {
							bundle.setStartLevel(4);
							bundle.setMarkedAsStarted(false);
							// continue;
						}
						if (policyBundles.contains(bundle)) {
							policyBundleInfos.add(bundle);
						}
					}
					locationTracker.open();
					Location location = (Location) locationTracker.getService();
					URI installURI = location.getURL().toURI();
					URI configRelURI = new File(configs[0].getFile()).getParentFile().toURI();
					manipulator.saveConfiguration(policyBundleInfos.toArray(new BundleInfo[policyBundleInfos.size()]), new File(policy,
							AdvancedConfiguratorConstants.CONFIG_LIST), configRelURI.equals(installURI) ? installURI : configRelURI);

					Policy newPolicy = buildPolicy(policy);
					return newPolicy;
				} catch (URISyntaxException e) {
					// won't happen
				} finally {
					tracker.close();
					locationTracker.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
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
		return null;
	}

	private String encodePolicyName(String policyName) {
		try {
			return URLEncoder.encode(policyName, "utf-8");
		} catch (UnsupportedEncodingException e) {
			return policyName;
		}
	}

	private void fireEvent(int addPolicy, Policy oldState, Policy newState) {
		ManipulatorEvent event = new ManipulatorEvent(addPolicy, oldState, newState);
		for (ManipulatorListener l : listeners)
			l.notify(event);
	}

	private void updatePolicyList(Policy policy) throws FileNotFoundException, IOException {
		URL[] configs = EquinoxUtils.getConfigAreaURL(Activator.getContext());
		if (configs != null) {
			File advConfig = new File(configs[0].getFile(), AdvancedConfiguratorConstants.CONFIGURATOR_FOLDER);
			OutputStream output = null;
			try {
				File policyList = new File(advConfig, AdvancedConfiguratorConstants.POLICY_LIST);
				Properties policyProp = new Properties();
				policyProp.put(AdvancedConfiguratorConstants.DEFAULT_POLICY_KEY, policy == null ? "" : new File(advConfig, policy.getName()).getAbsolutePath());
				output = new FileOutputStream(policyList);
				policyProp.store(output, null);
			} finally {
				if (output != null)
					output.close();
			}
		}
	}

	private void updateOSGiBundles(String symbolName, String symbolName2) throws IOException, URISyntaxException {
		ServiceTracker adminTracker = new ServiceTracker(Activator.getContext(), FrameworkAdmin.class.getName(), null);
		try {
			adminTracker.open();
			FrameworkAdmin frameworkAdmin = (FrameworkAdmin) adminTracker.getService();

			Bundle bundle = Platform.getBundle(symbolName);
			URI uri = getLocation(bundle);
			Bundle bundle2 = Platform.getBundle(symbolName2);
			URI uri2 = getLocation(bundle2);

			BundleInfo[] bundles = new BundleInfo[] { new BundleInfo(bundle.getSymbolicName(), bundle.getVersion().toString(), uri, 1, true),
					new BundleInfo(bundle2.getSymbolicName(), bundle2.getVersion().toString(), uri2, 4, false) };
			EquinoxFwConfigFileParser parser = new EquinoxFwConfigFileParser(Activator.getContext());
			Manipulator manipulator = frameworkAdmin.getRunningManipulator();
			manipulator.load();
			parser.saveFwConfig(bundles, manipulator, true, false);
		} finally {
			adminTracker.close();
		}
	}

	private URI getLocation(Bundle bundle) throws URISyntaxException, MalformedURLException {
		String location = bundle.getLocation();

		if (location.startsWith("initial@")) { //$NON-NLS-1$
			location = location.substring(8);
		}
		if (location.startsWith("reference:file:plugins/"))
			location = location.replace("reference:file:plugins/", "reference:file:");
		return new URL(location).toURI();
	}

	/**
	 * @param policy if it's null, revert to use simple configurator
	 */
	public boolean setDefault(Policy policy) {
		try {
			Policy previous = setDefaultInternal(policy);
			fireEvent(ManipulatorEvent.CHANGE_DEFAULT_POLICY, previous, policy);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return false;
	}

	private Policy setDefaultInternal(Policy policy) throws IOException, FileNotFoundException, URISyntaxException {
		Policy previous = null;
		for (Policy p : getPolicies()) {
			if (p.isDefault()) {
				previous = p;
				break;
			}
		}
		if (previous != null && !previous.equals(policy)) {
			((PolicyImpl) previous).setDefault(false);
		}
		if (policy != null) {
			((PolicyImpl) policy).setDefault(true);
			updateOSGiBundles(AdvancedConfiguratorConstants.TARGET_CONFIGURATOR_NAME, "org.eclipse.equinox.simpleconfigurator");
		} else
			updateOSGiBundles("org.eclipse.equinox.simpleconfigurator", AdvancedConfiguratorConstants.TARGET_CONFIGURATOR_NAME);
		updatePolicyList(policy);
		return previous;
	}

	public void addManipulatorListener(ManipulatorListener l) {
		this.listeners.add(l);
	}

	public void removeManipulatorListener(ManipulatorListener l) {
		listeners.remove(l);
	}

	public void removePolicy(Policy policy) {
		URL[] configs = EquinoxUtils.getConfigAreaURL(Activator.getContext());
		if (configs != null) {
			try {
				if (policy.isDefault())
					setDefaultInternal(null);
				File policyDir = new File(configs[0].getFile(), AdvancedConfiguratorConstants.CONFIGURATOR_FOLDER + File.separator
						+ encodePolicyName(policy.getName()));
				deleteFiles(policyDir);
				fireEvent(ManipulatorEvent.REMOVE_POLICY, policy, null);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}

	private void deleteFiles(File file) {
		if (file.isDirectory()) {
			for (File sub : file.listFiles())
				deleteFiles(sub);
		}
		file.delete();
	}

	public void updatePolicy(Policy oldPolicy, String newName, boolean isDefault, Component[] components) {
		Policy newPolicy = null;
		if (oldPolicy.getName().equals(newName)) {
			newPolicy = buildPolicy(newName, components);
			try {
				if (oldPolicy.isDefault() != isDefault) {
					setDefaultInternal(newPolicy);
					((PolicyImpl) newPolicy).setDefault(isDefault);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		} else {
			newPolicy = addInternal(newName, isDefault, components);
			if (newPolicy != null)
				removePolicy(oldPolicy);
		}
		if (newPolicy != null)
			fireEvent(ManipulatorEvent.Update_POLICY, oldPolicy, newPolicy);
	}
}
