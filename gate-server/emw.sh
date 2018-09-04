#! /usr/bin/env bash


# here-document
read -d '' USAGE <<- EOF
emw (e-magyar wrapper) reads from the standard input or files, processes the
text with the e-magyar toolchain and writes it to standard output.

USAGE: emw.sh [OPTION]... [FILE]...

OPTIONS:

    -h, --help           display this help and exit
    -t, --target TARGET  targeted level of analisys (default:all)
    -u, --url-encoding   URL encode the input then exit

Valid targets:
    tok: sentence splitter and tokenizer
    mor: morphological analyser + word stemmer
    pos: POS tagger
    dep: dependency parser
    con: constituent parser
    npc: NP chunker
    ner: NER tagger
    all: all e-magyar modules are active (this is the default)

Examples:
    cat *.txt | emw.sh -t ner
    emw.sh -t ner *.txt
EOF


# commandline arguments
while [[ $# -gt 0 ]]
do
    case "$1" in
        -t|--target)
            if [[ -z "$2" ]] ; then 
                echo "No given target! Exit."
                exit 1
            else
                TARGET="$2"
            fi
            shift
            shift
            ;;
        -u|--url-encoding)
            URL="true"
            shift
            ;;
        -h|--help)
            echo "$USAGE"
            exit 0
            ;;
        *)
            if [[ -f "$1" ]] ; then
                FILES+=" $1"
            else
                echo "$0: $1: No such file or directory! Exit."
                exit 1
            fi
            shift
            ;;
    esac
done


# input text
[[ -z $FILES ]] && TEXT=`cat -` || TEXT=`cat $FILES`
TEXT=`echo -n $TEXT | sed s/\'/\\\\\\\x27/g`
TEXT=`python -c "import urllib; print urllib.quote_plus('''$TEXT''')"`


# URL encoding
if [[ $URL ]] ; then
    echo "$TEXT"
    exit 0
fi


# main target
if [[ -z $TARGET ]] ; then
    TARGET="all"
fi


# modules
case $TARGET in
    tok)
        MODULES='QT'
        ;;
    mor)
        MODULES='QT,HFSTLemm'
        ;;
    pos)
        MODULES='QT,HFSTLemm,ML3-PosLem-hfstcode'
        ;;
    dep)
        MODULES='QT,HFSTLemm,ML3-PosLem-hfstcode,ML3-Dep,Preverb'
        ;;
    con)
        MODULES='QT,HFSTLemm,ML3-PosLem-hfstcode,ML3-Cons'
        ;;
    npc)
        MODULES='QT,HFSTLemm,ML3-PosLem-hfstcode,huntag3-NP-pipe-hfstcode,IOB4NP'
        ;;
    ner)
        MODULES='QT,HFSTLemm,ML3-PosLem-hfstcode,huntag3-NER-pipe-hfstcode,IOB4NER'
        ;;
    all)
        MODULES='QT,HFSTLemm,ML3-PosLem-hfstcode,ML3-Dep,Preverb,ML3-Cons,huntag3-NER-pipe-hfstcode,IOB4NER,huntag3-NP-pipe-hfstcode,IOB4NP'
        ;;
    *)
        echo "'$TARGET' is not valid target! Exit."
        exit 1
        ;;
esac


# # check variables
# echo 'TARGET:' $TARGET
# echo 'MODULES:' $MODULES
# echo 'FILES:' $FILES
# echo -e "TEXT:'''\n$TEXT\n'''"


# result
wget -q -O- "http://localhost:8000/process?run=$MODULES&text=$TEXT"



