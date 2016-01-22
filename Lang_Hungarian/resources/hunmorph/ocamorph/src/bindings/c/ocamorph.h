

//#DEFINE GUESS_NO 0
//#DEFINE GUESS_FALLBACK 1
//#DEFINE GUESS_ALL 2

typedef long* ocaml_object;

typedef ocaml_object ocamorph_engine;
typedef ocaml_object ocamorph_analyzer;


// This function initializes the Ocaml runtime environment.
// Must be called before everything else.
void ocamorph_startup();

// This function initializes the ocamorph engine with resources
// and returns an object that can provide analyzers with 
// various runtime options
ocamorph_engine init_from_aff_dic( const char* aff, const char* dic, const char* bin, const int no_caps );
ocamorph_engine init_from_bin( const char* bin, const int no_caps );


// this makes the analyzers with various runtime options 
// stop_at_first    0 1 for indexing
// blocking         0 1
// compounds        0 1
// guess            0 1 2 
ocamorph_analyzer make_analyzer( const ocamorph_engine engine, const int stop_at_first, const int blocking, const int compounds, const int guess );

// calls the analyzer created by 'make_analyzer' 
// substantive return values are memory managed by the caller, i.e., 
// the string array char** is assumed to be preallocated and freed
// by the user
// array length is limited in maxanal
// string length of one analysis is limited in maxanallen
// returns number of actual analyses (int)
// this is important if result char** is a constant buffer that persist
// during several calls to analyzer, since previous analyses are not
// overridden
int analyze( ocamorph_analyzer analyzer, const char * word, char ** result, const int maxanal, const int maxanallen );

