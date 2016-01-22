HUNMORPH?=hunmorph
HUNANAL?=../hun_analyzer
RESOURCE?=../hu_HU
AFF_FILE?=$(RESOURCE).aff
DIC_FILE?=$(RESOURCE).dic
SHELL=/bin/bash

new: 
	touch .force

%.hunmorph.out: %.hunmorph.out.tmp
	grep -v "Unknown" $< | tr -d '+' > $@

%.hunmorph.out.tmp: % .force
	time $(HUNMORPH) $(AFF_FILE) $(DIC_FILE) $< > $@

%.hunanal.out: % .force
	time $(HUNANAL) -aff $(AFF_FILE) -dic $(DIC_FILE) < $< > $@

%.diff: %.hunmorph.out %.hunanal.out
	diff $^ > $@

N?=10
%.hunmorph.bm: % 
	time $(HUNMORPH) $(AFF_FILE) $(DIC_FILE) \
	<(i=0; while [ $$i -lt $(N) ]; do \
		((++i)); echo $$i; cat $<; done ) \
	> /dev/null

SAF?=
%.hunanal.bm: % exec
	time $(HUNANAL) $(SAF) -aff $(AFF_FILE) -dic $(DIC_FILE) < \
	<(i=0; while [ $$i -lt $(N) ]; do \
		((++i)); echo $$i; cat $<; done )\
	> /dev/null

exec: 
	cd ..; make clean; make ; cd benchmark
