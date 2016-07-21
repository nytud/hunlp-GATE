# hunlp-GATE

Sources for the **Lang_Hungarian** [GATE](http://gate.ac.uk/) plugin containing Hungarian processing resources (wrappers around already existing Hungarian NLP tools) developed by the [Department of Language Technology](http://www.nytud.hu/oszt/nyte/index.html) at [RIL-MTA](http://www.nytud.hu/).

Developers: Márton Miháltz, Péter Kundráth, Bálint Sass, Mátyás Gerőcs

##Contents

The plugin contains the following GATE **Processing Resources**.
Firstly, the **Lang_Hungarian** toolchain:

* QunToken Hungarian tokenizer and sentence splitter (Linux)
* HFST morphological analyzer and lemmatizer
* Magyarlánc Hungarian POS Tagger and Lemmatizer
* Magyarlánc Hungarian Dependency Parser
* Magyarlánc Hungarian Constituency Parser
* Preverb Identifier Tool
* Hungarian NP chunking with Huntag3 (Linux)
* Hungarian NER with Huntag3 (Linux)
* IOB2Annotation Converter Tool

Some older tools are also integrated:

* Hunpos Hungarian PoS-tagger (Linux)
* HunMorph Hungarian morphological analyzer (Linux, OSX)
* Magyarlánc Hungarian Sentence Splitter and Tokenizer
* Magyarlánc Hungarian Morphological Analyzer [KR code]
* Magyarlánc Hungarian Morphological Analyzer And Guesser [MSD code]

XXX You will also find the following **ready made applications** in GATE Developer (to access, in the menu click *File -> Ready Made Applications -> Hungarian*, or right-click *Applications* in the GATE Resources tree):

* Magyarlánc Morphparse (Sentence Splitter and Tokenizer + Pos Tagger and Lemmatizer)
* Magyarlánc Depparse (Morphparse + Depdendency Parser)
* NP chunking with Huntag3 and Magyarlánc MorphParse

XXX Please see [this Wiki page](https://github.com/dlt-rilmta/hunlp-GATE/wiki/Hungarian-NLP-Tools-GATE-Integration) for more information on what tools are expected to be integrated and their statuses.

##Installing under GATE Developer

**Requirements:**

* Java runtime (JRE or JDK) version 1.8 or later
* GATE Developer 8.0 or later
* For best results (safely loading the Magyarlánc Depparse application) you will need a **64-bit operating system** with **64-bit Java** installed. Please use the following command to launch GATE Developer with request to 2GB of heap space (Linux, OS X):


 ```
 <your_GATE_Developer_path>\bin\gate.sh -Xmx2G
 ```

###Method 1 (for users):

This is the default recommended install method for users.
Only GATE Developer and internet access are required.

Follow these steps to install the plugin directly into GATE Developer using the ready-made online GATE plugin repository hosted at `corpus.nytud.hu` (*Note: the whole plugin complete with model files requires 1GB of space and may take a couple of minutes to download*):

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
(If you have already installed the plugin earlier, check the "Available Updates" tab for a newer version.)
10. You should now see **Lang_Hungarian** in the list of plugins available to install. Enable the checkbox left to its name in column "Install".
11. Click on the "Apply All" button to install the plugin. 
12. You should now see **Lang_Hungarian** in the list of installed plugins on the "Installed Plugins" tab.
13. Enable the "Load Now" checkbox for **Lang_Hungarian** and click "Apply All" to load the plugin. Several new PRs become available right clicking "Processing Resources" on the left hand side panel and selecting "New".
14. Now, open a terminal and issue the `sh xperm.sh` command in `Lang_Hungarian` directory under you GATE User Plugin Directory to add necessary execute permissions.
15. If you want to use the *Huntag3*-based processing resources (NER, NP chunking) you need to run `Lang_Hungarian/resources/huntag3/setup_linux.sh` (Ubuntu, Debian Linux) to install required dependencies (superuser privileges will be required).

###Method 2 (for developers):

This method gives more control over the installation process,
it uses a clone of this github repository.

1. Clone this git repository to your machine.
(*Optional: first build the plugin (see __Building the Lang_Hungarian plugin__), or just use the version already included in the repository.*)
2. Obtain all necessary resources not included in this repository
by running `complete.sh` (on Linux) *or* obtain these resources one by one:
  * to use *HFST* morphological analyser, see the corresponding [README.md](https://github.com/dlt-rilmta/hunlp-GATE/tree/master/Lang_Hungarian/resources/hfst) about obtaining binaries
  * to use *Magyarlánc*, see the corresponding [README.md](https://github.com/dlt-rilmta/hunlp-GATE/tree/master/Lang_Hungarian/resources/magyarlanc) about obtaining binaries
  * to use *Huntag3* (NP chunking, NER):
    * Run `Lang_Hungarian/resources/huntag3/setup_linux.sh` (on Ubuntu or Debian Linux) to install required dependencies for Huntag3 (superuser privileges required).
    * See the [README.md](https://github.com/dlt-rilmta/hunlp-GATE/tree/master/Lang_Hungarian/resources/huntag3/models) for obtaining trained models for Huntag3.  
3. Copy the whole directory `Lang_Hungarian` into your GATE user plugin directory (see __Plugin command-line installation__).
4. Restart GATE Developer. You should now see **Lang_Hungarian** in the list of installed plugins.
   If it's not there, check if your user plugin directory is set (see steps 2-4. in __Method 1__ above).

##Files

* `Lang_Hungarian`: directory tree for the Lang_Hungarian GATE plugin
 * `src`: Java sources of the included Processing Resources. See Javadocs for details.
 * `resources`: non-Java binaries, sources and resources files for the included tools
 * `hungarian.jar`: plugin Java binaries in a jar file
 * `build.xml`: use this to build the jar from sources using Apache Ant
 * `creole.xml`: this tells GATE how to use hungarian.jar as a CREOLE plugin
 * `.classpath`, `.project`: use these to import project into Eclipse Java IDE
* `Makefile`: use to rebuild, install etc. the plugin from command line

##Building the Lang_Hungarian plugin (for developers)

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

##Updating the GATE plugin repository (only for maintainers)

To update the GATE plugin repository hosted at `http://corpus.nytud.hu/GATE`,
first be sure that you have a fully functional plugin (see __Method 2__),
and then run `make upload` specifying your user name on `corpus.nytud.hu`:

```
make upload CORPUSUSER=yourusername
```

This will upload your local `hungarian.jar`, `creole.xml` and `resources` directory to the update server.
This enables users to use __Method 1__ for installation.

##Using or embedding the Lang_Hungarian plugin as a client-server system (for power users)

The **Lang_Hungarian** [GATE](http://gate.ac.uk/) Processing Resources can be run not just from the GATE GUI (called GATE Developer) but from Linux command line using GATE Embedded technology.

It is recommended to use the so called 
[gate-server](https://github.com/dlt-rilmta/hunlp-GATE/tree/master/gate-server)
which is an optimized solution for running GATE
Processing Resources.

##Using the Lang_Hungarian plugin from the command line (for power users)

The secondary option to use the **Lang_Hungarian** [GATE](http://gate.ac.uk/) Processing Resources from Linux command line is the simple method described here.

###Preparation

1. A working GATE installation *and* a clone of this github repository is needed.
2. Obtain all necessary resources not included in this repository (see step 2. in __Method 2__ above).

###What is it?

This functionality which is implemented in `Pipeline.java` means
that any combination of PRs in the **Lang_Hungarian plugin** can be run
with arbitrary parameter settings.

###How to use?

Just type:

```
make GATE_HOME=/your/gate/installation/dir pipeline
```

By default `texts/peldak.txt` is used as input file,
but it can be changed using the `PIPELINE_INPUT` parameter
to e.g. the XML version of the default input file:

```
make GATE_HOME=/your/gate/installation/dir PIPELINE_INPUT=texts/peldak.xml pipeline
```

###Configuration

The PRs to be run should be specified in a config file.
Lines of this config file should contain either only the name of a PR:

```
hu.nytud.gate.parsers.MagyarlancDependencyParser
```

... or the name of PR together with some parameters for this PR given as
`parameterName parameterValue` in the following format:

```
hu.nytud.gate.parsers.MagyarlancDependencyParser addPosTags true addMorphFeatures true
```

The default config file is
`Lang_Hungarian/resources/pipeline/pipeline.config`
which runs the full `Lang_Hungarian` plugin
and can be overridden using the `CONFIG` parameter:

```
make GATE_HOME=/your/gate/installation/dir CONFIG=/path/to/config/file pipeline
```

There are some ready-made config files in the
`Lang_Hungarian/resources/pipeline` directory
for some usage scenarios.

##Others

XXX how to get the transducer file for HFST

XXX tsvconverter.py

