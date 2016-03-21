#include <string.h>
#include "tokenizer.h"

void tokenize(	const char* text,
				const int maxtokens,
				const int maxwhites,
				int* token_starts,
				int* token_ends,
				int* ntokens,
				int* white_starts,
				int* white_ends,
				int* nwhites )
{
    int len = strlen(text);
    *ntokens = 0;
    *nwhites = 0;
    int pos;
    for (pos=0; pos<len; pos++) {
		if ((*nwhites == maxwhites) || (*ntokens == maxtokens))
			break;
		if (text[pos] == ' ' || text[pos] == '\n') { // whitespace
			if (pos == 0) {  // start
				white_starts[*nwhites] = 0;
				white_ends[*nwhites] = 1;
				*nwhites = *nwhites + 1;
			}
			else if (text[pos-1] != ' ' && text[pos-1] != '\n') { // last: not whitespace
				white_starts[*nwhites] = pos;
				white_ends[*nwhites] = pos + 1;
				*nwhites = *nwhites + 1;
			}
			else { // last: whitespace
				white_ends[*nwhites-1] += 1;
			}
		}
		else { // not whitespace
			if (pos == 0) {  // start
				token_starts[*ntokens] = 0;
				token_ends[*ntokens] = 1;
				*ntokens = *ntokens + 1;
			}
			else if (text[pos-1] != ' ' && text[pos-1] != '\n') { // last: not whitespace
				token_ends[*ntokens-1] += 1;
			}
			else { // last: whitespace
				token_starts[*ntokens] = pos;
				token_ends[*ntokens] = pos+1;
				*ntokens = *ntokens + 1;
			}
		}
    }
}
