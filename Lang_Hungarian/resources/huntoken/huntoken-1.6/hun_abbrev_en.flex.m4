%option noyywrap

/* hun_abbrev - mondatok összevonása a valószínûleg hibásan megállapított mondathatároknál */
/* 2003 (c) Németh László <nemethl@gyorsposta.hu> */

%s NUMBEGIN

/* nagybetû és paragrafusjel */

UPPER [A-ZÁÉÍÓÖÕÚÜÛ§¡£¥¦©ª«¬®¯ÀÂÃÄÅÆÇÈÊËÌÎÏÐÑÒÔØÙÝÞ]

/* nem szókarakter */
NONWORDCHAR ([^a-zA-ZáéíóöõúüûÁÉÍÓÖÕÚÜÛ\-.§%°0-9¡£¥¦©ª«¬®¯±³µ¶¹º»¼¾¿ÀÂÃÄÅÆÇÈÊËÌÎÏÐÑÒÔØÙÝÞßàâãäåæçèêëìîïðñòôøùýþ])

/* szóköz, vagy új sor karakter */
SPACE [ \n]

/* mondathatár */
BOUNDARY ".</s>"{SPACE}"<s>"

WPL ".</s>"{SPACE}"<s>"

/* rövidítések makró (külso állományból töltodik be) */

ABBREV (M4_MACRO_ABBREV)
%%

	/* Sorszámok, ha mondatkezdõk */

	/* BE: <s>25.</s> <s>Magyarország */

	/* KI: <s>25. Magyarország */

	/* BE: <s>25.</s> <s>§ */

	/* KI: <s>25. § */

"<s>"[0-9]+ { ECHO; BEGIN(NUMBEGIN); }

<NUMBEGIN>{BOUNDARY}{UPPER} {
	print_abbrev(yytext);
}

<NUMBEGIN>. {
	ECHO;
	BEGIN(INITIAL);
}

	/* zárójelezett dátum */
	
	/* BE: <s>A 2002.</s> <s>(IV. 25.)</s> <s> */

	/* KI: <s>A 2002. (IV. 25.)  */

[0-9]+{BOUNDARY}"("([VX]?"I"{1,3}|"I"?[VX])"."({SPACE}[0-9]+".")?")</s>"{SPACE}"<s>" {
	print_abbrev(yytext);
}

	/* ügyiratszám + dátum */

	/* BE: <s>A 25/2002.</s> <s>(IV.</s> <s>25.)</s> <s> */

	/* KI: <s>A 25/2002. (IV. 25.)  */

	/* BE: <s>A 25/2002.</s> <s>(IV. 25.)</s> <s> */

	/* KI: <s>A 25/2002. (IV. 25.)  */

{SPACE}[0-9]+"/"[0-9]+{BOUNDARY}"("([VX]?"I"{1,3}|"I"?[VX])".</s>"({SPACE}"<s>"[0-9]+".")?")</s>"{SPACE}"<s>" {
 	print_abbrev(yytext);
}

{SPACE}[0-9]+"/"[0-9]+{BOUNDARY}"("([VX]?"I"{1,3}|"I"?[VX])"."({SPACE}[0-9]+".")?")</s>"{SPACE}"<s>" {
 	print_abbrev(yytext);
}

	/* ügyiratszám + dátum II. */

	/* BE: <s>A 25/2002.</s> <s>(IV.</s> <s>25.) */

	/* KI: <s>A 25/2002. (IV. 25.) */

{SPACE}[0-9]+"/"[0-9]+{BOUNDARY}"("([VX]?"I"{1,3}|"I"?[VX])".</s>"{SPACE}"<s>" {
 	print_abbrev(yytext);
}

	/* ügyiratszám + dátum III. */

	/* BE: <s>A 25/2002.</s> <s>(IV.25.) */

	/* KI: <s>A 25/2002. (IV.25.) */

{SPACE}[0-9]+"/"[0-9]+{BOUNDARY}"("([VX]?"I"{1,3}|"I"?[VX])"."([0-9]+".")?")" {
 	print_abbrev(yytext);
}

	/* paragrafusjel, és sorszámot követo szám, vagy dátum római számmal */

	/* BE: <s>A 25.</s> <s>§ szerint 2002.</s> <s>IV. havában. */

	/* KI: <s>A 25. § szerint 2002. IV. havában. */

[0-9]+{BOUNDARY}([§0-9]|[VX]?"I"{1,3}"."|"I"?[VX]".") {
	print_abbrev(yytext);
}

[0-9]+{BOUNDARY}([VX]?"I"{1,3}|"I"?[VX]){BOUNDARY}[0-9] {
	print_abbrev(yytext);
}


	/* Monogramok (B. Jenõ) */
	
	/* BE: B.</s> <s>Jenõ. */

	/* KI: B. Jenõ. */

	/* BE: A.</s> <s>E.</s> <s>X.</s> <s>Wilson. */

	/* KI: A. E. X. Wilson. */

{NONWORDCHAR}({ABBREV}{BOUNDARY}|{UPPER}{BOUNDARY})+ {
	print_abbrev(yytext);
}

	/* Római számok (VI. Lajos), kivéve CD. */

	/* BE: XIV.</s> <s>Lajos. */

	/* KI: XIV. Lajos. */

	/* BE: Ott a CD.</s> <s>Hallod? */

	/* KI: Ott a CD.</s> <s>Hallod? */

{NONWORDCHAR}"CD"{BOUNDARY} { ECHO; }  /* robusztus */

{NONWORDCHAR}[IVXLCMD]+{BOUNDARY} { print_abbrev(yytext); }  /* robusztus */


	/* Pl. esetében mindig megszüntetjük a mondathatárt. */

	/* BE: Pl.</s> <s>Péter és Marcsa pl.</s> <s>25 fánkot is ehetne. */
	
	/* KI: Pl. Péter és Marcsa pl. 25 fánkot is ehetne. */

{NONWORDCHAR}?[Pp]"l"{BOUNDARY} { print_abbrev(yytext); }  /* l., L. */

	/* Gyakori rövidítések */

	/* BE: In apr.</s> <s>I was at Sci.</s> <s>Corp.</s> <s>Mexico. */
	
	/* KI: In apr. I was at Sci. Corp. Mexico. */

	/* {NONWORDCHAR}{ABBREV}{BOUNDARY} { print_abbrev(yytext); } */

	/* ,,stb.'' esetében nem vonunk egybe. (A macska, kutya stb. Az elsõ... */

	/* tokenizálás után mondatvégirövidítés-javítás */

"<w>CD\n</w>\n<c>.</c>" {
        ECHO;
}

"<w>"({ABBREV}|[A-Z]|[IVXLCMD]+)"\n</w>\n<c>.</c>" {
	strcpy(yytext + (yyleng - 14), ".\n</w>");
	printf("%s", yytext);
}

	/* hun_sentclean kiegészítése */

"\n\n</s>" {
	printf("\n</s>");
}

%%
/* 
 * <s> és </s> címkék törlése, és kiírás
 */
int print_abbrev(const char * s)
{
	char buff[8192];
	int i, j = 0;
	for (i = 0; i < strlen(s); i++) {
		if (strncmp(s+i, "<s>", 3) == 0) i+=2;
		else if (strncmp(s+i, "</s>", 4) == 0) i+=3;
		else { 
			buff[j] = s[i];
			j++;
		}
	}
	buff[j] = '\0';
	printf("%s",buff);
}
