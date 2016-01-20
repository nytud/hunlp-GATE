%option noyywrap

/* hun_sentence - mondatra bontó szûrõ */
/* 2003 (c) Németh László <nemethl@gyorsposta.hu> */

/* 											*/
/*  Makródefiníciók									*/
/* 											*/
/*  WORDCHAR: A szavak az ISO-8859-2 betûi mellett számokat, pontot,			*/
/*  paragrafus-, fok-, százalék- és kötõjelet, valamint HTML entitásként megadva	*/
/*  nagykötõjelet (&ndash;), valamint kvirtmínuszt (&mdash;) tartalmazhatnak,		*/
/*  továbbá betûszerepre nem vizsgált decimális UNICODE entitást (például &345;).	*/
/*  Az ISO-8859-2 betûi a locale csomag alapján automatikusan lettek kiválogatva.	*/
/*  További információk: man iso-8859-2.									*/

LATIN1 ("&Agrave"";"?|"&Atilde"";"?|"&Aring"";"?|"&AElig"";"?|"&Egrave"";"?|"&Ecirc"";"?|"&Igrave"";"?|"&Iuml"";"?|"&ETH"";"?|"&Ntilde"";"?|"&Ograve"";"?|"&Oslash"";"?|"&Ugrave"";"?|"&THORN"";"?|"&agrave"";"?|"&atilde"";"?|"&aring"";"?|"&aelig"";"?|"&egrave"";"?|"&ecirc"";"?|"&igrave"";"?|"&iuml"";"?|"&eth"";"?|"&ntilde"";"?|"&ograve"";"?|"&oslash"";"?|"&ugrave"";"?|"&thorn"";"?|"&yuml"";"?)
LATIN1LOWER ("&agrave"";"?|"&atilde"";"?|"&aring"";"?|"&aelig"";"?|"&egrave"";"?|"&ecirc"";"?|"&igrave"";"?|"&iuml"";"?|"&eth"";"?|"&ntilde"";"?|"&ograve"";"?|"&oslash"";"?|"&ugrave"";"?|"&thorn"";"?|"&yuml"";"?)

WORDCHAR ({LATIN1}|[a-zA-ZáéíóöõúüûÁÉÍÓÖÕÚÜÛ\-.§%°0-9¡£¥¦©ª«¬®¯±³µ¶¹º»¼¾¿ÀÂÃÄÅÆÇÈÊËÌÎÏÐÑÒÔØÙÝÞßàâãäåæçèêëìîïðñòôøùýþ]|"&ndash"";"?|"&mdash"";"?|"&"[0-9]+";"?)

/* WORDCHAR mínusz a pont */

WORDCHAR2 ({LATIN1}|[a-zA-ZáéíóöõúüûÁÉÍÓÖÕÚÜÛ\-§%°0-9¡£¥¦©ª«¬®¯±³µ¶¹º»¼¾¿ÀÂÃÄÅÆÇÈÊËÌÎÏÐÑÒÔØÙÝÞßàâãäåæçèêëìîïðñòôøùýþ]|"&ndash"";"?|"&mdash"";"?|"&"[0-9]+";"?)

/* kisbetûk */
LOWER ({LATIN1LOWER}|[a-záéíóöõúüû±³µ¶¹º»¼¾¿ßàâãäåæçèêëìîïðñòôøùýþ])

