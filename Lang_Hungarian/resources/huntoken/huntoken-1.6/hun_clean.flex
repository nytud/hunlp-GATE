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
		printf("\n\n");
	}

	/* sortörés */
	/* bemenet: egy sortörés sorvégi és következõ sor eleji szóközökkel */
	/* kimenet: egy sortörés */

	/* BE: Sortörés. Szóköz és tabulátor: 	 */
	/* BE: 		 Itt is a sor elején. */

	/* KI: Sortörés. Szóköz és tabulátor: */
	/* KI: Itt is a sor elején. */

{SPACE}*{NEWLINE}{SPACE}* {
		printf("\n");
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
		printf(" ");
	}

	/* < és > átkódolása */
	/* bemenet: <, vagy > */
	/* kimenet: &lt; vagy &gt; */

	/* BE: <> */

	/* KI: &lt;&gt; */
	
"<" {
	printf("&lt;");
}

">" {
	printf("&gt;");
}

	/* Windows karakterkódok egy részének átalakítása */

"&#128"";"?|[\200] { printf("&euro;"); }
"&#130"";"?|[\202] { printf("&lsquor;"); } /* HTML 4.0-ban sbquo; */
"&#132"";"?|[\204] { printf("&ldquor;"); }  /* HTML 4.0-ban bdquo; */
"&#133"";"?|[\205] { printf("..."); }

"&#139"";"?|[\213] { printf("\""); }

"&#145"";"?|[\221] { printf("&lsquo;"); } /* 6 */
"&#146"";"?|[\222] { printf("&rsquo;"); } /* 9 */
"&#147"";"?|[\223] { printf("&ldquo;"); } /* 66 */
"&#148"";"?|[\224] { printf("&rdquo;"); } /* 99 */

"&#150"";"?|[\226] { printf("&ndash;"); } /* -- */
"&#151"";"?|[\227] { printf("&mdash;"); } /* --- */

"&#153"";"?|[\231] { printf("&trade;"); } /* TM */

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
	if (i == 38) printf("&amp;"); // & jel
	else if (i == 60) printf("&lt;"); // < jel
	else if (i == 62) printf("&gt;"); // > jel
	else if (i < 256) printf("%c", i); // latin-1
	else if (i == 337) printf("õ");
	else if (i == 336) printf("Õ");
	else if (i == 369) printf("û");
	else if (i == 368) printf("Û");
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

"&hellip"";"? { printf("..."); }

"&quot"";"? { printf("\""); }

	/* magyar idézõjelek: HTML 4.0 -> SGML  */

	/* BE: &bdquo; */

	/* KI: &ldquor; */

"&bdquo"";"? { printf("&ldquor;"); }

"&sbquo"";"? { printf("&lsquor;"); }

	/* latin-1 HTML entitások részleges átalakítása (most még latin-2-re) */
	/* átalakítás ott történik, ahol a latin-2 megegyezik a latin-1-gyel */

	/* BE: &aacute; &Aacute; &otilde; &Otilde; &ucirc; &Ucirc; &ndash; &mdash; */

	/* KI: á Á õ Õ û Û &ndash; &mdash; */

	/* "&nbsp"";"? { printf("%c",(char) 160); } */ // ez már lecserélve */

	/* "&iexcl"";"? { printf("%c",(char) 161); } */ // fordított felkiáltójel

	/* "&cent"";"? { printf("%c",(char) 162); } */
	/* "&pound"";"? { printf("%c",(char) 163); } */

"&curren"";"? { printf("%c",(char) 164); }

	/* "&yen"";"? { printf("%c",(char) 165); } */
	/* "&brvbar"";"? { printf("%c",(char) 166); } */
	
"&sect"";"? { printf("%c",(char) 167); }

	/* "&uml"";"? { printf("%c",(char) 168); } */
	/* "&copy"";"? { printf("%c",(char) 169); } */
	/* "&ordf"";"? { printf("%c",(char) 170); } */
	/* "&laquo"";"? { printf("%c",(char) 171); } */
	/* "&not"";"? { printf("%c",(char) 172); } */

	/* BE: &shy&shy; */

	/* KI:  */

