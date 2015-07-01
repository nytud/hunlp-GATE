%option reentrant
%option noyywrap

/* hun_clean - karaktereket törlõ, illetve átalakító szûrõ */
/* 2003 (c) Németh László <nemethl@gyorsposta.hu> */

/* Makródefiníciók */

/* Szóköz értékû szekvenciák: szóköz, tabulátor, nem törõ szóköz. */

/* Az &nbsp; entitást, és a ,,kocsi vissza'' karaktert is felvesszük ide */

SPACE ([ 	\240]|"&nbsp"";"?|"\r")

/* Új sor értékû szekvenciák */

/* A kocsi vissza karaktert az MS-DOS szövegek redundáns kódolása miatt */
/* nem új sor, hanem szóköz értékû karakternek kódoltuk. */

/* \n: új sor */
/* \f: lapdobás */
/* \v: függõleges tabulátor */

NEWLINE [\n\f\v]

%%

	/* bekezdéshatárok */
	/* bemenet: legalább két sortörés + szóközök */
	/* kimenet: két sortörés */

	/* A következõkkel nem foglalkozunk: */
	/* XXX Az &nbspvalami szekvenciákból is töröljük az &nbsp-t */

	/* BE: Bekezdéshatárok. A sorokban tabulátorok és szóközök:	 */
	/* BE: 		 */
	/* BE:           */
	/* BE: 	   Vége. */

	/* KI: Bekezdéshatárok. A sorokban tabulátorok és szóközök: */
	/* KI:  */
	/* KI: Vége. */

{SPACE}*{NEWLINE}({SPACE}*{NEWLINE})+{SPACE}* {
		fprintf(yyout, "\n\n");
	}

	/* sortörés */
	/* bemenet: egy sortörés sorvégi és következõ sor eleji szóközökkel */
	/* kimenet: egy sortörés */

	/* BE: Sortörés. Szóköz és tabulátor: 	 */
	/* BE: 		 Itt is a sor elején. */

	/* KI: Sortörés. Szóköz és tabulátor: */
	/* KI: Itt is a sor elején. */

{SPACE}*{NEWLINE}{SPACE}* {
		fprintf(yyout, "\n");
	}

	/* elsõ sor elején lévõ szóközök törlése */
^{SPACE}*

	/* szóközsorozat és nem törõ szóközök lecserélése  egy szóközre */
	/* bemenet: több szóköz, vagyis SPACE szekvencia egymás után */
	/* kimenet: egy szóköz */

	/* BE: Szóközsorozat. Több	     	szóköz és   	  tabulátor. */

	/* KI: Szóközsorozat. Több szóköz és tabulátor. */

	/* BE: Nem törõ szóköz: Vége. */

	/* KI: Nem törõ szóköz: Vége. */

{SPACE}* {
		fprintf(yyout, " ");
	}

	/* < és > átkódolása */
	/* bemenet: <, vagy > */
	/* kimenet: &lt; vagy &gt; */

	/* BE: <> */

	/* KI: &lt;&gt; */
	
"<" {
	fprintf(yyout, "&lt;");
}

">" {
	fprintf(yyout, "&gt;");
}

	/* Windows karakterkódok egy részének átalakítása */

"&#128"";"?|[\200] { fprintf(yyout, "&euro;"); }
"&#130"";"?|[\202] { fprintf(yyout, "&lsquor;"); } /* HTML 4.0-ban sbquo; */
"&#132"";"?|[\204] { fprintf(yyout, "&ldquor;"); }  /* HTML 4.0-ban bdquo; */
"&#133"";"?|[\205] { fprintf(yyout, "..."); }

"&#139"";"?|[\213] { fprintf(yyout, "\""); }

"&#145"";"?|[\221] { fprintf(yyout, "&lsquo;"); } /* 6 */
"&#146"";"?|[\222] { fprintf(yyout, "&rsquo;"); } /* 9 */
"&#147"";"?|[\223] { fprintf(yyout, "&ldquo;"); } /* 66 */
"&#148"";"?|[\224] { fprintf(yyout, "&rdquo;"); } /* 99 */

"&#150"";"?|[\226] { fprintf(yyout, "&ndash;"); } /* -- */
"&#151"";"?|[\227] { fprintf(yyout, "&mdash;"); } /* --- */