/* mondaton belül elõforduló karakterek */
CHARINSNT ([\(\)\-[\],; "]|("&ndash"";"?)|("&mdash"";"?)|("&ldquor"";"?)|("&raquo"";"?))*
CHARINSNTN {CHARINSNT}"\n"?{CHARINSNT}

/* egyszerû mondathatárok, kivéve a szón belüli pont */
BOUNDARY ([.?!])

/* mondatban szereplõ karakterek, kivéve a szóköz */
SNTCHAR [^.?!\n]

/* mondat kezdõ karaktere, ponttal kezdhetünk mondatot: */
/* pl.: .hu vita a neten */
SNTBEGIN [^?!\n ]"\n"?

/* egyszerû mondat */
/* XXX Az írásjelek itt nem tapadók, hogy elfogadja a
/* helyesírásnak nem megfelelõ szövegeket is: Szép az idõ ! */
SIMPLESNT ({SNTCHAR}"\n"?)*{BOUNDARY}*

/* mondathatár után jöhetõ záró idézõjelek. */
ENDQUOPAR ("\""|"&rdquo""r"?";"|"&laquo;"|"''"|"'"|")"|"]"){BOUNDARY}*

%%

	/* egyszerû mondat */
	
	/* BE: Á. A kutya ugat. Jó? */

	/* KI: <s>Á.</s> <s>A kutya ugat.</s> <s>Jó?</s> */

	/* BE: A kutya ugat. Jó */
	/* BE: lenne, ha abbahagyná! */

	/* KI: <s>A kutya ugat.</s> <s>Jó */
	/* KI: lenne, ha abbahagyná!</s> */

	/* BE: Ez itt már */
	/* BE:  */
	/* BE: külön bekezdés! */

	/* KI: <s>Ez itt már */
	/* KI: </s> */
	/* KI: <s>külön bekezdés!</s> */

{SNTBEGIN}{SIMPLESNT} {
	printf("<s>%s</s>",yytext);
}
	/* egyszerû mondat szoros idézõjelekkel, zárójelekkel */
	
	/* BE: "A kutya ugat." &ldquor;Jó?&rdquo; ,,Nem.'' */

	/* KI: <s>"A kutya ugat."</s> <s>&ldquor;Jó?&rdquo;</s> <s>,,Nem.''</s> */

{SNTBEGIN}{SIMPLESNT}{ENDQUOPAR}+ {
	printf("<s>%s</s>",yytext);
}

	/* mondathatár túllépések: */

	/* Ha a mondatzáró írásjel után nem nagybetû, hanem kisbetû következik. */

	/* BE: N. kormányzósági székhely. */

	/* KI: <s>N. kormányzósági székhely.</s> */

	/* BE: N. */
	/* BE: kormányzósági székhely. */

	/* KI: <s>N. */
	/* KI: kormányzósági székhely.</s> */

	/* BE: A www.akármi.hu. */

	/* KI: <s>A www.akármi.hu.</s> */

	/* BE: A mond. folyt. */
	/* BE: (mivel?) kisbetûs a folytatás (bizony!)! */

	/* KI: <s>A mond. folyt. */
	/* KI: (mivel?) kisbetûs a folytatás (bizony!)!</s> */

	/* BE: - Nézd a! - mondta az egyik. */
	
	/* KI: <s>- Nézd a! - mondta az egyik.</s> */

	/* BE: A 4. javítócsomagot. */
	
	/* KI: <s>A 4. javítócsomagot.</s> */
	
	/* BE: 3434/1992. évi elszámolás. */
	
	/* KI: <s>3434/1992. évi elszámolás.</s> */

	/* BE: "Jót s jól! Ebben áll a nagy titok" - figyelmezteti */
	/* BE: Kazinczy költõtársait. */

	/* KI: <s>"Jót s jól!</s> <s>Ebben áll a nagy titok" - figyelmezteti */
	/* KI: Kazinczy költõtársait.</s> */
	
	
	/* BE: - Szia Péterkém! Holnap találkozunk - mondta Gizi. */

	/* KI: <s>- Szia Péterkém!</s> <s>Holnap találkozunk - mondta Gizi.</s> */

{SNTBEGIN}({SIMPLESNT}{BOUNDARY}{ENDQUOPAR}*{CHARINSNTN}{LOWER})+{SIMPLESNT}{ENDQUOPAR}* {
	printf("<s>%s</s>",yytext);
}

	/* Ha a mondatzáró írásjel után kötõjel következik esetleg idézõjel */
	/* közbeékelõdésével */

	/* BE: A "Ne már!"-ral az a baj. */

	/* KI: <s>A "Ne már!"-ral az a baj.</s> */

{SNTBEGIN}({SIMPLESNT}{BOUNDARY}{ENDQUOPAR}"-"{WORDCHAR})+{SIMPLESNT}{ENDQUOPAR}* {
	printf("<s>%s</s>",yytext);
}

	/* Ha a mondatzáró pont után szókarakter következik közvetlenül, de nem pont! */

	/* BE: A WWW.AKARMI.HU. */

	/* KI: <s>A WWW.AKARMI.HU.</s> */

{SNTBEGIN}({SIMPLESNT}[.]{WORDCHAR2})+{SIMPLESNT}{ENDQUOPAR}* {
	printf("<s>%s</s>",yytext);
}

	/* Ha a mondatzáró írásjel után közvetlenül vesszõ, vagy pontosvesszõ */
	/* következik, kombinálva a kisbetûs lehetõséggel: */

	/* BE: Azt mondta, hogy "Na!", "Csináld!" és így tovább. */

	/* KI: <s>Azt mondta, hogy "Na!", "Csináld!" és így tovább.</s> */

{SNTBEGIN}({SIMPLESNT}{BOUNDARY}{ENDQUOPAR}*([,;:]|{CHARINSNTN}{LOWER}))+{SIMPLESNT}{ENDQUOPAR}* {
	printf("<s>%s</s>",yytext);
}

	/* Ha a mondatban zárójeles rész található. */

	/* BE: A macska (családjában a 25.) Katinak nyávogott. */

	/* KI: <s>A macska (családjában a 25.) Katinak nyávogott.</s> */

{SNTBEGIN}{SIMPLESNT}"("[^.!?)]+[.?!][^.!?)]*")"{SIMPLESNT}{BOUNDARY}{ENDQUOPAR}* {
	printf("<s>%s</s>",yytext);
}

	/* az elozok kombinációja */

	/* BE: A Cica 4.0 4. része jó?, Macskák, és más könyvek. */
	
	/* KI: <s>A Cica 4.0 4. része jó?, Macskák, és más könyvek.</s> */

{SNTBEGIN}(({SIMPLESNT}{BOUNDARY}{ENDQUOPAR}*{CHARINSNTN}{LOWER})|({SIMPLESNT}{BOUNDARY}{ENDQUOPAR}"-"{WORDCHAR})|({SIMPLESNT}[.]{WORDCHAR2})|({SIMPLESNT}{BOUNDARY}{ENDQUOPAR}*([,;:]|{CHARINSNTN}{LOWER}))|({SIMPLESNT}"("[^.!?)]+[.?!][^.!?)]*")"))+{SIMPLESNT}{ENDQUOPAR}* {
	printf("<s>%s</s>",yytext);
}
