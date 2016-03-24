package hu.nytud.gate.morph;

import java.net.URL;

import gate.FeatureMap;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.taggerframework.*;

/**
 * This class is inherited from taggerframework.GenericTagger.
 * We only override some CREOLE resource parameter default values
 * which are needed to run the hunmorph command line binary correctly. 
 */
@CreoleResource(name = "Hunmorph (Ocamorph) Hungarian morphological analyzer (command-line)", 
				comment = "Expects tokenized text. Outputs lemmas and KR codes. Calls hunmorph command line binary via TaggerFramework's GenericTagger") 
public class HunMorphCommandLine extends GenericTagger {

	private static final long serialVersionUID = 1L;
	
	public Resource init() throws ResourceInstantiationException {
		super.init();
		// Override tagger binary accorindg to OS
		String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
		if (osName.contains("linux")) {
		    // Just leave default
		    // setTaggerBinary("resources/hunmorph/runhunmorph.sh");
		}
		else if (osName.contains("mac os") || osName.contains("macos") || osName.contains("darwin")) {
		    System.out.println("Mac OS detected, overriding tagger binary name");
		    setTaggerBinary("resources/hunmorph/runhunmorph_osx.sh");
		}
		/*
		else if (osName.contains("windows")) {
		    // TODO
		}
		*/
		else {
		    System.err.println("Warning: hunmorph binary is not supported on your operating system, this plugin will _not_ work");
		}
		return this;
	}
	
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

