OCAMLMAKEFILE=../../OCamlMakefile

SOURCES=ocamorph.ml
LIBS=ocamorph
#LIBDIRS=../../lib 
INCDIRS=../../lib/base ../../lib
RESULT=ocamorph
all: nc 

install:  nc
	cp -f ocamorph /usr/bin/

uninstall:
	rm -f /usr/bin/ocamorph

include $(OCAMLMAKEFILE)
