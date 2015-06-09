#JAVA_HOME=/usr/jdk/
#JAVA_HOME=/usr/
#JAVA_HOME=/cygdrive/c/Program\ Files/Java/jdk1.8.0_45/
JAVA_HOME=/cygdrive/c/Program\ Files\ \(x86\)/Java/jdk1.8.0_45/

.PHONY: stub all so jar javatest ctest clean cleanall

all:

# Generate C and JAVA files for JNI wrapping. Needs SWIG installed (version 3.0.5 was used)
# TODO: generate tokenizer_wrap.c into src/c/Linux or src/c/Windows
stub:
	mkdir -p src/java/hu/rilmta/gate/tokenizers/dummyctokenizer
	swig -outdir src/java/hu/rilmta/gate/tokenizers/dummyctokenizer -java -package hu.rilmta.gate.tokenizers.dummyctokenizer src/c/tokenizer.i

# Create Linux shared library for embedding in JAVA via JNI
so:
	cd src/c ; \
	gcc -fPIC -c tokenizer.c tokenizer_wrap.c -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux
	g++ -shared src/c/tokenizer.o src/c/tokenizer_wrap.o -Wl,-soname,libdummyctokenizer.so -o lib/libdummyctokenizer.so

# Create 32-bit Windows shared library for JNI (requires 32-bit cygwin; using DLL will require cygwin1.dll)
dll_win32:
	cd src/c ; \
	gcc -c tokenizer.c -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/win32
	cd src/c/Windows ; \
	gcc -c tokenizer_wrap.c -I../ -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/win32
	gcc -shared -Wl,--add-stdcall-alias -Wl,--export-all-symbols -Wl,--enable-auto-import \
            -o lib/cygdummyctokenizer32.dll src/c/tokenizer.o src/c/Windows/tokenizer_wrap.o
#           -Wl,--whole-archive,src/c/tokenizer.o,src/c/Windows/tokenizer_wrap.o \


# Create .jar file to be used by GATE
jar:
	rm -f -r bin/*
	mkdir -p bin
	$(JAVA_HOME)/bin/javac -d bin/ src/java/hu/rilmta/gate/tokenizers/dummyctokenizer/*.java
	$(JAVA_HOME)/bin/jar -cvf lib/dummyctokenizerwrapper.jar -C bin/ .

# Run JAVA test (Linux)
javatest: lib/libdummyctokenizer.so lib/dummyctokenizerwrapper.jar bin/TestDummyCTokenizerWrapper.class
	cd bin ; \
	$(JAVA_HOME)/bin/java TestDummyCTokenizerWrapper
#	$(JAVA_HOME)/bin/java -Djava.library.path="../lib/" -cp .:../lib/dummyctokenizerwrapper.jar TestDummyCTokenizerWrapper

# Run JAVA test (Win32)
javatest_win32: lib/cygdummyctokenizer32.dll lib/dummyctokenizerwrapper.jar bin/TestDummyCTokenizerWrapper.class
	cd bin ; \
	$(JAVA_HOME)/bin/java -Djava.library.path="..\lib" -cp .;..\lib\dummyctokenizerwrapper.jar TestDummyCTokenizerWrapper

# Create binary for JAVA test
bin/TestDummyCTokenizerWrapper.class: src/java/TestDummyCTokenizerWrapper.java
	$(JAVA_HOME)/bin/javac -g -cp lib/dummyctokenizerwrapper.jar -d bin src/java/TestDummyCTokenizerWrapper.java

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


