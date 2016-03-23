QTOKEN_DIR=../../../../quntoken
TMP_DIR=$(QTOKEN_DIR)/tmp
SOURCE_DIR=$(QTOKEN_DIR)/src
CPP_DIR=$(SOURCE_DIR)/cpp
QUEX_DIR=$(QTOKEN_DIR)/quex

CXXFLAGS +=	-Wall \
			-Wextra \
			-Wconversion \
			-std=c++11 \
			-I$(CPP_DIR)

CXXFLAGS_QUEX =	$(CXXFLAGS) \
				-I$(QUEX_DIR) \
				-I$(TMP_DIR) \
				-I$(QTOKEN_DIR)/ \
				-DQUEX_OPTION_ASSERTS_DISABLED \
				-DQUEX_OPTION_SEND_AFTER_TERMINATION_ADMISSIBLE \
				-DENCODING_NAME='"UTF8"' \
				-DQUEX_OPTION_MULTI


# This will link together with the libquntoken.so or .a file (if both are found .so is chosen)
test_quntokenapi: main.o
	$(CXX) -L $(QTOKEN_DIR)/lib main.o -lquntoken `icu-config --ldflags` -o $@

main.o: main.cpp
	$(CXX) $(CXXFLAGS_QUEX) -c $< -o $@
