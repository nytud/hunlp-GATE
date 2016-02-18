#!/usr/bin/python3
# -*- coding: utf-8, vim: expandtab:ts=4 -*-
"""
eval.py comes with HunTag and is capable of evaluating precision and
recall of sequential tagging output. It can also compute various other
statistics. When run with the -c option, its operation is identical to
the perl script conlleval.pl provided for the ConLL shared tasks
chunking and NER-tagging.
"""

import argparse

from collections import defaultdict
import sys


def getChunksFromCorp(taggedFile, goldField, autoField, mode, strict):
    goldChunks = getChunksFromColumn(taggedFile, goldField, mode, strict)
    autoChunks = getChunksFromColumn(taggedFile, autoField, mode, strict)

    return goldChunks, autoChunks


def printError(lineNum):
    print('illicit tag in line {0}'.format(lineNum))
    sys.exit(1)


def getChunksFromColumn(taggedFile, field, mode, strict):
    chunks = []
    chunkStart = None
    chunkType = None

    for c, line in enumerate(taggedFile):
        line = line.strip()
        if len(line) == 0:
            continue
 
        if len(line.split()) > field:
            tag = line.split()[field]
        else:
            print('\ninvalid number of fields in line {0}\n{1}'.format(c, line),
                  file=sys.stderr, flush=True)
            sys.exit(1)

        if tag[0] == 'B':
            if strict and mode == 'BIE1' and chunkStart is not None:
                printError(c)

            if chunkStart is not None:
                chunks.append((chunkStart, c - 1, chunkType))

            chunkStart = c
            chunkType = tag[2:]

        elif tag[0] == 'I':
            if strict and (chunkStart is None or chunkType != tag[2:]):
                printError(c)

        elif tag[0] == 'E':
            if strict and (mode != 'BIE1' or chunkStart is None or chunkType != tag[2:]):
                printError(c)

            if chunkStart is None:
                chunkStart = c
                chunkType = tag[2:]

            chunks.append((chunkStart, c, chunkType))
            chunkStart = None
            chunkType = None

        elif tag[0] == '1':
            if strict and mode != 'BIE1':
                printError(c)

            if chunkStart is not None:
                chunks.append((chunkStart, c - 1, chunkType))

            chunks.append((c, c, tag[2:]))

            chunkStart = None
            chunkType = None

        else:
            if strict and tag[0] != 'O':
                printError(c)

            if chunkStart is not None:
                if strict and mode == 'BIE1':
                    printError(c)

                chunks.append((chunkStart, c - 1, chunkType))
                chunkStart = None
                chunkType = None

        if c == len(taggedFile) - 1 and chunkStart is not None:
            chunks.append((chunkStart, c, chunkType))

    return set(chunks)


def evaluate(chunks):
    type_corr = defaultdict(float)
    type_auto = defaultdict(float)
    type_gold = defaultdict(float)

    gCh = chunks[0]
    aCh = chunks[1]

    n_goldChunks = len(gCh)
    n_autoChunks = len(aCh)

    corr = 0.0

    for chunk in aCh:
        chtype = chunk[2]
        # if chtype != 'NP': print chunk
        type_auto[chtype] += 1

        if chunk in gCh:  # or (chunk[0], chunk[1], 'MISC') in gCh:
            corr += 1
            type_corr[chtype] += 1

    for chunk in gCh:
        chtype = chunk[2]
        type_gold[chtype] += 1

    types_gold = list(type_gold.keys())
    types_auto = list(type_auto.keys())
    types = types_gold + types_auto

    return (n_goldChunks, n_autoChunks, corr, set(types), type_gold, type_auto,
            type_corr)


def count(chunkCounts, tokCounts):
    results = {}
    (n_goldChunks, n_autoChunks, corr, types, type_gold, type_auto,
     type_corr) = chunkCounts

    toks, corrToks = tokCounts

    results['tokens'] = toks
    results['acc'] = round(100 * (corrToks / toks), 2)

    try:
        precision = corr / n_autoChunks
    except ZeroDivisionError:
        precision = 0

    try:
        recall = corr / n_goldChunks
    except ZeroDivisionError:
        recall = 0

    try:
        fScore = (2 * precision * recall) / (precision + recall)
    except ZeroDivisionError:
        fScore = 0

    results['goldPhrases'] = n_goldChunks
    results['foundPhrases'] = n_autoChunks
    results['corrPhrases'] = corr
    results['All'] = (100 * precision, 100 * recall, 100 * fScore)

    for onetype in types:
        if onetype in type_auto:

            c = type_auto[onetype]
            if onetype in type_corr:
                prec = 100 * (type_corr[onetype] / c)
                rec = 100 * (type_corr[onetype] / type_gold[onetype])
                fSc = (2 * prec * rec) / (prec + rec)

                results[onetype] = (prec, rec, fSc, c)

            else:
                results[onetype] = (0, 0, 0, c)

        else:
            results[onetype] = (0, 0, 0, 0)

    return results


