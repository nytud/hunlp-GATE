import java.util.List;
import java.util.Set;

import edu.stanford.nlp.tagger.maxent.SzteMaxentTagger;
import hu.u_szeged.magyarlanc.MorAna;
import hu.u_szeged.magyarlanc.HunLemMor;


//import hu.u_szeged.splitter.HunSplitter;

class MLTestMorph {

	public static void main(String args[]) {

		SzteMaxentTagger maxentTagger = null;
		String POS_MODEL = "25.model"; // make this a GATE parameter
		try {
	        maxentTagger = new SzteMaxentTagger(POS_MODEL);
	        maxentTagger.setVerbose(false);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }	
		String[] sent = {"A", "nagy", "kutya", "futott", "."};

		String[][] morph = maxentTagger.morpSentence(sent);

		System.out.println("SzteMaxentTagger.morpSentence():");
		for (int i=0; i<morph.length; i++) {
			for (int j=0; j<morph[i].length; j++) {
				System.out.format("%d %d %s\n", i, j, morph[i][j]);
			}
		}
		
		System.out.println("\nHunLemMor.getMorphologicalAnalyses():");
		//HunLemMor hlm = new HunLemMor();
		for (int i=0; i<sent.length; i++) {
			Set<MorAna> morAnas = HunLemMor.getMorphologicalAnalyses(sent[i]);
			System.out.format("%s:", sent[i]);
			for (MorAna ana: morAnas) {
				System.out.format(" %s", ana.toString());
			}
			System.out.format("\n");
		}
		
	}
}