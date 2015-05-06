# hunlp-GATE

Source files for the [GATE](http://gate.ac.uk/) plugin **Lang_Hungarian** containing Hungarian processing resources, 
which are wrappers around already existing NLP tools in their original form (huntoken, hunmorph, hunpos, huntag3).

##Installing under GATE Developer

*Optional: first build the plugin (see __Building the Lang_Hungarian plugin__), or just use the version already included in this repository.*

###Method 1 (using a clone of this repository):

Copy the whole directory Lang_Hungarian/ under ./build/ into your GATE user plugin directory. 
Restart GATE Developer. You should now see **Lang_Hungarian** in the list of installed plugins.
If it's not there, check if your user plugin directory is set (see steps 2-4. below).

###Method 2 (on any computer with internet access running GATE Developer):

1. Start GATE Developer.
2. In the menu click: File / Manage CREOLE Plugins...
3. Click on the "Configuration" tab.
4. If you haven't already done so, set your User Plugin Directory (e.g. /home/username/My_GATE_plugins/).
5. Click the "+" sign button to the right of the Plugin Repositories list to add a new repository.
6. Enter:
 * Name: RIL-MTA
 * URL: http://corpus.nytud.hu/GATE/gate-update-site.xml
7. Click OK.
8. Click the "Apply All" button at the bottom.
9. Click on the "Available to Install" tab.
10. You should now see **Lang_Hungarian** in the list of plugins available to install. Enable the checkbox left to its name in column Install.
11. Click on the "Apply All" to install the plugin. You should now see **Lang_Hungarian** in the list of installed plugins on the "Installed Plugins" tab.

##Contents

* DummyTokenizer: source files for dummy tokenizer processing resource Java project
* TemplatePR: template for creating new processing resource classes
* creole: see creole/README.txt
* build: the generated plugin will be created here (see *Building the Lang_Hungarian plugin*)
* Makefile: various targets for building the plugin

##Building the Lang_Hungarian plugin

To build the GATE plugin from the Java sources and add the neccessarry metadat, type:

```
make build
```

This will create files in the directory ./build/Lang_Hungarian.

##Udating the plugin repository

To update the plugin repository hosted at corpus.nytud.hu/GATE:

1. Edit Makefile to set your username on corpus.nytud.hu:

```
UPLOAD_USER=myusername
```

2. Type:

```
make upload
```