def printResults(results):
    tok = results['tokens']
    phras = results['goldPhrases']
    found = results['foundPhrases']
    corr = results['corrPhrases']
    acc = results['acc']
    prec = results['All'][0]
    rec = results['All'][1]
    fb = results['All'][2]

    print('processed {0:.0f} tokens with {1:.0f} phrases;'.format(tok, phras), end='')
    print('found: {0:.0f} phrases; correct: {1:.0f}.\n'.format(found, corr), end='')
    
    print('accuracy: {0:6.2f}%; precision: {1:6.2f}%;'.format(acc, prec), end='')
    print('recall: {0:6.2f}%; FB1: {1:6.2f}'.format(rec, fb))

    sortedTypes = list(results.keys())
    sortedTypes.sort()

    for stype in sortedTypes:
        if stype not in ('corrPhrases', 'tokens', 'foundPhrases', 'acc', 'All',
                         'goldPhrases'):

            prec = results[stype][0]
            rec = results[stype][1]
            fb = results[stype][2]
            found = int(results[stype][3])

            print('{0:>18}:'.format(stype), end='')
            print('precision: {0:6.2f}%;'.format(prec), end='')
            print('recall: {0:6.2f}%; '.format(rec), end='')
            print('FB1: {0:6.2f}\t{1:.0f}'.format(fb, found))


def analyzeErrors(chunks):
    gCh = chunks[0]
    aCh = chunks[1]

    errorTypes = {'wrongType': 0, 'disjunct': 0, 'overlap': 0, 'aInG': 0, 'gInA': 0}
    wrongCategory = {}
    for chunk in aCh:
        if chunk in gCh:
            continue

        nearest = getNearestChunk(chunk, gCh)

        if chunk[2] != nearest[2]:
            errorTypes['wrongType'] += 1

            try:
                wrongCategory[(nearest[2], chunk[2])] += 1
            except KeyError:
                wrongCategory[(nearest[2], chunk[2])] = 1

            continue

        gPos = (nearest[0], nearest[1])
        aPos = (chunk[0], chunk[1])

        rel = compareChunks(gPos, aPos)
        errorTypes[rel] += 1

    allErrors = 0
    for etype in errorTypes:
        allErrors += errorTypes[etype]

    return errorTypes, allErrors, wrongCategory


def compareChunks(gPos, aPos):
    if aPos[0] < gPos[0]:
        if aPos[1] < gPos[0]:
            return 'disjunct'
        elif aPos[1] < gPos[1]:
            return 'overlap'
        else:
            return 'gInA'

    elif aPos[0] == gPos[0]:
        if aPos[1] > gPos[1]:
            return 'gInA'
        else:
            return 'aInG'

    else:
        if gPos[1] < aPos[0]:
            return 'disjunct'
        elif gPos[1] < aPos[1]:
            return 'overlap'
        else:
            return 'aInG'


def getNearestChunk(chunk, gCh):
    chSum = chunk[0] + chunk[1]
    nearest = None
    bestDist = 999
    for currChunk in gCh:
        currSum = currChunk[0] + currChunk[1]
        if abs(currSum - chSum) < bestDist:
            bestDist = abs(currSum - chSum)
            nearest = currChunk

    return nearest


def printErrorTypes(errorTypes):
    for etype in errorTypes:
        print(etype + ':\t' + str(errorTypes[etype]))


