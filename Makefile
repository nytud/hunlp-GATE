# userid on corpus.nytud.hu used for uploading, see target "build"
UPLOAD_USER=marci

.PHONY: build upload

# Build the GATE CREOLE plugin "Lang_Hungarian" to ./build/Lang_Hungarian/
build:
#	TODO: build hungarian.jar from Java projects and copy to ./build/Lang_Hungarian/
	cp ./creole/creole.xml ./build/Lang_Hungarian

# Upload the Lang_Hungarian plugin to the plugin repository at corpus.nytud.hu/GATE/
upload:
	rm -f ./build/Lang_Hungarian.zip
	cd ./build/ ; \
	zip Lang_Hungarian.zip Lang_Hungarian/*
	rsync -vR ./creole/./gate-update-site.xml ./build/./Lang_Hungarian.zip ./build/./Lang_Hungarian/* $(UPLOAD_USER)@corpus.nytud.hu:/var/www/GATE/
#	rsync -vR ./build/./Lang_Hungarian/* $(UPDATE_USER)@corpus.nytud.hu:/var/www/GATE/
