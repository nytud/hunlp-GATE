#!/usr/bin/python3
# -*- coding: utf-8, vim: expandtab:ts=4 -*-
"""
features.py stores implementations of individual feature types for use with
HunTag. Feel free to add your own features but please add comments to describe
them.
"""

import re
import sys

# GLOBAL DECLARATION BEGIN
# see: token_longPattern, token_shortPattern, sentence_lemmaLowered, possConnect, token_mmoSimple
smallcase = 'aábcdeéfghiíjklmnoóöőpqrstuúüűvwxyz'
bigcase = 'AÁBCDEÉFGHIÍJKLMNOÓÖŐPQRSTUÚÜŰVWXYZ'
big2small = {}
for i, _ in enumerate(bigcase):
    big2small[bigcase[i]] = smallcase[i]
casREKR = re.compile('<CAS')
casREMSD = re.compile('\[?N')
possessorMSD = re.compile(r'--[sp]\d')
objMSD = '\[?N'

possessorKR = re.compile('<POSS')
objKR = 'NOUN'
MMOpatt = re.compile('[\[,\]]')
# GLOBAL DECLARATION END


# HELPER FUNCTIONS BEGIN
def tags_since_pos(sen, tokRange, myPos, strict=True):
    """Gather all tags since POS myPos in the sentence (not used directly as a feature)

    Args:
       sen (list): List of tokens in the sentence
       tokRange (int): range of tokens from the start of the sentence
       myPos (str): POS tag to search for
       strict(bool): full matching or not...

    Returns:
       [tags joined by '+']: all tags since myPos POS tag

    HunTag:
        Type: Sentence
        Field: analysis
        Example: ???
        Use case: NER, Chunk

    Replaces:
        since_dt: abstraction
    """
    tags = []
    for pos in sen[:tokRange]:
        if (strict and myPos == pos) or (not strict and re.search(myPos, pos)):
            tags = [pos]
        else:
            tags.append(pos)
    return ['+'.join(tags)]


def sincePos(krVec, c, featVecElem, tag, featPrefix):
    """Parameter XXX

    Args:
       krVec (list): List of tokens in the sentence
       c (int): Range of tokens from the start of the sentence
       featVecElem (list): Current feature Vector element (to be updated)
       tag(str): full matching or not...
       featPrefix(str): prefix to set

    Returns (updates featVecELem):
       [tags joined by '+']: all tags since myPos POS tag
    """
    tagst = tags_since_pos(krVec, c, tag)[0]
    if len(tagst) > 0:
        featVecElem.append(featPrefix + tagst)


def doNothing(*_):
    pass


def casDiff(krVec, c, featVecElem, casRE, featName):
    """Test if subsequent nouns is in di'casDiff'fferent grammatical case...

    Args:
       krVec (list): List of tokens in the sentence
       c (int): Range of tokens from the start of the sentence
       featVecElem (list): Current feature Vector element (to be updated)
       casRE(re.pattern): pattern to match
       featName(str): Name of the feature to be set

    Returns (updates featVecELem):
       ['casDiff']: if case is different...
    """
    lastF = '' if c == 0 else krVec[c - 1]
    if casRE.search(lastF) and casRE.search(krVec[c]) and lastF != krVec[c]:
        featVecElem.append(featName)


def possConnect(krVec, c, featVecElem, possessor, obj, featPrefix):
    """Connect possessor with posessed object

    Args:
       krVec (list): List of tokens in the sentence
       c (int): Range of tokens from the start of the sentence
       featVecElem (list): Current feature Vector element (to be updated)
       possessor(re.pattern): Pattern of possessor
       obj(str): Possessed object string to be compiled into pattern
       featPrefix(str): prefix to set

    Returns (updates featVecELem):
       [tags joined by '+']: all tags since obj POS tag
    """
    if possessor.search(krVec[c]):
        tagst = tags_since_pos(krVec, c, obj, False)[0]
        if len(tagst) > 0:
            featVecElem.append(featPrefix + tagst)
# HELPER FUNCTIONS END


# XXX Return is not bool
def token_stupidStem(token, _=None):
    """Stem tokens with hyphen (-) in them.

    Args:
       token (str): The token
       _: Unused

    Returns:
       [Str]: The part of the token before hyphen (-) character

    HunTag:
        Type: Token
        Field: Token
        Example: 'MTI-nek' -> MTI
        Use case: NER

    Used extensively in other functions
    """
    r = token.rfind('-')
    if r == -1:
        return [token]
    else:
        return [token[:r]]


def token_hasCapOperator(form, _=None):
    """Has it capital letter anywhere?

    Args:
       form (str): The token
       _: Unused

    Returns:
       [Bool in int format]: True if lowercasing modifies the token

    HunTag:
        Type: Token
        Field: Token
        Example: 'aLma' -> [1], 'alma' -> [0]
        Use case: NER

    Replaces:
        token_isCapitalizedOperator: deprecated
        token_lowerCaseOperator: inverse, redundant
    """
    return [int(form.lower() != form)]


