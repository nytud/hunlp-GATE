#!/usr/bin/python3
# -*- coding: utf-8, vim: expandtab:ts=4 -*-

import argparse
from os.path import isdir, join, isfile
import sys
import os
import numpy as np
import yaml

from feature import Feature
from trainer import Trainer
from tagger import Tagger
from transmodel import TransModel


def mainTransModelTrain(options):
    transModel = TransModel(options['tagField'], lmw=options['lmw'], order=options['transModelOrder'])
    # It's possible to train multiple times incrementally...
    transModel.train(options['inputStream'])
    # Close training, compute probabilities
    transModel.count()
    transModel.writeToFile(options['transModelFileName'])


def mainTrain(featureSet, options):
    trainer = Trainer(featureSet, options)

    if 'inFeatFile' in options and options['inFeatFile']:
        # Use with featurized input
        trainer.getEventsFromFile(options['inFeatFile'])
    else:  # Use with raw input
        trainer.getEvents(options['inputStream'])

    if options['task'] == 'most-informative-features':
        trainer.cutoffFeats()
        trainer.mostInformativeFeatures(options['outputStream'])
    elif 'toCRFsuite' in options and options['toCRFsuite']:
        trainer.cutoffFeats()
        trainer.toCRFsuite(options['outputStream'])
        trainer.save()
    else:
        trainer.cutoffFeats()
        trainer.train()
        trainer.save()


def mainTag(featureSet, options):
    transModel = None
    if not (options['printWeights'] or options['toCRFsuite']):
        print('loading transition model...', end='', file=sys.stderr, flush=True)
        transModel = TransModel.getModelFromFile(options['transModelFileName'])
        print('done', file=sys.stderr, flush=True)

    tagger = Tagger(featureSet, transModel, options)
    if 'inFeatFile' in options and options['inFeatFile']:
        # Tag a featurized file to to outputStream
        for sen, comment in tagger.tagFeatures(options['inFeatFile']):
            writeSentence(sen, options['outputStream'], comment)
    elif 'ioDirs' in options and options['ioDirs']:
        # Tag all files in a directory file to to fileName.tagged
        for sen, fileName in tagger.tagDir(options['ioDirs'][0]):
            writeSentence(sen, open(join(options['ioDirs'][1], '{0}.tagged'.format(fileName)), 'a', encoding='UTF-8'))
    elif 'toCRFsuite' in options and options['toCRFsuite']:
        # Make CRFsuite format to outputStream for tagging
        tagger.toCRFsuite(options['inputStream'], options['outputStream'])
    elif 'printWeights' in options and options['printWeights']:
        # Print MaxEnt weights to STDOUT
        tagger.printWeights(options['printWeights'], options['outputStream'])
    else:
        # Tag inputStream to outputStream
        for sen, comment in tagger.tagCorp(options['inputStream']):
            writeSentence(sen, options['outputStream'], comment)


def writeSentence(sen, out=sys.stdout, comment=None):
    if comment:
        out.write('{0}\n'.format(comment))
    out.writelines('{0}\n'.format('\t'.join(tok)) for tok in sen)
    out.write('\n')


def loadYaml(cfgFile):
    lines = open(cfgFile, encoding='UTF-8').readlines()
    try:
        start = lines.index('%YAML 1.1\n')
    except ValueError:
        print('Error in config file: No document start marker found!', file=sys.stderr)
        sys.exit(1)
    rev = lines[start:]
    rev.reverse()
    try:
        end = rev.index('...\n')*(-1)
    except ValueError:
        print('Error in config file: No document end marker found!', file=sys.stderr)
        sys.exit(1)
    if end == 0:
        lines = lines[start:]
    else:
        lines = lines[start:end]

    return yaml.load(''.join(lines))


def getFeatureSetYAML(cfgFile):
    features = {}
    defaultRadius = -1
    defaultCutoff = 1
    cfg = loadYaml(cfgFile)

    if 'default' in cfg:
        if 'cutoff' in cfg['default']:
            defaultCutoff = cfg['default']['cutoff']
        if 'radius' in cfg['default']:
            defaultRadius = cfg['default']['radius']

    for feat in cfg['features']:
        options = {}
        if 'options' in feat:
            options = feat['options']

        if isinstance(feat['fields'], int):
            fields = [feat['fields']]
        else:
            fields = [int(field) for field in feat['fields'].split(',')]

        radius = defaultRadius
        if 'radius' in feat:
            radius = feat['radius']

        cutoff = defaultCutoff
        if 'cutoff' in feat:
            cutoff = feat['cutoff']

        name = feat['name']
        features[name] = Feature(feat['type'], name, feat['actionName'], fields, radius, cutoff, options)

    return features


def validDir(inputDir):
    if not isdir(inputDir):
        raise argparse.ArgumentTypeError('"{0}" must be a directory!'.format(inputDir))
    outDir = '{0}_out'.format(inputDir)
    os.mkdir(outDir)
    return inputDir, outDir


def validFile(inputFile):
    if not isfile(inputFile):
        raise argparse.ArgumentTypeError('"{0}" must be a file!'.format(inputFile))
    return inputFile


