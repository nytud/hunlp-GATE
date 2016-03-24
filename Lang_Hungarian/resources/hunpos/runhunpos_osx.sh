#!/bin/sh

# Wrapper shell script to run the hunpos tagger from taggerframework.GenericTagger PR in GATE
# Platform: Mac OS X

HUNPOS_DIR=`pwd`/bin/macosx
HUNPOS_TAG=hunpos-tag
HUNPOS_MODEL=`pwd`/hu_szeged_kr.model

cd ${HUNPOS_DIR}
${HUNPOS_DIR}/${HUNPOS_TAG} ${HUNPOS_MODEL} < $1
