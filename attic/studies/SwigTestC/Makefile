JAVAPATH=/usr/jdk/

stub:
	swig -java test.i
	gcc -Wall -fPIC -c test.c test_wrap.c -I$(JAVAPATH)/include -I$(JAVAPATH)/include/linux
	g++ -Wall -shared test.o test_wrap.o -Wl,-soname,libtest.so -o libtest.so

javatest:
	javac -Xdiags:verbose *.java
	java -Djava.library.path="." TestTest

clean:
	rm -f *.o *.class