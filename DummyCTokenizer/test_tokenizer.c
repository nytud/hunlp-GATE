#include <stdio.h>
#include "tokenizer.h"

int main(int argc, char** argv)
{
    char* test = " Ez egy mondat.  ";
    printf("'%s'\n", test);

    int maxtoks = 1000;
    OffsPair toks[maxtoks];
    int ntoks = 0;
    int maxwhs  = 1000;
    OffsPair  whs[maxwhs];
    int nwhs = 0;

    tokenize(test, toks, &ntoks, maxtoks, whs, &nwhs, maxwhs);

    printf("Tokens:\n");
    int i;
    for (i=0; i<ntoks; i++)
	printf("(%ld, %ld)\n", toks[i].start, toks[i].end);

    printf("Whitespaces:\n");
    for (i=0; i<nwhs; i++)
	printf("(%ld, %ld)\n", whs[i].start, whs[i].end);

    return 0;
}