def token_isCapOperator(form, _=None):
    """Only the first letter is capital
    This is the new isCapitalized: Starts with uppercase

    Args:
       form (str): The token
       _: Unused

    Returns:
       [Bool in int format]: True if lowercasing the first letter modifies the token

    HunTag:
        Type: Token
        Field: Token
        Example: 'Alma' -> [1], 'ALma' -> [0]
        Use case: NER

    Replaces:
        token_notCapitalizedOperator: inverse, redundant
    """
    return [int(form[0] != form[0].lower())]


def token_isAllcapsOperator(form, _=None):
    """StupidStem consists of uppercase letters

    Args:
       form (str): The token
       _: Unused

    Returns:
       [Bool in int format]: True if token_stupidStem is uppercase

    HunTag:
        Type: Token
        Field: Token
        Example: 'MTI-vel' -> [1], 'Mti-vel' -> [0]
        Use case: NER
    """
    return [int(token_stupidStem(form)[0].isupper())]


def token_isCamelOperator(form, _=None):
    """The first letter is lower, the others has, but not all uppercase (camelCasing)

    Args:
       form (str): The token
       _: Unused

    Returns:
       [Bool in int format]: True if Not all letter are uppercase, but at least one is from the second character

    HunTag:
        Type: Token
        Field: Token
        Example: 'aLMa' -> [1], 'ALMA' -> [0], 'alma' -> [0]
        Use case: NER
    """
    return [int(not token_isAllcapsOperator(form) and form[1:].lower() != form[1:])]


def token_threeCaps(form, _=None):
    """Token is three uppercase letters?

    Args:
       form (str): The token
       _: Unused

    Returns:
       [Bool in int format]: True if Token consist of three uppercase letters

    HunTag:
        Type: Token
        Field: Token
        Example: 'MTI' -> [1], 'Mti' -> [0], 'Matáv' -> [0]
        Use case: NER, but not in SzegedNER
    """
    return [int(len(form) == 3 and token_stupidStem(form)[0].isupper())]


def token_startsWithNumberOperator(form, _=None):
    """Token starts with number

    Args:
       form (str): The token
       _: Unused

    Returns:
       [Bool in int format]: True if Token's first letter is a digit

    HunTag:
        Type: Token
        Field: Token
        Example: '3-gram' -> [1], 'n-gram' -> [0]
        Use case: NER, but not in SzegedNER
    """
    return [int(form[0].isdigit())]


def token_hasNumberOperator(form, _=None):
    """Token contains numbers

    Args:
       form (str): The token
       _: Unused

    Returns:
       [Bool in int format]: True if token contains numbers

    HunTag:
        Type: Token
        Field: Token
        Example: '3-gram' -> [1], 'n-gram' -> [0]
        Use case: NER

    Replaces:
        token_isNumberOperator: deprecated
    """
    return [int(not set('0123456789').isdisjoint(set(form)))]


def token_hasDashOperator(form, _=None):
    """Token contains hyphen (-)

    Args:
       form (str): The token
       _: Unused

    Returns:
       [Bool in int format]: True if token contains hyphen (-)

    HunTag:
        Type: Token
        Field: Token
        Example: '3-gram' -> [1], 'ngram' -> [0]
        Use case: NER, but not in SzegedNER
    """
    return [int('-' in form)]


def token_hasUnderscoreOperator(form, _=None):
    """Token contains underscore (_)

    Args:
       form (str): The token
       _: Unused

    Returns:
       [Bool in int format]: True if token contains underscore (_)

    HunTag:
        Type: Token
        Field: Token
        Example: 'function_name' -> [1], 'functionName' -> [0]
        Use case: Chunk
    """
    return [int('_' in form)]


def token_hasPeriodOperator(form, _=None):
    """Token contains period (.)

    Args:
       form (str): The token
       _: Unused

    Returns:
       [Bool in int format]: True if token contains period (.)

    HunTag:
        Type: Token
        Field: Token
        Example: 'README.txt' -> [1], 'README' -> [0]
        Use case: NER, but not in SzegedNER
    """
    return [int('.' in form)]


# XXX Return is not bool
def token_longPattern(token, _=None):
    """Convert token by it's casing pattern

    Args:
       token (str): The token
       _: Unused

    Returns:
       [Str]: For each letter uppercase -> A, lowercase -> a, not letter -> _

    HunTag:
        Type: Token
        Field: Token
        Example: 'README.txt' -> [AAAAA_aaa], 'README' -> [AAAAA]
        Use case: NER
    """
    pattern = ''
    for char in token:
        if char in smallcase:
            pattern += 'a'
        elif char in bigcase:
            pattern += 'A'
        else:
            pattern += '_'

    return [pattern]


