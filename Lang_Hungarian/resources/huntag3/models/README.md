This directory should contain subdirectories that contain trained binary model files for Huntag3.
These model directories should be obtained separately.

Current models:

* `NER-szeged-hfst`: Hungarian Named Entity Recognition with eMmorph codes

* `maxNP-szeged-hfst`: Hungarian maxNP chunking with eMmorph codes

Older models:

* `NER-szeged-KR`: Hungarian Named Entity Recognition with KR codes(NER)

* `NP-szeged-msd`: Hungarian NP chunking with MSD codes

_All_ models can be obtained running `complete.sh` (on Linux)
(see [step 2 of __Method 2__ here](https://github.com/dlt-rilmta/hunlp-GATE#method-2-for-developers)).

Some of the (older) models can also be obtained from
their own separate repository:

* `NER-szeged-KR`: https://github.com/dlt-rilmta/NER-szeged-KR-model

* `NP-szeged-msd`: https://github.com/dlt-rilmta/NP-szeged-msd-model

