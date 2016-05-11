package hu.nytud.gate.morph;

import java.io.File;
import java.net.URL;
import java.util.Locale;

import gate.FeatureMap;
import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.taggerframework.*;


/** 
 *  HFST Morphological Analyzer.
 *  Produces a lemma with analyzation.
 *  Based on HunMorphCommandLine
 *  @author Peter K
 */ 
@CreoleResource(name = "HFST Morphological Analyzer (tagger)", 
				comment = "Expects tokenized text. Outputs lemmas and analyzations. Calls hfst command line via I/O redirection"
				) // TODO icon?
public class HFSTMorphTagger extends GenericTagger {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Resource init() throws ResourceInstantiationException {
		super.init();
		// Override tagger binary according to OS
		try {
			// Get the path of this jar file
			File jarFile = new File(HFSTMorphTagger.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			String jarDir = jarFile.getParentFile().getPath();
			// Get OS name
			String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
			if (osName.contains("linux")) {
			    // Just leave the default value
			}
			else if (osName.contains("mac os") || osName.contains("macos") || osName.contains("darwin")) {
			    URL url = new File(jarDir, "resources/hfst/runhfst_osx.sh").toURI().toURL();
			    System.out.println("Mac OS detected, overriding HFST wrapper script name: " + url.toString());
				setTaggerBinary(url);
			}
			else if (osName.contains("windows")) {
			    URL url = new File(jarDir, "resources/hfst/runhfst.cmd").toURI().toURL();
			    System.out.println("Windows detected, overriding HFST wrapper script name: " + url.toString());
				setTaggerBinary(url);
			}
			else {
			    System.err.println("Warning: HFST binary is not supported on your operating system!");
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
	
	@CreoleParameter(defaultValue = "string=1;anas=2")
	public void setFeatureMapping(FeatureMap featureMapping) {
		super.setFeatureMapping(featureMapping);
	}

	@CreoleParameter(defaultValue = "([^\t]+)\t(.+)")
	public void setRegex(String regex) {
		super.setRegex(regex);
	}
	
	@CreoleParameter(defaultValue = "resources/hfst/runhfst.sh") 
	public void setTaggerBinary(URL url) { 
	    super.setTaggerBinary(url); 
	}
	
	@CreoleParameter(defaultValue = "resources/hfst")
	public void setTaggerDir(URL taggerDir) {
		super.setTaggerDir(taggerDir);
	}
	
}

