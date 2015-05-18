JAVAPATH=/usr/jdk/

.PHONY: stub lib javatest jartest clean

bin/test_tokenizer: test_tokenizer.c tokenizer.c
	mkdir -p bin
	gcc test_tokenizer.c tokenizer.c -o bin/test_tokenizer

stub:
	mkdir -p ./hu/rilmta/gate/tokenizers/dummyctokenizer/
	swig -outdir ./hu/rilmta/gate/tokenizers/dummyctokenizer/ -java -package hu.rilmta.gate.tokenizers.dummyctokenizer tokenizer.i

lib:
	mkdir -p lib
	gcc -fPIC -c tokenizer.c tokenizer_wrap.c -I$(JAVAPATH)/include -I$(JAVAPATH)/include/linux
	g++ -shared tokenizer.o tokenizer_wrap.o -Wl,-soname,libdummyctokenizer.so -o lib/libdummyctokenizer.so

javatest:
	mkdir -p bin
	javac -Xdiags:verbose -d bin/ ./hu/rilmta/gate/tokenizers/dummyctokenizer/*.java
	javac -Xdiags:verbose -d bin/ -cp bin/ ./TestDummyCTokenizerWrapper.java
	java -cp bin/ -Djava.library.path="./lib/" TestDummyCTokenizerWrapper

jartest:
	mkdir -p bin
	mkdir -p lib
	javac -Xdiags:verbose -d bin/ ./hu/rilmta/gate/tokenizers/dummyctokenizer/*.java
	$(JAVAPATH)/bin/jar -cvf lib/dummyctokenizerwrapper.jar -C bin/ .
	javac -Xdiags:verbose -d bin/ -cp bin/ ./TestDummyCTokenizerWrapper.java
	java  -Djava.library.path="lib/" -cp bin/:lib/dummyctokenizerwrapper.jar TestDummyCTokenizerWrapper

clean:
	rm -f -r bin/*
	rm -f -r lib/*
	rm -f *.o

