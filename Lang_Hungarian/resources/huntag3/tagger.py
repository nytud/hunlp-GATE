#!/usr/bin/python3
# -*- coding: utf-8, vim: expandtab:ts=4 -*-

import sys
import os
from sklearn.externals import joblib
from scipy.sparse import csr_matrix

from tools import sentenceIterator, featurizeSentence, BookKeeper


class Tagger:
    def __init__(self, features, transModel, options):
        self._features = features
        self._dataSizes = options['dataSizes']
        self._transProbs = transModel
        print('loading observation model...', end='', file=sys.stderr, flush=True)
        self._model = joblib.load('{0}'.format(options['modelFileName']))
        self._featCounter = BookKeeper(options['featCounterFileName'])
        self._labelCounter = BookKeeper(options['labelCounterFileName'])
        print('done', file=sys.stderr, flush=True)

    def printWeights(self, n=100, outputStream=sys.stdout):
        coefs = self._model.coef_
        labelNoToName = self._labelCounter.noToName
        featNoToName = self._featCounter.noToName
        sortedFeats = sorted(featNoToName.items())
        for i, label in sorted(labelNoToName.items()):
            columns = ['{0}:{1}'.format(w, feat) for w, (no, feat) in sorted(zip(coefs[i, :], sortedFeats),
                                                                             reverse=True)]
            print('{0}\t{1}'.format(label, '\t'.join(columns[:n])), file=outputStream)  # Best
            # Worst -> Negative correlation
            print('{0}\t{1}'.format(label, '\t'.join(sorted(columns[-n:], reverse=True))), file=outputStream)

    def tagFeatures(self, data):
        senFeats = []
        senCount = 0
        for line in data:
            line = line.strip()
            if len(line) == 0:
                senCount += 1
                tagging = self._tagSenFeats(senFeats)
                yield [[tag] for tag in tagging]
                senFeats = []
                if senCount % 1000 == 0:
                    print('{0}...'.format(senCount), end='', file=sys.stderr, flush=True)
            senFeats.append(line.split())
        print('{0}...done'.format(senCount), file=sys.stderr, flush=True)

    def tagDir(self, dirName):
        for fn in os.listdir(dirName):
            print('processing file {0}...'.format(fn), end='', file=sys.stderr, flush=True)
            for sen, _ in self.tagCorp(open(os.path.join(dirName, fn), encoding='UTF-8')):
                yield sen, fn

    def tagCorp(self, inputStream=sys.stdin):
        senCount = 0
        for sen, comment in sentenceIterator(inputStream):
            senCount += 1
            senFeats = featurizeSentence(sen, self._features)
            bestTagging = self._tagSenFeats(senFeats)
            taggedSen = [tok + [bestTagging[c]] for c, tok in enumerate(sen)]  # Add tagging to sentence
            yield taggedSen, comment
            if senCount % 1000 == 0:
                print('{0}...'.format(senCount), end='', file=sys.stderr, flush=True)
        print('{0}...done'.format(senCount), file=sys.stderr, flush=True)

    def _getTagProbsByPos(self, senFeats):
        # Get Sentence Features translated to numbers and contexts in two steps
        getNoTag = self._featCounter.getNoTag
        featNumbers = [{getNoTag(feat) for feat in feats if getNoTag(feat) is not None} for feats in senFeats]

        rows = []
        cols = []
        data = []
        for rownum, featNumberSet in enumerate(featNumbers):
            for featNum in featNumberSet:
                rows.append(rownum)
                cols.append(featNum)
                data.append(1)
        contexts = csr_matrix((data, (rows, cols)), shape=(len(featNumbers), self._featCounter.numOfNames()),
                              dtype=self._dataSizes['dataNP'])
        tagProbsByPos = [{self._labelCounter.noToName[i]: prob for i, prob in enumerate(probDist)}
                         for probDist in self._model.predict_proba(contexts)]
        return tagProbsByPos

    def toCRFsuite(self, inputStream, outputStream=sys.stdout):
        senCount = 0
        getNoTag = self._featCounter.getNoTag
        featnoToName = self._featCounter.noToName
        for sen, comment in sentenceIterator(inputStream):
            senCount += 1
            senFeats = featurizeSentence(sen, self._features)
            # Get Sentence Features translated to numbers and contexts in two steps
            for featNumberSet in ({getNoTag(feat) for feat in feats if getNoTag(feat) is not None}
                                  for feats in senFeats):
                print('\t'.join(featnoToName[featNum].replace(':', 'colon') for featNum in featNumberSet),
                      file=outputStream)
            print(file=outputStream)  # Sentence separator blank line
            if senCount % 1000 == 0:
                print('{0}...'.format(str(senCount)), end='', file=sys.stderr, flush=True)
        print('{0}...done'.format(str(senCount)), file=sys.stderr, flush=True)

    def _tagSenFeats(self, senFeats):
        return self._transProbs.tagSent(self._getTagProbsByPos(senFeats))
