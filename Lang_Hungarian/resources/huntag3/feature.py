#!/usr/bin/python3
# -*- coding: utf-8, vim: expandtab:ts=4 -*-
"""
feature.py is a module of HunTag. The Feature class is used for representing
a feature type and calculating its value for some input. Feature instances are
created by the getFeatureSet function in huntag.py.
"""

import sys

import features


class Feature:
    def __init__(self, kind, name, actionName, fields, radius, cutoff, options):
        self.kind = kind
        self.name = name
        self.actionName = actionName
        self.fields = fields
        self.radius = int(radius)
        self.cutoff = int(cutoff)
        self.options = options
        if self.kind in ('token', 'lex') and len(self.fields) != 1:
            print('Error: Feature (token, lex) "{0}" field count must be one not {1}!'.format(self.name, self.fields),
                  file=sys.stderr, flush=True)
            sys.exit(1)
        if self.kind == 'lex':
            if len(self.options) > 0:
                print('Lexicon features do not yet support options', file=sys.stderr, flush=True)
                sys.exit(1)
            self.lexicon = Lexicon(actionName)  # Load input file

        elif self.kind in ('token', 'sentence'):
            functionName = '{0}_{1}'.format(self.kind, self.actionName)
            if functionName not in features.__dict__:
                print('Unknown operator named {0}\n'.format(self.actionName), file=sys.stderr, flush=True)
                sys.exit(1)
            self.function = features.__dict__[functionName]
        else:
            print('Unknown kind named {0}'.format(self.kind), file=sys.stderr, flush=True)
            sys.exit(1)

    def evalSentence(self, sentence):
        if self.kind == 'token':
            # Pick the relevant fields (label can be not just the last field)
            featVec = [self.function(word[self.fields[0]], self.options) for word in sentence]
        elif self.kind == 'lex':
            # Word will be substituted by its features from the Lexicon
            # self.fields denote the column of the word
            featVec = self.lexicon.lexEvalSentence([word[self.fields[0]] for word in sentence])
        elif self.kind == 'sentence':
            featVec = self.function(sentence, self.fields, self.options)
        else:
            print('evalSentence: Unknown kind named {0}'.format(self.kind), file=sys.stderr, flush=True)
            sys.exit(1)
        return self._multiplyFeatures(sentence, featVec)

    def _multiplyFeatures(self, sentence, featVec):
        sentenceLen = len(sentence)
        multipliedFeatVec = [[] for _ in range(sentenceLen)]
        for c in range(sentenceLen):
            # Iterate the radius, but keep the bounds of the list!
            for pos in range(max(c - self.radius, 0),
                             min(c + self.radius + 1, sentenceLen)):
                # All the feature that assigned for a token
                for feat in featVec[pos]:
                    if feat != 0:  # XXX feat COULD BE string...
                        multipliedFeatVec[c].append('{0}[{1}]={2}'.format(self.name, pos - c, feat))
        return multipliedFeatVec


class Lexicon:
    """
    the Lexicon class generates so-called lexicon features
    an instance of Lexicon() should be initialized for each lexicon file
    """
    def __init__(self, inputFile):
        self.phraseList = set()
        self.endParts = set()
        self.midParts = set()
        self.startParts = set()
        for line in open(inputFile, encoding='UTF-8'):
            phrase = line.strip()
            self.phraseList.add(phrase)
            words = phrase.split()
            if len(words) > 1:
                self.endParts.add(words[-1])
                self.startParts.add(words[0])
                for w in words[1:-1]:
                    self.midParts.add(w)

    def _getWordFeats(self, word):
        wordFeats = []
        if word in self.phraseList:
            wordFeats.append('lone')
        if word in self.endParts:
            wordFeats.append('end')
        if word in self.startParts:
            wordFeats.append('start')
        if word in self.midParts:
            wordFeats.append('mid')
        return wordFeats

    def lexEvalSentence(self, sentence):
        return [self._getWordFeats(word) for word in sentence]
