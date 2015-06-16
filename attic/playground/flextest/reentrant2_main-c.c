// Read string, parse, reuse, write to string
// TODO!

#include "reentrant2.h"

int main ( int argc, char * argv[] )
{
     yyscan_t scanner;
     const char* testinp = "Blablabla // comment part";
     const char* testinp2 = "// second comment";
     YY_BUFFER_STATE bs;

     printf("Input: '%s'\n", testinp);
     printf("%d\n", strlen(testinp));

     reentrant2lex_init ( &scanner );

     bs = reentrant2_scan_string( testinp, scanner);
     reentrant2lex ( scanner );
     reentrant2_delete_buffer( bs, scanner ); // delete buffer allocated by scan_string()

     printf("Input: '%s'\n", testinp2);
     printf("%d\n", strlen(testinp2));

     bs = reentrant2_scan_string( testinp2, scanner);
     reentrant2lex ( scanner );
     reentrant2_delete_buffer( bs, scanner);

     reentrant2lex_destroy ( scanner );
     return 0;
}
