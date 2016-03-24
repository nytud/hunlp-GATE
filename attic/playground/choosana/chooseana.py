#!/usr/bin/env python

# Input: .stem file (tsv: surface form, lemma, pos-tag, morphanas delimited by "||")
# Output: .stem file with only 1 ana in the last column (see heuristics below) and the lemma column is the lemma from the chosen ana

import sys


def get_lemma(ana, surf):
  '''Return the lemma for an ana: either the lemma it contains or the surf if it's not the proper format (lemma/morphinfo)
     Attempt to assemble lemma if it's a compound.
  '''
  if '+' in ana:
    ret = ''.join([x.split('/')[0] for x in ana.split('+')])
    return surf if ret == '' else ret
  tmp = ana.split('/')
  if len(tmp) == 1 or tmp[0] == '':
    return surf
  return tmp[0]

def get_minimal_partition(l, k):
  '''for a list l, return the sublist whose elements all have with the minimal value using key function k'''
  tmp = sorted(l, key=k)
  minc = k(tmp[0])
  out = []
  for x in tmp:
    if k(x) > minc:
      break
    else:
      out.append(x)  
  return out


if len(sys.argv) != 2:
  sys.exit('Missing input file name')

for line in open(sys.argv[1]):
  
  line = line.rstrip()
  if not line or line.startswith('#'):
    print(line)
    continue
  
  t = line.split('\t')
  if len(t) != 4:
    sys.stderr.write('Incorrect line format: ' + line)
    break
  
  surf = t[0]
  pos = t[2]
  anas = list(set(t[3].split('||'))) # 0th filter: uniq the anas
  if len(anas) == 1: # there was only 1 ana anyway: nothing to do
    print('\t'.join([surf, get_lemma(anas[0], surf), pos, anas[0]]))
    continue
    
  # 1st filter: filter out anas that don't end with /PoS-tag or don't contain PoS-tag
  anas2 = [x for x in anas if x.endswith('/' + pos)]
  if not anas2: # if no anas left that end with /postag:
    anas2 = [x for x in anas if pos in x ] # try again w/ more relaxed criterion: anas should only contain postag
  if not anas2: # still no anas left with pos:
    #sys.stderr.write('WARNING: no anas with PoS-tag: ' + line + '\n')
    anas2 = anas # give up on filtering using PoS-tag, carry on with all anas
  if len(anas2) == 1: # only 1 left: we're done
    print('\t'.join([surf, get_lemma(anas2[0], surf), pos, anas2[0]]))
    continue
    
  # 2nd filter: filter out anas that have more number of compounding operations than minimal ana (w/ respect to no. of comp. ops.)
  anas3 = get_minimal_partition(anas2, lambda x: x.count('+'))
  if len(anas3) == 1: # only 1 left: we're done
    print('\t'.join([surf, get_lemma(anas3[0], surf), pos, anas3[0]]))
    continue

  # 3rd filter: filter out anas that have more number of derivation operations than minimal ana (w/ respect to no. of deriv. ops.)
  anas4 = get_minimal_partition(anas3, lambda x: x.count('['))
  if len(anas4) == 1: # only 1 left: we're done
    print('\t'.join([surf, get_lemma(anas4[0], surf), pos, anas4[0]]))
    continue

  # 4th filter: choose anas with the longest lemma
  anas5 = sorted(anas4, key=lambda x: len(x.split('/')[0]), reverse=True)
  if len(anas5) == 1: # only 1 left: we're done
    print('\t'.join([surf, get_lemma(anas5[0], surf), pos, anas5[0]]))
    continue
  
  # 5th filter: choose 1st ana whose lemma has more similar initial capitalization to surface form
  anas6 = sorted(anas5, key=lambda x: 1 if x.split('/')[0][0] == surf[0] else 0, reverse=True)
  print('\t'.join([surf, get_lemma(anas6[0], surf), pos, anas6[0]]))

