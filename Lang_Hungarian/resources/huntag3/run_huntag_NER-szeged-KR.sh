#!/bin/sh

MDIR=`pwd`/models/NER-szeged-KR-model
CONFIG=${MDIR}/ner_hun_best.yaml
MODEL=${MDIR}/NER-szeged-KR

python3 huntag.py tag --model=${MODEL} --config-file=${CONFIG} < $1