"&shy"";"? // elválasztási hely jelének (feltételes v. lágy kötõjel) törlése

	/* "&reg"";"? { printf("%c",(char) 174); } */
	/* "&macr"";"? { printf("%c",(char) 175); } */

	/* BE: &deg; */

	/* KI: ° */

"&deg"";"? { printf("%c",(char) 176); } // fokjel, megegyezik latin-2-vel

	/* "&plusmn"";"? { printf("%c",(char) 177); } */
	/* "&sup2"";"? { printf("%c",(char) 178); } */
	/* "&sup3"";"? { printf("%c",(char) 179); } */

	/* BE: &acute; */

	/* KI: ´ */

"&acute"";"? { printf("%c",(char) 180); } // vesszõ ékezet, megegyezik a latin-2-vel

	/* "&micro"";"? { printf("%c",(char) 181); } */
	/* "&para"";"? { printf("%c",(char) 182); } */
	/* "&middot"";"? { printf("%c",(char) 183); } */

	/* BE: &cedil; */

	/* KI: ¸ */

"&cedil"";"? { printf("%c",(char) 184); } // cedilla ékezet, megegyezik a latin-2-vel

	/* "&sup1"";"? { printf("%c",(char) 185); } */
	/* "&ordm"";"? { printf("%c",(char) 186); } */
	/* "&raquo"";"? { printf("%c",(char) 187); } */
	/* "&frac14"";"? { printf("%c",(char) 188); } */
	/* "&frac12"";"? { printf("%c",(char) 189); } */
	/* "&frac34"";"? { printf("%c",(char) 190); } */
	/* "&iquest"";"? { printf("%c",(char) 191); } */
	/* "&Agrave"";"? { printf("%c",(char) 192); } */ // betû

	/* BE: &Aacute; */

	/* KI: Á */

"&Aacute"";"? { printf("%c",(char) 193); }

	/* BE: &Acirc; */

	/* KI: Â */

"&Acirc"";"? { printf("%c",(char) 194); }

	/* "&Atilde"";"? { printf("%c",(char) 195); } // betû

	/* BE: &Auml; */

	/* KI: Ä */

"&Auml"";"? { printf("%c",(char) 196); }

	/* "&Aring"";"? { printf("%c",(char) 197); } */ // betû
	/* "&AElig"";"? { printf("%c",(char) 198); } */ // betû

	/* BE: &Ccedil; */

	/* KI: Ç */

"&Ccedil"";"? { printf("%c",(char) 199); }

	/* "&Egrave"";"? { printf("%c",(char) 200); } */ // betû

	/* BE: &Eacute; */

	/* KI: É */

"&Eacute"";"? { printf("%c",(char) 201); }

	/* "&Ecirc"";"? { printf("%c",(char) 202); } */ // betû

	/* BE: &Euml; */

	/* KI: Ë */

"&Euml"";"? { printf("%c",(char) 203); }

	/* "&Igrave"";"? { printf("%c",(char) 204); } */ // betû

	/* BE: &Iacute; */

	/* KI: Í */

"&Iacute"";"? { printf("%c",(char) 205); }

	/* BE: &Icirc; */

	/* KI: Î */

"&Icirc"";"? { printf("%c",(char) 206); }

	/* "&Iuml"";"? { printf("%c",(char) 207); } */ // betû
	/* "&ETH"";"? { printf("%c",(char) 208); } */ // betû
	/* "&Ntilde"";"? { printf("%c",(char) 209); } */ // betû
	/* "&Ograve"";"? { printf("%c",(char) 210); } */ // betû

	/* BE: &Oacute; */

	/* KI: Ó */

"&Oacute"";"? { printf("%c",(char) 211); }

	/* BE: &Ocirc; */

	/* KI: Ô */

"&Ocirc"";"? { printf("%c",(char) 212); }

	/* XXX Tipikus tévesztés: hullámvonalas O javítása Õ-re */

	/* BE: &Otilde; */

	/* KI: Õ */

"&Otilde"";"? { printf("%c",(char) 213); }

	/* BE: &Ouml; */

	/* KI: Ö */

"&Ouml"";"? { printf("%c",(char) 214); }

	/* BE: &times; */

	/* KI: × */

