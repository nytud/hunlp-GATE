#ifndef __TOKENIZER_H__
#define __TOKENIZER_H__


void tokenize(	const char* text,
				const int maxtokens,
				const int maxwhites,
				int* token_starts,
				int* token_ends,
				int* ntokens,
				int* white_starts,
				int* white_ends,
				int* nwhites);
/***
Tokenize text.
text: string to tokenize (8-bit) by spaces.
maxtokens: size of token_starts and token_ends arrays (reserved by caller)
maxwhites: size of white_starts and white_ends arrays (reserved by caller)
token_starts: starting offsets of tokens will be added here, starting from index 0. This array should already be reserved by the caller. Caller should specify
        size of reserved array in variable maxtokens.
token_ends: ending offsets of tokens will be added here. This array should already be reserved by the caller. Caller should specify
        size of reserved array in variable maxtokens.
ntokens: the number of items inserted to token_starts and token_ends (equal!) will be returned here (ntokens <= maxtokens)
white_starts: starting offsets of whitespaces will be added here, starting from index 0. This array should already be reserved by the caller. Caller should specify
        size of reserved array in variable maxwhites.
white_ends: ending offsets of whitespaces will be added here. This array should already be reserved by the caller. Caller should specify
        size of reserved array in variable maxwhites.
nwhites: the number of items inserted to white_starts and whites_ends (equal!) will be returned here (nwhites <= maxwhites)

***/


#endif // #ifndef __TOKENIZER_H__
