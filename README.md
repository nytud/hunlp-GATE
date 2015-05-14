# hunlp-GATE

Source files for the [GATE](http://gate.ac.uk/) plugin **Lang_Hungarian** containing Hungarian processing resources, 
which are wrappers around already existing NLP tools in their original form (huntoken, hunmorph, hunpos, huntag3).

##Installing under GATE Developer

*Optional: first build the plugin (see __Building the Lang_Hungarian plugin__), or just use the version already included in this repository.*

###Method 1 (using a clone of this repository):

Copy the whole directory `Lang_Hungarian` into your GATE user plugin directory. 
Restart GATE Developer. You should now see **Lang_Hungarian** in the list of installed plugins.
If it's not there, check if your user plugin directory is set (see steps 2-4. below).

###Method 2 (on any computer with internet access running GATE Developer):

You can also install the plugin directly in GATE Developer using the online plugin repository hosted at `corpus.nytud.hu`:

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
11. Click on the "Apply All" button to install the plugin. You should now see **Lang_Hungarian** in the list of installed plugins on the "Installed Plugins" tab.

##Contents

* `Lang_Hungarian`: directory tree for the Lang_Hungarian GATE plugin
 * `DummyTokenizer.java`: dummy tokenizer processing resource -- splits at space
 * `DummyNER.java`: dummy NER processing resource -- NE = 2 adjacent uppercase words
 * `hungarian.jar`: precompiled GATE plugin containing DummyTokenizer + DummyNER
 * `TemplatePR.java`: template for creating new processing resource classes
* `update-site`: see `update-site/README.txt`
* `Makefile`: targets for building the plugin + uploading the plugin

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

##Udating the plugin repository

To update the plugin repository hosted at `http://corpus.nytud.hu/GATE`,
run `make upload` with specifying your user name on `corpus.nytud.hu`:

```
make upload CORPUSUSER=yourusername
```