def parseArgs():
    parser = argparse.ArgumentParser()

    parser.add_argument('task', choices=['transmodel-train', 'most-informative-features', 'train', 'tag'],
                        help='avaliable tasks: transmodel-train, most-informative-features, train, tag')

    parser.add_argument('-c', '--config-file', dest='cfgFile', type=validFile,
                        help='read feature configuration from FILE',
                        metavar='FILE')

    parser.add_argument('-m', '--model', dest='modelName',
                        help='name of the (trans) model to be read/written',
                        metavar='NAME')

    parser.add_argument('--model-ext', dest='modelExt', default='.model',
                        help='extension of model to be read/written',
                        metavar='EXT')

    parser.add_argument('--trans-model-ext', dest='transModelExt', default='.transmodel',
                        help='extension of trans model file to be read/written',
                        metavar='EXT')

    parser.add_argument('--trans-model-order', dest='transModelOrder', default=3,
                        help='order of the transition model',
                        metavar='EXT')

    parser.add_argument('-l', '--language-model-weight', dest='lmw',
                        type=float, default=1,
                        help='set relative weight of the language model to L',
                        metavar='L')

    parser.add_argument('-O', '--cutoff', dest='cutoff', type=int, default=1,
                        help='set global cutoff to C',
                        metavar='C')

    parser.add_argument('-p', '--parameters', dest='trainParams',
                        help='pass PARAMS to trainer',
                        metavar='PARAMS')

    parser.add_argument('-u', '--used-feats', dest='usedFeats', type=validFile,
                        help='limit used features to those in FILE',
                        metavar='FILE')

    parser.add_argument('-t', '--tag-field', dest='tagField', type=int, default=-1,
                        help='specify FIELD containing the labels to build models from',
                        metavar='FIELD')

    groupI = parser.add_mutually_exclusive_group()

    groupI.add_argument('-i', '--input', dest='inputFileName', type=validFile,
                        help='Use input file instead of STDIN',
                        metavar='FILE')

    groupI.add_argument('-d', '--input-dir', dest='ioDirs', type=validDir,
                        help='process all files in DIR (instead of stdin)',
                        metavar='DIR')

    groupI.add_argument('-f', '--input-feature-file', dest='inFeatFileName', type=validFile,
                        help='use training events in FILE (already featurized input, see --toCRFsuite)',
                        metavar='FILE')

    groupO = parser.add_mutually_exclusive_group()

    groupO.add_argument('-F', '--feature-file', dest='outFeatFileName',
                        help='write training events to FILE (deprecated, use --toCRFsuite instead)',
                        metavar='FILE')

    groupO.add_argument('-o', '--output', dest='outputFileName',
                        help='Use output file instead of STDOUT',
                        metavar='FILE')

    groupO.add_argument('--toCRFsuite', dest='toCRFsuite', action='store_true', default=False,
                        help='convert input to CRFsuite format to STDOUT')

    groupO.add_argument('--printWeights', dest='printWeights', type=int,
                        help='print model weights instead of tagging')

    return parser.parse_args()


def main():
    options = parseArgs()
    if options.outFeatFileName:
        print('Error: Argument --feature-file is deprecated! Use --toCRFsuite instead!',
              file=sys.stderr, flush=True)
        sys.exit(1)

    if not options.modelName:
        print('Error: Model name must be specified! Please see --help!', file=sys.stderr, flush=True)
        sys.exit(1)
    options.modelFileName = '{0}{1}'.format(options.modelName, options.modelExt)
    options.transModelFileName = '{0}{1}'.format(options.modelName, options.transModelExt)

    # Data sizes across the program (training and tagging). Check manuals for other sizes
    options.dataSizes = {'rows': 'Q', 'rowsNP': np.uint64,       # Really big...
                         'cols': 'Q', 'colsNP': np.uint64,       # ...enough for indices
                         'data': 'B', 'dataNP': np.uint8,        # Currently data = {0, 1}
                         'labels': 'H', 'labelsNP': np.uint16,   # Currently labels > 256...
                         'sentEnd': 'Q', 'sentEndNP': np.uint64  # Sentence Ends in rowIndex
                         }                                       # ...for safety
    options.outputStream = sys.stdout
    options.inputStream = sys.stdin

    optionsDict = vars(options)
    if optionsDict['inputFileName']:
        optionsDict['inputStream'] = open(optionsDict['inputFileName'], encoding='UTF-8')
    if optionsDict['outputFileName']:
        optionsDict['outputStream'] = open(optionsDict['outputFileName'], 'w', encoding='UTF-8')

    if optionsDict['task'] == 'transmodel-train':
        mainTransModelTrain(optionsDict)
    elif optionsDict['task'] == 'train' or optionsDict['task'] == 'most-informative-features':
        featureSet = getFeatureSetYAML(optionsDict['cfgFile'])
        mainTrain(featureSet, optionsDict)
    elif optionsDict['task'] == 'tag':
        if optionsDict['inFeatFileName']:
            featureSet = None
            optionsDict['inFeatFile'] = open(optionsDict['inFeatFileName'], encoding='UTF-8')
        else:
            featureSet = getFeatureSetYAML(optionsDict['cfgFile'])
        mainTag(featureSet, optionsDict)
    else:
        print('Error: Task name must be specified! Please see --help!', file=sys.stderr, flush=True)
        sys.exit(1)

if __name__ == '__main__':
    main()
