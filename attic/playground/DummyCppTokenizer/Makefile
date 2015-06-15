#JAVAPATH=/usr/lib/jvm/java-7-openjdk-i386
JAVAPATH=/usr/jdk/

test_tokenizer: test_tokenizer.cpp tokenizer.cpp
	g++ test_tokenizer.cpp tokenizer.cpp -o test_tokenizer

stub:
	swig -c++ -java tokenizer.i
	g++ -fPIC -c tokenizer.cpp tokenizer_wrap.cxx -I$(JAVAPATH)/include -I$(JAVAPATH)/include/linux
	g++ -shared tokenizer.o tokenizer_wrap.o -Wl,-soname,libtokenizer.so -o libtokenizer.so

javatest:
	javac -Xdiags:verbose DummyTokenizerCppTest.java  SWIGTYPE_p_std__string.java  SWIGTYPE_p_std__vectorT_std__pairT_long_long_t_t.java  Tokenizer.java  TokenizerJNI.java
	java -Djava.library.path="." DummyTokenizerCppTest
#:/home/mm/Infra2/hunlp-GATE/DummyCppTokenizer" DummyTokenizerCppTest
#	java DummyTokenizerCppTest