# XXX Return is not bool
def token_shortPattern(token, _=None):
    """Convert token by it's shortened casing pattern

    Args:
       token (str): The token
       _: Unused

    Returns:
       [Str]: For each letter uppercase -> A, lowercase -> a, not letter -> _
              consecutive identical letters shortened to one letter

    HunTag:
        Type: Token
        Field: Token
        Example: 'README.txt' -> [A_a], 'README' -> [A]
        Use case: NER
    """
    pattern = ''
    prev = ''
    for char in token:
        if char in smallcase:
            if prev != 'a':
                pattern += 'a'
                prev = 'a'
        elif char in bigcase:
            if prev != 'A':
                pattern += 'A'
                prev = 'A'
        else:
            if prev != '_':
                pattern += '_'
                prev = '_'

    return [pattern]


# XXX Return is not bool
def token_chunkTag(chunkTag, _=None):
    """Returns the field as it is. (getForm do the same for non-merged tokens)

    Args:
       chunkTag (str):  NP chunking tag
       _: Unused

    Returns:
       [Str]: The field as it is

    HunTag:
        Type: Token
        Field: Any
        Example: 'B-NP' -> [B-NP]
        Use case: NER

    Replaces:
        token_getBNCtag: is same
    """
    return [chunkTag]


# XXX Return is not bool
def token_chunkType(chunkTag, _=None):
    """Returns the field type from the 3rd character

    Args:
       chunkTag (str):  NP chunking tag
       _: Unused

    Returns:
       [Str]: The field from the 3rd character

    HunTag:
        Type: Token
        Field: Any
        Example: 'B-NP' -> [NP]
        Use case: NER, but not in SzegedNER

    Replaces:
        token_getTagType: is same
    """
    return [chunkTag[2:]]


# XXX Return is not bool
def token_getForm(token, _=None):
    """Returns input if it has no underscore (_) in it, else returns 'MERGED'
     (Recski merged multi-token names by underscore (_) in chunking)

    Args:
       token (str): The token
       _: Unused

    Returns:
       [Str]: Token or MERGED if multi-token name merged by underscore

    HunTag:
        Type: Token
        Field: Token
        Example: 'Alma_fa' -> [MERGED], 'alma' -> [alma]
        Use case: NER, Chunk
    """
    if '_' not in token:
        return [token]
    return ['MERGED']


# XXX Return is not bool
def token_ngrams(token, options):
    """Make character n-grams

    Args:
       token (str): The token
       options (dict): n, the order of grams

    Returns:
       [Str]: List of Token's character n-grams

    HunTag:
        Type: Token
        Field: Token
        Example: 'alma', n=3 -> ['@alm', 'lma@'], 'almafa' -> ['@alm', 'lma', 'maf', 'afa@'], 'Y2K' -> ['@Y2K']
        Use case: NER, Chunk
    """
    n = int(options['n'])
    f = [str(token[c:c + n]) for c in range(max(0, len(token) - n + 1))]
    flen = len(f)
    if flen > 0:
        f[0] = '@{0}'.format(f[0])
        if flen > 1:
            f[-1] = '{0}@'.format(f[-1])
    return f


# XXX Return is not bool
def token_firstChar(token, _=None):
    """Return the first character

    Args:
       token (str): The token
       _: Unused

    Returns:
       [Str]: Field's first character

    HunTag:
        Type: Token
        Field: Any field
        Example: 'B-NP' -> [B]
        Use case: NER, Chunk

    Replaces:
         token_chunkPart: is same
         token_msdPos(?): is same
         token_posStart: is same
    """
    return [token[0]]


# XXX Return is not bool
def token_msdPos(msdAnal, _=None):
    """Return the second character (Square brackets enclosed)

    Args:
       msdAnal (str): MSD code analysis
       _: Unused

    Returns:
       [Str]: Field's second character

    HunTag:
        Type: Token
        Field: Analysis
        Example: '[Nc-sa—s3]' -> N
        Use case: NER, Chunk
    """
    return [msdAnal[1]]


# XXX Return is not bool
def token_msdPosAndChar(msdAnal, _=None):
    """MSD code's 'krPieces' function (Square brackets enclosed)

    Args:
       msdAnal (str):  MSD code analysis
       _: Unused

    Returns:
       [Str]: MSD code's pieces

    HunTag:
        Type: Token
        Field: Analysis
        Example: '[Nc-sa—s3]' -> [N2c, N]
        Use case: NER, Chunk
    """
    pos = msdAnal[1]  # main POS
    return ['{0}{1}{2}'.format(pos, c, ch) for c, ch in enumerate(msdAnal[2:-1]) if ch != '-']


