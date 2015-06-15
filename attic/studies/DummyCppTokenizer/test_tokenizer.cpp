#include "tokenizer.h"
#include <string>
#include <iostream>

using namespace std;

int main(int argc, char** argv)
{
    string test(" Ez egy mondat.  ");
    cout << '"' << test << '"' << endl;

    vector<pair<long,long> > toks, whs;
    tokenize(test, toks, whs);

    cout << "Tokens:\n";
    for (int i=0; i<toks.size(); i++)
	cout << "(" << toks[i].first << ", " << toks[i].second << ")\n";

    cout << "Whitespaces:\n";
    for (int i=0; i<whs.size(); i++)
	cout << "(" << whs[i].first << ", " << whs[i].second << ")\n";

    return 0;
}