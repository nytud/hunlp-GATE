// Reads/writes stdin/stdout

#include "reentrant2.h"

int main ( int argc, char * argv[] )
{
     yyscan_t scanner;

     reentrant2lex_init ( &scanner );
     reentrant2lex ( scanner );
     reentrant2lex_destroy ( scanner );
     return 0;
}
