%module Test


%{
/* Includes the header in the wrapper code */
#include "test.h"
%}

%include "carrays.i"
%array_class(long, longArray);
%array_class(OffsPair, OffsPairArray);

%include "typemaps.i"
%apply int *OUTPUT { int *x };

/* Parse the header file to generate wrappers */
%include "test.h"
