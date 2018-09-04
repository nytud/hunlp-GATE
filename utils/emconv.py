#! /usr/bin/env python3
# -*- coding: utf-8 -*-
"""Convert GATE-XML from  e-magyar to TSV.

With no FILE, read the stdin and write to stdout. With FIEL(s) write output
to the <filename>.conv.tsv file.
"""

import argparse
import os
import xml.etree.ElementTree as ET
import sys


def check_dir(directory):
    """Check directory exists.
    """
    if not os.path.exists(directory):
        raise argparse.ArgumentError()
    return directory


def get_args():
    """Get commandline arguments.
    """
    pars = argparse.ArgumentParser(description=__doc__)
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


def convert(xml):
    """Get text (in xml format), return with the converted tsv. Entry point.
    """
    tree = ET.fromstring(xml)
    root = tree.find('AnnotationSet')
    res = ['ID', 'string', 'anas', 'feature', 'lemma', 'hfstana', 'pos',
           'chunk', 'depType', 'depTarget', 'cons', 'NER']
    res = '\t'.join(res) + '\n'
    annots = root.findall('Annotation')
    if len(annots) > 0:
        sentences = []
        snt = ''
        for annot in annots:
            fullline = ''
            type = annot.get('Type')
            tid = annot.get('Id')
            if type == 'Token':
                fullline += str(tid) + '\t'
                fts = {}
                for feature in annot.findall('Feature'):
                    pair = {feature.find('Name').text:
                            feature.find('Value').text}
                    fts.update(pair)
                fullline += getLine(fts)
                snt += fullline
            elif type == 'Sentence':
                sentences.append(snt)
                snt = ''
        res += '<s>\n' + '</s>\n<s>\n'.join(sentences) + '</s>'
    return res


def getLine(features):
    line = ""
    if 'string' in features and features['string'] is not None:
        line += features['string']
    line += "\t"
    if 'anas' in features and features['anas'] is not None:
        line += features['anas']
    line += "\t"
    if 'feature' in features and features['feature'] is not None:
        line += features['feature']
    line += "\t"
    if 'lemma' in features and features['lemma'] is not None:
        line += features['lemma']
    line += "\t"
    if 'hfstana' in features and features['hfstana'] is not None:
        line += features['hfstana']
    line += "\t"
    if 'pos' in features and features['pos'] is not None:
        line += features['pos']
    line += "\t"
    if 'NP-BIO' in features and features['NP-BIO'] is not None:
        line += features['NP-BIO']
    line += "\t"
    if 'depType' in features and features['depType'] is not None:
        line += features['depType']
    line += "\t"
    if 'depTarget' in features and features['depTarget'] is not None:
        line += features['depTarget']
    line += "\t"
    if 'cons' in features and features['cons'] is not None:
        line += features['cons']
    line += "\t"
    if 'NER-BIO1' in features and features['NER-BIO1'] is not None:
        line += features['NER-BIO1']
    line += "\n"
    return line


def main():
    """I/O
    """
    args = get_args()

    def get_path(file_name):
        """Get input filename and args, return the path of the output file.
        """
        file_name = os.path.abspath(file_name)
        dir_ = args['dir'] if args['dir'] else os.path.dirname(file_name)
        name = os.path.splitext(os.path.basename(file_name))[0]
        name += '.conv.tsv'
        return os.path.join(dir_, name)
    if args['FILE']:
        for file_ in args['FILE']:
            tsv = convert(file_.read())
            path = get_path(file_.name)
            with open(path, 'w') as file_:
                print(tsv, file=file_)
    else:
            tsv = convert(sys.stdin.read())
            print(tsv)
    return


if __name__ == "__main__":
    main()
