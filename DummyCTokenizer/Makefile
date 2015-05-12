JAVAPATH=/usr/jdk/

test_tokenizer: test_tokenizer.c tokenizer.c
	gcc test_tokenizer.c tokenizer.c -o test_tokenizer

stub:
	swig -java tokenizer.i

libtokenizer.so:
	gcc -fPIC -c tokenizer.c tokenizer_wrap.c -I$(JAVAPATH)/include -I$(JAVAPATH)/include/linux
	g++ -shared tokenizer.o tokenizer_wrap.o -Wl,-soname,libtokenizer.so -o libtokenizer.so

javatest:
	javac -Xdiags:verbose *.java
	java -Djava.library.path="." TestDummyCTokenizer

clean:
	rm -f *.o *.class *.so
	rm -f DummyCTokenizer.java DummyCTokenizerJNI.java  OffsPairArray.java  OffsPair.java test_tokenizer