def getChunkPatterns(taggedFile, goldField, autoField, mode):
    if mode != 'BIE1':
        print('extracting chunk patterns is currently not available in \'BI\' mode!', file=sys.stderr, flush=True)
        sys.exit(1)

    patternCount = defaultdict(int)

    # prevTags = ('O', 'O')
    pattern = []
    curr = ''
    G = False
    A = False
    for _, l in enumerate(taggedFile):
        l = l.strip()
        if len(l) == 0:
            if len(pattern) != 0:
                wPatt = ' '.join(pattern)
                # print wPatt
                patternCount[wPatt] += 1
                pattern = []

            continue

        line = l.split()
        goldTag = line[goldField][0]
        autoTag = line[autoField][0]

        if goldTag in ('B', 'E'):
            curr += 'g'
            G = False
        elif goldTag == '1':
            curr += 'gg'
            G = False
        elif goldTag == 'O':
            if not G:
                curr += 'G'
                G = True

        else:
            assert goldTag == 'I'

        if autoTag in ('B', 'E'):
            curr += 'a'
            A = False
        elif autoTag == '1':
            curr += 'aa'
            A = False
        elif autoTag == 'O':
            if not A:
                curr += 'A'
                A = True

        else:
            assert autoTag == 'I'

        if curr not in ('', 'GA'):
            pattern.append(curr)

        curr = ''

        if goldTag == autoTag:

            if autoTag in ('I', 'B'):
                pass

            elif autoTag != 'O' or pattern != []:
                wPatt = ' '.join(pattern)
                # print wPatt
                patternCount[wPatt] += 1

                pattern = []

    return patternCount


def printPatterns(patternCount):

    # print('1', end='', file=sys.stderr, flush=True)

    pall = 0.0
    patterns = []

    """
    patternsToAscii = {'ga ga': '---_____---\n---_____---',
                                         'ggaa' : '---_---\n---_---',
                                         'ga g g ga': '---_____---_____---\n---_____________---',
                                         'ga aa ga': '---_______---\n---___|___---',
                                         'ga g g g g ga': '---_____---_____---_____---\n---_____________________---',
                                         'ga a a ga': '---_____________---\n--_____---_____---',
                                         'a g ga':'-------_____---\n---_________---',
                                         'g a ga':'---_________---\n-------_____---'}
    """

    for patt in patternCount:
        if patt not in ('ga ga', 'ggaa', 'GA'):
            pall += patternCount[patt]
            patterns.append((patternCount[patt], patt))

    patterns.sort()
    patterns.reverse()
    for patt in patterns:
        percent = (patternCount[patt[1]] / pall) * 100
        print('{0}%\t{1}\n{2}'.format(str(percent)[:6], patt[1], patternsToAscii(patt[1])))


def patternsToAscii(patt):
    # print patt
    asciiPattG = ''
    asciiPattA = ''
    for pos in patt.split():
        if 'gg' in pos:
            asciiPattG += '1'
        elif 'g' in pos:
            asciiPattG += '|'

        elif 'G' in pos:
            asciiPattG += ' '

        else:
            asciiPattG += '-'

        if 'aa' in pos:
            asciiPattA += '1'
        elif 'a' in pos:
            asciiPattA += '|'

        elif 'A' in pos:
            asciiPattA += ' '

        else:
            asciiPattA += '-'

    return '{0}\n{1}'.format(asciiPattG, asciiPattA)


def countToks(taggedFile, goldField, autoField):
    toks = 0.0
    corrToks = 0.0
    # print taggedFile
    for line in taggedFile:
        # print '@'
        line = line.split()
        if len(line) == 0:
            continue

        toks += 1
        if line[goldField] == line[autoField]:
            corrToks += 1

    return toks, corrToks


def printConfMatrix(wrongCategory):
    pairs = list(wrongCategory.keys())
    pairs.sort()
    for pair in pairs:
        print('%s --> %s %.0f' % (pair[0], pair[1], wrongCategory[pair]))


def leaveInternalBs(corp):
    newS = True
    newCorp = []
    for line in corp:
        line = line.strip()
        if len(line) == 0:
            newCorp.append('\n')
            newS = True
        else:
            l = line.split()
            newL = l[:-2]
            if l[-2][0] == 'B' and not newS:
                newL += ['B{0}'.format(l[-2][1:])]
            else:
                newL += ['O']

            if l[-1][0] == 'B' and not newS:
                newL += ['B{0}'.format(l[-1][1:])]
            else:
                newL += ['O']

            assert len(newL) == len(l)
            newCorp.append('\t'.join(newL))
            newS = False
    return newCorp


def getSenPrec(corp):
    allSen = 0.0
    corrSen = 0.0
    thisSen = True
    for line in corp:
        line = line.strip()
        if len(line) == 0:
            allSen += 1
            if thisSen:
                corrSen += 1
            thisSen = True

        else:
            if line.split()[-2] != line.split()[-1]:
                thisSen = False
    print('sentence precision:{0}%'.format(str((corrSen / allSen) * 100)[:5]))


