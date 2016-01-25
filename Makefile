# GATE installation directory
export GATE_HOME=/home/$(USER)/GATE_Developer

# userid on corpus.nytud.hu used for uploading, see target "upload"
CORPUSUSER=your_user_name

.PHONY: build upload local_install runtest

# Build the GATE CREOLE plugin "Lang_Hungarian" in ./Lang_Hungarian/
build:
	cd Lang_Hungarian ; ant

# Upload the Lang_Hungarian plugin to the plugin repository at corpus.nytud.hu/GATE/
# Invoke with your own username on corpus.nytud.hu:
# make upload CORPUSUSER=mylogin
upload:
	mkdir -p upload_dir/Lang_Hungarian
	cp -p Lang_Hungarian/hungarian.jar upload_dir/Lang_Hungarian
	cp -p Lang_Hungarian/creole.xml upload_dir/Lang_Hungarian
	cp -p Lang_Hungarian/*.gapp upload_dir/Lang_Hungarian
	mkdir -p upload_dir/Lang_Hungarian/resources/dummyctokenizer
	cp -p -r Lang_Hungarian/resources/dummyctokenizer upload_dir/Lang_Hungarian/resources/
	mkdir -p upload_dir/Lang_Hungarian/resources/magyarlanc
	cp -p -r Lang_Hungarian/resources/magyarlanc upload_dir/Lang_Hungarian/resources/
	cd upload_dir ; zip -r Lang_Hungarian.zip Lang_Hungarian/*
	cp -p update-site/gate-update-site.xml upload_dir
	rsync -vRr upload_dir/./gate-update-site.xml upload_dir/./Lang_Hungarian.zip upload_dir/./Lang_Hungarian/* $(CORPUSUSER)@corpus.nytud.hu:/var/www/GATE/
	rm -rf upload_dir


# Install Lang_Hungarian locally to user's GATE user plugin directory
#GATE_USER_PLUGINS_DIR=`grep ' gate.user.plugins="' ~/.gate.xml | cut -d "=" -f 2 | sed 's/"//g'`
GATE_USER_PLUGINS_DIR=/home/mm/GATE_plugins
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


# Run command-line test
RTCP=Lang_Hungarian/hungarian.jar:Lang_Hungarian/resources/magyarlanc/magyarlanc-2.0.1.jar:Lang_Hungarian/resources/gate_plugins/Tagger_Framework/TaggerFramework.jar:$(GATE_HOME)/bin/gate.jar:$(GATE_HOME)/lib/*
runtest:
	java -cp $(RTCP) hu.nytud.gate.testing.PRTest
