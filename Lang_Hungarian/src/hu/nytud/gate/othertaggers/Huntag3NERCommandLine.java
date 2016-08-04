package hu.nytud.gate.othertaggers;

import java.io.File;
import java.net.URL;
import java.util.Locale;

import gate.FeatureMap;
import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.taggerframework.*;
import hu.nytud.gate.morph.HFSTMorphPipe;

/**
 * Hungarian NER (Named Entity Recognition) with the huntag3 tagger and the NER-szeged-KR model.
 * This class is inherited from taggerframework.GenericTagger.
 * We only override some CREOLE resource parameter default values
 * which are needed to run the huntag3 command line binary correctly.
 */
@CreoleResource(name = "HU [OLD] 4. Named Entity Recognizer (HunTag3, KR, GenericTagger) [Linux?]", 
				comment = "Expects tokenized text with KR pos tags and morphanalyzes with lemmas. Outputs BIOE1-* codes."
						+ "Calls huntag3 command line binary via TaggerFramework's GenericTagger") 
public class Huntag3NERCommandLine extends GenericTagger {

	private static final long serialVersionUID = 1L;

	@Override
	public Resource init() throws ResourceInstantiationException {
		try {
			// Get the path of this jar file
			File jarFile = new File(HFSTMorphPipe.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			String jarDir = jarFile.getParentFile().getPath();
			// Get OS name
			String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
			if (osName.contains("windows")) {
			    URL url = new File(jarDir, "resources/huntag3/run_huntag_NER-szeged-KR.cmd").toURI().toURL();
			    System.out.println("Windows detected, overriding HFST wrapper script name: " + url.toString());
			    setTaggerBinary(url);
			}
		} catch (Exception e) {
			throw new ResourceInstantiationException(e);
		}
		return this;
    }

	@CreoleParameter(defaultValue = "UTF-8") 
	public void setEncoding(String encoding) { 
	    super.setEncoding(encoding); 
	} 	
	
	@CreoleParameter(defaultValue = "false")
	public void setFailOnUnmappableCharacter(Boolean failOnUnmappableCharacter) {
		super.setFailOnUnmappableCharacter(failOnUnmappableCharacter);;
	}
	
	/**
	 * Annotation names to be created from fields of the tagger's output file, specified by setRegex
	 */
	@CreoleParameter(defaultValue = "string=1;pos=2;ana=3;NER-BIO1=4")
	public void setFeatureMapping(FeatureMap featureMapping) {
		super.setFeatureMapping(featureMapping);
	}

	/**
	 * Fields of the tagger's input file, to be created from tokens' annotations 
	 */
	@CreoleParameter(defaultValue = "${string}\t${pos}\t[${ana}]")
	public void setInputTemplate(String inputTemplate) {
		super.setInputTemplate(inputTemplate);
	}
	
	@CreoleParameter(defaultValue = "([^\t]+)\t([^\t]+)\t([^\t]+)\t([^\t]+)")
	public void setRegex(String regex) {
		super.setRegex(regex);
	}
	
	@CreoleParameter(defaultValue = "resources/huntag3/run_huntag_NER-szeged-KR.sh") 
	public void setTaggerBinary(URL url) { 
	    super.setTaggerBinary(url); 
	}
	
	@CreoleParameter(defaultValue = "resources/huntag3")
	public void setTaggerDir(URL taggerDir) {
		super.setTaggerDir(taggerDir);
	}
	
}
