%option reentrant
%option noyywrap

/* hun_sentclean - mondatra bontó szûrõ kimenetét alakítja tovább */
/* 2003 (c) Németh László <nemethl@gyorsposta.hu> */

/* Makródefiníciók */

/* szóköz, vagy új sor karakter */
SPACE [ \n]

%%

	/* paragrafusok beillesztése */
	
"\n</s>\n\n" {
	fprintf(yyout, "</s>\n  </p>\n  <p>\n");
}

"</s>\n\n" {
	fprintf(yyout, "\n</s>\n  </p>\n  <p>\n");
}

	/* új sorok szóközre történõ cseréje */

"\n" {
	fprintf(yyout, " ");
}

	/* törés mondathatárnál */

"</s>"{SPACE}* {
	fprintf(yyout, "\n</s>\n");
}
