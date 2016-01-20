OCAMLOPT_BIN=ocamlopt 

OCAMLOPT_LIBS=str.cmxa
OCAMLOPT_ARGS= -I ../util -I ../hunpos -output-obj

SOURCES_UTIL = amap.ml mfhash.ml  parse.ml vocab.mli vocab.ml
SOURCES_HUNPOS= io.ml lexicon.ml  linear_interpolated_lm.ml   morphtable.ml  ngram.ml  special_tokens.ml  suffix_guesser.ml  viterbi.ml hmm_tagger.ml 

SOURCE_WRAPPER_ML=hunposcaml.ml
SOURCE_WRAPPER_C=hunposwrap.c

SOURCES=$(addprefix ../util/,${SOURCES_UTIL}) $(addprefix ../hunpos/,${SOURCES_HUNPOS}) ${SOURCE_WRAPPER_ML} 
OCAMLC_WHERE=`ocamlc -where`

all: test

hunpos.o: ${SOURCES}
	${OCAMLOPT_BIN} ${OCAMLOPT_ARGS} ${OCAMLOPT_LIBS}  ${SOURCES} -o hunpos.o
	
test: hunpos.o hunposwrap.c test.c
	cc -o test -I. -I${OCAMLC_WHERE} -L${OCAMLC_WHERE} ${SOURCE_WRAPPER_C}  hunpos.o  test.c  -lunix -lasmrun -lcurses -lm -lstr
	
clean:
	rm *.o *.cmi *.cmx


# added by MM
test_test:
	iconv -f UTF-8 -t ISO-8859-2 | ./test ../../hu_szeged_kr.model /dev/null | iconv -f ISO-8859-2 -t UTF-8
