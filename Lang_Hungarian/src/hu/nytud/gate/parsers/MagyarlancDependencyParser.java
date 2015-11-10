package hu.nytud.gate.parsers;

import java.text.NumberFormat;
import java.util.List;
import java.net.URL;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import gate.*;
import gate.creole.*; 
import gate.creole.metadata.*; 
import gate.util.GateRuntimeException;
import gate.util.InvalidOffsetException;
import hu.u_szeged.dep.parser.MateParserWrapper;
import hu.u_szeged.magyarlanc.Magyarlanc;
import hu.u_szeged.magyarlanc.resource.ResourceHolder;
import hu.nytud.gate.parsers.DependencyRelation;

/** 
 *  Magyarlánc Hungarian Dependency Parser.
 *  Uses hu.u_szeged.dep.parser.MateParserWrapper.parseSentence().
 *  Requires sentence and token annotations, and msd and lemma features on tokens.
 *  Produces dependency features on tokens, and optionally category and morphfeatures features.
 *  Warning: it is not possible to separate initialization of required resources from ResourceHolder class's static members
 *  without greatly modifying Magyarlanc's sources.
 *  This means that all running instances of this PR will share the same resources,
 *  which may or may not be thread safe.
 *  Tested with Magyarlánc "2.0.1" (2015-11-04 from J.Zs.)
 *  @author Márton Miháltz
 */ 
@CreoleResource(name = "Magyarlánc Hungarian Dependency Parser", 
				comment = "Requires sentences and tokens with lemma and msd features"
				)
public class MagyarlancDependencyParser extends AbstractLanguageAnalyser {


	private static final long serialVersionUID = 1L;
	
	protected Logger logger = Logger.getLogger(this.getClass().getName());

	public Resource init() throws ResourceInstantiationException {
		Magyarlanc.setDataPath(parserModelPath.getPath());
		ResourceHolder.initParser();
		return this;
    }
	
	/** Requires token annotations (see inputTokenType), 
	 *  adds dependency analysis with feature key outputDependencyFeature,
	 *  (+ main PoS tag and morph features if addPosTags and addMorpFeatures are true.)
	 */
	public void execute() throws ExecutionException { 
	    
		// sanity checks:
		if(document == null) throw new ExecutionException(
	    	      "No document to process!");

	    if(annotationSetName != null && annotationSetName.equals("")) annotationSetName = null;
	    AnnotationSet inputAS = (annotationSetName == null) ?
	                            document.getAnnotations() :
	                            document.getAnnotations(annotationSetName);
	                           
	    if(inputTokenType == null || inputTokenType.trim().length()==0)
	        throw new ExecutionException("No input Token Annotation Type provided!");

	    if(inputSentenceType == null || inputSentenceType.trim().length()==0)
	        throw new ExecutionException("No input Sentence Annotation Type provided!");
	    	            
	    // start the work
	    long startTime = System.currentTimeMillis();
	    fireStatusChanged("Parsing " + document.getName());
	    fireProgressChanged(0);	    
	        
	    // Get sentence annotations
	    List<Annotation> sentences = gate.Utils.inDocumentOrder(inputAS.get(inputSentenceType));
	    if (sentences.size() == 0) {
	    	if(failOnMissingInputAnnotations) {
	    		throw new ExecutionException("No sentences to process in document " + document.getName() + "\n" +
	                                         "Please run a sentence splitter & tokenizer first!");
	        } else {
	            Utils.logOnce(logger, Level.INFO, "Magyarlánc Dependency Parser: " +
	            								  "no sentence annotations in input document");
	            logger.debug("No input sentence annotations in document " + document.getName());	        	
	        	return;
	        }	    	
	    }
	    
	    // Parse each sentence
	    int sentencesDone = 0;
	    int nbrSentences = sentences.size();	    
	    for (Annotation sentence : sentences) {
	    	// parse sentence:
	    	parseSentence(inputAS, sentence, sentencesDone); 	    	
	        //checkInterruption();
	        fireProgressChanged(++sentencesDone * 100 / nbrSentences);
        }

	    // Finished
	    fireProcessFinished();
	    fireStatusChanged(document.getName() + " analyzed in " +
	    	    NumberFormat.getInstance().format(
	            (double)(System.currentTimeMillis() - startTime) / 1000) +
	            " seconds!");
	}

