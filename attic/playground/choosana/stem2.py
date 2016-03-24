#!/usr/bin/env python
# coding: utf-8

# Python 2.x !!!
#
# Requires the PyTrie module: http://pypi.python.org/pypi/PyTrie 
#

# Choose a stem from the available morphological analyzes using the PoS-tagger's output.
# Arguments:
# infile: output of 011.hunpos-hunmorph.sh (tokenized, PoS-tagged and morphologically analyzed text, UTF-8)
# outfie: word form,  tab, word stem from selected morph. analysis, tab, PoS-tag
#         also append a tab + '?' if for some reason the stem could not be determined using the morph. analysis (the surface form is copied to the stem column in this case)
#         empty lines for sentence boundaries
# Options:
# see help (running with 0 args)
#
# A 'stemming exceptions' file can also be used (see stemexcfile variable).
# The stemexcfile contains lines of tab-separeted (<key>, stem) pairs,
# where <key> is either a surface form or a prefix, indicated by a final '%' character.
# If a surface form in the text is among the ones listed in this file,
# then the stem given in the file is used instead of the one provided by the morphological analyzer.
# If a surface form in the text has a prefix listed in this file,
# then the stem given for the longest possible prefix will be used, if that stem is among the stems
# provided by the morphological analyzer (otherwise, the surface form will be used as stem!)    
#
# TODO:
# - in case PoS-tagger's output doesn't exactly match any of morph.analyzer's tags, do ? (eg. keringésére   NOUN<CAS<SBL>>   6 keringés/NOUN<POSS><CAS<SBL>> kering/VERB[GERUND]/NOUN<POSS><CAS<SBL>> kering/VERB[GERUND]/NOUN<POSS><CAS<SBL>> keringés/NOUN<ANP><CAS<SBL>> kering/VERB[GERUND]/NOUN<ANP><CAS<SBL>> kering/VERB[GERUND]/NOUN<ANP><CAS<SBL>> => keringés)
# - for proper names (title-case tokens inside sentences) choose analysis with longest stem (?) (eg. in "Ismerem Szegedi Pétert" Szegedi => Szegedi)
# - for productive compounds (ocamorph --compounds enabled), construct the stem from the parts (eg. polckereten polc/NOUN+keret/NOUN<CAS<SUE>> => polckeret)
# - if only PoS-category is given by PoS-tagger, choose longest stem with that category (eg. faxol VERB 2 faxol/VERB fax/NOUN[ACT2]/VERB => faxol)
# - derivations: ?...

# Changes:
# - 2014.03.05:
#   Added command line options --morphdel, --oovstr
# - 2013.05.17:
#   If morph. analyzer has only 1 analysis, and if it's not identical to PoS-tagger's label
#    (TODO: morphana's tag subsumes PoS-tagger's tag), then use morphana's tag instead of
#   PoS-tagger's.
# - 2011.02.08: -t has new effect (capitalization indicator):
#   append 0 if token starts with a lower-case character,
#   1 if token starts with upper-case character and is not the first in sentence,
#   2 if token starts with upper-case character and is first in sentence
#   3 if token starts with a non-alphabetic character

import codecs, re, sys
from optparse import OptionParser
from pytrie import StringTrie as trie

# Some globals
# Name of file that contains stemming exceptions in the form of <surface form>TAB<stem> each line
stemexcfile = sys.path[0] + '/stem.exc' # in the same directory as this script is in
stemexc = {} # surf form => stem
stemexcpref = trie() # surface form prefix => stem
opts = None # command-line options

