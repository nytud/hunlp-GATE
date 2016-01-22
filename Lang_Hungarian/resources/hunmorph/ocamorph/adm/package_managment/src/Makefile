all:	package

BASE=../../..
SOURCES=${BASE}/src ${BASE}/README ${BASE}/Makefile ${BASE}/README.ocastem
package:
	rm -rf ocamorph
	mkdir  ocamorph
	cp -r ${SOURCES} ocamorph
	tar -czf ocamorph-src.tgz ocamorph
	rm -rf ocamorph 


