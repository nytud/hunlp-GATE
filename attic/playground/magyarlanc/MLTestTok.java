import java.util.List;
import hu.u_szeged.splitter.HunSplitter;

class MLTestTok {

	public static void main(String args[]) {
		String text = "A nagy kutya gyorsan fut és szépen szereti Kati vádliját. Ez pedig egy másik mondat, te fassz... Na és a Dr. Senki. Ez meg az utolsó.";
		System.out.format("'%s'\n", text);

		HunSplitter hs = new HunSplitter();		
		
		System.out.println("splitToArray():");
		String[][] x = hs.splitToArray(text);
		for (int i=0; i<x.length; i++)
			for (int j=0; j<x[i].length; j++)
				System.out.format("%d.%d '%s'\n", i+1, j+1, x[i][j]);
						
		System.out.println("getTokenOffsets():");
		int[] o = hs.getTokenOffsets(text);
		for (int i=0; i<o.length; i++)
			System.out.format("%d. %d\n", i, o[i]);
			
		int[] tokOffs = null;
		String sent = null;
		int ts, te;
		List<List<String>> splitted = hs.split(text);
		int[] sentOffs = hs.getSentenceOffsets(text);
		
		System.out.println("");
		for (int i=0; i<splitted.size(); i++) {
			// sentence
			sent = text.substring(sentOffs[i], sentOffs[i+1]);
			System.out.format("%d. %d %d: '%s'\n", i+1, sentOffs[i], sentOffs[i+1], sent);
			// tokens
			tokOffs = hs.getTokenizer().findWordOffsets(sent, splitted.get(i));
			// TODO: assert len(splitted) == len(tokOffs)
			for (int j=0; j<splitted.get(i).size(); ++j) {
				ts = tokOffs[j];
				te = tokOffs[j]+splitted.get(i).get(j).length();
				String tok = sent.substring(ts, te);				
				System.out.format("%d.%d %d %d '%s'\n", i+1, j+1, sentOffs[i]+ts, sentOffs[i]+te, tok);
			}				
		}
		
	}
}