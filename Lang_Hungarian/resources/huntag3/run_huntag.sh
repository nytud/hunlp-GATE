#!/bin/bash

if [ "$1" == "NER" ]; then
  MDIR=`pwd`/models/NER-szeged-KR-model
  CONFIG=${MDIR}/ner_hun_best.yaml
  MODEL=${MDIR}/NER-szeged-KR
elif [ "$1" == "NP" ]; then
  MDIR=`pwd`/models/NP-szeged-msd-model
  CONFIG=${MDIR}/hunchunk.hunMIGE_simple.yaml
  MODEL=${MDIR}/NP-szeged-msd
fi

python3 huntag.py tag --model=${MODEL} --config-file=${CONFIG}
