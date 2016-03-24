GATE_DIR=/home/mm/GATE_Developer_8.1

TaggerFramework.jar: src/gate/taggerframework/GenericTagger.java
	mkdir -p bin
	javac -cp $(GATE_DIR)/bin/gate.jar:$(GATE_DIR)/lib/* \
		-d bin/ src/gate/taggerframework/GenericTagger.java
	jar -cvf TaggerFramework.jar -C bin/ .