# surff: string, surface form
# poss: string, Part-of-Speech tag returned by PoS-tagger
# morphstr: string, the list of "stem1/pos1/...(+stem2/pos21/...)*" morphological analyses returned by morph. analyzer
# 1. find surff in stemexc; if found, return stem associated there
# 2. if there are no anas (morphstr==None), return surff
# 3. find surff in stemexcpref; if found (the longest prefix),
#  3.1 then check if associated stem exists among stems in morphs; if yes, return it 
# 4. if there's only 1 ana, return its stem (if no stem in ana, return surf)
# 5. if more than 1 anas:
#  5.1 select stems from morphstr whose 1st PoS-tag is equal to, or (if none such), contains poss
#  5.2 if there are more than one, return the longest one
#  5.3 if there are none, return (surff, True)
# Return: (identified stem, is_surfaceform_used_as_stem) pair 
def get_stem( surff, poss, morphstr):

  # find surff in stemexc; if found, return stem associated there
  if surff in stemexc:
      return (stemexc[surff], False)
  
  # 0 analyzes: return surface form
  if not morphstr:
      return (surff, True)
  
  # parse morphstr (output of morphological analyzer for surface form)
  anas = [] # [[stem1, [pos11, ...]], ...] 
  for ana in set(morphstr.split(' ')): # uniq
    if '+' in ana: # we don't handle compounds yet, sorry...
      continue
    tmp = ana.split('/')
    if not tmp: # weird ana
      anas.append([surff, []])
    elif len(tmp) == 1:  # weird ana too 
      anas.append([tmp[0], []])
    else:
      anas.append([tmp[0], tmp[1:]])     
  
  # find surff in stemexcpref;
  stem = stemexcpref.longest_prefix_value(surff, None)
  if stem: # if found: find stem among stems from morphstr
    for (astem, apostags) in anas:
      if stem == astem: # stem associated with prefix in exc. list is a valid stem from morph. analyzer
        return (stem, False)        

  # if there's only 1 ana, return its stem (if no stem in ana, return surf)
  if len(anas)==1:
      return (anas[0][0], False)
  
  # if more than 1 anas:
  candstems = [] # candidate stems
  # 1. try exact poss matches
  for (astem, apostags) in anas:
    if apostags and apostags[0] == poss:
      candstems.append(astem)
  # 2. try partial poss matches if exact returned none
  if not candstems:
    for (astem, apostags) in anas:
      for apos in apostags:
        if poss in apos: 
          candstems.append(astem)
          break
  # Select stem from candidates:
  if not candstems: # no stems found: return surf
    return (surff, True)
  if len(candstems) == 1: # only 1 stem found: return that
    return (candstems[0], False)
  else: # several candidates: return longest one
    candstems.sort(key=len, reverse=True)
    return (candstems[0], False) 

# ------------------------------------------------------------------------------------------------

# Parse command line
oparser = OptionParser(usage="usage: %prog [options] infile outfile")
oparser.add_option("-d", action="store_true", dest="debug", default=False,
                  help="print \"?\" at end of line if analysis could not be chosen and surface form was used for stem")
oparser.add_option("-t", action="store_true", dest="tok", default=False,
                  help="print 4th column containing capitalization information")
oparser.add_option("-m", action="store_true", dest="morph", default=False,
                  help="append output of morphological analyzer to output")
oparser.add_option("-o", action="store_true", dest="override", default=False,
                  help="override PoS-tagger's tag by morphological analyzer's tag if it's unambiguous")
oparser.add_option("--oovstr", action="store", dest="oovstr", default='0',
                  help='string to display when -m used and there are no morphanalyses (default: "0")')
oparser.add_option("--morphdel", action="store", dest="morphdel", default=' ',
                  help='delimiter for morphanas when -m used (default: " ")')

(opts, args) = oparser.parse_args()
if (len(args) != 2):
    oparser.print_help()
    sys.exit(2)

# Load stemming exceptions file
for line in codecs.open(stemexcfile, encoding='utf-8'):
  line = line.rstrip()
  if not line or line.startswith('#'): # ignore empty and comment lines
    continue
  t = line.split('\t')
  if len(t) != 2:
    sys.exit('Format error in stemming exception file:\n' + line + '\n')
  if t[0].endswith('%'):
    stemexcpref[ t[0][:len(t[0])-1] ] = t[1]
  else:
    stemexc[ t[0] ] = t[1]

# Process I/O
inp = codecs.open(args[0], encoding='utf-8')
outp = codecs.open(args[1], 'w', encoding='utf-8')
tidx = 0 # index of token in sentence 
for line in inp:

  line = line.rstrip()
  tidx += 1

  # sentence boundary
  if line == ' 0':
    outp.write('\n')
    tidx = 0
    continue

  # parse line
  m = re.match(r'^([^\t]+)\t([^\t]+)\t ([0-9]+)( (.+))?', line)
  if not m:
    sys.stderr.write('Input error: ' + line + "\n")
    continue
  surf, pos, _dummy1_, _dummy2_, morphstr = m.groups()
  
  # capitalization feature
  tcol = ''
  if opts.tok:
    if surf[0].isupper():
      if tidx == 1:
        tcol = '\t2'
      else:
        tcol = '\t1'
    elif surf[0].isalpha():
        tcol = '\t0'
    else:
        tcol = '\t3'

  # get the stem
  (stem, is_surface) = get_stem(surf, pos, morphstr)

  # if -o: override PoS-tagger's tag by morphological analyzer's tag if it's unambiguous
  if opts.override and morphstr:
    anas = set(morphstr.split(' ')) # uniq anas
    if len(anas) == 1:
      pos = re.sub('[^/]+/(.+)', r'\1', anas.pop())

  # print the output line
  dcol = '\t?' if is_surface and opts.debug else ''
  morphstr = opts.oovstr if not morphstr else opts.morphdel.join(list(set(morphstr.split(' ')))) # uniq
  mcol = '\t' + morphstr if opts.morph else ''
  outp.write( surf + '\t' + stem + '\t' + pos + tcol + dcol + mcol + '\n')
  
  
 
