# hunlp-GATE
[GATE](http://gate.ac.uk/) plugin containing Hungarian processing resources, which are wrappers around already existing NLP tools in their original form (huntoken, hunmorph, hunpos, huntag3).

The embedding of the existing hun* NLP tools is not always efficient (i.e. tools that don't have APIs are called as shell commands in a separate process), so this package mainly serves as a reference for benchmarking purposes.