"&#153"";"?|[\231] { fprintf(yyout, "&trade;"); } /* TM */

	/* decimális karakterkód */
	/* feltesszük, hogy az input latin-2, és a 256-nál kisebb kódok
	/* is erre vonatkoznak. */

	/* bemenet: "&" max. 5 jegyû szám és esetleg pontosvesszõ */
	/* kimenet: latin-2 karakter, vagy a változatlan kód, illetve */
	/* ha a szám a < és > karakterek kódja -> &lt; és &gt; entitás */
	/* átkódolásra kerülnek az UNICODE nagykötõjelek, és idézõjelek is */

	/* BE: (EEC&#041; (EEC&#041;	&#107	&#60;	&#337;	&#3456;	&#2343235325; */

	/* KI: (EEC) (EEC) k &lt; õ &#3456; &#2343235325; */

"&#"[0-9]{1,5}";"? {
	// decimális karakterkódok kezelése
	// nem latin-1 esetében csak a magyar õÕûÛ
	int i;
	i = atoi(yytext + 2);
	if (i == 38) fprintf(yyout, "&amp;"); // & jel
	else if (i == 60) fprintf(yyout, "&lt;"); // < jel
	else if (i == 62) fprintf(yyout, "&gt;"); // > jel
	else if (i < 256) fprintf(yyout, "%c", i); // latin-1
	else if (i == 337) fprintf(yyout, "õ");
	else if (i == 336) fprintf(yyout, "Õ");
	else if (i == 369) fprintf(yyout, "û");
	else if (i == 368) fprintf(yyout, "Û");
	// ismeretlen kódot nem változtatjuk
	else ECHO;
}

		/* Ha hosszabb, mint 5 karakter a szám, nem változtatunk */
"&#"[0-9]{6,}";"? {
	ECHO;
}

	/* hexadecimális karakterkód */
	/* XXX most nem foglalkozunk ezzel (ritka) */

	/* BE: &#xF456 &#Xabc; &#xABCDEF; */

	/* KI: &#xF456 &#Xabc; &#xABCDEF; */

"&#"[xX][0-9a-fA-F]+";"? {
	ECHO;
}

	/* XXX speciális és szimbólum HTML 4.0 entitásokkal nem foglalkoztunk még */

	/* kivéve a 3 pont, és a &quot; átalakítását */

	/* BE: &hellip; */

	/* KI: ... */

"&hellip"";"? { fprintf(yyout, "..."); }

"&quot"";"? { fprintf(yyout, "\""); }

	/* magyar idézõjelek: HTML 4.0 -> SGML  */

	/* BE: &bdquo; */

	/* KI: &ldquor; */

"&bdquo"";"? { fprintf(yyout, "&ldquor;"); }

"&sbquo"";"? { fprintf(yyout, "&lsquor;"); }

	/* latin-1 HTML entitások részleges átalakítása (most még latin-2-re) */
	/* átalakítás ott történik, ahol a latin-2 megegyezik a latin-1-gyel */

	/* BE: &aacute; &Aacute; &otilde; &Otilde; &ucirc; &Ucirc; &ndash; &mdash; */

	/* KI: á Á õ Õ û Û &ndash; &mdash; */

	/* "&nbsp"";"? { fprintf(yyout, "%c",(char) 160); } */ // ez már lecserélve */

	/* "&iexcl"";"? { fprintf(yyout, "%c",(char) 161); } */ // fordított felkiáltójel

	/* "&cent"";"? { fprintf(yyout, "%c",(char) 162); } */
	/* "&pound"";"? { fprintf(yyout, "%c",(char) 163); } */

"&curren"";"? { fprintf(yyout, "%c",(char) 164); }

	/* "&yen"";"? { fprintf(yyout, "%c",(char) 165); } */
	/* "&brvbar"";"? { fprintf(yyout, "%c",(char) 166); } */
	
"&sect"";"? { fprintf(yyout, "%c",(char) 167); }

	/* "&uml"";"? { fprintf(yyout, "%c",(char) 168); } */
	/* "&copy"";"? { fprintf(yyout, "%c",(char) 169); } */
	/* "&ordf"";"? { fprintf(yyout, "%c",(char) 170); } */
	/* "&laquo"";"? { fprintf(yyout, "%c",(char) 171); } */
	/* "&not"";"? { fprintf(yyout, "%c",(char) 172); } */

	/* BE: &shy&shy; */

	/* KI:  */

"&shy"";"? // elválasztási hely jelének (feltételes v. lágy kötõjel) törlése

	/* "&reg"";"? { fprintf(yyout, "%c",(char) 174); } */
	/* "&macr"";"? { fprintf(yyout, "%c",(char) 175); } */

	/* BE: &deg; */

	/* KI: ° */

"&deg"";"? { fprintf(yyout, "%c",(char) 176); } // fokjel, megegyezik latin-2-vel

	/* "&plusmn"";"? { fprintf(yyout, "%c",(char) 177); } */
	/* "&sup2"";"? { fprintf(yyout, "%c",(char) 178); } */
	/* "&sup3"";"? { fprintf(yyout, "%c",(char) 179); } */

	/* BE: &acute; */

	/* KI: ´ */

