OCAMLMAKEFILE = ../OCamlMakefile

SOURCES=utils.ml \
        bv.ml \
        zip_list.ml \
        trie.ml \
        stringTrie.ml \
        tag.ml  \
        parser_common.ml \
        constraint.ml   \
        minimize.ml \
        dic_parser.mly \
        dic_lexer.mll \
        aff_parser.mly \
        aff_lexer.mll \
        analysis.ml

RESULT  = ocamorph
LIBINSTALL_FILES = *.cmi *.cma *.cmo *.cmx *.cmxa *.a *.o
all: native-code-library byte-code-library

install:	libinstall
uninstall:	libuninstall

clean::  clean-doc

include $(OCAMLMAKEFILE)

