# hunlp-GATE

Source files for the [GATE](http://gate.ac.uk/) plugin **Lang_Hungarian** containing Hungarian processing resources, 
which are wrappers around already existing NLP tools in their original form.

##Installing under GATE Developer

*Optional: first build the plugin (see __Building the Lang_Hungarian plugin__), or just use the version already included in this repository.*

###Method 1 (using a clone of this repository):

Copy the whole directory `Lang_Hungarian` into your GATE user plugin directory. 
Restart GATE Developer. You should now see **Lang_Hungarian** in the list of installed plugins.
If it's not there, check if your user plugin directory is set (see steps 2-4. below).

###Method 2 (only GATE Developer & internet acces required):

Follow these steps to install the plugin directly in GATE Developer using the online plugin repository hosted at `corpus.nytud.hu`:

1. Start GATE Developer.
2. In the menu click: File / Manage CREOLE Plugins...
3. Click on the "Configuration" tab.
4. If you haven't already done so, set your User Plugin Directory (e.g. `/home/username/My_GATE_plugins/`).
5. Click the "+" sign button to the right of the Plugin Repositories list to add a new repository.
6. Enter:
 * Name: RIL-MTA
 * URL: `http://corpus.nytud.hu/GATE/gate-update-site.xml`
7. Click OK.
8. Click the "Apply All" button at the bottom.
9. Click on the "Available to Install" tab.
10. You should now see **Lang_Hungarian** in the list of plugins available to install. Enable the checkbox left to its name in column "Install".
11. Click on the "Apply All" button to install the plugin. 
12. You should now see **Lang_Hungarian** in the list of installed plugins on the "Installed Plugins" tab.

##Contents

* `Lang_Hungarian`: directory tree for the Lang_Hungarian GATE plugin
 * `src`: Java sources of the included Processing Resources. See Javadocs for details.
 * `resources`: non-Java binaries, sources and resources files for the included tools
 * `hungarian.jar`: plugin Java binaries in a jar file
 * `build.xml`: use this to build the jar from sources using Apache Ant
 * `creole.xml`: this tells GATE how to use hungarian.jar as a CREOLE plugin
 * `.classpath`, `.project`: use these to import project into Eclipse Java IDE
* `Makefile`: use to rebuild, install etc. the plugin from command line

##Building the Lang_Hungarian plugin

To build the GATE plugin from the Java sources
(and add the neccessary metadata) run `make build`.
A working GATE installation is necessary.
The GATE installation directory should be given to `make` as `GATE_HOME`:

```
make build GATE_HOME=/your/gate/installation/dir
```

This will create `hungarian.jar` in the directory `Lang_Hungarian`.
(A precompiled `hungarian.jar` is also accessible directly from the repository.)

##Plugin command-line installation (developers)

If you have rebuilt the plugin, it is also possible to install it to your GATE user plugin directory with the
following command:

```
make local_install GATE_USER_PLUGINS_DIR=/your/gate/user/plugin/directory
```

##Udating the plugin repository

To update the plugin repository  hosted at `http://corpus.nytud.hu/GATE`,
run `make upload` specifying your user name on `corpus.nytud.hu`:

```
make upload CORPUSUSER=yourusername
```

This will upload your local `hungarian.jar`, `creole.xml` and `resources` directory.