"&acute"";"? { fprintf(yyout, "%c",(char) 180); } // vesszõ ékezet, megegyezik a latin-2-vel

	/* "&micro"";"? { fprintf(yyout, "%c",(char) 181); } */
	/* "&para"";"? { fprintf(yyout, "%c",(char) 182); } */
	/* "&middot"";"? { fprintf(yyout, "%c",(char) 183); } */

	/* BE: &cedil; */

	/* KI: ¸ */

"&cedil"";"? { fprintf(yyout, "%c",(char) 184); } // cedilla ékezet, megegyezik a latin-2-vel

	/* "&sup1"";"? { fprintf(yyout, "%c",(char) 185); } */
	/* "&ordm"";"? { fprintf(yyout, "%c",(char) 186); } */
	/* "&raquo"";"? { fprintf(yyout, "%c",(char) 187); } */
	/* "&frac14"";"? { fprintf(yyout, "%c",(char) 188); } */
	/* "&frac12"";"? { fprintf(yyout, "%c",(char) 189); } */
	/* "&frac34"";"? { fprintf(yyout, "%c",(char) 190); } */
	/* "&iquest"";"? { fprintf(yyout, "%c",(char) 191); } */
	/* "&Agrave"";"? { fprintf(yyout, "%c",(char) 192); } */ // betû

	/* BE: &Aacute; */

	/* KI: Á */

"&Aacute"";"? { fprintf(yyout, "%c",(char) 193); }

	/* BE: &Acirc; */

	/* KI: Â */

"&Acirc"";"? { fprintf(yyout, "%c",(char) 194); }

	/* "&Atilde"";"? { fprintf(yyout, "%c",(char) 195); } // betû

	/* BE: &Auml; */

	/* KI: Ä */

"&Auml"";"? { fprintf(yyout, "%c",(char) 196); }

	/* "&Aring"";"? { fprintf(yyout, "%c",(char) 197); } */ // betû
	/* "&AElig"";"? { fprintf(yyout, "%c",(char) 198); } */ // betû

	/* BE: &Ccedil; */

	/* KI: Ç */

"&Ccedil"";"? { fprintf(yyout, "%c",(char) 199); }

	/* "&Egrave"";"? { fprintf(yyout, "%c",(char) 200); } */ // betû

	/* BE: &Eacute; */

	/* KI: É */

"&Eacute"";"? { fprintf(yyout, "%c",(char) 201); }

	/* "&Ecirc"";"? { fprintf(yyout, "%c",(char) 202); } */ // betû

	/* BE: &Euml; */

	/* KI: Ë */

"&Euml"";"? { fprintf(yyout, "%c",(char) 203); }

	/* "&Igrave"";"? { fprintf(yyout, "%c",(char) 204); } */ // betû

	/* BE: &Iacute; */

	/* KI: Í */

"&Iacute"";"? { fprintf(yyout, "%c",(char) 205); }

	/* BE: &Icirc; */

	/* KI: Î */

"&Icirc"";"? { fprintf(yyout, "%c",(char) 206); }

	/* "&Iuml"";"? { fprintf(yyout, "%c",(char) 207); } */ // betû
	/* "&ETH"";"? { fprintf(yyout, "%c",(char) 208); } */ // betû
	/* "&Ntilde"";"? { fprintf(yyout, "%c",(char) 209); } */ // betû
	/* "&Ograve"";"? { fprintf(yyout, "%c",(char) 210); } */ // betû

	/* BE: &Oacute; */

	/* KI: Ó */

"&Oacute"";"? { fprintf(yyout, "%c",(char) 211); }

	/* BE: &Ocirc; */

	/* KI: Ô */

"&Ocirc"";"? { fprintf(yyout, "%c",(char) 212); }

	/* XXX Tipikus tévesztés: hullámvonalas O javítása Õ-re */

	/* BE: &Otilde; */

	/* KI: Õ */

"&Otilde"";"? { fprintf(yyout, "%c",(char) 213); }

	/* BE: &Ouml; */

	/* KI: Ö */

"&Ouml"";"? { fprintf(yyout, "%c",(char) 214); }

	/* BE: &times; */

	/* KI: × */

"&times"";"? { fprintf(yyout, "%c",(char) 215); }

	/* "&Oslash"";"? { fprintf(yyout, "%c",(char) 216); } */ // betû
	/* "&Ugrave"";"? { fprintf(yyout, "%c",(char) 217); } */ // betû

	/* BE: &Uacute; */

	/* KI: Ú */

