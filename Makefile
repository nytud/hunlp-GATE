# userid on corpus.nytud.hu used for uploading, see target "build"
CORPUSUSER=yourusername

.PHONY: build upload

# Build the GATE CREOLE plugin "Lang_Hungarian" to ./build/Lang_Hungarian/
build:
#	TODO: build hungarian.jar from Java projects and copy to ./build/Lang_Hungarian/
	cp ./creole/creole.xml ./build/Lang_Hungarian

# Upload the Lang_Hungarian plugin to the plugin repository at corpus.nytud.hu/GATE/
# Invoke with your own username on corpus.nytud.hu: make upload CORPUSUSER=mylogin
upload:
	rm -f ./build/Lang_Hungarian.zip
	cd ./build/ ; \
	zip Lang_Hungarian.zip Lang_Hungarian/*
	rsync -vR ./creole/./gate-update-site.xml ./build/./Lang_Hungarian.zip ./build/./Lang_Hungarian/* $(CORPUSUSER)@corpus.nytud.hu:/var/www/GATE/
