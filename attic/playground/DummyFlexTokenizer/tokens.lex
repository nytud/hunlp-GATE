/* Tokenize: replace spaces with newlines */

%option reentrant noyywrap

%%

[ ]+    { fprintf( yyout, "\n"); }

%%
