#! /usr/bin/env python3
# -*- coding: utf-8 -*-
"""Convert GATE-XML coming from e-magyar to TSV.

If no FILE, read the stdin and write to stdout. If FILE(s), write output
to the <filename>.conv.tsv file.
"""

import argparse
import os
import xml.etree.ElementTree as ET
import sys


def check_dir(directory):
    """Check if directory exists.
    """
    if not os.path.exists(directory):
        raise argparse.ArgumentError()
    return directory


def check_format(form):
    """Check if format valid.
    """
    if form not in ['x', 'c']:
        raise argparse.ArgumentError()
    return form


def get_args():
    """Get commandline arguments.
    """
    pars = argparse.ArgumentParser(description=__doc__)
    pars.add_argument(
            '-f',
            '--format',
            help='Format of sentense separations: '
                 'x (<s> xml tag, default); '
                 'c (conll, new line) ',
            required=False,
            default='x',
            type=check_format
    )
    pars.add_argument(
            '-d',
            '--dir',
            help='target directory',
            required=False,
            type=check_dir
    )
    pars.add_argument(
            'FILE',
            help='file names',
            nargs='*',
            type=argparse.FileType('r')
    )
    return vars(pars.parse_args())


def get_features(annot):
    """Get an xml element tree, return a dict containing the features for a
    token.
    """
    res = {}
    res['ID'] = annot.get('Id')
    for feature in annot.findall('Feature'):
        key = feature.find('Name').text
        value = feature.find('Value').text
        if value == None:
            value = ''
        res[key] = value
    return res


def get_line(features):
    """Get a dict of token features, return a line of TSV.
    """
    columns = ['ID', 'string', 'anas', 'feature', 'lemma', 'hfstana', 'pos',
               'NP-BIO', 'depType', 'depTarget', 'cons', 'NER-BIO1', ]
    res = [features.get(key, '') for key in columns]
    res = '\t'.join(res)
    return res


def convert(xml):
    """Convert GATE-XML to list of sentences (format independent
    representation). Entry point.
    """
    root = ET.fromstring(xml)
    sentences = []  # list of sentences
    snt = []  # list of TSV-lines
    for annot in root.findall('./AnnotationSet/Annotation'):
        type_ = annot.get('Type')
        if type_ == 'Token':
            features = get_features(annot)
            line = get_line(features)
            snt.append(line)
        elif type_ == 'Sentence':
            sentences.append(snt)
            snt = []
    sentences = ['\n'.join(snt) for snt in sentences]
    return sentences


def format_tsv(sentences, form):
    """Get list of sentences and format them.
    """
    res = ''
    header = ['ID', 'string', 'anas', 'feature', 'lemma', 'hfstana', 'pos',
           'chunk', 'depType', 'depTarget', 'cons', 'NER']
    res = '\t'.join(header) + '\n'
    if form == 'x':
        res += '<s>\n'
        res += '\n</s>\n<s>\n'.join(sentences)
        res += '\n</s>'
    elif form == 'c':
        res += '\n\n'.join(sentences)
    return res


def main():
    """I/O
    """
    args = get_args()

    def get_path(file_name):
        """Get input filename, return the path of the output file.
        """
        file_name = os.path.abspath(file_name)
        dir_ = args['dir'] if args['dir'] else os.path.dirname(file_name)
        name = os.path.splitext(os.path.basename(file_name))[0]
        name += '.conv.tsv'
        return os.path.join(dir_, name)

    if args['FILE']:
        # read from files
        for file_ in args['FILE']:
            tsv = format_tsv(convert(file_.read()), args['format'])
            path = get_path(file_.name)
            with open(path, 'w') as file_:
                print(tsv, file=file_)
    else:
        # read from stdin
        tsv = format_tsv(convert(sys.stdin.read()), args['format'])
        print(tsv)
    return


if __name__ == "__main__":
    main()
