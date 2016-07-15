This sub-project creates a wrapper around the hfst-lookup binary (OS dependent, written in C++).

Can be used as Standalone or included in any java based project.

Run as standalone with:

java -jar hfst-wrapper [--config=<config file>] [--mode=stem] [word1] [word2] ...

* config - alternative configuration file with relative path to the jar file (default: hfst-wrapper.props)
* mode - can be set to run only the stemmer (needs the analization string as input in this case)
* word1..N - (for test purposes) you can add a word to run the analyzation on it and then exit

See details on the configuration in the example found in the parent directory.

Default behaviour:
The program reads from stdin (1 word / line) and then writes the analyzations in format:

<source> [TAB] <analyzation> [TAB] <lemma> [TAB] <features>

OR

<source> [TAB] "<unknown>"

After all analyzations for a word (1 analyzation / line) it writes 1 empty line to the output.
