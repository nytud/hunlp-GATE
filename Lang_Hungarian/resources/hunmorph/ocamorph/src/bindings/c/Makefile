all: cmorph

ocamorph.o:  ../../lib/analysis.ml
	cd ../../lib ; ocamlopt -output-obj -o ocamorph.o utils.ml bv.ml zip_list.ml  trie.ml  stringTrie.ml         tag.ml         parser_common.ml         constraint.ml minimize.ml        dic_parser.ml   dic_lexer.ml        aff_parser.ml  aff_lexer.ml analysis.ml ; cd ../bindings/c ; mv ../../lib/ocamorph.o .

cmorph:  ocamorph.o cmorph.c ocamorph_stubs.c
#	cc -o cmorph -I . -I /usr/local/lib/ocaml/ -L/usr/local/lib/ocaml/ ocamorph.o ocamorph_stubs.c cmorph.c -lunix -lasmrun -lcurses -lm
	cc -o cmorph -I . -I /usr/lib/ocaml/ -L/usr/lib/ocaml/ ocamorph.o ocamorph_stubs.c cmorph.c -lunix -lasmrun -lcurses -lm


clean:
	rm ocamorph.o cmorph


# added by MM
test_cmorph: cmorph
	iconv -f UTF-8 -t ISO-8859-2 | ./cmorph ../../../../../hunmorph/morphdb.hu/morphdb_hu.bin | iconv -f ISO-8859-2 -t UTF-8
