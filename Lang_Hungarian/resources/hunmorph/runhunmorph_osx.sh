#!/bin/sh

# Wrapper script for running Hunmorph from GenericTagger in GATE on Mac OS X

OMORPH=`pwd`/ocamorph/bin/macosx/ocamorph
LEXICON=`pwd`/morphdb.hu/morphdb_hu.bin

${OMORPH} --bin ${LEXICON} --tag_preamble "" --tag_sep " " < $1