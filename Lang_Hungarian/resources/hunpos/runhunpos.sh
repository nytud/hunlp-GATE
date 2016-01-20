#!/bin/sh

# Wrapper shell script to run the hunpos tagger from taggerframework.GenericTagger PR in GATE
# Platforms: Linux (32 and 64-bit)
# TODO: Mac OS X


# set the correct location of your hunpos installation here
# TODO: this should be automatically platform-dependent
HUNPOS_DIR=`pwd`/bin/linux.32
HUNPOS_TAG=hunpos-tag

cd ${HUNPOS_DIR}
${HUNPOS_DIR}/${HUNPOS_TAG} $1 < $2