# XXX Return is not bool
def token_prefix(token, options):
    """Make n-long prefix

    Args:
       token (str): The token
       options (dict): n, the length of prefix

    Returns:
       [Str]: An n-long prefix of the token

    HunTag:
        Type: Token
        Field: Token
        Example: 'alma', n=3 -> [alm], 'almafa' n=5 -> [almaf]
        Use case: NER
    """
    return [token[0:int(options['n'])]]


# XXX Return is not bool
def token_suffix(token, options):
    """Make n-long suffix

    Args:
       token (str): The token
       options (dict): n, the length of suffix

    Returns:
       [Str]: An n-long suffix of the token

    HunTag:
        Type: Token
        Field: Token
        Example: 'alma', n=3 -> [lma], 'almafa' n=5 -> [lmafa]
        Use case: NER
    """
    return [token[-int(options['n']):]]


# XXX Return is not bool
def sentence_lemmaLowered(sen, fields, _=None):
    """Lemma or Token has first letter capitalized

    Args:
       sen (list): List of tokens in the sentence
       fields (list): number of fields used (order: token, lemma)
       _: Unused

    Returns:
       [[Str/Int]]: 1,0,'raised' See the truth table below

    HunTag:
        Type: Sentence
        Field: Token, Lemma
        Example: 'alma', 'Alma -> ['raised'], 'Almafa', 'almafa' -> [1]
        Use case: NER
    """
    assert len(fields) == 2
    featVec = []
    for token, lemma in zip((tok[fields[0]] for tok in sen), (tok[fields[1]] for tok in sen)):
        if token[0] not in bigcase and big2small[lemma[0]] == token[0]:  # token lower and lemma upper
            featVec.append(['raised'])

        elif token[0] in bigcase and token[0] == lemma[0]:  # token upper and lemma upper
            featVec.append([0])

        elif token[0] in bigcase and big2small[token[0]] == lemma[0]:  # token upper and lemma lower
            featVec.append([1])

        featVec.append(['N/A'])  # token lower and lemma lower

    return featVec


# XXX Return is not bool
def token_krPieces(krAnal, _=None):
    """Split KR code analysis to pieces

    Args:
       krAnal (str): KR code analysis
       _: Unused

    Returns:
       [Str]: Pass KR code pieces

    HunTag:
        Type: Token
        Field: Token
        Example: ???
        Use case: NER, Chunk

    Known bug: token_krPieces is incorrect e.g. not all occurences of PLUR refer
               to NOUN. KR codes must be parsed in a more sophisticated manner
               -- we have the code that does so, but we must decide on what
               kr features should be like!
               see: https://github.com/recski/HunTag/issues/4
    """
    pieces = re.split(r'\W+', krAnal.split('/')[-1])
    pos = pieces[0]
    feats = []
    last = ''
    for piece in pieces:
        if piece == 'PLUR':
            processed = '{0}_PLUR'.format(pos)
        elif piece in ('1', '2') or last == 'CAS':
            processed = '{0}_{1}'.format(last, piece)
        else:
            processed = piece
        if processed != 'CAS':
            feats.append(processed)

        last = piece

    return [feat for feat in feats if feat]

# XXX Return is not bool
def token_univPieces(univAnal, _=None):
    """Split Univmorf feature-value pairs to pieces

    Args:
       univAnal (str): Univmorf feature-value pairs
       _: Unused

    Returns:
       [Str]: Pass univmorf feature-value pairs

    HunTag:
        Type: Token
        Field: Token
        Example: 'Case=Nom|Number=Sing' --> ['Case=Nom', 'Number=Sing']
        Use case: Chunk

    """
    pieces = univAnal.split('|')
    return [piece for piece in pieces if piece]


# XXX Return is not bool
def token_hfstPieces(hfstAnal, _=None):
    """Split the morphological codes of the HFST-based eM-morph morphological analyser to pieces

    Args:
       hfstAnal (str): eM-morph morphological codes
       _: Unused

    Returns:
       [Str]: Pass eM-morph morphological code pieces

    HunTag:
        Type: Token
        Field: Token
        Example: '[Pl.Poss.3Sg][Nom]' --> ['Pl.Poss.3Sg', 'Nom']
        Use case: Chunk

    """
    pieces = hfstAnal.split('[')
    for piece in pieces:
        piece = piece.strip('[]')
        return [piece for piece in pieces if piece]

# XXX Return is not bool
def token_fullKrPieces(krAnal, _=None):
    """Split KR code analysis to pieces from full analysis (with lemma)

    Args:
       krAnal (str): KR code analysis
       _: Unused

    Returns:
       [Str]: Pass KR code pieces

    HunTag:
        Type: Token
        Field: Token
        Example: ???
        Use case: NER, Chunk
    """
    return token_krPieces('/'.join(krAnal.split('/')[1:]))


