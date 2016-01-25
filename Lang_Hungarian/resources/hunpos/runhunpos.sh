#!/bin/sh

# Wrapper shell script to run the hunpos tagger from taggerframework.GenericTagger PR in GATE
# with the Hungarian PoS model
# Platforms: Linux (32 and 64-bit)
# TODO: Mac OS X

# TODO: this should be automatically platform-dependent
HUNPOS_DIR=`pwd`/bin/linux.32
HUNPOS_TAG=hunpos-tag
HUNPOS_MODEL=`pwd`/hu_szeged_kr.model

cd ${HUNPOS_DIR}
${HUNPOS_DIR}/${HUNPOS_TAG} ${HUNPOS_MODEL} < $1
