#!/usr/bin/python3
# -*- coding: utf-8, vim: expandtab:ts=4 -*-
# Miscellaneous tools for HunTag

from operator import itemgetter
from collections import Counter, defaultdict
import sys


def sentenceIterator(inputStream):
    currSen = []
    currComment = None
    for line in inputStream:
        line = line.strip()
        # Comment handling
        if line.startswith('"""'):
            if len(currSen) == 0:  # Comment before sentence
                currComment = line
            else:  # Error: Comment in the middle of sentence
                print('ERROR: comments are only allowed before a sentence!', file=sys.stderr, flush=True)
                sys.exit(1)
        # Blank line handling
        elif len(line) == 0:
            if currSen:  # End of sentence
                yield currSen, currComment
                currSen = []
                currComment = None
            else:  # Error: Multiple blank line
                print('ERROR: wrong formatted sentences, only one blank line allowed!', file=sys.stderr, flush=True)
                sys.exit(1)
        else:
            currSen.append(line.split())
    # XXX Here should be an error because of missing blank line before EOF
    if currSen:
        print('WARNING: No blank line before EOF!', file=sys.stderr, flush=True)
        yield currSen, currComment


def featurizeSentence(sen, features):
    sentenceFeats = [[] for _ in sen]
    for feature in features.values():
        for c, feats in enumerate(feature.evalSentence(sen)):
            sentenceFeats[c] += feats
    return sentenceFeats


class intGen:
    """
    Original source: http://stackoverflow.com/a/6173641
    """
    def __init__(self, i=-1):  # To start from 0...
        self.i = i

    def __call__(self):
        self.i += 1
        return self.i


# Keeps Feature/Label-Number translation maps, for faster computations
class BookKeeper:
    def __init__(self):
        self._counter = Counter()
        nextID = intGen()  # Initializes autoincr class
        self._nameToNo = defaultdict(nextID)
        self.noToName = {}  # This is built only upon reading back from file

    def makeInvertedDict(self):
        self.noToName = {}  # This is built only upon reading back from file
        for name, no in self._nameToNo.items():
            self.noToName[no] = name

    def numOfNames(self):
        return len(self._nameToNo)

    def makenoToName(self):
        self.noToName = {v: k for k, v in self._nameToNo.items()}

    def cutoff(self, cutoff):
        toDelete = {self._nameToNo.pop(name) for name, count in self._counter.items() if count < cutoff}
        del self._counter
        newNameNo = {name: i for i, (name, _) in enumerate(sorted(self._nameToNo.items(), key=itemgetter(1)))}
        del self._nameToNo
        self._nameToNo = newNameNo
        return toDelete

    def getNoTag(self, name):
        return self._nameToNo.get(name)  # Defaults to None

    def getNoTrain(self, name):
        self._counter[name] += 1
        return self._nameToNo[name]  # Starts from 0 newcomers will get autoincremented value and stored
