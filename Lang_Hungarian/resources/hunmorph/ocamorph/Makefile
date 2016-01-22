SUBDIRS?=src/lib src/wrappers/ocamorph src/wrappers/ocastem doc

.PHONY: all install clean

all: $(SUBDIRS:%=%/all)

install: $(SUBDIRS:%=%/install)

uninstall: $(SUBDIRS:%=%/uninstall) 

clean: $(SUBDIRS:%=%/clean)

%:
	cd $(*D) && $(MAKE) $(*F)
 
