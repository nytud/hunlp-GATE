#include <stdio.h>
#include "tokenizer.h"

int main(int argc, char** argv)
{
    char* text = " Ez egy mondat.  ";
    printf("'%s'\n", text);

    int maxtoks = 1000;
    int tok_starts[maxtoks];
    int tok_ends[maxtoks];
    int ntoks = 0;
    int maxwhs  = 1000;
	int wh_starts[maxwhs];
	int wh_ends[maxwhs];
    int nwhs = 0;

    tokenize(text, maxtoks, maxwhs, tok_starts, tok_ends, &ntoks, wh_starts, wh_ends, &nwhs);

    printf("Tokens:\n");
    int i;
    for (i=0; i<ntoks; i++)
		printf("(%d, %d)\n", tok_starts[i], tok_ends[i]);

    printf("Whitespaces:\n");
    for (i=0; i<nwhs; i++)
		printf("(%d, %d)\n", wh_starts[i], wh_ends[i]);	

    return 0;
}
