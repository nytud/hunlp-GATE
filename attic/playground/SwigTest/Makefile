JAVAPATH=/usr/jdk/

stub:
	swig -c++ -java test.i
	g++ -fPIC -c test.cpp test_wrap.cxx -I$(JAVAPATH)/include -I$(JAVAPATH)/include/linux
	g++ -shared test.o test_wrap.o -Wl,-soname,libtest.so -o libtest.so

javatest:
	javac -Xdiags:verbose *.java
	java -Djava.library.path="." TestTest