"&Uacute"";"? { fprintf(yyout, "%c",(char) 218); }

	/* XXX Tipikus tévesztés: pontos U javítása Û-re */

	/* BE: &Ucirc; */

	/* KI: Û */

"&Ucirc"";"? { fprintf(yyout, "%c",(char) 219); }

	/* BE: &Uuml; */

	/* KI: Ü */

"&Uuml"";"? { fprintf(yyout, "%c",(char) 220); }

	/* BE: &Yacute; */

	/* KI: Ý */

"&Yacute"";"? { fprintf(yyout, "%c",(char) 221); }

	/* "&THORN"";"? { fprintf(yyout, "%c",(char) 222); } */ // betû

	/* BE: &szlig; */

	/* KI: ß */

"&szlig"";"? { fprintf(yyout, "%c",(char) 223); }

	/* "&agrave"";"? { fprintf(yyout, "%c",(char) 224); } */ // betû

	/* BE: &aacute; */

	/* KI: á */

"&aacute"";"? { fprintf(yyout, "%c",(char) 225); }

	/* BE: &acirc; */

	/* KI: â */

"&acirc"";"? { fprintf(yyout, "%c",(char) 226); }

	/* "&atilde"";"? { fprintf(yyout, "%c",(char) 227); } */ // betû

	/* BE: &auml; */

	/* KI: ä */

"&auml"";"? { fprintf(yyout, "%c",(char) 228); }

	/* "&aring"";"? { fprintf(yyout, "%c",(char) 229); } */ // betû
	/* "&aelig"";"? { fprintf(yyout, "%c",(char) 230); } */ // betû

	/* BE: &ccedil; */

	/* KI: ç */

"&ccedil"";"? { fprintf(yyout, "%c",(char) 231); }

	/* "&egrave"";"? { fprintf(yyout, "%c",(char) 232); } */ // betû

	/* BE: &eacute; */

	/* KI: é */

"&eacute"";"? { fprintf(yyout, "%c",(char) 233); }

	/* "&ecirc"";"? { fprintf(yyout, "%c",(char) 234); } */ // betû

	/* BE: &euml; */

	/* KI: ë */

"&euml"";"? { fprintf(yyout, "%c",(char) 235); }

	/* "&igrave"";"? { fprintf(yyout, "%c",(char) 236); } */ // betû

	/* BE: &iacute; */

	/* KI: í */

"&iacute"";"? { fprintf(yyout, "%c",(char) 237); }

	/* BE: &icirc; */

	/* KI: î */

"&icirc"";"? { fprintf(yyout, "%c",(char) 238); }

	/* "&iuml"";"? { fprintf(yyout, "%c",(char) 239); } */ // betû
	/* "&eth"";"? { fprintf(yyout, "%c",(char) 240); } */ // betû
	/* "&ntilde"";"? { fprintf(yyout, "%c",(char) 241); } */ // betû
	/* "&ograve"";"? { fprintf(yyout, "%c",(char) 242); } */ // betû

	/* BE: &oacute; */

	/* KI: ó */

"&oacute"";"? { fprintf(yyout, "%c",(char) 243); }

	/* BE: &ocirc; */

	/* KI: ô */

"&ocirc"";"? { fprintf(yyout, "%c",(char) 244); }

	/* XXX Tipikus tévesztés: hullámvonalas o javítása õ-re */

	/* BE: &otilde; */

	/* KI: õ */

"&otilde"";"? { fprintf(yyout, "%c",(char) 245); }

	/* BE: &ouml; */

	/* KI: ö */

"&ouml"";"? { fprintf(yyout, "%c",(char) 246); }

	/* BE: &divide; */

	/* KI: ÷ */

"&divide"";"? { fprintf(yyout, "%c",(char) 247); }

	/* "&oslash"";"? { fprintf(yyout, "%c",(char) 248); } */ // betû
	/* "&ugrave"";"? { fprintf(yyout, "%c",(char) 249); } */ // betû

	/* BE: &uacute; */

	/* KI: ú */

"&uacute"";"? { fprintf(yyout, "%c",(char) 250); }

	/* BE: &ucirc; */

	/* KI: û */

	/* XXX Tipikus tévesztés: pontos u javítása û-re */

"&ucirc"";"? { fprintf(yyout, "%c",(char) 251); }

	/* BE: &uuml; */

	/* KI: ü */

"&uuml"";"? { fprintf(yyout, "%c",(char) 252); }

	/* BE: &yacute; */

	/* KI: ý */

"&yacute"";"? { fprintf(yyout, "%c",(char) 253); }

	/* "&thorn"";"? { fprintf(yyout, "%c",(char) 254); } */ // betû
	/* "&yuml"";"? { fprintf(yyout, "%c",(char) 255); } */ // betû
