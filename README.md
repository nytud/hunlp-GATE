# hunlp-GATE

Sources for the **Lang_Hungarian** [GATE](http://gate.ac.uk/) plugin containing Hungarian processing resources (wrappers around already existing Hungarian NLP tools) developed by the [Department of Language Technology](http://www.nytud.hu/oszt/nyte/index.html) at [RIL-MTA](http://www.nytud.hu/).

Developers: Márton Miháltz, Bálint Sass

##Installing under GATE Developer

**Requirements:**

* Java runtime (JRE or JDK) version 1.8 or later
* GATE Developer 8.0 or later
* For best results (safely loading the Magyarlánc Depparse application) you will need a **64-bit operating system** with **64-bit Java** installed. Please use the following command to launch GATE Developer with request to 2GB of heap space (Linux, OS X):


 ```
 <your_GATE_Developer_path>\bin\gate.sh -Xmx2G
 ```

###Method 1 (only GATE Developer & internet acces required):

Follow these steps to install the plugin directly in GATE Developer using the online plugin repository hosted at `corpus.nytud.hu` (*Note: the whole plugin complete with model files requires 600MB of space and may take a couple of minutes to download*):

1. Start GATE Developer.
2. In the menu click: File / Manage CREOLE Plugins...
3. Click on the "Configuration" tab.
4. If you haven't already done so, set your User Plugin Directory (e.g. `/home/username/My_GATE_plugins/`).
5. Click the "+" sign button to the right of the Plugin Repositories list to add a new repository.
6. Enter:
 * Name: `RIL-MTA`
 * URL: `http://corpus.nytud.hu/GATE/gate-update-site.xml`
7. Click OK.
8. Click the "Apply All" button at the bottom.
9. Click on the "Available to Install" tab.
10. You should now see **Lang_Hungarian** in the list of plugins available to install. Enable the checkbox left to its name in column "Install".
11. Click on the "Apply All" button to install the plugin. 
12. You should now see **Lang_Hungarian** in the list of installed plugins on the "Installed Plugins" tab.
13. If you want to use the *Huntag3*-based processing resources (NER, NP chunking) you need to run `Lang_Hungarian/resources/huntag3/setup_linux.sh` (Ubuntu, Debian Linux) to install required dependencies (superuser privileges will be required).

###Method 2 (using a clone of this repository):

(*Optional: first build the plugin (see __Building the Lang_Hungarian plugin__), or just use the version already included in this repository.*)

1. Clone this git repository to your machine.
2. Obtain 3rd party tools not included in this repository:
  * If you want to use *Magyarlánc*, see `Lang_Hungarian/resources/magyarlanc/README.md` about obtaining binaries
  * If you want to use *Huntag3* (NP chunking, NER):
    * Run `Lang_Hungarian/resources/huntag3/setup_linux.sh` (Ubuntu, Debian Linux) to install required dependencies for Huntag3 (superuser privileges required).
    * See `Lang_Hungarian/resources/huntag3/models/README.md` for obtaining trained models for Huntag3.
3. Copy the whole directory `Lang_Hungarian` into your GATE user plugin directory.
4. Restart GATE Developer. You should now see **Lang_Hungarian** in the list of installed plugins.
   If it's not there, check if your user plugin directory is set (see steps 2-4. above).

##Contents

The plugin contains the following GATE **Processing Resources**:

* Hunpos Hungarian PoS-tagger (Linux)
* HunMorph Hungarian morphological analyzer (Linux)
* Hungarian NP chunking with Huntag3 (Linux)
* Hungarian NER with Huntag3 (Linux)
* Magyarlánc Hungarian Sentence Splitter
* Magyarlánc Hungarian Tokenizer
* Magyarlánc Hungarian Sentence Splitter and Tokenizer
* Magyarlánc Hungarian Morphological Analyzer (KR)
* Magyarlánc Hungarian Morphological Analyzer And Guesser (MSD)
* Magyarlánc Hungarian POS Tagger and Lemmatizer
* Magyarlánc Hungarian Dependency Parser

You will also find the following **ready made applications** in GATE Developer (to access, in the menu click *File -> Ready Made Applications -> Hungarian*, or right-click *Applications* in the GATE Resources tree):

* Magyarlánc Morphparse (Sentence Splitter and Tokenizer + Pos Tagger and Lemmatizer)
* Magyarlánc Depparse (Morphparse + Depdendency Parser)
* NP chunking with Huntag3 and Magyarlánc MorphParse

Plase see [this Wiki page](https://github.com/dlt-rilmta/hunlp-GATE/wiki/Hungarian-NLP-Tools-GATE-Integraion) for more information on what tools are expected to be integrated and their statuses.

##Files

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

##Plugin command-line installation (for developers)

If you have rebuilt the plugin, it is also possible to install it to your GATE user plugin directory with the
following command:

```
make local_install GATE_USER_PLUGINS_DIR=/your/gate/user/plugin/directory
```

This will copy the whole directory tree under `Lang_Hungarian/` from this repository to your GATE user plugin directory. Alternatively, you can also make a symbolic link using the following command:

```
make link_devdir GATE_USER_PLUGINS_DIR=/your/gate/user/plugin/directory
```

##Updating the plugin repository

To update the plugin repository  hosted at `http://corpus.nytud.hu/GATE`,
run `make upload` specifying your user name on `corpus.nytud.hu`:

```
make upload CORPUSUSER=yourusername
```

This will upload your local `hungarian.jar`, `creole.xml` and `resources` directory
to the update server.
