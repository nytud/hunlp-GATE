// Read from string, write to string

#include "stdio.h"
#include "stdlib.h"
#include "reentrant2.h"

int main ( int argc, char * argv[] )
{
     yyscan_t scanner;
     const char* testinp = "Blablabla // comment part";
     const char* testinp2 = "// second comment";
     YY_BUFFER_STATE bs;
     char *bp; // output buffer
     size_t bsize; // length of streing in output buffer
     FILE *ostream; // output file stream linked to output buffer

     printf("Input: '%s'\n", testinp);
     printf("%d\n", strlen(testinp));

     // set up scanner
     reentrant2lex_init ( &scanner );

     // create output stream, set for scanner
     ostream = open_memstream (&bp, &bsize);
     reentrant2set_out(ostream, scanner);

     // scan string
     bs = reentrant2_scan_string( testinp, scanner);
     reentrant2lex ( scanner );
     reentrant2_delete_buffer( bs, scanner ); // delete buffer allocated by scan_string()

     // get output
     fclose( ostream); // must be called before using bp and size (or use fflush())
     printf("Output: '%s'\n%d\n", bp, bsize);
     free(bp); // free up ostream's buffer!

     // Now let's reuse the scanner on another input string:
     printf("Input: '%s'\n", testinp2);
     printf("%d\n", strlen(testinp2));

     ostream = open_memstream (&bp, &bsize);
     reentrant2set_out(ostream, scanner);

     bs = reentrant2_scan_string( testinp2, scanner);
     reentrant2lex ( scanner );
     reentrant2_delete_buffer( bs, scanner);

     fclose( ostream);
     printf("Output: '%s'\n%d\n", bp, bsize);
     free(bp);

     // finally: clean up scanner
     reentrant2lex_destroy ( scanner );

     return 0;
}