def sentence_isBetweenSameCases(sen, fields, options=None):
    """Is between same grammatical cases

    Args:
       sen (list): The list of tokens in the sentence
       fields (list): Field numbers, that will be used
       options (dict): options (maxDist default: 6

    Returns:
       [[Bool in int format]]: Pass the resulting array

    HunTag:
        Type: Sentence
        Field: Analysis
        Example: ???
        Use case: NER, Chunk
    """
    if options is None:
        options = {'maxDist': '6'}
    if len(fields) > 1:
        print('Error: "isBetweenSameCases" function\'s "fields" argument\'s\
            length must be one not {0}'.format(len(fields)), file=sys.stderr, flush=True)
        sys.exit(1)
    maxDist = int(options['maxDist'])
    nounCases = [[] for _ in sen]
    featVec = [[] for _ in sen]
    krVec = [token[fields[0]] for token in sen]

    for c, kr in enumerate(krVec):
        if 'CAS' in kr:
            cases = re.findall(r'CAS<...>', kr)
            if not cases:
                nounCases[c] = ['NO_CASE']
            else:
                case = cases[0][-4:-1]
                nounCases[c] = [case]
        elif 'Case=' in kr:
            cases = re.findall(r'Case=[A-Z][a-z][a-z]', kr)
            if not cases:
                nounCases[c] = ['NO_CASE']
            else:
                case = cases[0][-4:-1]
                nounCases[c] = [case]

    leftCase = {}
    rightCase = {}
    currCase = None
    casePos = None
    for j, _ in enumerate(sen):
        if not nounCases[j]:
            leftCase[j] = (currCase, casePos)
        else:
            currCase = nounCases[j]
            casePos = j
            leftCase[j] = (None, None)

    currCase = None
    casePos = None
    for j in range(len(sen) - 1, -1, -1):
        if not nounCases[j]:
            rightCase[j] = (currCase, casePos)
        else:
            currCase = nounCases[j]
            casePos = j
            rightCase[j] = (None, None)

    for j, _ in enumerate(sen):
        featVec[j] = [0]
        if (rightCase[j][0] == leftCase[j][0] and rightCase[j][0] is not None and
                abs(rightCase[j][1] - leftCase[j][1]) <= maxDist):
            featVec[j] = [1]

    return featVec


# XXX Return is not bool
def token_getPosTag(krAnal, _=None):
    """Return KR code POS tag

    Args:
       krAnal (str): KR code analysis
       _: Unused

    Returns:
       [Str]: Pass ???

    HunTag:
        Type: Token
        Field: Analysis
        Example: ???
        Use case: NER, Chunk
    """
    return [re.split(r'\W+', krAnal.split('/')[-1])[0]]


# XXX Return is not bool
def sentence_krPatts(sen, fields, options):
    """Return KR code patterns
    G. Recski, D. Varga: Magyar főnévi csoportok azonosítása In: Általános Nyelvészeti Tanulmányok 24. 2012.
    http://people.mokk.bme.hu/~recski/pub/huntag_anyt.pdf Page 6
    G. Recski MA thesis: NP chunking in Hungarian
    http://people.mokk.bme.hu/~recski/pub/np_thesis.pdf Page 21

    Args:
       sen (list): List of tokens in the sentence
       fields (list): number of fields used
       options (dict): available options

    Returns:
       [[Str]]: Pass KR code patterns (see above)

    HunTag:
        Type: Sentence
        Field: Analysis
        Example: ???
        Use case: NER, Chunk

    Replaces:
       parsePatts: superseded
       mySpecPatts: superseded
       myPatts: Unimplementable, almost same
       sentence_parsePatts: superseded
    """
    assert options['lang'] in ('en', 'hu')
    minLength = int(options['minLength'])
    maxLength = int(options['maxLength'])
    rad = int(options['rad'])
    assert len(fields) == 1
    f = fields[0]
    featVec = [[] for _ in sen]
    krVec = [tok[f] for tok in sen]

    if options['lang'] == 'hu':
        if not options['fullKr'] and not options['MSD']:
            krVec = [token_getPosTag(kr)[0] for kr in krVec]
        elif options['MSD']:
            krVec = [tok[f] for tok in sen]
    else:
        krVec = [tok[f][0] for tok in sen]

    applyCasDiffFun = doNothing
    applyPossConnectFun = doNothing

    if options['lang'] == 'hu':
        if options['MSD']:
            tag_dt, featPrefix_dt = '[Tf]', 'dt_'  # "(since) last detrminant" MSD
            casRE, featName = casREMSD, 'casDiff'  # casDiff
            possRE, obj, featPrefix_poss = possessorMSD, objMSD, 'possession_'
        else:
            tag_dt, featPrefix_dt = 'DT', 'dt_'    # "(since) last detrminant" KR
            casRE, featName = casREKR, 'casDiff'   # casDiff
            possRE, obj, featPrefix_poss = possessorKR, objKR, 'possession_'
        if options['CASDiff'] == 1:
            applyCasDiffFun = casDiff
        if options['POSSConnect'] == 1:
            applyPossConnectFun = possConnect
    else:
        tag_dt, featPrefix_dt = 'DT', 'dt_'  # "(since) last detrminant" CoNLL (possConnect and CasDiff not used)
        casRE, featName = None, None
        possRE, obj, featPrefix_poss = None, None, None

    if options['since_dt'] == 1:
        applySincePosFun = sincePos
    else:
        applySincePosFun = doNothing

    assert len(krVec) == len(sen)
    krVecLen = len(krVec)
    # For every token in sentence
    for c in range(krVecLen):
        applySincePosFun(krVec, c, featVec[c], tag_dt, featPrefix_dt)
        applyCasDiffFun(krVec, c, featVec[c], casRE, featName)
        applyPossConnectFun(krVec, c, featVec[c], possRE, obj, featPrefix_poss)
        # Begining in -rad and rad but starts in the list boundaries (lower)
        for k in range(max(-rad, -c), rad):
            # Ending in -rad + 1 and rad + 2  but starts in the list boundaries (upper)
            # and keep minimal and maximal length
            for j in range(max(-rad + 1, minLength + k), min(rad + 2, maxLength + k + 1, krVecLen - c + 1)):
                value = '+'.join(krVec[c + k:c + j])
                feat = '{0}_{1}_{2}'.format(k, j, value)
                featVec[c].append(feat)
    return featVec


