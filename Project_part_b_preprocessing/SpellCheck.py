

import re, collections
#converting words in to small case
def words(text):
    return re.findall('[a-z]+', text.lower())

def train(features):
    model = collections.defaultdict(lambda: 1)
    for f in features:
        model[f] += 1
    return model

#words containing in the bigfile.txt
NWORDS = train(words(file('bigtext.txt').read()))
alphabet = 'abcdefghijklmnopqrstuvwxyz'

#calculating words with edit distance 1
def edits1(word):
    s = [(word[:i], word[i:]) for i in range(len(word) + 1)]
    deletes    = [a + b[1:] for a, b in s if b]
    transposes = [a + b[1] + b[0] + b[2:] for a, b in s if len(b)>1]
    replaces   = [a + c + b[1:] for a, b in s for c in alphabet if b]
    inserts    = [a + c + b     for a, b in s for c in alphabet]
    return set(deletes + transposes + replaces + inserts)

#calculating words with edit distance two
def known_edits2(word):
    return set(e2 for e1 in edits1(word) for e2 in edits1(e1) if e2 in NWORDS)

#returns the set of words in the big.txt file
def known(words):
    return set(w for w in words if w in NWORDS)
# function which transforms the incorrect spelling word to correct spelling
def correct(word):
    #candidate generation word
    candidates = known([word]) or known(edits1(word)) or    known_edits2(word) or [word]
    return max(candidates, key=NWORDS.get)

def correct_top(word, n):
    candidates = known([word]) or known(edits1(word)) or    known_edits2(word) or [word]
    s = sorted(candidates, key=NWORDS.get, reverse=True)
    return s[0], s[:n]

