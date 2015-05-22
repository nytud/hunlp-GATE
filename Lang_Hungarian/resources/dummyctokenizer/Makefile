#JAVAPATH=/usr/jdk/
JAVAPATH=/usr/

.PHONY: stub all so jar javatest ctest clean cleanall

all:

# Generate C and JAVA files for JNI wrapping. Needs SWIG installed (version 3.0.5 was used)
stub:
	mkdir -p src/java/hu/rilmta/gate/tokenizers/dummyctokenizer
	swig -outdir src/java/hu/rilmta/gate/tokenizers/dummyctokenizer -java -package hu.rilmta.gate.tokenizers.dummyctokenizer src/c/tokenizer.i

# Create shared library for embedding in JAVA via JNI
so:
	cd src/c ; \
	gcc -fPIC -c tokenizer.c tokenizer_wrap.c -I$(JAVAPATH)/include -I$(JAVAPATH)/include/linux
	g++ -shared src/c/tokenizer.o src/c/tokenizer_wrap.o -Wl,-soname,libdummyctokenizer.so -o lib/libdummyctokenizer.so

# Create .jar file to be used by GATE
jar:
	rm -f -r bin/*
	mkdir -p bin
	$(JAVAPATH)/bin/javac -d bin/ src/java/hu/rilmta/gate/tokenizers/dummyctokenizer/*.java
	$(JAVAPATH)/bin/jar -cvf lib/dummyctokenizerwrapper.jar -C bin/ .

# Run JAVA test
javatest: lib/libdummyctokenizer.so lib/dummyctokenizerwrapper.jar bin/TestDummyCTokenizerWrapper.class
	cd bin ; \
	java -Djava.library.path="../lib/" -cp .:../lib/dummyctokenizerwrapper.jar TestDummyCTokenizerWrapper

# Create binary for JAVA test
bin/TestDummyCTokenizerWrapper.class: src/java/TestDummyCTokenizerWrapper.java
	javac -g -cp lib/dummyctokenizerwrapper.jar -d bin src/java/TestDummyCTokenizerWrapper.java

# Run C test
ctest: bin/test_tokenizer
	bin/test_tokenizer

# Create binary for C test
bin/test_tokenizer: src/c/test_tokenizer.c src/c/tokenizer.c
	gcc src/c/test_tokenizer.c src/c/tokenizer.c -o bin/test_tokenizer

# Delete intermediate binary files
clean:
	rm -f -r bin/*
	rm -f src/c/*.o

# Delete all generated files
cleanall: clean
	rm -f src/c/tokenizer_wrap.c
	rm -f -r src/java/hu/*
	rm -f lib/*


