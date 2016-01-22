#include <stdio.h>  //for printf
#include <stdlib.h> //for malloc
#include <ocamorph.h> // for the analyzer

#define MAX_ANALYSIS 100
#define ANALYSIS_MAXLEN 100

void cerr(char* txt)
{
  fprintf(stderr,txt);
}

int main(int argc, char ** argv)
{
  /* Initialize OCaml Engine */
  cerr("caml_startup\n");
  ocamorph_startup();
  /* initialize engine and analyzer "objects"
  */

 ocamorph_engine engine;
 ocamorph_analyzer analyzer;

 // initialize the analysis string
  char analysis[ANALYSIS_MAXLEN];
  // initialize input buffer
  char buffer[500];
  char* analyses[MAX_ANALYSIS];
  int i;
  for (i=0; i<MAX_ANALYSIS;i++) {
    analyses[i] = (char *) malloc(ANALYSIS_MAXLEN * sizeof(char));
  };
  
  // check arguments
  //if (argc != 3) {
  //  cerr("usage: cmorph aff dic");
  //}
  cerr("start test\n");
  //char ** analyses ;

  // ez atrakando a stub-ba. egyszer lefoglalja, majd talan visszaadja?
  // vagy ha nem kell thread safe, mert most se az, akkor mehet static-ba
  //analyses = (char **) malloc(MAX_ANALYSIS*ANALYSIS_MAXLEN, sizeof(char));
  
  /* initialize analysis engine with resources
     init(aff,dic, bin) 
     if dic and aff are given then a bin file is dumped if bin is nonzero
     if dic and aff are given then init attempts to read the bin file
	     in the former case this step might take a while since it involves
     the construction of a compacted trie
     the latter case reads the ocamorph native format in matter of a second
  */
  cerr("init\n");
  int no_caps = 0 ; // yes_caps: the original, Latin-1-dependent capitalization-handling is running.
  if (argc == 3) { engine = init_from_aff_dic(argv[1],argv[2],"",no_caps); }
  else if (argc == 2) { engine = init_from_bin(argv[1],no_caps); }
  else if (argc == 4) { engine = init_from_aff_dic(argv[1],argv[2],argv[3],no_caps); }
  else { cerr("wrong number of arguments\n"); exit(1); } ;
  cerr("ok\n");
  /* construct various the analyzers
     make_analyzer(engine,stop_at_first,blocking,compounds, guess)
     gives an analyzer with the given runtime options
     see ocamorph.h
   */
  cerr("make analyzer\n");
  analyzer = make_analyzer(engine,0,0,1,0);


  cerr("ok\n");
  /* use the analyzer */ 
  cerr("analyze from standard input\n");
  while ( fgets(buffer, 500, stdin) ) {
   
    // chomp(buffer)
    for (i=0; buffer[i] != '\012'; ++i) {};
    buffer[i] = 0;
	
    printf("%s",buffer);
    
    int n = analyze(analyzer,buffer,analyses,MAX_ANALYSIS, ANALYSIS_MAXLEN);
	
    for (i=0; i < n; ++i) {
      printf("\t%s",analyses[i]);
    }
    printf("\n");
  }
  return 0;
}
