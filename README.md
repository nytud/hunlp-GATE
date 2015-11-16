# Lang_Hungarian

Pre-release #1 (2015-11-16)

This is a public pre-release of the **Lang_Hungarian** [GATE](http://gate.ac.uk/) plugin developed by RIL-MTA.

The plugin contains the following GATE Processing Resources:

* Magyarlánc Hungarian Sentence Splitter
* Magyarlánc Hungarian Tokenizer
* Magyarlánc Hungarian Sentence Splitter and Tokenizer
* Magyarlánc Hungarian Morphological Analyzer (KR)
* Magyarlánc Hungarian Morphological Analyzer And Guesser (MSD)
* Magyarlánc Hungarian POS Tagger and Lemmatizer
* Magyarlánc Hungarian Dependency Parser

You will also find the following ready made applications in GATE Developer (to access, in the menu click *File* -> *Ready Made Applications* -> *Hungarian*, or right-click *Applications* in the GATE Resources tree):

* Magyaránc Morphparse
* Magyarlánc Depparse 

Source files are included.

Development of more processing resources and ready made applications is under way. For more information, see https://github.com/dlt-rilmta/hunlp-GATE

##Installing under GATE Developer

**Requirements:**

* Java runtime (JRE or JDK) version 1.8 or later
* GATE Developer 8.0 or later
* For best results (safely loading the Magyarlánc Depparse application) you will need a **64-bit** operating system with 64-bit Java installed. Please use the following command to launch GATE Developer with request to 2GB of heap space (Linux, OS X):

 ```
 <your_GATE_Developer_path>\bin\gate.sh -Xmx2G
 ```

**Installation:** follow these steps to install the plugin directly in GATE Developer using the online plugin repository hosted at `corpus.nytud.hu`:

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
