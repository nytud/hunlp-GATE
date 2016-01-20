%option noyywrap

/* hun_latin1 - ISO-8859-1 (illetve Windows-1252) betûit entitásokra alakító szûrõ */
/* 2003 (c) Németh László <nemethl@gyorsposta.hu> */

%%

	/* [\240] { printf("%s", "&nbsp;" ); }  // ez már lecserélve */

[\241] { printf("%s", "&iexcl;" ); }

[\242] { printf("%s", "&cent;" ); }
[\243] { printf("%s", "&pound;" ); }

	/** "&curren"";"? { printf("%c",(char) 164); } */
    
[\245] { printf("%s", "&yen;" ); }
[\246] { printf("%s", "&brvbar;" ); }
[\247] { printf("%s", "&sect;" ); }
[\250] { printf("%s", "&uml;" ); }
[\251] { printf("%s", "&copy;" ); }
[\252] { printf("%s", "&ordf;" ); }
[\253] { printf("%s", "&laquo;" ); }
[\254] { printf("%s", "&not;" ); }

	/* *BE: &shy&shy; */

	/* *KI:  */

	/** "&shy"";"? // elválasztási hely jelének (feltételes v. lágy kötõjel) törlése */

[\256] { printf("%s", "&reg;" ); }
[\257] { printf("%s", "&macr;" ); }

	/* *BE: &deg; */

	/* *KI: ° */

	/** "&deg"";"? { printf("%c",(char) 176); } // fokjel, megegyezik latin-2-vel */

[\261] { printf("%s", "&plusmn;" ); }
[\262] { printf("%s", "&sup2;" ); }
[\263] { printf("%s", "&sup3;" ); }

	/* *BE: &acute; */

	/* *KI: ´ */

	/** "&acute"";"? { printf("%c",(char) 180); } // vesszõ ékezet, megegyezik a latin-2-vel */

[\265] { printf("%s", "&micro;" ); }
[\266] { printf("%s", "&para;" ); }
[\267] { printf("%s", "&middot;" ); }

	/* *BE: &cedil; */

	/* *KI: ¸ */

	/** "&cedil"";"? { printf("%c",(char) 184); } // cedilla ékezet, megegyezik a latin-2-vel */

[\271] { printf("%s", "&sup1;" ); }
[\272] { printf("%s", "&ordm;" ); }
[\273] { printf("%s", "&raquo;" ); }
[\274] { printf("%s", "&frac14;" ); }
[\275] { printf("%s", "&frac12;" ); }
[\276] { printf("%s", "&frac34;" ); }
[\277] { printf("%s", "&iquest;" ); }
[\300] { printf("%s", "&Agrave;" ); }

	/* *BE: &Aacute; */

	/* *KI: Á */

	/** "&Aacute"";"? { printf("%c",(char) 193); } */

	/* *BE: &Acirc; */

	/* *KI: Â */

	/** "&Acirc"";"? { printf("%c",(char) 194); } */

	/* "&Atilde"";"? { printf("%c",(char) 195); } // betû

	/* *BE: &Auml; */

	/* *KI: Ä */

	/** "&Auml"";"? { printf("%c",(char) 196); } */

[\305] { printf("%s", "&Aring;" ); }
[\306] { printf("%s", "&AElig;" ); }

	/* *BE: &Ccedil; */

	/* *KI: Ç */

	/** "&Ccedil"";"? { printf("%c",(char) 199); } */

[\310] { printf("%s", "&Egrave;" ); }

	/* *BE: &Eacute; */

	/* *KI: É */

	/** "&Eacute"";"? { printf("%c",(char) 201); } */

[\312] { printf("%s", "&Ecirc;" ); }

	/* *BE: &Euml; */

	/* *KI: Ë */

	/** "&Euml"";"? { printf("%c",(char) 203); } */

[\314] { printf("%s", "&Igrave;" ); }

	/* *BE: &Iacute; */

	/* *KI: Í */

	/** "&Iacute"";"? { printf("%c",(char) 205); } */

	/* *BE: &Icirc; */

	/* *KI: Î */

	/** "&Icirc"";"? { printf("%c",(char) 206); } */

[\317] { printf("%s", "&Iuml;" ); }
[\320] { printf("%s", "&ETH;" ); }
[\321] { printf("%s", "&Ntilde;" ); }
[\322] { printf("%s", "&Ograve;" ); }

	/* *BE: &Oacute; */

	/* *KI: Ó */

	/** "&Oacute"";"? { printf("%c",(char) 211); } */

	/* *BE: &Ocirc; */

	/* *KI: Ô */

	/** "&Ocirc"";"? { printf("%c",(char) 212); } */

	/* XXX Tipikus tévesztés: hullámvonalas O javítása Õ-re */

	/* *BE: &Otilde; */

	/* *KI: Õ */

	/** "&Otilde"";"? { printf("%c",(char) 213); } */

	/* *BE: &Ouml; */

	/* *KI: Ö */

	/** "&Ouml"";"? { printf("%c",(char) 214); } */

	/* *BE: &times; */

	/* *KI: × */

	/** "&times"";"? { printf("%c",(char) 215); } */

