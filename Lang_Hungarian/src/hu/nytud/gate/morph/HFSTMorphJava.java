package hu.nytud.gate.morph;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.Resource;
import gate.Utils;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import gate.util.GateRuntimeException;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sf.hfst.NoTokenizationException;
import net.sf.hfst.Transducer;
import net.sf.hfst.TransducerAlphabet;
import net.sf.hfst.TransducerHeader;
import net.sf.hfst.UnweightedTransducer;
import net.sf.hfst.WeightedTransducer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *  HFST Morphological Analyzer.
 *  Produces a list of alternative lemma with analyzations code pairs.
 *  Based on MagyarlancKRMorphAnalyzer
 *  @author Peter K
 */ 
@CreoleResource(name = "HFST Morphological Analyzer (java)", 
				comment = "Expects tokenized text. Outputs lemmas and analyzations. Calls hfst-ol for java"
				) // TODO icon?
public class HFSTMorphJava extends AbstractLanguageAnalyser {


	private static final long serialVersionUID = 1L;
	
	private static final Collection<String> EMPTY_ANA = new ArrayList<String>();

	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	protected Transducer transducer; 
	
	@Override
	public Resource init() throws ResourceInstantiationException {
		try {
			FileInputStream transducerfile = null;
			transducerfile = new FileInputStream(hfstLex.toURI().getSchemeSpecificPart());
			TransducerHeader h = new TransducerHeader(transducerfile);
			DataInputStream charstream = new DataInputStream(transducerfile);
			TransducerAlphabet a = new TransducerAlphabet(charstream, h.getSymbolCount());
			if (h.isWeighted()) {
			    transducer = new WeightedTransducer(transducerfile, h, a);
		    } else {
				transducer = new UnweightedTransducer(transducerfile, h, a);
		    }
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
        	
	        Iterator<Annotation> tokenIter = tokensAS.iterator();
	        for (int n=0; tokenIter.hasNext(); ++n) {

		    	Annotation currentToken = tokenIter.next();
		    	String tok = document.getContent().getContent(
					currentToken.getStartNode().getOffset(),
					currentToken.getEndNode().getOffset()
				).toString();
			    
		    	try {
		    		Collection<String> anas = new ArrayList<String>(transducer.analyze(tok));
		    		addFeatures(currentToken, anas.isEmpty() ? EMPTY_ANA : anas);
		    	} catch (NoTokenizationException e) {
		    		addFeatures(currentToken, EMPTY_ANA);
		    	}
	
				fireProgressChanged(++n * 100 / tokenCnt);

				if (isInterrupted()) { logger.info("HFST interrupted"); break; }
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
	
	/**
	 * Add lemma+MSD code pairs to a token annotation.
	 * Code from gate.creole.POSTagger.addFeatures()
	 */
	private void addFeatures(Annotation annot, Collection<String> anas) throws GateRuntimeException {
		// TODO: assert that anas is of type ArrayList<String>?
			    
 	  	String tempIASN = inputASName == null ? "" : inputASName;
	    String tempOASN = outputASName == null ? "" : outputASName;

	    if(outputAnnotationType.equals(baseTokenAnnotationType) && tempIASN.equals(tempOASN)) {
	    	annot.getFeatures().put(outputAnasFeatureName, anas);
	        return;
	    } else {
	    	int start = annot.getStartNode().getOffset().intValue();
	        int end = annot.getEndNode().getOffset().intValue();
	        // get the annotations of type outputAnnotationType
	        AnnotationSet outputAS = (outputASName == null) ?
	        		document.getAnnotations() :
	                document.getAnnotations(outputASName);
	        AnnotationSet annotations = outputAS.get(outputAnnotationType);
	        if(annotations == null || annotations.size() == 0) {
	        	// add new annotation
	            FeatureMap features = Factory.newFeatureMap();
	            features.put(outputAnasFeatureName, anas);
	            try {
	            	outputAS.add(new Long(start), new Long(end), outputAnnotationType, features);
	            } catch(Exception e) {
	                throw new GateRuntimeException("Invalid Offsets");
	            }
	        } else {
	        	// search for the annotation if there is one with the same start and end offsets
	            List<Annotation> tempList = new ArrayList<Annotation>(annotations.get());
	            boolean found = false;
	            for (int i=0; i<tempList.size(); i++) {
	            	Annotation annotation = tempList.get(i);
	                if (annotation.getStartNode().getOffset().intValue() == start 
	                    && annotation.getEndNode().getOffset().intValue() == end) {
	                	// this is the one
	        	        annot.getFeatures().put(outputAnasFeatureName, anas);
                        found = true;
	                    break;
	                }
	            }
	            if(!found) {
	            	// add new annotation
	                FeatureMap features = Factory.newFeatureMap();
		            features.put(outputAnasFeatureName, anas);
	                try {
	                	outputAS.add(new Long(start), new Long(end), outputAnnotationType, features);
	                } catch(Exception e) {
	                    throw new GateRuntimeException("Invalid Offsets");
	                }
	            }
	        }
	    }
	    
	}
	
	@CreoleParameter(
			comment="Path to the HFST transducer",
			defaultValue = "resources/hfst/hu.hfstol")
	public void setHFSTLex(URL url) {
		hfstLex = url;
	}
	public URL getHFSTLex() { 
		return hfstLex; 
	}
	private URL hfstLex;	

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
