import java.util.Locale;
import hu.rilmta.gate.tokenizers.dummyctokenizer.*;

public class TestDummyCTokenizerWrapper {

    static {
	String libFileName = null;
	String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
	if (osName.contains("linux")) {
		libFileName = "libdummyctokenizer.so";
	}
	else if (osName.contains("windows")) {
		libFileName = "cygdummyctokenizer32.dll";
	}
	else if (osName.contains("mac os") || osName.contains("macos") || osName.contains("darwin")) {
		libFileName = "libdummyctokenizer.dylib";
	}
	System.load("C:\\Work\\Infra2\\hunlp-GATE\\Lang_Hungarian\\resources\\dummyctokenizer\\bin\\" + libFileName);
	//System.loadLibrary("dummyctokenizer");
    }

    public static void main(String argv[]) {

	DummyCTokenizerWrapper tokenizer = new DummyCTokenizerWrapper();

	String text = " Ez egy mondat.  ";
	int maxtoks = 1000;
	OffsPairArray toks = new OffsPairArray(maxtoks);
	int[] ntoks = {0};
	int maxwhs = 1000;
	OffsPairArray whs = new OffsPairArray(maxwhs);
	int[] nwhs = {0};

	tokenizer.tokenize(text, toks.cast(), ntoks, maxtoks, whs.cast(), nwhs, maxwhs);

	System.out.format("Text: '%s'\n", text);
	System.out.println("Tokens:");
	for (int i=0; i<ntoks[0]; i++)
		System.out.format("(%d, %d)\n", toks.getitem(i).getStart(), toks.getitem(i).getEnd());
	System.out.println("Whitespaces:");
	for (int i=0; i<nwhs[0]; i++)
		System.out.format("(%d, %d)\n", whs.getitem(i).getStart(), whs.getitem(i).getEnd());

    }
}
