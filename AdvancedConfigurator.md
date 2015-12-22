# Introduction #

The provisioning and installation management of Eclipse have been into the period of p2. P2 is an intelligent tool to install new plug-ins, update the plug-ins to latest version and manage the dependencies among the different plug-ins. However the eclipse would be heavier and low performance after installing a lot of plug-ins to extend its capability. There is no a good way to configurate eclipse running with different policies to satisfy the different development requirement of users. For example, users want to the eclipse only have C++ development kit and git when doing c/c++ code development. Vice versa, the eclipse only has JDT and Clearcase plug-ins when doing Java development. Loading less plug-ins would improve the performance of eclipse and avoid the conflict among different plug-ins.

So **Advanced Configurator** is an enhancement implementation of bundle manager comparing to the default bundles management of Eclipse(the internal name is simple configurator). This tool is intended to provide a flexible and configurable way to run the single eclipse instance with enabling different plug-ins.


# How to install #

### Install it via Eclipse Marketplace ###

You could install it via Eclipse Marketplace if your eclipse has installed Marketplace feature.

[The home page of advanced configurator on Eclipse Marketplace](http://marketplace.eclipse.org/content/equinox-advanced-configurator)

### Manual install ###

Add below repository into your eclipse

> `http://kane-toolkit.googlecode.com/svn/trunk/equinox-advancedconfigurator/advanced-repository/`

# How to use #

Click the menu item '_Help_' - '_Advanced Configurator..._' to open the wizard to help you create policy or manage existing ones.
![http://kane-toolkit.googlecode.com/svn-history/r267/trunk/equinox-advancedconfigurator/images/menu.png](http://kane-toolkit.googlecode.com/svn-history/r267/trunk/equinox-advancedconfigurator/images/menu.png)
You could create the new policy, change another policy as default and reverting to the Eclipse's default configuration via the wizard.
![http://kane-toolkit.googlecode.com/svn-history/r273/trunk/equinox-advancedconfigurator/images/main.png](http://kane-toolkit.googlecode.com/svn-history/r273/trunk/equinox-advancedconfigurator/images/main.png)
Click 'Next' to create a new policy or modify existing policy. Input the name for the new policy. In below example I created a policy that only contains Android development capability.
![http://kane-toolkit.googlecode.com/svn-history/r273/trunk/equinox-advancedconfigurator/images/inputname.png](http://kane-toolkit.googlecode.com/svn-history/r273/trunk/equinox-advancedconfigurator/images/inputname.png)
Click 'Next' to last page to choose the products related to Android development, then click 'Finish' to complete the configuration.
![http://kane-toolkit.googlecode.com/svn-history/r273/trunk/equinox-advancedconfigurator/images/choose.png](http://kane-toolkit.googlecode.com/svn-history/r273/trunk/equinox-advancedconfigurator/images/choose.png)

# Known limitation #

Eclipse would load the all installed plug-ins to make thing mess if installing, updating or uninstalling features when you're launching your eclipse via advanced configurator.
So you'd better installing, updating or uninstalling features after switching back to use simple configurator.