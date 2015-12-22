#A plug-in import/export the p2 installation to/from configuration file.

# Introduction #

**This feature has been contributed to p2 itself, you can find it in Eclipse Indigo(3.7) M7+ now!** Note: the exported file is incompatible with previous versions.

It's a tool to help quickly replicate the p2 installation to other Eclipse instance.
Eclipse provisioning goes into p2 world from Eclipse 3.4. The classical way to install features/plug-ins is NOT recommended that simply copy them into features/plugins folder. And p2 dev team don't consider it's necessary to support installing features/plugins into external folder.
So there is a lot of manual efforts to 'replicate' the same plug-ins in different Eclipse instance, different workstations or different platforms.

This tool can help simplify the installing effort, just export/import the p2 installation to/from file like preference.

Currently this tool also supports importing from another existing Eclipse instance!


# Details #

## How to install ##

- built-in feature since Eclipse Indigo(3.7) M7

- install it from online repository(recommended)
> - [For Eclipse Galileo](http://kane-toolkit.googlecode.com/svn/trunk/p2-replication/repository/3.5/)

> - [For Helios](http://kane-toolkit.googlecode.com/svn/trunk/p2-replication/repository/helios/)

- download the [featured version](http://kane-toolkit.googlecode.com/files/org.eclipse.equinox.p2.replication_1.0.0.200912241518.jar), then copy it into dropin folder of your Eclipse

## Usage ##

**export**

Choose ‘File' - 'Export'- 'P2' - 'P2 Installation' to export your p2 installation configuration to file.

**import**

Choose 'File' - 'Import' - 'P2' - 'P2 Installation' to import p2 installation from the configuration file.

Choose 'File' - 'Import' - 'P2' - 'From existing installation' to import features/plugins from another existing Eclipse instance to save time to download them.

Notes: the already installed features can't be installed again.