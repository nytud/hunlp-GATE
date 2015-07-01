#include "stdio.h"
#include "stdlib.h"
#include "string.h"

typedef struct t_my {
	char* x;
	char* y;
} t_my;

typedef void* t;

void init(t* y)
{
	*y = (t)malloc(sizeof(t_my));
	memset(*y, 0, sizeof(t_my));
}

int main(int argc, char* argv[])
{
	t x;
/*
	x = (t)malloc(sizeof(t_my));
	memset(x, 0, sizeof(t_my));
*/
	init(&x);

	t_my* p = (t_my*)x;
	p->x = malloc(100);
	p->y = malloc(100);
	
	strcpy(p->x, "First blabla"); 
	strcpy(p->y, "Second salalala");

	printf("%s\n%s\n", p->x, p->y);
	
	free( p->x );
	free( p->y );
	free( x );

	return 0;
}