	/**
	 * Parse a single sentence
	 * @param annotationSet the complete input annotation set that contains token and sentence annotations
	 * @param sentence the current sentence's annotation
	 * @param nsent index of current sentence in input document
	 * @throws GateRuntimeException 
	 */
	private void parseSentence(AnnotationSet annotationSet, Annotation sentence, int nsent) throws GateRuntimeException {
        
		// get tokens in sentence
		long sentenceStartOffset = sentence.getStartNode().getOffset();
        long sentenceEndOffset   = sentence.getEndNode().getOffset();
        List<Annotation> tokens = Utils.inDocumentOrder(annotationSet.getContained(sentenceStartOffset, sentenceEndOffset).get(inputTokenType));
        if (tokens.size() == 0)
        	throw new GateRuntimeException("No tokens found in sentence " + nsent);
        
        // get form, lemma, msd for each token into a String[][]
        String[][] toksflm = new String[tokens.size()][3];
        int i = 0;
        for (Annotation tok: tokens) {
        	try {
				toksflm[i][0] = document.getContent().getContent(
						tok.getStartNode().getOffset(),
						tok.getEndNode().getOffset()).toString();
			} catch (InvalidOffsetException e) {
				throw new GateRuntimeException(e);
			}
        	toksflm[i][1] = tok.getFeatures().get(inputLemmaFeature).toString();       		
        	if (toksflm[i][1] == null)
        		throw new GateRuntimeException("Error, no '" + inputLemmaFeature + "' feature for token " + i + " in sentence " + nsent); 
        	toksflm[i][2] = tok.getFeatures().get(inputMsdFeature).toString();
        	if (toksflm[i][2] == null)
        		throw new GateRuntimeException("Error, no '" + inputMsdFeature + "' feature for token " + i + " in sentence " + nsent); 
        	i++;
        }
                
        // call parser
        String[][] pars = MateParserWrapper.parseSentence(toksflm);
        
        // Annotate tokens
        if (pars.length != tokens.size()) // sanity check
        	throw new GateRuntimeException("Internal error: pars.length != tokens.size() for sentence " + nsent);       
        for (i=0; i<tokens.size(); i++) {
        	int governorId = Integer.decode(pars[i][6]);
        	int governorTokenId = (governorId == 0 ? -1 : tokens.get(governorId-1).getId());
        	addFeatures(tokens.get(i), pars[i], governorTokenId);
        }
    	
	}
	
	/**
	 * Add dependency relation features to a token annotation.
	 * @param token: the Annotation unit representing the token to add these features to
	 * @param pdata: String array of {token_id, form, lemma, msd, pos, conllfeats, governor_id, deprel_type}, as returned by MateParserWrapper.parseSentence() for this token
	 * @param governorTokenId: annotation id (int) of the governor token in the GATE sentence, or -1 if it has no governor (root, punctuations)
	 */
	private void addFeatures(Annotation token, String[] pdata, int governorTokenId) throws GateRuntimeException {
		if (pdata.length != 8)
			throw new GateRuntimeException("Internal error: parse data array size == " + pdata.length + " for token " + token.toString());
		if (governorTokenId != -1) {
			DependencyRelation deprel = new DependencyRelation(pdata[7], governorTokenId);
			token.getFeatures().put(outputDependencyFeature, deprel);
		}
		if (addPosTags)
			token.getFeatures().put(ANNIEConstants.TOKEN_CATEGORY_FEATURE_NAME, pdata[4]);
		if (addMorphFeatures)
			token.getFeatures().put("morphfeatures", pdata[5]);
	}

	@CreoleParameter(comment="Path to the model file used by the parser",
					 defaultValue="resources/magyarlanc/")
	public void setParserModelPath(URL newPath) {
		parserModelPath = newPath;
	}
	public URL getParserModelPath() {
		return parserModelPath;
	}
	private URL parserModelPath;
	
    @RunTime
	@Optional
	@CreoleParameter(comment="The annotation set to be used as input and output, must contain 'Token' and 'Sentence' annotations")
	public void setAnnotationSetName(String newInputASName) {
	    annotationSetName = newInputASName;
	}
	public String getAnnotationSetName() {
		return annotationSetName;
	}
	private String annotationSetName;	
		
	@RunTime
	@CreoleParameter(
			comment="The name of the input 'Token' annotation type",
			defaultValue="Token")
	public void setInputTokenType(String x) {
		this.inputTokenType = x;
	}
	public String getInputTokenType() {
		return this.inputTokenType;
	}
	private String inputTokenType;

	@RunTime
	@CreoleParameter(
			comment="The name of the input 'Sentence' annotation type",
			defaultValue="Sentence")
	public void setInputSentenceType(String newBaseSentenceAnnotationType) {
		this.inputSentenceType = newBaseSentenceAnnotationType;
	}
	public String getInputSentenceType() {
		return this.inputSentenceType;
	}
	private String inputSentenceType;
	
	@RunTime
	@CreoleParameter(
			comment="The name of the lemma feature on input 'Token' annotations",
			defaultValue="lemma")
	public void setInputLemmaFeature(String f) {
		this.inputLemmaFeature = f;
	}
	public String getInputLemmaFeature() {
		return this.inputLemmaFeature;
	}
	private String inputLemmaFeature;

	@RunTime
	@CreoleParameter(
			comment="The name of the msd feature on input 'Token' annotations",
			defaultValue="msd")
	public void setInputMsdFeature(String f) {
		this.inputMsdFeature = f;
	}
	public String getInputMsdFeature() {
		return this.inputMsdFeature;
	}
	private String inputMsdFeature;
		  	
	@RunTime
	@CreoleParameter(
			comment = "The name of the feature that will hold the dependency information on token annotation types",
			defaultValue = "dependency")  
	public void setOutputDependencyFeature(String x) {
		outputDependencyFeature = x;
	}
	public String getOutputDependencyFeature() {
	    return outputDependencyFeature;
	}
	protected String outputDependencyFeature;

	@RunTime
	@CreoleParameter(
			comment = "Add main PoS labels from parser to tokens",
			defaultValue = "false")  
	public void setAddPosTags(Boolean x) {
		addPosTags = x;
	}
	public Boolean getAddPosTags() {
	    return addPosTags;
	}
	protected boolean addPosTags;
	
	@RunTime
	@CreoleParameter(
			comment = "Add morphological features from parser to tokens",
			defaultValue = "false")  
	public void setAddMorphFeatures(Boolean x) {
		addMorphFeatures = x;
	}
	public Boolean getAddMorphFeatures() {
	    return addMorphFeatures;
	}
	protected boolean addMorphFeatures;
	
	
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
