package hu.nytud.gate.postaggers;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import gate.*;
import gate.creole.*; 
import gate.creole.metadata.*; 
import gate.util.GateRuntimeException;
import gate.util.InvalidOffsetException;
import gate.util.OffsetComparator;
import edu.stanford.nlp.tagger.maxent.SzteMaxentTagger;

/** 
 *  Magyarlánc Hungarian POS-tagger and lemmatizer processing resource,
 *  using edu.stanford.nlp.tagger.maxent.SzteMaxentTagger
 *  which uses Stanford POS-tagger + morphological analysis and corpus frequencies.
 *  Outputs MSD morphological codes and lemmas. Needs tokenized and sentence splitted document.
 *  Tested with Magyarlánc 2.0 (http://rgai.inf.u-szeged.hu/project/nlp/research/magyarlanc/magyarlanc-2.0.jar)
 *  @author Márton Miháltz
 */ 
@CreoleResource(name = "Magyarlánc Hungarian POS Tagger and Lemmatizer", 
				comment = "Adds MSD code and lemma annotations",
				icon = "pos-tagger") 
public class MagyarlancPOSTaggerLemmatizer extends AbstractLanguageAnalyser {


	private static final long serialVersionUID = 1L;
	
	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	private SzteMaxentTagger tagger = null;
	
	public Resource init() throws ResourceInstantiationException { 
        if (posModelName == null) 
            throw new ResourceInstantiationException("PoS Model name not set"); 	

        try {
	        tagger = new SzteMaxentTagger(posModelName);
	        tagger.setVerbose(false);
	    } catch (Exception e) {
	    	throw new ResourceInstantiationException(e);
	    }
        
        return this; 
    }
   
	/* Based on code from gate.creole.POSTagger.execute() */
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
	        
	    if(baseSentenceAnnotationType == null || baseSentenceAnnotationType.trim().length()==0) {
	        throw new ExecutionException("No base Sentence Annotation Type provided!");
	    }
	    
	    if(outputAnnotationType == null || outputAnnotationType.trim().length()==0) {
	        throw new ExecutionException("No AnnotationType provided to store the new feature!");
	    }
	    
	    // get sentence and token annotations
	    AnnotationSet sentencesAS = inputAS.get(baseSentenceAnnotationType);
	    AnnotationSet tokensAS = inputAS.get(baseTokenAnnotationType);
	    
	    if(sentencesAS == null || sentencesAS.size() == 0 || tokensAS == null || tokensAS.size() == 0) {
	    	if(failOnMissingInputAnnotations) {
	    		throw new ExecutionException("No sentences or tokens to process in document " + document.getName() + "\n" +
	                                         "Please run a sentence splitter " +
	                                         "and tokeniser first!");
	        } else {
	            Utils.logOnce(logger, Level.INFO, "Magyarlánc POS Tagger and Lemmatizer: " +
	            								  "no sentence or token annotations in input document");
	            logger.debug("No input annotations in document "+document.getName());	        	
	        	return;
	        }
	    }
	    
	    // start the work:
	    long startTime = System.currentTimeMillis();
	    fireStatusChanged("POS tagging " + document.getName());
	    fireProgressChanged(0);
	    
	    //prepare the input for the tagger
	    List<String> sentenceForTagger = new ArrayList<String>();

	    //define a comparator for annotations by start offset
	    Comparator<Annotation> offsetComparator = new OffsetComparator();

	    //read all the tokens and all the sentences, sort by document order
	    List<Annotation> sentencesList = new ArrayList<Annotation>(sentencesAS);
	    Collections.sort(sentencesList, offsetComparator);
	    List<Annotation> tokensList = new ArrayList<Annotation>(tokensAS);
	    Collections.sort(tokensList, offsetComparator);

	    Iterator<Annotation> sentencesIter = sentencesList.iterator();
	    ListIterator<Annotation> tokensIter = tokensList.listIterator();

