wget -r -l 128 --no-parent --reject "index.html*" "http://corpus.nytud.hu/gate-resources/Lang_Hungarian/resources"
cp -rp corpus.nytud.hu/gate-resources/Lang_Hungarian/resources/* Lang_Hungarian/resources

# ez hekkparádé, mert a wget nem őrzi meg a permission-öket
chmod +x Lang_Hungarian/resources/hfst/linux/x64/hfst-lookup
chmod +x Lang_Hungarian/resources/hfst/linux/x64/hfst-optimized-lookup

rm -rf corpus.nytud.hu
