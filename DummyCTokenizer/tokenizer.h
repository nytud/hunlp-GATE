
typedef struct {
    long start, end;
} OffsPair;
/*** Start and end offset of an annotation ***/

void tokenize(	const char* text,
		OffsPair* tokens,
		int* ntokens,
		int maxtokens,
		OffsPair* whites,
		int* nwhites,
		int maxwhites);
/***
Tokenize text.
text: string to tokenize (8-bit)
tokens: offsets of tokens will be added here, starting from index 0. This array should already be reserved by the caller. Caller should specify
        size of tokens in variable maxtokens.
ntokens: the number of items inserted to tokens will be returned here (ntokens <= maxtokens)
whites: offsets of whitespaces will be added here, starting from index 0. This array should already be reserved by the caller. Caller should specify
        size of tokens with variable maxwhites.
nwhites: the number of items inserted to whites will be returned here (nwhites <= maxwhites)
***/