def runEval(stdin, goldField='-2', autoField='-1', mode='BI', conll=False,
            bPoints=False, sen=False, strict=False, pattern=False):
    """

    Args:
        stdin:
        goldField:
        autoField:
        mode:
        conll:
        bPoints:
        sen:
        strict:
        pattern:

    Returns:

    """
    """
    try:
        taggedFile = sys.stdin.readlines()
        goldField = int(sys.argv[1])
        autoField = int(sys.argv[2])
        mode = sys.argv[3]
        assert mode in ('BI', 'BIE1')
    except:
        print('usage: {0} <gold field> <auto field> <mode>'.format(sys.argv[0]), file=sys.stderr, flush=True)
        sys.exit(1)
    """

    tF = stdin.readlines()
    chunks = getChunksFromCorp(tF, int(goldField), int(autoField), mode, strict)

    chunkCounts = evaluate(chunks)
    # print tF
    tokCounts = countToks(tF, int(goldField), int(autoField))
    # print tokCounts
    results = count(chunkCounts, tokCounts)

    if conll:
        printResults(results)

    if bPoints:
        newCorp = leaveInternalBs(tF)
        # print '\n'.join(newCorp)
        newChunks = getChunksFromCorp(newCorp, int(goldField), int(autoField),
                                      'BI', strict)
        newChunkCounts = evaluate(newChunks)
        newTokCounts = countToks(newCorp, int(goldField), int(autoField))
        newResults = count(newChunkCounts, newTokCounts)
        printResults(newResults)
    # errorTypes, allErrors, wrongCategory = analyzeErrors(chunks)
    # print str(wrongCategory)

    if sen:
        getSenPrec(tF)

    # assert allErrors+results[2] == results[1]
    # printErrorTypes(errorTypes)
    # printConfMatrix(wrongCategory)
    if pattern:
        patternCount = getChunkPatterns(tF, int(goldField), int(autoField),
                                        mode)
        printPatterns(patternCount)


def evalInput(inputStream, autoField=-1, goldField=-2):
    corp = []
    for sen in inputStream:
        for tok in sen:
            corp.append('{0}\n'.format('\t'.join(tok)))
        corp.append('\n')

    chunks = getChunksFromCorp(corp, goldField, autoField, 'BI', False)
    chunkCounts = evaluate(chunks)
    tokCounts = countToks(corp, goldField, autoField)
    results = count(chunkCounts, tokCounts)
    printResults(results)


def parse_args():
    parser = argparse.ArgumentParser()

    parser.add_argument('-c', '--conll', dest='conll', action='store_true', default=False,
                        help='ConLL')

    parser.add_argument('-p', '--pattern', dest='pattern', action='store_true', default=False,
                        help='pattern')

    parser.add_argument('-g', '--gold-field', dest='gold', default=-2,
                        help='fieldNo of the gold standad (default: -2)',
                        metavar='FIELD')

    parser.add_argument('-a', '--auto-field', dest='auto', default=-1,
                        help='fieldNo of the automatic solution  (default: -1)',
                        metavar='FIELD')

    parser.add_argument('-i', '--input-file', dest='inputFile',
                        help='input file (default: STDIN)',
                        metavar='FILE')

    parser.add_argument('-s', '--strict', dest='strict', action='store_true', default=False,
                        help='strict')

    parser.add_argument('-m', '--mode', dest='mode', choices=['BI', 'BIE1'],
                        help='type of labeling (BI or BIE1, default: BIE1)', default='BIE1',
                        metavar='TYPE')

    parser.add_argument('-e', '--sen', dest='sen', action='store_true', default=False,
                        help='sentence precision')

    parser.add_argument('-b', '--bpoints', dest='bpoints', action='store_true', default=False,
                        help='bPoints')

    return parser.parse_args()

if __name__ == '__main__':
    options = parse_args()
    if len(sys.argv) == 1:
        print('usage: {0} <gold field> <auto field> <mode>'.format(sys.argv[0]), file=sys.stderr, flush=True)
        sys.exit(1)
    if not options.inputFile:
        options.inputFile = sys.stdin
    else:
        options.inputFile = open(options.inputFile)
    # runEval(stdin, goldField='-2', autoField='-1', mode='BI', conll=False,
    # bPoints=False, sen=False, strict=False, pattern=False)
    runEval(options.inputFile, options.gold, options.auto, options.mode, 
            options.conll, options.bpoints, options.sen, options.strict, options.pattern)
