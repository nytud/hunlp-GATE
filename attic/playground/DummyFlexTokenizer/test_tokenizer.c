#include "tokenizer.h"
#include "stdio.h"
#include "stdlib.h"

int main ( int argc, char * argv[] )
{
    static char* testinp = "First sentence. Second nice sentence. Is this all? No, this is the end!";
    char* output;
    int outlen;
	t_tokenizer mytokenizer;
	int i;

	printf("Input='%s'\n", testinp);

	printf("Initializing... ");	
	i = tokenizer_init(&mytokenizer);
	printf("Done(%d)\n", i);
	
	tokenizer_tokenize(mytokenizer, testinp, &output, &outlen);
	printf("Output(%d)='%s'\n", outlen, output);
	
	free(output);
	tokenizer_destroy(mytokenizer);

	return 0;
}
