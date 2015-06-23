/*
Simple dummy tokenizer that uses 2 Flex scanners in a pipe
Provides API for tokenizing strings, returning strings
*/
#ifndef __TOKENIZER_H__
#define __TOKENIZER_H__

// opaque data type for tokenizer objects
typedef void* t_tokenizer;

// Initialize a new tokenizer
// Return 1 if error, 0 othrewise
int tokenizer_init( t_tokenizer tokenizer);

// Tokenize 0-terminated string input: set output to a new dynamically
// allocated 0-terminated string that contains the results.
// It is your responsibility to free up string output.
void tokenizer_tokenize( t_tokenizer tokenizer, char* input, char* output);

// Free up dynamic objects allocated by tokenizer when not used any more
void tokenizer_destroy( t_tokenizer tokenizer);

#endif /* ##ifndef __TOKENIZER_H__ */
