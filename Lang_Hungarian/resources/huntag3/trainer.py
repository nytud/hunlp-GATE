#!/usr/bin/python3
# -*- coding: utf-8, vim: expandtab:ts=4 -*-

"""
trainer.py is a module of HunTag and is used to train maxent models
"""

import sys
from collections import Counter, defaultdict
from sklearn.externals import joblib
from scipy.sparse import csr_matrix
import numpy as np
from array import array
from sklearn.linear_model import LogisticRegression
# from sklearn.linear_model import SGDClassifier
# from sklearn.svm import SVC
# from sklearn.multiclass import OneVsRestClassifier

from tools import BookKeeper, sentenceIterator, featurizeSentence


class Trainer:
    def __init__(self, features, options):

        # Set clasifier algorithm here
        parameters = dict()  # dict(solver='lbfgs')
        solver = LogisticRegression

        # Possible alternative solvers:
        # parameters = {'loss':'modified_huber',  'n_jobs': -1}
        # solver = SGDClassifier

        # ‘linear’, ‘poly’, ‘rbf’, ‘sigmoid’, ‘precomputed’
        # parameters = {'kernel': 'rbf', 'probability': True}
        # solver = SVC

        # ‘linear’, ‘poly’, ‘rbf’, ‘sigmoid’, ‘precomputed’
        # parameters = {'kernel': 'linear', 'probability': True}
        # solver = OneVsRestClassifier(SVC(**parameters))  # XXX won't work because ** in parameters...

        self._model = solver(**parameters)
        self._dataSizes = options['dataSizes']
        self._tagField = options['tagField']
        self._modelFileName = options['modelFileName']
        self._parameters = options['trainParams']
        self._cutoff = options['cutoff']
        self._features = features

        self._tokCount = -1  # Index starts from 0

        self._rows = array(self._dataSizes['rows'])
        self._cols = array(self._dataSizes['cols'])
        self._data = array(self._dataSizes['data'])
        self._labels = array(self._dataSizes['labels'])
        self._sentEnd = array(self._dataSizes['sentEnd'])  # Keep track of sentence boundaries
        self._matrix = None

        self._featCounter = BookKeeper()
        self._labelCounter = BookKeeper()
        self._usedFeats = None
        if 'usedFeats' in options and options['usedFeats']:
            self._usedFeats = {line.strip() for line in open(options['usedFeats'], encoding='UTF-8')}

    def save(self):
        print('saving model, feature and label lists...', end='', file=sys.stderr, flush=True)
        joblib.dump((self._model, self._featCounter, self._labelCounter), '{0}'.format(self._modelFileName), compress=3)
        print('done', file=sys.stderr, flush=True)

    def _updateSentEnd(self, sentEnds, rowNums):
        newEnds = array(self._dataSizes['sentEnd'])
        vbeg = 0
        for end in sentEnds:
            vend = -1
            for i, e in enumerate(rowNums[vbeg:]):
                if e <= end:
                    vend = vbeg + i
                else:
                    break
            if vend > 0:
                newEnds.append(vend)
                vbeg = vend + 1
        return newEnds

    def _convertToNPArray(self):
        rowsNP = np.array(self._rows, dtype=self._dataSizes['rowsNP'])
        colsNP = np.array(self._cols, dtype=self._dataSizes['cols'])
        dataNP = np.array(self._data, dtype=self._dataSizes['data'])
        labelsNP = np.array(self._labels, dtype=self._dataSizes['labels'])
        del self._rows
        del self._cols
        del self._data
        del self._labels
        self._rows = rowsNP
        self._cols = colsNP
        self._data = dataNP
        self._labels = labelsNP

    def _makeSparseArray(self, rowNum, colNum):
        print('creating training problem...', end='', file=sys.stderr, flush=True)
        matrix = csr_matrix((self._data, (self._rows, self._cols)), shape=(rowNum, colNum),
                            dtype=self._dataSizes['data'])
        del self._rows
        del self._cols
        del self._data
        print('done!', file=sys.stderr, flush=True)
        return matrix

    def cutoffFeats(self):
        self._convertToNPArray()
        colNum = self._featCounter.numOfNames()
        if self._cutoff < 2:
            self._matrix = self._makeSparseArray(self._tokCount, colNum)
        else:
            print('discarding features with less than {0} occurences...'.format(self._cutoff), end='', file=sys.stderr,
                  flush=True)

            toDelete = self._featCounter.cutoff(self._cutoff)
            print('done!\nreducing training events by {0}...'.format(len(toDelete)), end='', file=sys.stderr,
                  flush=True)
            # ...that are not in featCounter anymore
            indicesToKeepNP = np.fromiter((ind for ind, featNo in enumerate(self._cols) if featNo not in toDelete),
                                          dtype=self._dataSizes['cols'])
            del toDelete

            # Reduce cols
            colsNPNew = self._cols[indicesToKeepNP]
            del self._cols
            self._cols = colsNPNew

            # Reduce data
            dataNPNew = self._data[indicesToKeepNP]
            del self._data
            self._data = dataNPNew

            # Reduce rows
            rowsNPNew = self._rows[indicesToKeepNP]
            rowNumKeep = np.unique(rowsNPNew)
            rowNum = rowNumKeep.shape[0]
            colNum = indicesToKeepNP.max() + 1
            del self._rows
            self._rows = rowsNPNew
            del indicesToKeepNP

            # Reduce labels
            labelsNPNew = self._labels[rowNumKeep]
            del self._labels
            self._labels = labelsNPNew

            # Update sentence end markers
            newEnd = self._updateSentEnd(self._sentEnd, rowNumKeep)
            del self._sentEnd
            self._sentEnd = newEnd
            del rowNumKeep

            print('done!', file=sys.stderr, flush=True)
            matrix = self._makeSparseArray(rowNum, colNum)
            print('updating indices...', end='', file=sys.stderr, flush=True)

            # Update rowNos
            rows, _ = matrix.nonzero()
            matrixNew = matrix[np.unique(rows), :]
            del matrix
            del rows

            # Update featNos
            _, cols = matrixNew.nonzero()
            self._matrix = matrixNew[:, np.unique(cols)]
            del matrixNew
            del cols

            print('done!', file=sys.stderr, flush=True)

    # Input need featurizing
    def getEvents(self, data):
        print('featurizing sentences...', end='', file=sys.stderr, flush=True)
        senCount = 0
        tokIndex = -1  # Index starts from 0
        for sen, _ in sentenceIterator(data):
            senCount += 1
            sentenceFeats = featurizeSentence(sen, self._features)
            for c, tok in enumerate(sen):
                tokIndex += 1
                tokFeats = sentenceFeats[c]
                if self._usedFeats:
                    tokFeats = [feat for feat in tokFeats if feat in self._usedFeats]
                self._addContext(tokFeats, tok[self._tagField], tokIndex)
            self._sentEnd.append(tokIndex)
            if senCount % 1000 == 0:
                print('{0}...'.format(str(senCount)), end='', file=sys.stderr, flush=True)

        self._tokCount = tokIndex + 1
        print('{0}...done!'.format(str(senCount)), file=sys.stderr, flush=True)

    # Already featurized input
    def getEventsFromFile(self, data):
        tokIndex = -1  # Index starts from 0
        for line in data:
            line = line.strip()
            if len(line) > 0:
                tokIndex += 1
                l = line.split()
                label, feats = l[0], l[1:]
                self._addContext(feats, label, tokIndex)
            self._sentEnd.append(tokIndex)
        self._tokCount = tokIndex + 1

    def _addContext(self, tokFeats, label, curTok):
        rowsAppend = self._rows.append
        colsAppend = self._cols.append
        dataAppend = self._data.append

        # Features are sorted to ensure identical output no matter where the features are coming from
        for featNumber in {self._featCounter.getNoTrain(feat) for feat in sorted(tokFeats)}:
            rowsAppend(curTok)
            colsAppend(featNumber)
            dataAppend(1)

        self._labels.append(self._labelCounter.getNoTrain(label))

    # Counting zero elements can be really slow...
    def mostInformativeFeatures(self, outputStream=sys.stdout, n=-1, countZero=False):
        # Compute min(P(feature=value|label1), for any label1)/max(P(feature=value|label2), for any label2)
        # (using contitional probs using joint probabilities) as in NLTK (Bird et al. 2009):
        # P(feature=value|label) = P(feature=value, label)/P(label)
        # P(feature=value, label) = C(feature=value, label)/C(feature=value)
        # P(label) = C(label)/sum_i(C(label_i))
        #
        # P(feature=value|label) = (C(feature=value, label)/C(feature=value))/(C(label)/sum_i(C(label_i))) =
        # (C(feature=value, label)*sum_i(C(label_i)))/(C(feature=value)*C(label))
        #
        # min(P(feature=value|label1), for any label1)/max(P(feature=value|label2), for any label2) =
        #
        # min((C(feature=value, label1)*sum_i(C(label_i)))/(C(feature=value)*C(label1)), for any label1)/
        # max((C(feature=value, label2)*sum_i(C(label_i)))/(C(feature=value)*C(label2)), for any label2) =
        #
        # (sum_i(C(label_i))/C(feature=value))*min(C(feature=value, label1)/C(label1)), for any label1)/
        # (sum_i(C(label_i))/C(feature=value))*max(C(feature=value, label2)/C(label2)), for any label2) =
        #
        # min(C(feature=value, label1)/C(label1), for any label1)/
        # max(C(feature=value, label2)/C(label2), for any label2)
        matrix = self._matrix  # For easiser handling
        self._featCounter.makenoToName()
        self._labelCounter.makenoToName()
        featnoToName = self._featCounter.noToName
        labelnoToName = self._labelCounter.noToName
        labels = self._labels  # indexed by token rows (row = token number, column = feature number)
        featValCounts = defaultdict(Counter)  # feat, val -> label: count

        if countZero:
            # Every index (including zeros to consider negative correlation)
            for feat in range(matrix.shape[1]):
                for tok in range(matrix.shape[0]):
                    featValCounts[feat, matrix[tok, feat]][labels[tok]] += 1
        else:
            matrix = matrix.tocoo()
            # Every nonzero index
            for tok, feat, val in zip(matrix.row, matrix.col, matrix.data):
                featValCounts[feat, val][labels[tok]] += 1
        del matrix

        # (C(label2), for any label2)
        labelCounts = Counter()
        for k, v in zip(*np.unique(self._labels, return_counts=True)):
            labelCounts[k] = v

        numOfLabels = len(labelCounts)
        maxprob = defaultdict(lambda: 0.0)
        minprob = defaultdict(lambda: 1.0)
        features = set()
        # For every (feature, val) touple (that has nonzero count)
        for feature, counts in featValCounts.items():
            # For every label label...
            features.add(feature)
            for label, count in counts.items():
                # prob can only be 0 if the nominator is 0, but this case is already filtered in the Counter...
                prob = count/labelCounts[label]
                maxprob[feature] = max(prob, maxprob[feature])
                minprob[feature] = min(prob, minprob[feature])

        # Convert features to a list, & sort it by how informative features are.
        """
        From NTLK docs:
        For the purpose of this function, the
        informativeness of a feature ``(fname,fval)`` is equal to the
        highest value of P(fname=fval|label), for any label, divided by
        the lowest value of P(fname=fval|label), for any label:

        |  max[ P(fname=fval|label1) / P(fname=fval|label2) ]
        """
        print('"Feature name"=Value (True/False)', 'Sum of occurences', 'Counts per label', 'Probability per label',
              'Max prob.:Min prob.=Ratio:1.0', sep='\t', file=outputStream)  # Print header (legend)
        # To avoid division by zero...
        for feature in sorted(features, key=lambda feature_: minprob[feature_]/maxprob[feature_])[:n]:
            sumOccurences = sum(featValCounts[feature].values())
            if len(featValCounts[feature]) < numOfLabels:
                ratio = 'INF'
            else:
                ratio = maxprob[feature]/minprob[feature]
            # NLTK notation
            # print('{0:50} = {1:} {2:6} : {3:-6} = {4} : 1.0'.format(featnoToName(feature[0]), feature[1],
            #                                                                maxprob[feature],
            #                                                                minprob[feature], ratio))
            # More detailed notation
            print('"{0:50s}"={1}\t{2}\t{3}\t{4}\t{5:6}:{6:-6}={7}:1.0'.format(
                featnoToName[feature[0]],
                bool(feature[1]),
                sumOccurences,
                '/'.join(('{0}:{1}'.format(labelnoToName[l], c)
                          for l, c in featValCounts[feature].items())),
                '/'.join(('{0}:{1:.8f}'.format(labelnoToName[l], c/labelCounts[l])
                          for l, c in featValCounts[feature].items())),
                maxprob[feature], minprob[feature], ratio), file=outputStream)

    def toCRFsuite(self, outputStream=sys.stdout):
        self._featCounter.makenoToName()
        self._labelCounter.makenoToName()
        featnoToName = self._featCounter.noToName
        labelnoToName = self._labelCounter.noToName
        sentEnd = self._sentEnd
        matrix = self._matrix.tocsr()
        labels = self._labels
        beg = 0
        for end in sentEnd:
            for row in range(beg, end + 1):
                print('{0}\t{1}'.format(labelnoToName[labels[row]], '\t'.join(featnoToName[col].replace(':', 'colon')
                                                                              for col in matrix[row, :].nonzero()[1])),
                      file=outputStream)
            print(file=outputStream)  # Sentence separator blank line
            beg = end + 1

    def train(self):
        print('training with option(s) "{0}"...'.format(self._parameters), end='', file=sys.stderr, flush=True)
        _ = self._model.fit(self._matrix, self._labels)
        print('done', file=sys.stderr, flush=True)
