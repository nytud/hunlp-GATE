#include "tokenizer.h"

using namespace std;

void tokenize(const std::string& instr, 
              std::vector<std::pair<long, long> >& tokens, 
              std::vector<std::pair<long, long> >& whites )
/*
  instr: input string, 8-bit
  tokens: (start_offset, end_offset) pairs describing tokens
  whites: (start_offset, end_offset) pairs describing whitespaces
*/
{
    for (long pos=0; pos < instr.length(); pos++) {
	if (instr[pos] == ' ') { // whitespace
	    if (pos == 0) {  // start
		whites.push_back( pair<long,long>(0, 1) );
	    }
	    else if (instr[pos-1] != ' ') { // last: not whitespace
		whites.push_back( pair<long,long>(pos, pos+1) );
	    }
	    else { // last: whitespace
		whites.back().second += 1;
	    }
	}
	else { // not whitespace
	    if (pos == 0) {  // start
		tokens.push_back( pair<long,long>(0, 1) );
	    }
	    else if (instr[pos-1] != ' ') { // last: not whitespace
		tokens.back().second += 1;
	    }
	    else { // last: whitespace
		tokens.push_back( pair<long,long>(pos, pos+1) );
	    }
	}
    }
}