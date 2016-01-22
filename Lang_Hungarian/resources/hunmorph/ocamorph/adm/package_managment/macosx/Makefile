PACKAGE_MAKER=/Developer/Applications/Utilities/PackageMaker.app/Contents/MacOS/PackageMaker

BIN_FILE=../../ocamorph
BIN_TARGET=/usr/bin/

OUTPUT=out

PKG_NAME=ocamorph
PKG_VERSION=1.0

DMG_NAME=ocamorph

PKG_DIR      = ${OUTPUT}/${PKG_NAME}-${PKG_VERSION}.pkg
PACKAGE_ROOT = ./PackageRoot
RESOURCES    = ./Resources
PLIST        = ./Info.plist

PUBLIC_DIR   = /public/Tool/Hunmorph/Runtime/Ocamorph/MacOSX

################################################################################
# Customizable variables
################################################################################

NAME=${DMG_NAME}
VERSION=${PKG_VERSION}

SOURCE_DIR=${OUTPUT}
SOURCE_FILES=${PKG_NAME}-${PKG_VERSION}.pkg

TEMPLATE_DMG=${OUTPUT}/template.dmg


################################################################################
# DMG building. No editing should be needed beyond this point.
################################################################################

MASTER_DMG=${OUTPUT}/$(NAME)-$(VERSION).dmg
WC_DMG=${OUTPUT}/wc.dmg
WC_DIR=${OUTPUT}/wc


all: $(MASTER_DMG)

${PACKAGE_ROOT}: ${BIN_FILE}
	mkdir -p ${PACKAGE_ROOT}/${BIN_TARGET}
	cp ${BIN_FILE} ${PACKAGE_ROOT}/${BIN_TARGET}

${PKG_DIR}: ${PACKAGE_ROOT} ${RESOURCES}
	mkdir -p ${OUTPUT}
	${PACKAGE_MAKER} -build -p ${PKG_DIR} -f ${PACKAGE_ROOT} -b /tmp -ds -v -r ${RESOURCES} -i ${PLIST} 


# DMG file creator Makefile from
#  http://www.cs.kuleuven.ac.be/~remko/fancy_dmg/Makefile

#
# Build file for creating DMG files.
#
# The DMG packager looks for a template.dmg.bz2 for using as its 
# DMG template. If it doesn't find one, it generates a clean one.
#
# If you create a DMG template, you should make one containing all
# the files listed in $(SOURCE_FILES) below, and arrange everything to suit
# your style. The contents of the files themselves does not matter, so
# they can be empty (they will be overwritten later). 
#
# Remko Troncon <remko.troncon@cs.kuleuven.ac.be>
#



################################################################################
# DMG building. No editing should be needed beyond this point.
################################################################################

MASTER_DMG=${OUTPUT}/$(NAME)-$(VERSION).dmg
WC_DMG=${OUTPUT}/wc.dmg
WC_DIR=${OUTPUT}/wc

.PHONY: all

$(TEMPLATE_DMG): $(TEMPLATE_DMG).bz2
	bunzip2 -k $<

$(TEMPLATE_DMG).bz2: 
	@echo
	@echo --------------------- Generating empty template --------------------
	mkdir template
	hdiutil create -size 40m "$(TEMPLATE_DMG)" -srcfolder template -format UDRW -volname "$(NAME)" -quiet
	rmdir template
	bzip2 "$(TEMPLATE_DMG)"
	@echo

$(WC_DMG): $(TEMPLATE_DMG)
	cp $< $@

$(MASTER_DMG): ${PKG_DIR} $(WC_DMG)  
	@echo
	@echo --------------------- Creating Disk Image --------------------
	mkdir -p $(WC_DIR)
	hdiutil attach "$(WC_DMG)" -noautoopen -quiet -mountpoint "$(WC_DIR)"
	for i in $(SOURCE_FILES); do  \
		rm -rf "$(WC_DIR)/$$i"; \
		ditto -rsrc "$(SOURCE_DIR)/$$i" "$(WC_DIR)/$$i"; \
	done
	#rm -f "$@"
	#hdiutil create -srcfolder "$(WC_DIR)" -format UDZO -imagekey zlib-level=9 "$@" -volname "$(NAME) $(VERSION)" -scrub -quiet
	WC_DEV=`hdiutil info | grep "$(WC_DIR)" | grep "Apple_HFS" | awk '{print $$1}'` && \
	hdiutil detach $$WC_DEV -quiet -force
	rm -f "$(MASTER_DMG)"
	hdiutil convert "$(WC_DMG)" -quiet -format UDZO -imagekey zlib-level=9 -o "$@"
	rm -rf $(WC_DIR)
	@echo

publish: ${MASTER_DMG}
	scp ${MASTER_DMG} kruso.mokk.bme.hu:${PUBLIC_DIR}/

.PHONY: clean
clean:
	-rm -rf $(TEMPLATE_DMG) $(MASTER_DMG) $(WC_DMG) ${PACKAGE_ROOT} ${OUTPUT}
