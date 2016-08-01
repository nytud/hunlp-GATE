#!/bin/bash

# NER: NER with KR codes (old NER model)
if [ "$1" == "NER" ]; then
  MDIR=`pwd`/models/NER-szeged-KR-model
  CONFIG=${MDIR}/ner_hun_best.yaml
  # canonical name should be: CONFIG=${MDIR}/ner.szeged.kr.yaml
  MODEL=${MDIR}/NER-szeged-KR

# NP: maxNP chunking with msd codes (old NP model)
elif [ "$1" == "NP" ]; then
  MDIR=`pwd`/models/NP-szeged-msd-model
  CONFIG=${MDIR}/hunchunk.hunMIGE_simple.yaml
  MODEL=${MDIR}/NP-szeged-msd

# NER_hfst: NER with (HFST-based) eM-morph codes
elif [ "$1" == "NER_hfst" ]; then
  MDIR=`pwd`/models/NER-szeged-hfst-model
  CONFIG=${MDIR}/ner.szeged.hfst.yaml
  MODEL=${MDIR}/NER-szeged-hfst

# NP_hfst: maxNP chunking with (HFST-based) eM-morph codes
elif [ "$1" == "NP_hfst" ]; then
  MDIR=`pwd`/models/maxNP-szeged-hfst-model
  CONFIG=${MDIR}/maxnp.szeged.hfst.yaml
  MODEL=${MDIR}/maxNP-szeged-hfst

fi

python3 huntag.py tag --model=${MODEL} --config-file=${CONFIG}