[\330] { printf("%s", "&Oslash;" ); }
[\331] { printf("%s", "&Ugrave;" ); }

	/* *BE: &Uacute; */

	/* *KI: Ú */

	/** "&Uacute"";"? { printf("%c",(char) 218); } */

	/* XXX Tipikus tévesztés: pontos U javítása Û-re */

	/* *BE: &Ucirc; */

	/* *KI: Û */

	/** "&Ucirc"";"? { printf("%c",(char) 219); } */

	/* *BE: &Uuml; */

	/* *KI: Ü */

	/** "&Uuml"";"? { printf("%c",(char) 220); } */

	/* *BE: &Yacute; */

	/* *KI: Ý */

	/** "&Yacute"";"? { printf("%c",(char) 221); } */

[\336] { printf("%s", "&THORN;" ); }

	/* *BE: &szlig; */

	/* *KI: ß */

	/** "&szlig"";"? { printf("%c",(char) 223); } */

[\340] { printf("%s", "&agrave;" ); }

	/* *BE: &aacute; */

	/* *KI: á */

	/** "&aacute"";"? { printf("%c",(char) 225); } */

	/* *BE: &acirc; */

	/* *KI: â */

	/** "&acirc"";"? { printf("%c",(char) 226); } */

[\343] { printf("%s", "&atilde;" ); }

	/* *BE: &auml; */

	/* *KI: ä */

	/** "&auml"";"? { printf("%c",(char) 228); } */

[\345] { printf("%s", "&aring;" ); }
[\346] { printf("%s", "&aelig;" ); }

	/* *BE: &ccedil; */

	/* *KI: ç */

	/** "&ccedil"";"? { printf("%c",(char) 231); } */

[\350] { printf("%s", "&egrave;" ); }

	/* *BE: &eacute; */

	/* *KI: é */

	/** "&eacute"";"? { printf("%c",(char) 233); } */

[\352] { printf("%s", "&ecirc;" ); }

	/* *BE: &euml; */

	/* *KI: ë */

	/** "&euml"";"? { printf("%c",(char) 235); } */

[\354] { printf("%s", "&igrave;" ); }

	/* *BE: &iacute; */

	/* *KI: í */

	/** "&iacute"";"? { printf("%c",(char) 237); } */

	/* *BE: &icirc; */

	/* *KI: î */

	/** "&icirc"";"? { printf("%c",(char) 238); } */

[\357] { printf("%s", "&iuml;" ); }
[\360] { printf("%s", "&eth;" ); }
[\361] { printf("%s", "&ntilde;" ); }
[\362] { printf("%s", "&ograve;" ); }

	/* *BE: &oacute; */

	/* *KI: ó */

	/** "&oacute"";"? { printf("%c",(char) 243); } */

	/* *BE: &ocirc; */

	/* *KI: ô */

	/** "&ocirc"";"? { printf("%c",(char) 244); } */

	/* XXX Tipikus tévesztés: hullámvonalas o javítása õ-re */

	/* *BE: &otilde; */

	/* *KI: õ */

	/** "&otilde"";"? { printf("%c",(char) 245); } */

	/* *BE: &ouml; */

	/* *KI: ö */

	/** "&ouml"";"? { printf("%c",(char) 246); } */

	/* *BE: &divide; */

	/* *KI: ÷ */

	/** "&divide"";"? { printf("%c",(char) 247); } */

[\370] { printf("%s", "&oslash;" ); }
[\371] { printf("%s", "&ugrave;" ); }

	/* *BE: &uacute; */

	/* *KI: ú */

	/** "&uacute"";"? { printf("%c",(char) 250); } */

	/* *BE: &ucirc; */

	/* *KI: û */

	/* XXX Tipikus tévesztés: pontos u javítása û-re */

	/** "&ucirc"";"? { printf("%c",(char) 251); } */

	/* *BE: &uuml; */

	/* *KI: ü */

	/** "&uuml"";"? { printf("%c",(char) 252); } */

	/* *BE: &yacute; */

	/* *KI: ý */

	/** "&yacute"";"? { printf("%c",(char) 253); } */

[\376] { printf("%s", "&thorn;" ); }
[\377] { printf("%s", "&yuml;" ); }

