# userid on corpus.nytud.hu used for uploading, see target "upload"
CORPUSUSER=your_user_name

.PHONY: build upload

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
	mkdir -p upload_dir/Lang_Hungarian/resources/magyarlanc
	cp -p -r Lang_Hungarian/resources/magyarlanc upload_dir/Lang_Hungarian/resources/
	cd upload_dir ; zip -r Lang_Hungarian.zip Lang_Hungarian/*
	cp -p update-site/gate-update-site.xml upload_dir
	rsync -vRr upload_dir/./gate-update-site.xml upload_dir/./Lang_Hungarian.zip upload_dir/./Lang_Hungarian/* $(CORPUSUSER)@corpus.nytud.hu:/var/www/GATE/
	rm -rf upload_dir
