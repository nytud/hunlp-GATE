# GATE-huntools
[GATE](http://gate.ac.uk/) plugin containing Hungarian processing resources, which are wrappers around already existing NLP tools in their original form (huntoken, hunmorph, hunpos, huntag3).

The embedding of existing hun* NLP tools does not always follow the most efficient implementation possible (i.e. calling a command-line tool in a separate process), so this package mainly serves as a reference for benchmarking purposes.
