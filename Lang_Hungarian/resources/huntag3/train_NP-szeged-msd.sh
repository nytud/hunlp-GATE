#!/bin/sh

MDIR=./models/NP-szeged-msd
TRAIN=${MDIR}/szeged.tree.MSD.MIGE.full.txtu.train+test.simple.gz
CONFIG=${MDIR}/hunchunk.hunMIGE_simple.yaml
MODEL=${MDIR}/NP-szeged-msd

zcat ${TRAIN} | python3 huntag.py train --model=${MODEL} --config-file=${CONFIG}
zcat ${TRAIN} | python3 huntag.py transmodel-train --model=${MODEL}
