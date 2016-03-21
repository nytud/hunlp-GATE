%module DummyCTokenizerWrapper

%{
/* Includes the header in the wrapper code */
#include "tokenizer.h"
%}

// To manage arrays
%include "carrays.i"
%array_class(int, IntArray);

// To manage output parameters via int*
%include "typemaps.i"
%apply int *OUTPUT { int *ntokens };
%apply int *OUTPUT { int *nwhites };

/* Parse the header file to generate wrappers */
%include "tokenizer.h"