"&times"";"? { printf("%c",(char) 215); }

	/* "&Oslash"";"? { printf("%c",(char) 216); } */ // betû
	/* "&Ugrave"";"? { printf("%c",(char) 217); } */ // betû

	/* BE: &Uacute; */

	/* KI: Ú */

"&Uacute"";"? { printf("%c",(char) 218); }

	/* XXX Tipikus tévesztés: pontos U javítása Û-re */

	/* BE: &Ucirc; */

	/* KI: Û */

"&Ucirc"";"? { printf("%c",(char) 219); }

	/* BE: &Uuml; */

	/* KI: Ü */

"&Uuml"";"? { printf("%c",(char) 220); }

	/* BE: &Yacute; */

	/* KI: Ý */

"&Yacute"";"? { printf("%c",(char) 221); }

	/* "&THORN"";"? { printf("%c",(char) 222); } */ // betû

	/* BE: &szlig; */

	/* KI: ß */

"&szlig"";"? { printf("%c",(char) 223); }

	/* "&agrave"";"? { printf("%c",(char) 224); } */ // betû

	/* BE: &aacute; */

	/* KI: á */

"&aacute"";"? { printf("%c",(char) 225); }

	/* BE: &acirc; */

	/* KI: â */

"&acirc"";"? { printf("%c",(char) 226); }

	/* "&atilde"";"? { printf("%c",(char) 227); } */ // betû

	/* BE: &auml; */

	/* KI: ä */

"&auml"";"? { printf("%c",(char) 228); }

	/* "&aring"";"? { printf("%c",(char) 229); } */ // betû
	/* "&aelig"";"? { printf("%c",(char) 230); } */ // betû

	/* BE: &ccedil; */

	/* KI: ç */

"&ccedil"";"? { printf("%c",(char) 231); }

	/* "&egrave"";"? { printf("%c",(char) 232); } */ // betû

	/* BE: &eacute; */

	/* KI: é */

"&eacute"";"? { printf("%c",(char) 233); }

	/* "&ecirc"";"? { printf("%c",(char) 234); } */ // betû

	/* BE: &euml; */

	/* KI: ë */

"&euml"";"? { printf("%c",(char) 235); }

	/* "&igrave"";"? { printf("%c",(char) 236); } */ // betû

	/* BE: &iacute; */

	/* KI: í */

"&iacute"";"? { printf("%c",(char) 237); }

	/* BE: &icirc; */

	/* KI: î */

"&icirc"";"? { printf("%c",(char) 238); }

	/* "&iuml"";"? { printf("%c",(char) 239); } */ // betû
	/* "&eth"";"? { printf("%c",(char) 240); } */ // betû
	/* "&ntilde"";"? { printf("%c",(char) 241); } */ // betû
	/* "&ograve"";"? { printf("%c",(char) 242); } */ // betû

	/* BE: &oacute; */

	/* KI: ó */

"&oacute"";"? { printf("%c",(char) 243); }

	/* BE: &ocirc; */

	/* KI: ô */

"&ocirc"";"? { printf("%c",(char) 244); }

	/* XXX Tipikus tévesztés: hullámvonalas o javítása õ-re */

	/* BE: &otilde; */

	/* KI: õ */

"&otilde"";"? { printf("%c",(char) 245); }

	/* BE: &ouml; */

	/* KI: ö */

"&ouml"";"? { printf("%c",(char) 246); }

	/* BE: &divide; */

	/* KI: ÷ */

"&divide"";"? { printf("%c",(char) 247); }

	/* "&oslash"";"? { printf("%c",(char) 248); } */ // betû
	/* "&ugrave"";"? { printf("%c",(char) 249); } */ // betû

	/* BE: &uacute; */

	/* KI: ú */

"&uacute"";"? { printf("%c",(char) 250); }

	/* BE: &ucirc; */

	/* KI: û */

	/* XXX Tipikus tévesztés: pontos u javítása û-re */

"&ucirc"";"? { printf("%c",(char) 251); }

	/* BE: &uuml; */

	/* KI: ü */

"&uuml"";"? { printf("%c",(char) 252); }

	/* BE: &yacute; */

	/* KI: ý */

"&yacute"";"? { printf("%c",(char) 253); }

	/* "&thorn"";"? { printf("%c",(char) 254); } */ // betû
	/* "&yuml"";"? { printf("%c",(char) 255); } */ // betû
