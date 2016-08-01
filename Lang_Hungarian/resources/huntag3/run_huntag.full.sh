#!/bin/bash

#NER: NER with KR codes
if [ "$1" == "NER" ]; then
  MDIR=`pwd`/models/NER-szeged-KR-model
  CONFIG=${MDIR}/ner.szeged.kr.yaml
  MODEL=${MDIR}/NER-szeged-KR

#NER_univmorf: NER with Universal Morphology codes
elif [ "$1" == "NER_univmorf" ]; then
  MDIR=`pwd`/models/NER-szeged-univmorf-model
  CONFIG=${MDIR}/ner.szeged.univmorf.yaml
  MODEL=${MDIR}/NER-szeged-univmorf

#NER_hfst: NER with (HFST-based) eM-morph codes
elif [ "$1" == "NER_hfst" ]; then
  MDIR=`pwd`/models/NER-szeged-hfst-model
  CONFIG=${MDIR}/ner.szeged.hfst.yaml
  MODEL=${MDIR}/NER-szeged-hfst

#commented because it won't be used in the pipeline
#NP: maxNP chunking with MSD codes
#elif [ "$1" == "NP" ]; then
#  MDIR=`pwd`/models/maxNP-szeged-msd-model
#  CONFIG=${MDIR}/hunchunk.hunMIGE_simple.yaml
#  MODEL=${MDIR}/NP-szeged-msd

#NP_KR: maxNP chunking with KR codes
elif [ "$1" == "NP_KR" ]; then
  MDIR=`pwd`/models/maxNP-szeged-KR-model
  CONFIG=${MDIR}/maxnp.szeged.kr.yaml
  MODEL=${MDIR}/maxNP-szeged-KR

#NP_hfst: maxNP chunking with (HFST-based) eM-morph codes
elif [ "$1" == "NP_hfst" ]; then
  MDIR=`pwd`/models/maxNP-szeged-hfst-model
  CONFIG=${MDIR}/maxnp.szeged.hfst.yaml
  MODEL=${MDIR}/maxNP-szeged-hfst

#NP_univmorf: maxNP chunking with univmorf codes
elif [ "$1" == "NP_univmorf" ]; then
  MDIR=`pwd`/models/maxNP-szeged-univmorf-model
  CONFIG=${MDIR}/maxnp.szeged.univmorf.yaml
  MODEL=${MDIR}/maxNP-szeged-univmorf

#allP_hfst: all chunktypes with (HFST-based) eM-morph codes
elif [ "$1" == "allP_hfst" ]; then
  MDIR=`pwd`/models/allP-szeged-hfst-model
  CONFIG=${MDIR}/allp.szeged.hfst.yaml
  MODEL=${MDIR}/allP-szeged-hfst

#allP_KR: all chunktypes with KR codes
elif [ "$1" == "allP_KR" ]; then
  MDIR=`pwd`/models/allP-szeged-KR-model
  CONFIG=${MDIR}/allp.szeged.kr.yaml
  MODEL=${MDIR}/allP-szeged-KR

#allP_univmorf: all chunktypes with univmorf codes
elif [ "$1" == "allP_univmorf" ]; then
  MDIR=`pwd`/models/allP-szeged-univmorf-model
  CONFIG=${MDIR}/allp.szeged.univmorf.yaml
  MODEL=${MDIR}/allP-szeged-univmorf
fi

python3 huntag.py tag --model=${MODEL} --config-file=${CONFIG}
