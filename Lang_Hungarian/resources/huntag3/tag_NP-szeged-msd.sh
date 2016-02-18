#!/bin/sh

MDIR=./models/NP-szeged-msd
CONFIG=${MDIR}/hunchunk.hunMIGE_simple.yaml
MODEL=${MDIR}/NP-szeged-msd

python3 huntag.py tag --model=${MODEL} --config-file=${CONFIG} < $1
