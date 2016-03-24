import java.util.List;
import java.util.Set;

import edu.stanford.nlp.tagger.maxent.SzteMaxentTagger;
import hu.u_szeged.magyarlanc.MorAna;
import hu.u_szeged.magyarlanc.HunLemMor;
import is2.parser.Parser;
import is2.parser.Options;
import hu.u_szeged.dep.parser.MateParserWrapper;
import hu.u_szeged.magyarlanc.resource.ResourceHolder;


class MLTestParse {

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
		
		/*
		String PARSER_MODEL = "./data/szeged.dep.model";
		int NCORES = 1;
		Parser parser = new Parser(new Options(new String[] 
				{ "-model", PARSER_MODEL, "-cores", String.valueOf(NCORES) }));
		*/
		ResourceHolder.initParser();
		
		String[][] pars = MateParserWrapper.parseSentence(morph);

		System.out.println("MateParserWrapper.parseSentence():");
		for (int i=0; i<pars.length; i++) {
			for (int j=0; j<pars[i].length; j++) {
				System.out.format("%d %d %s\n", i, j, pars[i][j]);
			}
		}
		
	}
}