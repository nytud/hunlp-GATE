# -*- coding: utf-8 -*-

import io
import os
import xml.etree.ElementTree as ET
import sys


def main(argv):

    xml = ""

    try:

        #argument checks
        if len(argv) < 1:
            raise ValueError("Error: No filename provided.")

        xml = argv[0]

        #check if xml
        ext = os.path.splitext(xml)[-1].lower()
        if ext != ".xml":
            raise ValueError("Error: Not XML.")

        #IO check
        if not(os.path.exists(xml)):
            raise IOError("Error: File does not exist.")

        name = os.path.splitext(xml)[0]

        #delete converted if exists
        if os.path.exists("./" + name + "_conv.tsv"):
            os.remove("./" + name + "_conv.tsv")

        convert(xml, name)

    except IOError() as err:
         print(err)
         sys.exit(2)
    except ValueError as err:
        print(err)
        sys.exit(2)
    except Exception as err:
        print(err)
        sys.exit(2)


def convert(xml, name):

    tree = ET.parse(xml)
    root = tree.find('AnnotationSet')

    with open("./" + name + "_conv.tsv", 'a') as f:

        f.write("ID\ttoken\tlemma\tanas\tmsd\tdepType\tdepTarget\n");

        for annot in root.findall('Annotation'):
            fullline = ""
            type = annot.get('Type')
            tid = annot.get('Id')
            if type == "Token":
                fullline += str(tid) + "\t"
                fts = {}
                for feature in annot.findall('Feature'):
                    pair = {feature.find('Name').text : feature.find('Value').text}
                    fts.update(pair)
                fullline += getLine(fts)

            f.write(fullline.encode('utf-8'))

    f.close()

    print("DONE.")

def getLine(features):
    line = ""
    if 'string' in features and features['string'] is not None:
        line += features['string']
    line += "\t"
    if 'lemma' in features and features['lemma'] is not None:
        line += features['lemma']
    line += "\t"
    if 'anas' in features and features['anas'] is not None:
        line += features['anas']
    line += "\t"
    if 'msd' in features and features['msd'] is not None:
        line += features['msd'] + "\t"
    line += "\t"
    if 'depType' in features and features['depType'] is not None:
        line += features['depType']
    line += "\t"
    if 'depTarget' in features and features['depTarget'] is not None:
        line += features['depTarget']
    line += "\t"
    line += "\n"
    return line

if __name__ == "__main__":
   main(sys.argv[1:])