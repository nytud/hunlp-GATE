#!/bin/sh

# Wrapper script for running Hunmorph from GenericTagger in GATE.

# TODO: select platform dependent binary automatically
OMORPH=`pwd`/ocamorph/bin/linux.32/ocamorph
LEXICON=`pwd`/morphdb.hu/morphdb_hu.bin

${OMORPH} --bin ${LEXICON} --tag_preamble "" --tag_sep " " < $1