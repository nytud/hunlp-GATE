# OS name
UNAME:=$(shell uname -s)

# GATE installation directory
GATE_HOME=/home/$(USER)/GATE_Developer
ifeq ($(UNAME), Darwin)
GATE_HOME=/Users/$(USER)/GATE_Developer/
endif
# TODO set on Windows

# GATE User plugin directory
#GATE_USER_PLUGINS_DIR=`grep ' gate.user.plugins="' ~/.gate.xml | cut -d "=" -f 2 | sed 's/"//g'`
GATE_USER_PLUGINS_DIR=/home/$(USER)/GATE_plugins
ifeq ($(UNAME), Darwin)
GATE_USER_PLUGINS_DIR=/Users/$(USER)/GATE_plugins
endif
# TODO set on Windows

# userid on corpus.nytud.hu used for uploading, see target "upload"
CORPUSUSER=your_user_name

.PHONY: build prepare_upload upload local_install link_devdir runtest

# Build the GATE CREOLE plugin "Lang_Hungarian" in ./Lang_Hungarian/
build:
	cd Lang_Hungarian ; ant

# Prepare upload_dir for target upload
prepare_upload:
	rm -rf upload_dir
	mkdir -p upload_dir/Lang_Hungarian
	cp -p -l Lang_Hungarian/hungarian.jar upload_dir/Lang_Hungarian
	cp -p -l Lang_Hungarian/creole.xml upload_dir/Lang_Hungarian
	cp -p -l Lang_Hungarian/*.gapp upload_dir/Lang_Hungarian
	mkdir -p upload_dir/Lang_Hungarian/resources
	cp -p -r -l Lang_Hungarian/resources/gate_plugins upload_dir/Lang_Hungarian/resources/
	cp -p -r -l Lang_Hungarian/resources/hunmorph upload_dir/Lang_Hungarian/resources/
	cp -p -r -l Lang_Hungarian/resources/hunpos upload_dir/Lang_Hungarian/resources/
	cp -p -r -l Lang_Hungarian/resources/huntag3 upload_dir/Lang_Hungarian/resources/
	cp -p -r -l Lang_Hungarian/resources/magyarlanc upload_dir/Lang_Hungarian/resources/
	cp -p -r -l Lang_Hungarian/resources/quntoken upload_dir/Lang_Hungarian/resources/
	cp -p -r -l Lang_Hungarian/resources/hfst upload_dir/Lang_Hungarian/resources/
	cd upload_dir ; zip -r Lang_Hungarian.zip Lang_Hungarian/*
	cp -p -l update-site/gate-update-site.xml upload_dir

# Upload the Lang_Hungarian plugin to the plugin repository at corpus.nytud.hu/GATE/
# Invoke with your own username on corpus.nytud.hu:
# make upload CORPUSUSER=mylogin
upload:
	@echo 
	@echo "wait a minute while rsync is working..."
	@echo 
	rsync -vRr upload_dir/./gate-update-site.xml upload_dir/./Lang_Hungarian.zip upload_dir/./Lang_Hungarian/* $(CORPUSUSER)@corpus.nytud.hu:/var/www/GATE/ > rsync.out 2> rsync.err
	@echo 
	@echo "see rsync.out and rsync.err whether everything was successful..."
	@echo "if it was, run 'make clean_upload' finally"
	@echo 

# Run 'make clean_upload' after 'make upload' step was successful :)
clean_upload:
	rm -rf upload_dir rsync.out rsync.err

# Install Lang_Hungarian locally to user's GATE user plugin directory
local_install:
	@echo "Your GATE user plugin directory appears to be: $(GATE_USER_PLUGINS_DIR)"
	rm -rf "$(GATE_USER_PLUGINS_DIR)/Lang_Hungarian"
	mkdir -p "$(GATE_USER_PLUGINS_DIR)/Lang_Hungarian"
	cp Lang_Hungarian/hungarian.jar "$(GATE_USER_PLUGINS_DIR)/Lang_Hungarian/"
	cp Lang_Hungarian/creole.xml "$(GATE_USER_PLUGINS_DIR)/Lang_Hungarian/"
	cp Lang_Hungarian/*.gapp "$(GATE_USER_PLUGINS_DIR)/Lang_Hungarian/"
	cp -r Lang_Hungarian/resources "$(GATE_USER_PLUGINS_DIR)/Lang_Hungarian/"
	rm -rf $(GATE_USER_PLUGINS_DIR)/Lang_Hungarian/resources/dummyctokenizer/src/
	echo INSTALLATION SUCCESSFUL
# TODO: delete (don't copy) all src files under resources

# Create symbolic link to ./Lang_Hungarian in gate user plugin directory
link_devdir:
	rm -rf "$(GATE_USER_PLUGINS_DIR)/Lang_Hungarian"
	ln -s `pwd`/Lang_Hungarian $(GATE_USER_PLUGINS_DIR)/Lang_Hungarian

# Run command-line test
RTCP=Lang_Hungarian/hungarian.jar:Lang_Hungarian/resources/magyarlanc/magyarlanc-2.0.1.jar:Lang_Hungarian/resources/gate_plugins/Tagger_Framework/TaggerFramework.jar:$(GATE_HOME)/bin/gate.jar:$(GATE_HOME)/lib/*
runtest:
	java -cp $(RTCP) hu.nytud.gate.testing.PRTest

# Run command-line pipeline
RTCP=Lang_Hungarian/hungarian.jar:Lang_Hungarian/resources/magyarlanc/magyarlanc-2.0.1.jar:Lang_Hungarian/resources/gate_plugins/Tagger_Framework/TaggerFramework.jar:$(GATE_HOME)/bin/gate.jar:$(GATE_HOME)/lib/*
PIPELINE_INPUT=texts/peldak.txt
pipeline:
	@java -cp $(RTCP) hu.nytud.gate.pipeline.Pipeline < $(PIPELINE_INPUT)