def token_krPlural(krAnal, _=None):
    """Detect plural form in KR code

    Args:
       krAnal (str): KR code analysis
       _: Unused

    Returns:
       [Bool in int format]: True if KR code is plural

    HunTag:
        Type: Token
        Field: Analysis
        Example: ???
        Use case: NER, Chunk
    """
    return [int('NOUN<PLUR' in krAnal)]


def token_univPlural(univAnal, _=None):
    """Detect plural form in univmorf feature-value pairs

    Args:
       univAnal (str): univmorf code analysis
       _: Unused

    Returns:
       [Bool in int format]: True if univmorf code contains plural

    HunTag:
        Type: Token
        Field: Analysis
        Example: 'teendők' Case=Nom|Number=Plur --> univPlural = 1
        Use case: NER, Chunk
    """
    return [int('Number=Plural' in univAnal)]


# XXX Return is not bool
def token_hfstPlural(hfstAnal, _=None):
    """Detect plural form in the new formalism of HFST-based Hungarian morphological analyser (aka eM-morph)

    Args:
       hfstAnal (str): hfstmorf code analysis
       _: Unused

    Returns:
       [Bool in int format]: True if hfst code contains plural

    HunTag:
        Type: Token
        Field: Analysis
        Example: 'teendők' [Pl][Nom] --> hfstPlural = 1
        Use case: NER
    """
    return [int('[Pl]' in hfstAnal)]


# XXX Return is not bool
def token_getNpPart(chunkTag, _=None):
    """Checks if the token is part of NP

    Args:
       chunkTag (Str): NP chunking tag
       _: Unused

    Returns:
       [Str]: Return tag's first character or 'O'

    HunTag:
        Type: Token
        Field: NP chunks
        Example: ???
        Use case: NER, Chunk
    """
    if chunkTag == 'O' or chunkTag[2:] != 'NP':
        return ['O']
    else:
        return [chunkTag[0]]


def token_capPeriodOperator(form, _=None):
    """Token is an uppercase letter followed by a period (from Bikel et al. (1999))

    Args:
       form (str): The token
       _: Unused

    Returns:
       [Bool in int format]: Matches re [A-Z]\.$

    HunTag:
        Type: Token
        Field: Token
        Example: 'A.' -> [1], 'alma' -> [0]
        Use case: NER
    """
    return [int(bool(re.match(r'[A-Z]\.$', form)))]


def token_isDigitOperator(form, _=None):
    """Token is number

    Args:
       form (str): The token
       _: Unused

    Returns:
       [Bool in int format]: True if Token is number

    HunTag:
        Type: Token
        Field: Token
        Example: '333' -> [1], '3-gram' -> [0]
        Use case: NER
    """
    return [int(form.isdigit())]


def token_oneDigitNumOperator(form, _=None):
    """Token is one digit (from Zhou and Su (2002))

    Args:
       form (str): The token
       _: Unused

    Returns:
       [Bool in int format]: True if Token is one digit

    HunTag:
        Type: Token
        Field: Token
        Example: '3' -> [1], '333' -> [0]
        Use case: NER, but not in SzegedNER
    """
    return [int(len(form) == 1 and form.isdigit())]


def token_twoDigitNumOperator(form, _=None):
    """Token is two digit (from Bikel et al. (1999))

    Args:
       form (str): The token
       _: Unused

    Returns:
       [Bool in int format]: True if Token is two digit

    HunTag:
        Type: Token
        Field: Token
        Example: '33' -> [1], '333' -> [0]
        Use case: NER
    """
    return [int(len(form) == 2 and form.isdigit())]


