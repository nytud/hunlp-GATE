lex.tokens.c: tokens.lex
	flex -Ptokens --header-file=lex.tokens.h tokens.lex

lex.sentences.c: sentences.lex
	flex -Psentences --header-file=lex.sentences.h sentences.lex

test_tokenizer: test_tokenizer.c tokenizer.c tokenizer.h lex.tokens.c lex.sentences.c
	gcc -g test_tokenizer.c tokenizer.c lex.sentences.c lex.tokens.c -o test_tokenizer

memcheck_test_tokenizer: test_tokenizer
	valgrind --tool=memcheck --leak-check=yes ./test_tokenizer

tokenizer_singleton: tokenizer_singleton.c lex.sentences.c lex.tokens.c
	gcc tokenizer_singleton.c lex.sentences.c lex.tokens.c -o tokenizer_singleton

test_strctest:
	gcc -g strctest.c -o strctest
	valgrind --tool=memcheck --leak-check=yes ./strctest
