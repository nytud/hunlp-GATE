// Reads string, writes to stdout

#include "reentrant2.h"

int main ( int argc, char * argv[] )
{
     const char* testinp = "Blablabla // comment part";
     yyscan_t scanner;

     printf("Input: '%s'\n", testinp);
     printf("%d\n", strlen(testinp));

     reentrant2lex_init ( &scanner );
     reentrant2_scan_string( testinp, scanner);
     reentrant2lex ( scanner );
     reentrant2lex_destroy ( scanner );
     return 0;
}
