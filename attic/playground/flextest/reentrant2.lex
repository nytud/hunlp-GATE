/* This scanner prints "//" comments. */

%option reentrant stack noyywrap
%x COMMENT

%%

"//"                 yy_push_state( COMMENT, yyscanner);
.|\n

<COMMENT>\n          yy_pop_state( yyscanner );
<COMMENT>[^\n]+      fprintf( yyout, "%s\n", yytext);

%%

