#!/usr/bin/python3
# -*- coding: utf-8, vim: expandtab:ts=4 -*-
"""
bigram.py contains the Bigram class which implements a simple bigram model
which can be built from observations of type (word1, word2). Bigram models are
built and used by HunTag
"""

import sys
import math
import pickle
from collections import Counter

from tools import sentenceIterator


def safe_div(v1, v2):
    """
    Safe floating point division function, does not allow division by 0
    returns -1 if the denominator is 0

    Args:
        v1: numerator
        v2: denominator
    """
    if v2 == 0:
        return -1.0
    else:
        return float(v1) / float(v2)


# Bigram or Trigram transition model
class TransModel:
    def __init__(self, tagField=-1, smooth=0.000000000000001, boundarySymbol='S', lmw=1.0, order=3):
        self._unigramCount = Counter()
        self.unigramLogProb = {}
        self._labda1 = 0.0
        self._bigramCount = Counter()
        self.bigramLogProb = {}
        self._labda2 = 0.0
        self._trigramCount = Counter()
        self.trigramLogProb = {}
        self._labda3 = 0.0
        self._obsCount = 0
        self._sentCount = 0
        self.tags = set()
        self.updated = True
        self.reset()

        self._tagField = tagField
        self._logSmooth = math.log(float(smooth))
        self._boundarySymbol = boundarySymbol
        self._languageModelWeight = float(lmw)
        self._order = int(order)
        if self._order == 2:
            self.viterbi = self._viterbiBigram
        elif self._order == 3:
            self.viterbi = self._viterbiTrigram
        else:
            print('Error: Transition modell order should be 2 or 3 got {0}!'.format(order), file=sys.stderr, flush=True)
            sys.exit(1)

        self._updateWarning = 'WARNING: Probabilities have not been recalculated since last input!'

    def reset(self):
        self._unigramCount = Counter()
        self._bigramCount = Counter()
        self._trigramCount = Counter()
        self._obsCount = 0
        self._sentCount = 0
        self.updated = True

    # Tag a sentence given the probability dists. of words
    def tagSent(self, tagProbsByPos):
        return self.viterbi(tagProbsByPos)[1]

    # Train a Stream
    def train(self, inputStream):
        for sen, _ in sentenceIterator(inputStream):
            self.obsSequence((tok[self._tagField] for tok in sen))

    # Train a Sentence (Either way we count trigrams, but later we will not use them)
    def obsSequence(self, tagSequence):
        lastBefore = self._boundarySymbol
        last = self._boundarySymbol
        # Add the two boundary symbol to the counts...
        self._bigramCount[self._boundarySymbol, self._boundarySymbol] += 1
        self._unigramCount[self._boundarySymbol] += 2
        self._obsCount += 2
        # Count sentences, for later normalization
        self._sentCount += 1 
        for tag in tagSequence:
            self.obs(lastBefore, last, tag)
            lastBefore = last
            last = tag
        # XXX Maybe we should make explicit difference between sentence begin sentence end
        self.obs(lastBefore, last, self._boundarySymbol)

    # Train a Bigram or Trigram (Compute trigrams, and later optionally use bigrams only)
    # To train directly a bigram use: obs(nMinusOne=firstToken, nth=secondToken) or obs(None, firstToken, secondToken)
    def obs(self, nMinusTwo=None, nMinusOne=None, nth=None):
        self._trigramCount[nMinusTwo, nMinusOne, nth] += 1
        self._bigramCount[nMinusOne, nth] += 1
        self._unigramCount[nth] += 1
        self._obsCount += 1
        self.updated = False

    # Close model, and compute probabilities after (possibly incremental) training
    def count(self):
        self.trigramLogProb = {}
        self.bigramLogProb = {}
        self.unigramLogProb = {}

        bigramJointLogProb = {}
        
        if self._order == 2:
            # Remove (self._boundarySymbol, self._boundarySymbol) as it has no meaning for bigrams...
            self._bigramCount.pop((self._boundarySymbol, self._boundarySymbol), None)
            # Remove the Unigram count of the removed self._boundarySymbol
            self._unigramCount[self._boundarySymbol] -= self._sentCount
            self._obsCount -= self._sentCount
            # Reset, as incremental training (if there is any) will start from here...
            self._sentCount = 0

        # Compute unigram probs: P(t_n) = C(t_n)/sum_i(C(t_i))
        self.tags = set(self._unigramCount.keys())
        self.unigramLogProb = {tag: math.log(count) - math.log(self._obsCount)
                               for tag, count in self._unigramCount.items()}

        # Compute bigram probs (Conditional probability using joint probabilities):
        # Unigram prob: P(t_n-1) = C(t_n)/sum_i(C(t_i)) = self.unigramLogProb[tag]
        # Joint prob (bigram): P(t_n-1, t_n) = C(t_n-1, t_n)/C(t_n-1) = bigramJointLogProb(tag1,tag2)
        # Conditional prob (bigram): P(t_n|t_n-1) = P(t_n-1, t_n)/P(t_n-1) =
        #     bigramJointLogProb(tag1,tag2) - self.unigramLogProb[tag1]
        for pair, count in self._bigramCount.items():  # log(Bigram / Unigram)
            bigramJointLogProb[pair] = math.log(count) - math.log(self._unigramCount[pair[0]])
            self.bigramLogProb[pair] = bigramJointLogProb[pair] - self.unigramLogProb[pair[0]]

        if self._order == 3:
            # Compute trigram probs (Conditional probability using joint probabilities):
            # Joint prob (bigram): P(t_n-1, t_n) = C(t_n-1, t_n)/C(t_n-1) = bigramJointLogProb(tag1,tag2)
            # Joint prob (trigram): P(t_n-2, t_n-1, t_n) = C(t_n-2, t_n-1, t_n)/C(t_n-2, t_n-1) =
            #     trigramJointLogProb(tag1, tag2, tag3)
            # Conditional prob (trigram): P(t_n|t_n-2, t_n-1) = P(t_n-2, t_n-1, t_n)/P(t_n-2, t_n-1) =
            #     trigramJointLogProb(tag1, tag2, tag3) - bigramJointLogProb(tag1, tag2)
            for tri, count in self._trigramCount.items():  # log(Trigram / Bigram)
                trigramJointLogProb = math.log(count) - math.log(self._bigramCount[tri[0:2]])
                self.trigramLogProb[tri] = trigramJointLogProb - bigramJointLogProb[tri[0:2]]

        # Compute lambdas
        self._compute_lambda()

        self.updated = True

    def _compute_lambda(self):
        """
        This function originates from NLTK
        creates lambda values based upon training data (Brants 2000, Figure 1)

        NOTE: no need to explicitly reference C,
        it is contained within the tag variable :: tag == (tag,C)

        for each tag trigram (t1, t2, t3)
        depending on the maximum value of
        - f(t1,t2,t3)-1 / f(t1,t2)-1
        - f(t2,t3)-1 / f(t2)-1
        - f(t3)-1 / N-1

        increment l3,l2, or l1 by f(t1,t2,t3)

        ISSUES -- Resolutions:
        if 2 values are equal, increment both lambda values
        by (f(t1,t2,t3) / 2)
        """

        # temporary lambda variables
        tl1 = 0.0
        tl2 = 0.0
        tl3 = 0.0

        # for each t3 given t1,t2 in system
        for h1, h2, tag in self._trigramCount.keys():

            # if there has only been 1 occurrence of this tag in the data
            # then ignore this trigram.
            if self._unigramCount[tag] > 1:

                # safe_div provides a safe floating point division
                # it returns -1 if the denominator is 0
                if self._order == 3:
                    c3 = safe_div(self._trigramCount[h1, h2, tag] - 1, self._bigramCount[h1, h2] - 1)
                else:
                    c3 = -2.0  # Never will be maximum
                c2 = safe_div(self._bigramCount[h2, tag] - 1, self._unigramCount[h2] - 1)
                c1 = safe_div(self._unigramCount[tag] - 1, self._obsCount - 1)

                # if c1 is the maximum value:
                if (c1 > c3) and (c1 > c2):
                    tl1 += self._trigramCount[h1, h2, tag]

                # if c2 is the maximum value
                elif (c2 > c3) and (c2 > c1):
                    tl2 += self._trigramCount[h1, h2, tag]

                # if c3 is the maximum value
                elif (c3 > c2) and (c3 > c1):
                    tl3 += self._trigramCount[h1, h2, tag]

                # if c3, and c2 are equal and larger than c1
                elif (c3 == c2) and (c3 > c1):
                    tl2 += self._trigramCount[h1, h2, tag] / 2.0
                    tl3 += self._trigramCount[h1, h2, tag] / 2.0

                # if c1, and c2 are equal and larger than c3
                # this might be a dumb thing to do....(not sure yet)
                elif (c2 == c1) and (c1 > c3):
                    tl1 += self._trigramCount[h1, h2, tag] / 2.0
                    tl2 += self._trigramCount[h1, h2, tag] / 2.0

                """
                # otherwise there might be a problem
                # eg: all values = 0
                else:
                    print('Problem', c1, c2 ,c3)
                    pass
                """
        # Lambda normalisation:
        # ensures that l1+l2+l3 = 1
        self._lambda1 = tl1 / (tl1 + tl2 + tl3)
        self._lambda2 = tl2 / (tl1 + tl2 + tl3)
        self._lambda3 = tl3 / (tl1 + tl2 + tl3)
        print('lambda1: {0}\nlambda2: {1}\nlambda3: {2}'.format(self._lambda1, self._lambda2, self._lambda3),
              file=sys.stderr, flush=True)

    # Allways use smoothing with lambdas (Brants 2000, formula 2-6)
    def _logProb(self, nMinusTwo=None, nMinusOne=None, nth=None):
        if not self.updated:
            print(self._updateWarning, file=sys.stderr, flush=True)

        # Trigram, which is seen in training set or using smoothing
        tri = self.trigramLogProb.get((nMinusTwo, nMinusOne, nth), self._logSmooth)

        # Bigram, which is seen in training set or using smoothing
        bi = self.bigramLogProb.get((nMinusOne, nth), self._logSmooth)

        # Unigram, which is seen in training set or using smoothing
        uni = self.unigramLogProb.get(nth, self._logSmooth)

        # Weighted by lambdas...
        return self._lambda1 * uni + self._lambda2 * bi + self._lambda3 * tri

    def prob(self, nMinusTwo=None, nMinusOne=None, nth=None):
        return math.exp(self._logProb(nMinusTwo, nMinusOne, nth))

    def writeToFile(self, fileName):
        self.tags.remove(self._boundarySymbol)
        with open(fileName, 'wb') as f:
            pickle.dump(self, f)

    @staticmethod
    def getModelFromFile(fileName):
        with open(fileName, 'rb') as f:
            return pickle.load(f)

    """
    source: http://en.wikipedia.org/wiki/Viterbi_algorithm
    The code has been modified to match our Bigram models:
    - models are dictionaries with tuples as keys
    - starting probabilities are not separate and end probabilities are also
    taken into consideration
    - transProbs should be a Bigram instance
    - tagProbsByPos should be a list containing, for each position,
      the probability distribution over tags as returned by the maxent model
    - all probabilities are expected to be in log space
    """
    def _viterbiBigram(self, tagProbsByPos):
        # Make logprob from probs...
        tagProbsByPos = [dict([(key, math.log(val))
                               for key, val in probDist.items()])
                         for probDist in tagProbsByPos]
        V = [{}]
        path = {}
        states = self.tags

        # Initialize base cases (t == 0)
        for y in states:
            V[0][y] = (self._languageModelWeight *
                       # We can come only from boundary symbols, so there is no need for loop and max...
                       # We must remember len(states) piece of states
                       self._logProb(None, self._boundarySymbol, y) +
                       tagProbsByPos[0][y])
            path[y] = [y]

        # Run Viterbi for t > 0
        for t in range(1, len(tagProbsByPos)):
            V.append({})
            newpath = {}
            # We extend the graph to every possible states
            # To every possible states, we can only come from the maximum
            # We remember this particular state
            for y in states:
                # In t-1 we stand at y0 with some specific probability but we only could come from the maximum
                (prob, state) = max([(V[t - 1][y0] +
                                      self._languageModelWeight *
                                      # If we come from (z, y0)
                                      self._logProb(None, y0, y) +
                                      # If we extend, we get this
                                      tagProbsByPos[t][y],
                                      # This is the history, that we check
                                      # We compute max by y0
                                      y0) for y0 in states])
                # Now we stand at y, because it has the maximal probability 'prob'
                V[t][y] = prob
                # Extending the path with y, we came from state 'state'
                newpath[y] = path[state] + [y]

            # Don't need to remember the old paths
            path = newpath

        # At the end of the text we do a multiplication with a transition to check
        # 'If we were in the end, would we come this way or not?'...
        (prob, state) = max([(V[len(tagProbsByPos) - 1][y] + self._logProb(None, y, self._boundarySymbol), y)
                             for y in states])
        return prob, path[state]

    def _viterbiTrigram(self, tagProbsByPos):
        # Make logprob from probs...
        tagProbsByPos = [dict([(key, math.log(val))
                               for key, val in probDist.items()])
                         for probDist in tagProbsByPos]
        V = [{}]
        path = {}
        states = self.tags

        # Initialize base cases (t == 0)
        for z in states:
            for y in states:
                V[0][z, y] = (self._languageModelWeight *
                              self._logProb(self._boundarySymbol, self._boundarySymbol, y) +
                              tagProbsByPos[0][y])
                path[z, y] = [y]

        if len(tagProbsByPos) > 1:
            # Run Viterbi for t == 1
            V.append({})
            newpath = {}

            for z in states:
                for y in states:
                    (prob, state) = max([(V[0][y0, z] +
                                        self._languageModelWeight *
                                        self._logProb(self._boundarySymbol, z, y) +
                                        tagProbsByPos[1][y],
                                        y0) for y0 in states])
                    V[1][z, y] = prob
                    newpath[z, y] = path[state, z] + [y]

            # Don't need to remember the old paths
            path = newpath

            # Run Viterbi for t > 1
            for t in range(2, len(tagProbsByPos)):
                V.append({})
                newpath = {}

                for z in states:
                    for y in states:
                        (prob, state) = max([(V[t - 1][y0, z] +
                                            self._languageModelWeight *
                                            self._logProb(y0, z, y) +
                                            tagProbsByPos[t][y],
                                            y0) for y0 in states])
                        V[t][z, y] = prob
                        newpath[z, y] = path[state, z] + [y]

                # Don't need to remember the old paths
                path = newpath

        # Micro-optimalization: Brants (2000) say self._logProb(None, y, self._boundarySymbol),
        # but why not self._logProb(z, y, self._boundarySymbol) ?
        (prob, state, state2) = max([(V[len(tagProbsByPos) - 1][z, y] +
                                      # self._logProb(z, y, self._boundarySymbol), z, y)
                                      self._logProb(None, y, self._boundarySymbol), z, y)
                                     for z in states for y in states])
        return prob, path[state, state2]
