TEST=test.txt

all:
	make OCAMORPH=ocamorph_debug BINRES=morphdb_hu.bin bm 
	make OCAMORPH=ocamorph_noassert BINRES=morphdb_hu.bin bm 
	make OCAMORPH=ocamorph_debug BINRES=morphdb_hu.min.bin bm 
	make OCAMORPH=ocamorph_noassert BINRES=morphdb_hu.min.bin bm 

bm:
	time cat $(TEST) | $(OCAMORPH) --bin $(BINRES) > $(OCAMORPH).$(BINRES).out