package hu.nytud.gate.morph;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import gate.*;
import gate.creole.*; 
import gate.creole.metadata.*; 
import gate.util.GateRuntimeException;
import gate.util.InvalidOffsetException;

import org.apache.ibatis.io.Resources;

import rfsa.RFSA;

/** 
 *  Magyarlánc Hungarian Morphological Analyzer that uses KR morphological codes.
 *  Uses rfsa.RFSA.analyse().
 *  Produces a list of alternative lemma + KR code pairs.
 *  It does not use the extra features that hu.u_szeged.magyarlanc.HunLemMor.getMorphologicalAnalyses() has (guessing etc.)
 *  Tested with Magyarlánc 2.0 (http://rgai.inf.u-szeged.hu/project/nlp/research/magyarlanc/magyarlanc-2.0.jar)
 *  @author Márton Miháltz
 */ 
@CreoleResource(name = "Magyarlánc Hungarian Morphological Analyzer (KR)", 
				comment = "Adds KR code and lemma annotations (not disambiguated) to tokens"
				) // TODO icon?
public class MagyarlancKRMorphAnalyzer extends AbstractLanguageAnalyser {


	private static final long serialVersionUID = 1L;
	
	protected Logger logger = Logger.getLogger(this.getClass().getName());

	private RFSA rfsa = null;
	
	public Resource init() throws ResourceInstantiationException {
		try {
			rfsa = RFSA.read(Resources.getResourceAsStream(RFSAFile), RFSAFileEncoding);
	    } catch (Exception e) {
	        throw new ResourceInstantiationException(e);
	    }	
	    return this;
    }
	
	/** Requires token annotations (see baseTokenAnnotationType), 
	 *  adds morphological analyzes with feature key outputAnasFeatureName,
	 *  value is an ArrayList<String> of "$lemma/KR" alternatives.
	 *  Based on code from gate.creole.POSTagger.execute()
	 */
	public void execute() throws ExecutionException { 
	    
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
	    
	    if(tokensAS == null || tokensAS.size() == 0) {
	    	if(failOnMissingInputAnnotations) {
	    		throw new ExecutionException("No tokens to process in document " + document.getName() + "\n" +
	                                         "Please run a tokeniser first!");
	        } else {
	            Utils.logOnce(logger, Level.INFO, "Magyarlánc Morphological Analyzer: " +
	            								  "no token annotations in input document");
	            logger.debug("No input annotations in document "+document.getName());	        	
	        	return;
	        }
	    }
	    
	    // start the work:
	    long startTime = System.currentTimeMillis();
	    fireStatusChanged("POS tagging " + document.getName());
	    fireProgressChanged(0);
	    
	    // process all tokens
	    List<Annotation> tokensList = new ArrayList<Annotation>(tokensAS);
	    ListIterator<Annotation> tokensIter = tokensList.listIterator();
	    Annotation currentToken = (tokensIter.hasNext() ? tokensIter.next() : null);
	    String tok = null; // surface form of current token
	    int tokIdx = 0;
	    int tokCnt = tokensList.size();
	    while(currentToken != null) {
	    	try {
				tok = document.getContent().getContent(
						currentToken.getStartNode().getOffset(),
						currentToken.getEndNode().getOffset()).toString();
			} catch (InvalidOffsetException e) {
			    	throw new ExecutionException(e);
			}
	        //run the analyzer
	    	if (tok != null && rfsa != null) {
	    		Collection<String> anas = rfsa.analyse(tok);
	    		addFeatures(currentToken, anas);
	    	}
	    	currentToken = (tokensIter.hasNext() ? tokensIter.next() : null);
	    	fireProgressChanged(++tokIdx * 100 / tokCnt);
	    } // currentToken != null

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

	@CreoleParameter(comment="RFSA file name", defaultValue="rfsa.txt")
	public void setRFSAFile(String f) {
		RFSAFile = f;
	}
	public String getRFSAFile() {
		return RFSAFile;
	}
	private String RFSAFile;	
	
	@CreoleParameter(comment="RFSA file character encoding", defaultValue="UTF-8")
	public void setRFSAFileEncoding(String e) {
		RFSAFileEncoding = e;
	}
	public String getRFSAFileEncoding() {
		return RFSAFileEncoding;
	}
	private String RFSAFileEncoding;	
	
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
