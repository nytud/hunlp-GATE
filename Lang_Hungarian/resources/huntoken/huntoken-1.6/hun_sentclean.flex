%option noyywrap

/* hun_sentclean - mondatra bontó szûrõ kimenetét alakítja tovább */
/* 2003 (c) Németh László <nemethl@gyorsposta.hu> */

/* Makródefiníciók */

/* szóköz, vagy új sor karakter */
SPACE [ \n]

%%

	/* paragrafusok beillesztése */
	
"\n</s>\n\n" {
	printf("</s>\n  </p>\n  <p>\n");
}

"</s>\n\n" {
	printf("\n</s>\n  </p>\n  <p>\n");
}

	/* új sorok szóközre történõ cseréje */

"\n" {
	printf(" ");
}

	/* törés mondathatárnál */

"</s>"{SPACE}* {
	printf("\n</s>\n");
}
