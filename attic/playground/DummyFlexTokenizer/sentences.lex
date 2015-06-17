/* Split sentences: add newlines before and after . or ? or ! */

%option reentrant noyywrap

%%

[\.\?!]+    { fprintf( yyout, "\n%s\n", yytext); }

%%
