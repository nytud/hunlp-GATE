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
 * This class is inherited from taggerframework.GenericTagger.
 * We only override some CREOLE resource parameter default values
 * which are needed to run the hunmorph command line binary correctly. 
 */
@CreoleResource(name = "HU [OLD] 2. Morphological Analyzer (HunMorph, KR?, GenericTagger) [Linux]", 
				comment = "Expects tokenized text. Outputs lemmas and KR codes. Calls hunmorph command line binary via TaggerFramework's GenericTagger") 
public class HunMorphCommandLine extends GenericTagger {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Resource init() throws ResourceInstantiationException {
		super.init();
		// Override tagger binary according to OS
		try {
			// Get the path of this jar file
			File jarFile = new File(HunMorphCommandLine.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			String jarDir = jarFile.getParentFile().getPath();
			// Get OS name
			String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
			if (osName.contains("linux")) {
			    // Just leave the default value
			}
			else if (osName.contains("mac os") || osName.contains("macos") || osName.contains("darwin")) {
			    URL url = new File(jarDir, "resources/hunmorph/runhunmorph_osx.sh").toURI().toURL();
			    System.out.println("Mac OS detected, overriding hunmorph wrapper script name: " + url.toString());
				setTaggerBinary(url);
			}
			/*
			else if (osName.contains("windows")) {
			    // TODO
			}
			*/
			else {
			    System.err.println("Warning: hunmorph binary is not supported on your operating system!");
			}
		} catch (Exception e) {
			throw new ResourceInstantiationException(e);
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

