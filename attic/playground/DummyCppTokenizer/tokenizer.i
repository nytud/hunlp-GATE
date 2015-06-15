%module Tokenizer

%{
/* Includes the header in the wrapper code */
#include "tokenizer.h"
%}

/* Parse the header file to generate wrappers */
%include "tokenizer.h"
