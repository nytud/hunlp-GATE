#include <string.h>
#include "tokenizer.h"

void tokenize(	const char* text,
		OffsPair* tokens,
		int* ntokens,
		int maxtokens,
		OffsPair* whites,
		int* nwhites,
		int maxwhites)
{
    long len = strlen(text);
    *ntokens = 0;
    *nwhites = 0;
    long pos;
    for (pos=0; pos<len; pos++) {
	if ((*nwhites == maxwhites) || (*ntokens == maxtokens))
		break;
	if (text[pos] == ' ') { // whitespace
	    if (pos == 0) {  // start
		whites[*nwhites].start = 0;
		whites[*nwhites].end = 1;
		*nwhites = *nwhites + 1;
	    }
	    else if (text[pos-1] != ' ') { // last: not whitespace
		whites[*nwhites].start = pos;
		whites[*nwhites].end = pos + 1;
		*nwhites = *nwhites + 1;
	    }
	    else { // last: whitespace
		whites[*nwhites-1].end += 1;
	    }
	}
	else { // not whitespace
	    if (pos == 0) {  // start
		tokens[*ntokens].start = 0;
		tokens[*ntokens].end = 1;
		*ntokens = *ntokens + 1;
	    }
	    else if (text[pos-1] != ' ') { // last: not whitespace
		tokens[*ntokens-1].end += 1;
	    }
	    else { // last: whitespace
		tokens[*ntokens].start = pos;
		tokens[*ntokens].end = pos+1;
		*ntokens = *ntokens + 1;
	    }
	}
    }
}