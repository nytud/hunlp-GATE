#include "stdio.h"
#include "stdlib.h"
#include "lex.tokens.h"
#include "lex.sentences.h"

int main ( int argc, char * argv[] )
{
     char* testinp = "First sentence. Second nice sentence. Is this all? No, this is the end!";

     yyscan_t scantoks, scansents; // the scanners
     YY_BUFFER_STATE bs1, bs2; // for setting the input buffers
     char *bp1, *bp2; // output buffers (of the memstreams)
     size_t bsize1, bsize2; // length of string in output buffers
     FILE *ostream1, *ostream2; // output file streams linked to output buffer

     printf("Input: '%s'\n", testinp);
     printf("%d\n", strlen(testinp));

     // initialize scanners
     tokenslex_init ( &scantoks );
     sentenceslex_init ( &scansents);

     // create output stream1, set for scanner1
     ostream1 = open_memstream (&bp1, &bsize1);
     tokensset_out(ostream1, scantoks);

     // create output stream2, set for scanner2
     ostream2 = open_memstream (&bp2, &bsize2);
     sentencesset_out(ostream2, scansents);

     // scan input with scanner1
     bs1 = tokens_scan_string( testinp, scantoks);
     tokenslex ( scantoks );
     tokens_delete_buffer( bs1, scantoks ); // delete buffer allocated by scan_string()
     fclose( ostream1);

     // scan output of scanner1 with scanner2
     bs2 = sentences_scan_string( bp1, scansents);
     sentenceslex ( scansents );
     sentences_delete_buffer( bs2, scansents ); // delete buffer allocated by scan_string()
     fclose( ostream2);

     // print output
     //printf("Output: '%s'\n%d\n", bp2, bsize2);
     char *output;
     output = bp2;
     printf("%s", bp2);

     // clean up
     free(bp1);
     free(bp2);
     tokenslex_destroy( scantoks);
     sentenceslex_destroy( scansents);

     return 0;
}