	    List<Annotation> tokensInCurrentSentence = new ArrayList<Annotation>();
	    Annotation currentToken = tokensIter.next();
	    int sentIndex = 0;
	    int sentCnt = sentencesAS.size();
	    while(sentencesIter.hasNext()){
	    	Annotation currentSentence = sentencesIter.next();
	        tokensInCurrentSentence.clear();
	        sentenceForTagger.clear();
	        // get tokens (Annotation objects, surface forms) in current sentence
	        while(currentToken != null
	        	  && currentToken.withinSpanOf(currentSentence)) {
	        	tokensInCurrentSentence.add(currentToken);
			    try {
				    // surface form of current token
				    String tok = document.getContent().getContent(
						currentToken.getStartNode().getOffset(),
						currentToken.getEndNode().getOffset()).toString();
	                sentenceForTagger.add(tok);
			    } catch (InvalidOffsetException e) {
				    throw new ExecutionException(e);
			    }
	            currentToken = (tokensIter.hasNext() ? tokensIter.next() : null);
	        }
	        //run the POS tagger
	        String[] forms = sentenceForTagger.toArray(new String[sentenceForTagger.size()]);
	        String[][] tags = tagger.morpSentence(forms);
	        if (tags == null || tags.length != tokensInCurrentSentence.size())
	        	throw new ExecutionException("MagyarlancPOSTaggerLemmatizer malfunction: " +
	        								 "output array is null or size is different from input size");
	        // save annotations
	        Iterator<Annotation> tokIter = tokensInCurrentSentence.iterator();
	        for (String[] slt: tags) {
	        	if (slt.length != 3) {
	        		throw new ExecutionException("MagyarlancPOSTaggerLemmatizer malfunction: " +
							 					 "size of tag array for current token is not 3");
	        	}
	        	Annotation annot = tokIter.next();
	        	addFeatures(annot, slt[1], slt[2]);	        	
	        }
	        fireProgressChanged(sentIndex++ * 100 / sentCnt);
	    } //while(sentencesIter.hasNext())

	    // Finished
	    fireProcessFinished();
	    fireStatusChanged(
	    		document.getName() + " tagged in " +
	    	    NumberFormat.getInstance().format(
	            (double)(System.currentTimeMillis() - startTime) / 1000) +
	            " seconds!");
	}
	
	/**
	 * Add lemma and MSD code features to token annotation.
	 * Code from gate.creole.POSTagger.addFeatures()
	 */
	private void addFeatures(Annotation annot, String lemma, String msd) throws GateRuntimeException {
	      String tempIASN = inputASName == null ? "" : inputASName;
	      String tempOASN = outputASName == null ? "" : outputASName;
	      if(outputAnnotationType.equals(baseTokenAnnotationType) && tempIASN.equals(tempOASN)) {
	          annot.getFeatures().put(outputLemmaFeatureName, lemma);
	          annot.getFeatures().put(outputMSDFeatureName, msd);
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
	              features.put(outputLemmaFeatureName, lemma);
	              features.put(outputMSDFeatureName, msd);
	              try {
	                  outputAS.add(new Long(start), new Long(end), outputAnnotationType, features);
	              } catch(Exception e) {
	                  throw new GateRuntimeException("Invalid Offsets");
	              }
	          } else {
	              // search for the annotation if there is one with the same start and end offsets
	              List<Annotation> tempList = new ArrayList<Annotation>(annotations.get());
	              boolean found = false;
	              for(int i=0;i<tempList.size();i++) {
	                  Annotation annotation = tempList.get(i);
	                  if(annotation.getStartNode().getOffset().intValue() == start 
	                	 && annotation.getEndNode().getOffset().intValue() == end) {
	                      // this is the one
	        	          annot.getFeatures().put(outputLemmaFeatureName, lemma);
	        	          annot.getFeatures().put(outputMSDFeatureName, msd);
	                      found = true;
	                      break;
	                  }
	              }
	              if(!found) {
	                  // add new annotation
	                  FeatureMap features = Factory.newFeatureMap();
		              features.put(outputLemmaFeatureName, lemma);
		              features.put(outputMSDFeatureName, msd);
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
			comment = "File name of PoS model (inside magyarlanc-X.Y.jar) to be used by PoS-tagger", 
			defaultValue = "25.model") 	
	public void setPosModelName(String newPosModel) {
		posModelName = newPosModel;
	}
	public String getPosModelName() {
		return posModelName;
	}
	private String posModelName;

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
			comment="The name of the base 'Sentence' annotation type", 
			defaultValue="Sentence")
	public void setBaseSentenceAnnotationType(String baseSentenceAnnotationtype) {
	    this.baseSentenceAnnotationType = baseSentenceAnnotationtype;
	}  
	public String getBaseSentenceAnnotationType() {
		return this.baseSentenceAnnotationType;
	}
	private String baseSentenceAnnotationType;
	  
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
	  	
	@RunTime
	@CreoleParameter(
			comment = "The name of the feature that will hold the MSD code morphological annotation on output annotation types (tokens)",
			defaultValue = "msd")  
	public void setOutputMSDFeatureName(String newOutputMSDFeatureName) {
		outputMSDFeatureName = newOutputMSDFeatureName;
	}
	public String getOutputMSDFeatureName() {
	    return outputMSDFeatureName;
	}
	protected String outputMSDFeatureName;
	
	@RunTime
	@CreoleParameter(
			comment = "The name of the feature that will hold the lemma annotation on output annotation types (tokens)",
			defaultValue = "lemma")  
	public void setOutputLemmaFeatureName(String newOutputLemmaFeatureName) {
		outputLemmaFeatureName = newOutputLemmaFeatureName;
	}
	public String getOutputLemmaFeatureName() {
	    return outputLemmaFeatureName;
	}
	protected String outputLemmaFeatureName;

	
}
