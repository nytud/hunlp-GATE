#include "test.h"
#include <stdio.h>

void doit(const char* msg)
{
    printf("Hello %s\n", msg); 
}

int doit2(long* v)
{
    v[0] = 100;
    v[1] = 200;
    return 2;
}

int doit3(OffsPair offs[])
{
    offs[0].start = 100;
    offs[0].end = 150;
    return 1;
}

void doit4(int* x)
{
    *x = 333;
}
