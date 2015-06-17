lex.reentrant2.c: reentrant2.lex
	flex -Preentrant2 --header-file=reentrant2.h reentrant2.lex

reentrant2: lex.reentrant2.c reentrant2_main.c
	gcc lex.reentrant2.c reentrant2_main.c -o reentrant2

reentrant2b: lex.reentrant2.c reentrant2_main-b.c
	gcc lex.reentrant2.c reentrant2_main-b.c -o reentrant2b

reentrant2c: lex.reentrant2.c reentrant2_main-c.c
	gcc lex.reentrant2.c reentrant2_main-c.c -o reentrant2c