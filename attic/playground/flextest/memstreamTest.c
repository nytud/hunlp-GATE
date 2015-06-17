#include "stdio.h"
#include "stdlib.h"

// http://www.gnu.org/software/libc/manual/html_node/String-Streams.html

int main(int argc, char* argv[])
{
  char *bp;
  size_t size;
  FILE *stream;

  stream = open_memstream (&bp, &size);
  fprintf (stream, "hello");
  fflush (stream);
  printf ("buf = `%s', size = %d\n", bp, size);
  fprintf (stream, ", world");
  fclose (stream);
  printf ("buf = `%s', size = %d\n", bp, size);

  free(bp); // clean up buffer!

  return 0;
}