def token_threeDigitNumOperator(form, _=None):
    """Token is three digit

    Args:
       form (str): The token
       _: Unused

    Returns:
       [Bool in int format]: True if Token is three digit

    HunTag:
        Type: Token
        Field: Token
        Example: '333' -> [1], '3333' -> [0]
        Use case: NER
    """
    return [int(len(form) == 3 and form.isdigit())]


def token_fourDigitNumOperator(form, _=None):
    """Token is four digit

    Args:
       form (str): The token
       _: Unused

    Returns:
       [Bool in int format]: True if Token is four digit

    HunTag:
        Type: Token
        Field: Token
        Example: '2015' -> [1], '333' -> [0]
        Use case: NER
    """
    return [int(len(form) == 4 and form.isdigit())]


def token_isPunctuationOperator(form, _=None):
    """Token is punctuation

    Args:
       form (str): The token
       _: Unused

    Returns:
       [Bool in int format]: True if Token is punctuation

    HunTag:
        Type: Token
        Field: Token
        Example: '.' -> [1], 'A.' -> [0]
        Use case: NER, but not in SzegedNER
    """
    return [int(set(form).issubset(set(',.!"\'():?<>[];{}')))]


def token_containsDigitAndDashOperator(form, _=None):
    """Token contains digit and hyphen (-) (from Bikel et al. (1999))

    Args:
       form (str): The token
       _: Unused

    Returns:
       [Bool in int format]: True if Token contains digits and hyphen (-)

    HunTag:
        Type: Token
        Field: Token
        Example: '2014-15' -> [1], '3-gram' -> [0]
        Use case: NER, but not in SzegedNER
    """
    return [int(bool(re.match('[0-9]+-[0-9]+', form)))]


def token_containsDigitAndSlashOperator(form, _=None):
    """Token contains digit and slash (/) (from Bikel et al. (1999))

    Args:
       form (str): The token
       _: Unused

    Returns:
       [Bool in int format]: True if Token contains digit and slash (/)

    HunTag:
        Type: Token
        Field: Token
        Example: '2014/2015' -> [1], '3/A' -> [0]
        Use case: NER, but not in SzegedNER
    """
    return [int(bool(re.match('[0-9]+/[0-9]+', form)))]


def token_containsDigitAndCommaOperator(form, _=None):
    """Token contains digit and comma (. or ,) (from Bikel et al. (1999))

    Args:
       form (str): The token
       _: Unused

    Returns:
       [Bool in int format]: True if Token contains digit and comma (. or ,)

    HunTag:
        Type: Token
        Field: Token
        Example: '2015.04.07.' -> [1], '2015.txt' -> [0]
        Use case: NER
    """
    return [int(bool(re.match('[0-9]+[,.][0-9]+', form)))]


def token_yearDecadeOperator(form, _=None):
    """Token contains year decade (from Zhou and Su (2002))

    Args:
       form (str): The token
       _: Unused

    Returns:
       [Bool in int format]: True if Token contains year decade

    HunTag:
        Type: Token
        Field: Token
        Example: '1990s' -> [1], '80s' -> [1]
        Use case: NER
    """
    return [int(bool(re.match('[0-9][0-9]s$', form) or
                re.match('[0-9][0-9][0-9][0-9]s$', form)))]


def sentence_newSentenceStart(sen, *_):
    """Sentence starting token or not

    Args:
       sen (list): List of tokens in the sentence
       _: Unused

    Returns:
       [[Bool in int format]]: True if Token is at the sentence start

    HunTag:
        Type: Sentence
        Field: Token
        Example: ???
        Use case: NER, Chunk
    """
    featVec = [[0] for _ in sen]
    featVec[0][0] = 1
    return featVec


def sentence_newSentenceEnd(sen, *_):
    """Sentence ending token or not

    Args:
       sen (list): List of tokens in the sentence
       _: Unused

    Returns:
       [[Bool in int format]]: True if Token is at the sentence end

    HunTag:
        Type: Sentence
        Field: Token
        Example: ???
        Use case: NER, Chunk
    """
    featVec = [[0] for _ in sen]
    featVec[-1][0] = 1
    return featVec


def token_unknown(anal, _=None):
    """Guessed Unknown word from HunMorph?

    Args:
       anal (str): The analysis
       _: Unused

    Returns:
       [[Bool in int format]]: True if Tokens analysis contains OOV OR UNKNOWN

    HunTag:
        Type: Token
        Field: analysis
        Example: ???
        Use case: NER

    Replaces:
        token_OOV: superseded
    """
    return [int('UNKNOWN' in anal or 'OOV' in anal)]


