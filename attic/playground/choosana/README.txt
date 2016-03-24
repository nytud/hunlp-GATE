Choosana: lemma and KR-code morphological analysis disambiguator in python, to be implemented in Java

The current python tool is made of 2 parts: stem2.py, choosana.py.

stem2.py's input is a tokenized, PoS-tagged (Hunpos) and morphologically analyzed (Hunmorph) document.
See example input file comments.00001.posmorph. (Also included: raw text source file (.txt), tokenized file (.tok) and scripts that were used for tokenization (010.huntoken) + calling Hunpos and Hunmorph (011.hunpos-hunmorph)) 
Input file is a tsv file with the following columns: word, PoS-tag from Hunpos, number of analyses + space + space-separated list of analysis strings from Hunmorph.
Output is a tsv file with the following columns: word, lemma, PoS-tag from Hunpos, analyses from Hunmorph separated by "||".
In the output only analyses that are compatible with the PoS-tag are kept, with no duplicates.
Example output for the example input: comments.00001.stem.
The following command was used to run stem2.py:
python stem2.py -m --oovstr "OOV" --morphdel "||" comments.00001.posmorph comments.00001.stem
stem2.py also uses an exception dictionary (see included sample stem.exc).
For more information on what stem2.py does, see comments at the beginning of stem2.py.

chooseana.py's input is stem2.py's output.
The output file is a tsv file with the following columns: word, lemma, PoS-tag, a single morphological analyzis.
Sample output file: comments.00001.stem1.
chooseana.py chooses 1 morphological analysis from the last column in stem2.py's output. Also, the lemma is not from stem2.py's output, but chooseana.py constructs lemma from the analysis it selected.

Marton Mihaltz <mmihaltz@gmail.com> 2016-03-24