%option noyywrap

/* hun_sentbreak - hosszú mondatok tördelése */
/* 2003 (c) Németh László <nemethl@gyorsposta.hu> */

/* Makródefiníciók */

/* szóköz, vagy új sor karakter */
SPACE [ \n]

        #define SLEN 10000
%%

        /* nagyon hosszú mondatok törése */
        
"<s>"[^\n]* {
	if (yyleng > SLEN) {
            char * s = yytext;
            char r;
            int i;
            for (i = 0; i < yyleng / SLEN; i++) {
                r = s[SLEN];
                s[SLEN] = '\0';
                fprintf(stderr,"%s\n</s>\n<s>",s);
                s[SLEN] = r;
                s += SLEN;
            }
            fprintf(stderr,"%s",s);
        } else {
            printf("%s", yytext);
        }
}
