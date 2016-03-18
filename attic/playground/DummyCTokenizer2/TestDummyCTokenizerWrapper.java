import hu.rilmta.gate.tokenizers.dummyctokenizer.*;

public class TestDummyCTokenizerWrapper {

    static {
		System.loadLibrary("dummyctokenizer");
    }

    public static void main(String argv[]) {

		DummyCTokenizerWrapper tokenizer = new DummyCTokenizerWrapper();

		String text = " Ez egy mondat.  ";
		int maxtoks = 1000;
		IntArray tok_starts = new IntArray(maxtoks);
		IntArray tok_ends = new IntArray(maxtoks);
		int[] ntoks = {0};
		int maxwhs = 1000;
		IntArray wh_starts = new IntArray(maxwhs);
		IntArray wh_ends = new IntArray(maxwhs);
		int[] nwhs = {0};

		System.out.println("Let's try this...");
		tokenizer.tokenize(text, maxtoks, maxwhs, 
						tok_starts.cast(), tok_ends.cast(), ntoks, 
						wh_starts.cast(), wh_ends.cast(), nwhs);

		System.out.format("Text: '%s'\n", text);
		System.out.println("Tokens:");
		for (int i=0; i<ntoks[0]; i++)
			System.out.format("(%d, %d)\n", tok_starts.getitem(i), tok_ends.getitem(i));
		System.out.println("Whitespaces:");
		for (int i=0; i<nwhs[0]; i++)
			System.out.format("(%d, %d)\n", wh_starts.getitem(i), wh_ends.getitem(i));
    
	}
	
}
