#include "ocamorph.h"

#include <string.h>
#include <caml/mlvalues.h>
#include <caml/callback.h>
#include <caml/alloc.h>
#include <caml/memory.h>
#include <caml/fail.h>

#include <stdio.h>


void ocamorph_startup()
{
  char* dummyargv[2];
  dummyargv[0]="cmorph";
  dummyargv[1]=0;
  caml_startup(dummyargv);
}

ocamorph_engine init_from_aff_dic(const char* aff, const char* dic, const char* bin, const int no_caps)
{
  
  static value* init_fun;
  if (init_fun == NULL) {
  	init_fun = caml_named_value("init_from_aff_dic");
  }
  
  ocamorph_engine engine = (ocamorph_engine) malloc(sizeof(value));
  
  CAMLparam0();
  CAMLlocalN ( args, 4 );
  args[0] = caml_copy_string(aff);
  args[1] = caml_copy_string(dic);
  args[2] = caml_copy_string(bin);
  args[3] = Val_bool(no_caps);

  caml_register_global_root(engine);
  *engine = caml_callbackN( *init_fun, 4, args );

  return engine;
}

ocamorph_engine init_from_bin(const char* bin, const int no_caps)
{
  static value* init_fun;
  if (init_fun == NULL) {
  	init_fun = caml_named_value("init_from_bin");
  }
  ocamorph_engine engine = (ocamorph_engine) malloc(sizeof(value));
  caml_register_global_root(engine);
	
  *engine = caml_callback2( *init_fun, caml_copy_string(bin), Val_bool(no_caps) );


  return engine;
}

ocamorph_analyzer make_analyzer( ocamorph_engine engine , const int blocking, const int stop_at_first, const int compounds, const int guess )
{
  CAMLparam0();
  CAMLlocalN ( args, 4 );

  args[0] = Val_bool(stop_at_first);  
  args[1] = Val_bool(blocking);
  args[2] = Val_bool(compounds);
  args[3] = Val_int(guess);
 
  ocamorph_analyzer analyzer = (ocamorph_analyzer) malloc(sizeof(value));

  caml_register_global_root(analyzer);
  
  *analyzer = caml_callbackN(*engine, 4, args);

  CAMLreturn (analyzer);
}

int analyze( ocamorph_analyzer analyzer, const char * word, char ** result, const int maxanal, const int maxanallen )
{
  CAMLparam0();
  CAMLlocal2 (return_value,analyses);

  int i;
  int mlen;

  return_value = caml_callback(*analyzer, caml_copy_string(word));

  mlen = Int_val(Field(return_value,0));
  analyses = Field(return_value,1);
  if (mlen > maxanal) mlen = maxanal;
  
  for (i = 0; i < mlen; ++i) {
    //printf("%s\n", String_val(Field(analyses, i)));	
    strncpy(result[i], String_val(Field(analyses, i)), maxanallen);
  }
  //printf("tag val: %i\n", Tag_val(analyses));
  //printf("wsize: %u\n", Wosize_val(analyses));
  
  
  //value field = Field(analyses, 2);
  //printf("val string: %s\n", String_val(field));
  CAMLreturn(mlen);
}

//void analyze(const ocamorph_func analyzer, const char * word, char * result, const int maxlen) {
//  ocamorph_func result ;
//  strncpy(result,String_val(caml_callback(analyzer,caml_copy_string(word))),maxlen);
//}

