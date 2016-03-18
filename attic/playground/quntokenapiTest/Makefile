QTOKEN_DIR=../../../../quntoken

test_quntokenapi: main.cpp
	$(CXX) -I$(QTOKEN_DIR)/src/cpp \
		-I$(QTOKEN_DIR)/tmp \
		-I$(QTOKEN_DIR)/quex \
		-I$(QTOKEN_DIR) \
		-L $(QTOKEN_DIR)/lib \
		-c main.cpp \
		`icu-config --ldflags` \
		-lquntoken \
		-o $@
#TODO: link to libquntoken.a?
