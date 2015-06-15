
import java.util.*;

public class DummyTokenizerCppTest {

    public class LPair {
	public long first;
        public long second;
        public LPair(long f, long s) {
		this.first = f;
		this.second = s;
        }
    }

    public static void main(String argv[]) {

//	System.setProperty("java.library.path", System.getProperty("java.library.path") + ":/home/mm/Infra2/hunlp-GATE/DummyCppTokenizer/");
//	System.setProperty("java.library.path", System.getProperty("java.library.path") + ":./");
//	System.out.println(System.getProperty("java.library.path"));
	System.loadLibrary("tokenizer");
//	System.load("/home/mm/NYTI/hunlp-GATE/DummyCppTokenizer/tokenizercpp.so");

	Tokenizer t = new Tokenizer();
	List<LPair> tokens = new ArrayList<LPair>();
	List<LPair> whites = new ArrayList<LPair>();

	t.tokenize("This is a sentence.", tokens, whites);

    }
}