# XXX Return is not bool
def token_getKrLemma(krLemma, _=None):
    """Get lemma from HunMorph's analysis

    Args:
       krLemma (str): The token
       _: Unused

    Returns:
       [Str]: Returns lemma

    HunTag:
        Type: Token
        Field: analysis
        Example: ???
        Use case: NER
    """
    return [krLemma.split('/')[0]]


# XXX Return is not bool
def token_getKrPos(krAnal, _=None):
    """Get KR code POS tag

    Args:
       krAnal (str): The token
       _: Unused

    Returns:
       [Str]: Returns POS part of KR code

    HunTag:
        Type: Token
        Field: analysis
        Example: ???
        Use case: NER, but not in SzegedNER
    """
    if '<' in krAnal:
        return [krAnal.split('<')[0]]
    return [krAnal]


# XXX Return is not bool
def token_getPennTags(pennTag, _=None):
    """Reduces Penn tagset's similar tags

    Args:
       pennTag (str): Penn Tag
       _: Unused

    Returns:
       [Str]: ???

    HunTag:
        Type: Token
        Field: analysis
        Example: ???
        Use case: NER
    """
    if re.match('^N', pennTag) or re.match('^PRP', pennTag):
        return ['noun']
    elif pennTag == 'IN' or pennTag == 'TO' or pennTag == 'RP':
        return ['prep']
    elif re.match('DT$', pennTag):
        return ['det']
    elif re.match('^VB', pennTag) or pennTag == 'MD':
        return ['verb']
    else:
        return ['0']


def token_humorPlural(humorTag, _=None):
    """Check if Humor code plural

    Args:
       humorTag (str): Humor analysis
       _: Unused

    Returns:
       [[Bool in int format]]: True if Humor analysis is plural

    HunTag:
        Type: Token
        Field: analysis
        Example: ???
        Use case: NER
    """
    return [int('PL' in humorTag)]


# XXX Return is not bool
def token_getKrEnd(krAnal, _=None):
    """Return KR code end

    Args:
       krAnal (str): The token
       _: Unused

    Returns:
       [Str]: ???

    HunTag:
        Type: Token
        Field: analysis
        Example: ???
        Use case: NER, but not in SzegedNER
    """
    end = '0'
    if '<' in krAnal:
        pieces = krAnal.split('<', 1)
        end = pieces[1]

    return [end]


def token_pennPlural(pennTag, _=None):
    """Check if Penn code plural

    Args:
       pennTag (str): Penn tag
       _: Unused

    Returns:
       [Bool in int format]: ???

    HunTag:
        Type: Token
        Field: analysis
        Example: ???
        Use case: NER, but not in SzegedNER

    Replaces:
        token_plural: is same
    """
    return [int(pennTag == 'NNS' or pennTag == 'NNPS')]


# XXX Return is not bool
def token_humorPieces(humorTag, _=None):
    """Return Humor tag pieces
    For Humor code:
    'FN/N|PROP|FIRST/ffinev;veznev' => ['FN', 'N|PROP|FIRST', 'ffinev;veznev']
    'FN+PSe3+DEL' => ['FN', 'PSe3', 'DEL']

    Args:
       humorTag (str): Humor tag
       _: Unused

    Returns:
       [str]: ???

    HunTag:
        Type: Token
        Field: analysis
        Example: ???
        Use case: Chunk, NER
    """
    return [item for part in humorTag.split('/') for item in part.split('+')]


# XXX Return is not bool
def token_humorSimple(humorTag, _=None):
    """Humor test: return self

    Args:
       humorTag (str): Humor tag
       _: Unused

    Returns:
       [str]: self

    HunTag:
        Type: Token
        Field: analysis
        Example: ???
        Use case: Chunk, NER
    """
    return [humorTag]


# XXX Return is not bool
def token_wordNetSimple(wordNetTags, _=None):
    """Wordnet synsets as tags

    Args:
       wordNetTags (str): wordNet synsets as tags
       _: Unused

    Returns:
       [str]: list of wordNet synsets as features

    HunTag:
        Type: Token
        Field: WordNet tags
        Example: animate.n.1/human.n.1 -> ['animate.n.1', 'human.n.1'], '' -> []
        Use case: Chunk, NER
    """
    if len(wordNetTags) == 0:
        return []
    return wordNetTags.split('/')


# XXX Return is not bool
def token_mmoSimple(mmoTags, _=None):
    """MMO properties

    Args:
       mmoTags (str): wordNet synsets as tags
       _: Unused

    Returns:
       [str]: list of MMO tags as features

    HunTag:
        Type: Token
        Field: MMOtags
        Example:
            in:'NX[abstract=YES,animate=NIL,auth=YES,company=NIL,encnt=YES,human=NIL]'
            out: ['NX', 'abstract=YES', 'animate=NIL', 'auth=YES', 'company=NIL', 'encnt=YES', 'human=NIL']
        Use case: Chunk, NER
    """
    if mmoTags == '-':
        return []
    return [x for x in MMOpatt.split(mmoTags) if x]
