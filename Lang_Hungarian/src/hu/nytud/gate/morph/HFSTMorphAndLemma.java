package hu.nytud.gate.morph;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Resource;
import gate.Utils;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import hu.nytud.hfst.Analyzer;
import hu.nytud.hfst.Analyzer.Analyzation;
import hu.nytud.hfst.Stemmer;
import hu.nytud.hfst.Stemmer.Stem;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *  HFST Morphological Analyzer.
 *  Produces a list of alternative lemma with analyzations code pairs.
 *  Based on MagyarlancKRMorphAnalyzer
 *  @author Peter K
 */ 
@CreoleResource(name = "2. Morphological Analyzer and Lemmatizer (HU) [hfst native+Java]", 
				comment = "Expects tokenized text. Outputs lemmas and analyzations. Calls hfst-wrapper"
				) // TODO icon?
public class HFSTMorphAndLemma extends AbstractLanguageAnalyser {

	private static final long serialVersionUID = 1L;
	
	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	private Analyzer analyzer;
	private Stemmer stemmer;
	
	@Override
	public Resource init() throws ResourceInstantiationException {
		try {
			File jarFile = new File(Analyzer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			
			Properties props = new Properties();
			FileInputStream is = new FileInputStream(hfstWrapperConf.toURI().getSchemeSpecificPart());
			props.load(is);
			
			analyzer = new Analyzer(jarFile.getParentFile(),props);
			stemmer  = new Stemmer(props);

		} catch (Exception e) {
			throw new ResourceInstantiationException(e);
		}
		return this;
    }
	
	/** Requires token annotations (see baseTokenAnnotationType), 
	 *  adds morphological analyzes with feature key outputAnasFeatureName,
	 *  value is an ArrayList<String> of "$lemma[TAGS]" alternatives.
	 *  Based on code from gate.creole.POSTagger.execute()
	 */
	@Override
	public void execute() throws ExecutionException { 
	    
	    long startTime = System.currentTimeMillis();
		
		// sanity checks:
		if(document == null) throw new ExecutionException(
	    	      "No document to process!");

	    if(inputASName != null && inputASName.equals("")) inputASName = null;
	    AnnotationSet inputAS = (inputASName == null) ?
	                            document.getAnnotations() :
	                            document.getAnnotations(inputASName);
	                           
	    if(baseTokenAnnotationType == null || baseTokenAnnotationType.trim().length()==0) {
	        throw new ExecutionException("No base Token Annotation Type provided!");
	    }

	    if(outputASName != null && outputASName.equals("")) outputASName = null;
	            
	    if(outputAnnotationType == null || outputAnnotationType.trim().length()==0) {
	        throw new ExecutionException("No AnnotationType provided to store the new feature!");
	    }
	    
	    // get sentence and token annotations
	    AnnotationSet tokensAS = inputAS.get(baseTokenAnnotationType);
	    int tokenCnt = tokensAS.size();

	    if(tokensAS == null || tokensAS.size() == 0) {
	    	if(failOnMissingInputAnnotations) {
	    		throw new ExecutionException("No tokens to process in document " + document.getName() + "\n" +
	                                         "Please run a tokeniser first!");
	        } else {
	            Utils.logOnce(logger, Level.INFO, "HFST Morphological Analyzer: " +
	            								  "no token annotations in input document");
	            logger.debug("No input annotations in document "+document.getName());	        	
	        	return;
	        }
	    }
	    
	    // start the work:
	    fireStatusChanged("Analysing " + document.getName());
	    fireProgressChanged(0);
	    	    
        try {
    	    // initialize process
        	Analyzer.Worker worker = analyzer.getWorker();

	        Iterator<Annotation> tokenIter = tokensAS.iterator();
	        
	        while (tokenIter.hasNext()) {
	        	Annotation currentToken = tokenIter.next();
	        	worker.addWord(document.getContent().getContent(
	        			currentToken.getStartNode().getOffset(),
	        			currentToken.getEndNode().getOffset()
				).toString());
	        }
	        
	        tokenIter = tokensAS.iterator(); int n=0;
	        while (tokenIter.hasNext()) {
	        	Annotation tok = tokenIter.next();
	    		Collection<Map<String,String>> annot = new ArrayList<>();
	    		List<Analyzation> anas = worker.getResult(); 
	    		if (anas==null) logger.error(
	    				"timeout for word: " + 
	    				document.getContent().getContent(tok.getStartNode().getOffset(), tok.getEndNode().getOffset()).toString());
				else for (Analyzation ana: anas) {
	    			Stem stem = stemmer.process(ana.formatted);
	    			Map<String,String> res = new TreeMap<>();
	    			res.put("ana", ana.formatted);
	    			res.put("lemma", stem.szStem);
	    			res.put("feats", stem.getTags(false));
	    			if (stem.bIncorrectWord) res.put("incorrect", "1");
	    			annot.add(res);
	    		}
	    		
	    		tok.getFeatures().put(outputAnasFeatureName, annot);	    		
	    		fireProgressChanged(++n * 100 / tokenCnt);
	        }
	        
		} catch (Exception e) {
		    	throw new ExecutionException(e);
		}
        
	    // Finished
	    fireProcessFinished();
	    fireStatusChanged(document.getName() + " analyzed in " +
	    	    NumberFormat.getInstance().format(
	            (double)(System.currentTimeMillis() - startTime) / 1000) +
	            " seconds!");
	}
	
	@CreoleParameter(
			comment="Path to the hfst-wrapper configuration",
			defaultValue = "resources/hfst/hfst-wrapper.props")
	public void setHFSTWrapperConf(URL url) {
		hfstWrapperConf = url;
	}
	public URL getHFSTWrapperConf() { 
		return hfstWrapperConf; 
	}
	private URL hfstWrapperConf;	

	@RunTime
	@Optional
	@CreoleParameter(comment="The annotation set to be used as input that must contain 'Token' and 'Sentence' annotations")
	public void setInputASName(String newInputASName) {
	    inputASName = newInputASName;
	}
	public String getInputASName() {
		return inputASName;
	}
	private String inputASName;	
	
	@Optional 
	@RunTime 
	@CreoleParameter( 
			comment = "The annotation set to be used as output")
	public void setOutputASName(String setName) { 
		this.outputASName = setName; 
	} 
	public String getOutputASName() { 
		return outputASName; 
	}
	private String outputASName;
	
	@RunTime
	@CreoleParameter(
			comment="The name of the base 'Token' annotation type",
			defaultValue="Token")
	public void setBaseTokenAnnotationType(String baseTokenAnnotationType) {
		this.baseTokenAnnotationType = baseTokenAnnotationType;
	}
	public String getBaseTokenAnnotationType() {
		return this.baseTokenAnnotationType;
	}
	private String baseTokenAnnotationType;
	  	  
	@RunTime
	@CreoleParameter(
			comment="The name of the annotation type where the new features should be added",
			defaultValue="Token")
	public void setOutputAnnotationType(String outputAnnotationType) {
		this.outputAnnotationType = outputAnnotationType;
	}
	public String getOutputAnnotationType() {
		return this.outputAnnotationType;
	}
	private String outputAnnotationType;
		  	
	@RunTime
	@CreoleParameter(
			comment = "The name of the feature that will hold the morphological analysis strings on output annotation types (tokens)",
			defaultValue = "anas")  
	public void setOutputAnasFeatureName(String newOutputAnasFeatureName) {
		outputAnasFeatureName = newOutputAnasFeatureName;
	}
	public String getOutputAnasFeatureName() {
	    return outputAnasFeatureName;
	}
	protected String outputAnasFeatureName;

	@RunTime
	@CreoleParameter(
			comment = "The name of the feature that will hold the morphological analysis strings on output annotation types (tokens)",
			defaultValue = "lemma")  
	public void setOutputLemmaFeatureName(String newOutputLemmaFeatureName) {
		outputLemmaFeatureName = newOutputLemmaFeatureName;
	}
	public String getOutputLemmaFeatureName() {
	    return outputLemmaFeatureName;
	}
	protected String outputLemmaFeatureName;

	@RunTime
	@Optional
	@CreoleParameter(
			comment = "Throw an exception when there are none of the required input annotations",
			defaultValue = "true")  
	public void setFailOnMissingInputAnnotations(Boolean fail) {
		failOnMissingInputAnnotations = fail;
	}
	public Boolean getFailOnMissingInputAnnotations() {
		return failOnMissingInputAnnotations;
	}
	protected Boolean failOnMissingInputAnnotations = true;

}
