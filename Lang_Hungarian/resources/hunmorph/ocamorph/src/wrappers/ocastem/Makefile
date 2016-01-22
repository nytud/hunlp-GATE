OCAMLMAKEFILE=../../OCamlMakefile
SOURCES=ocastem.ml
LIBS=ocamorph
INCDIRS=../../lib/base ../../lib
RESULT=ocastem
all: nc 

install: nc
	cp ocastem /usr/bin/

uninstall:
	rm -f /usr/bin/ocastem

include $(OCAMLMAKEFILE)
