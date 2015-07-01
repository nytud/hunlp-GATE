#include "stdio.h"
#include "stdlib.h"
#include "string.h"


void init(void **p)
{
	*p = 0;
}

void init2(char** c)
{
	*c = malloc(sizeof(char)*100);
	strcpy(*c, "Hello, world!");
}

int main ( int argc, char * argv[] )
{
	void* x;
	init(&x);
	printf("%p\n", x);
	
	char* y;
	init2(&y);
	printf("%s\n", y);
	free(y);
		
	return 0;
}
