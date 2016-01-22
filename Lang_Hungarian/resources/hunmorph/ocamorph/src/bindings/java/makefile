all: output/libocamorph.so

output/ocamorph.o:  ../../lib/analysis.ml
	cd ../../lib ; ocamlopt -output-obj -o ocamorph.o utils.ml bv.ml zip_list.ml  trie.ml  stringTrie.ml         tag.ml         parser_common.ml         constraint.ml minimize.ml        dic_parser.ml   dic_lexer.ml        aff_parser.ml  aff_lexer.ml analysis.ml ; cd ../bindings/java ; mv ../../lib/ocamorph.o output/

## see http://www.mindspring.com/~david.dagon/jni/Native.txt
OCAMORPH_HOME=../../lib
JAVAINCLUDE=/usr/lib/j2sdk1.5-sun/include

output/bme_mokk_hunmorph_HunmorphStub.h: output/bme/mokk/hunmorph/HunmorphStub.class
	javah -d output  -classpath output/ -jni bme.mokk.hunmorph.HunmorphStub

output/libocamorph.so: src/c/hunmorph_jnistub.c output/bme_mokk_hunmorph_HunmorphStub.h ../c/ocamorph_stubs.c output/ocamorph.o
	cc -shared -o output/libocamorph.so -Ioutput/ -I../ -I /usr/local/lib/ocaml/ -I../c/ -L/usr/local/lib/ocaml/  -I /usr/lib/j2sdk1.5-sun/include -I /usr/lib/j2sdk1.5-sun/include/linux/  output/ocamorph.o src/c/hunmorph_jnistub.c ../c/ocamorph_stubs.c -lunix -lasmrun


output/bme/mokk/hunmorph/HunmorphStub.class: src/java/bme/mokk/hunmorph/HunmorphStub.java
	javac -d output src/java/bme/mokk/hunmorph/HunmorphStub.java

clean:
	rm -rf output/*  
test:
	java -Djava.library.path=./output/ -cp output bme.mokk.hunmorph.HunmorphStub morphdb_hu.bin
