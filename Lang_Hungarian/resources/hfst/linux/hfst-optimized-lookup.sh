#!/bin/bash

if [ "`uname -m`" == "x86_64" ]; then
  DIR=`dirname $0`/x64
else
  DIR=`dirname $0`/x86
fi

if [ -e $DIR/hfst-optimized-lookup ]; then
  export LD_LIBRARY_PATH=$DIR
  $DIR/hfst-optimized-lookup "$@"
else
  hfst-optimized-lookup "$@"
fi