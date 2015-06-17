#include "tokenizer.h"
#include "stdio.h"

int main ( int argc, char * argv[] )
{
    static char* testinp = "First sentence. Second nice sentence. Is this all? No, this is the end!";
    char* output;
	t_tokenizer mytokenizer;
	int i;

	printf("Input='%s'\n", testinp);

	printf("Initializing...\n");
	
	i = tokenizer_init(&mytokenizer);
	
	printf("Done: %d\n", i);
	
	//tokenizer_tokenize(mytokenizer, testinp, output);
	//printf("Output='%s'", testinp, output);
	
	//free(output);
	
	tokenizer_destroy(mytokenizer);

	return 0;
}
