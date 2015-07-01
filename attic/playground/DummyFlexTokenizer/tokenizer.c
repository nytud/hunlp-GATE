#include "stdio.h"
#include "stdlib.h"
#include "string.h"

#include "lex.tokens.h"
#include "lex.sentences.h"

#include "tokenizer.h"

/* The real internal state type */
typedef struct t_tokenizer_internals {
     yyscan_t scantoks;
     yyscan_t scansents;
} t_tokenizer_internals;


// Initialize tokenizer: allocate memory for internal variables
// Return 1 if error, 0 otherwise
int tokenizer_init(t_tokenizer* t)
{
	// allocate, set to 0
	if (t == NULL)
		return 1;
	*t = (t_tokenizer)malloc(sizeof(t_tokenizer_internals));
	if (*t == NULL)
		return 1;
    memset(*t, 0, sizeof(t_tokenizer_internals));		
	
	// initialize scanners
	t_tokenizer_internals* ptr = (t_tokenizer_internals*)(*t);
    tokenslex_init ( &(ptr->scantoks) );
    sentenceslex_init ( &(ptr->scansents) );
    
	return 0;
}

// Tokenize 0-terminated string input: set *output to a new dynamically
// allocated 0-terminated string that contains the results.
// The length of the output string will be set to *outsize.
// It is your responsibility to free up string output.
void tokenizer_tokenize( t_tokenizer t, char* input, char** output, int* outsize)
{
	t_tokenizer_internals* ptr = (t_tokenizer_internals*)t;	// easier to work with this
	
	YY_BUFFER_STATE bs1, bs2; // for setting the input buffers
    char *bp1, *bp2; // output buffers (of the memstreams)
    size_t bsize1, bsize2; // lengths of strings in the output buffers
    FILE *ostream1, *ostream2; // output file streams linked to output buffer

    // create output stream1, set for scanner1
    ostream1 = open_memstream(&bp1, &bsize1);
    tokensset_out(ostream1, ptr->scantoks);

    // create output stream2, set for scanner2
    ostream2 = open_memstream(&bp2, &bsize2);
    sentencesset_out(ostream2, ptr->scansents);

    // scan input with scanner1
    bs1 = tokens_scan_string(input, ptr->scantoks); // set input of ptr->scantoks to input
    tokenslex(ptr->scantoks); // scan (into ostream1)
    tokens_delete_buffer(bs1, ptr->scantoks); // delete buffer allocated by scan_string()
    fclose(ostream1); // close ostream1 so its buffer can be used

    // scan output of scanner1 with scanner2
    bs2 = sentences_scan_string(bp1, ptr->scansents); // set input of ptr->scansents to buffer of ostream1
    sentenceslex(ptr->scansents); // scan (into ostream2)
    sentences_delete_buffer(bs2, ptr->scansents);
    fclose(ostream2);

    // clean up output buffer of scanner1 (ostream1)
    free(bp1);
    
    // set output parameter to output buffer of scanner2
    *output = bp2;
    *outsize = bsize2; // set length of output string
}

// Free up dynamic objects allocated by tokenizer when not used any more
void tokenizer_destroy(t_tokenizer t)
{
	t_tokenizer_internals* ptr = (t_tokenizer_internals*)t;
    tokenslex_destroy(ptr->scantoks);
    sentenceslex_destroy(ptr->scansents);
    free(t);
    t = NULL;
}
