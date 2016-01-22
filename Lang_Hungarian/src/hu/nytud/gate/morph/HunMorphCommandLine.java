package hu.nytud.gate.morph;

import java.net.URL;
import java.util.List;

import gate.FeatureMap;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.taggerframework.*;

/**
 * This class is inherited from taggerframework.GenericTagger.
 * We only override some CREOLE resource parameter default values
 * which are needed to run the hunmorph command line binary correctly. 
 */
@CreoleResource(name = "Hunmorph (ocamorph) Hungarian morphological analyzer (command-line)", 
				comment = "Expects tokenized text. Outputs lemmas and KR codes. Calls hunmorph command line binary via TaggerFramework's GenericTagger") 
public class HunMorphCommandLine extends GenericTagger {

	private static final long serialVersionUID = 1L;
	
	@CreoleParameter(defaultValue = "ISO-8859-2") 
	public void setEncoding(String encoding) { 
	    super.setEncoding(encoding); 
	} 	
	
	@CreoleParameter(defaultValue = "false")
	public void setFailOnUnmappableCharacter(Boolean failOnUnmappableCharacter) {
		super.setFailOnUnmappableCharacter(failOnUnmappableCharacter);;
	}
	
	@CreoleParameter(defaultValue = "string=1;anas=2")
	public void setFeatureMapping(FeatureMap featureMapping) {
		super.setFeatureMapping(featureMapping);
	}

	@CreoleParameter(defaultValue = "([^ ]+) (.+)")
	public void setRegex(String regex) {
		super.setRegex(regex);
	}
	
	@CreoleParameter(defaultValue = "resources/hunmorph/runhunmorph.sh") 
	public void setTaggerBinary(URL url) { 
	    super.setTaggerBinary(url); 
	}
	
	@CreoleParameter(defaultValue = "resources/hunmorph")
	public void setTaggerDir(URL taggerDir) {
		super.setTaggerDir(taggerDir);
	}
	
}

