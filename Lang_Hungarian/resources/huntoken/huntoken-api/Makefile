
# TODO: change refs to yytext to get_... in function bodies?
# copy the flex source files from huntoken-1.6 & apply automatic modifications
huntoken_source_dir=../huntoken-1.6
huntoken_sources=hun_clean.flex hun_sentence.flex __hun_abbrev.flex hun_sentclean.flex hun_token.flex
import_huntoken:
	for file in $(huntoken_sources) ; do \
		{ echo "%option reentrant" ; cat $(huntoken_source_dir)/$$file ; } | sed -r "s/printf\(/fprintf\(yyout, /g" > ./$$file ; \
	done

# gnerate lexer c sources from flex files
do_flex:
	for file in $(huntoken_sources) ; do \
		name=`echo $$file | sed -r "s/^__//" | sed -r "s/\.flex//"` ; \
		flex -P$$name --header-file=lex.$$name.h $$file ; \
	done
