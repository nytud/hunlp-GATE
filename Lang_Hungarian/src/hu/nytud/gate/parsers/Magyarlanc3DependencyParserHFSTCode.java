package hu.nytud.gate.parsers;

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
import gate.util.GateRuntimeException;
import gate.util.InvalidOffsetException;
import hu.u_szeged2.dep.parser.MyMateParser;

import java.text.NumberFormat;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
//import hu.nytud.gate.parsers.DependencyRelation;

/** 
 *  Magyarlanc Hungarian Dependency Parser.
 *  Requires sentence and token annotations with lemma, pos and morph features on tokens.
 *  Produces dependency features on tokens.
 *  Tested with Magyarlánc "3.0"
 *  @author Peter Kundrath, Balint Sass
 */ 
@CreoleResource(name = "HU [OBS] 4. Dependency Parser (magyarlanc3.0, hfst)", 
				comment = "Requires sentences and tokens with lemma, pos and morph features"
				)
public class Magyarlanc3DependencyParserHFSTCode extends AbstractLanguageAnalyser {


	private static final long serialVersionUID = 1L;
	
	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	public Resource init() throws ResourceInstantiationException {
		//ResourceHolder.init();
		return this;
    }

	/** Requires token annotations (see inputTokenType), 
	 *  adds dependency type with feature key outputDepTypeFeature,
	 *  adds target Token id with feature key outputDepTargetFeature,
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
        
        // get form, lemma, pos and feat for each token into a String[]
        String[] form = new String[tokens.size()];
        String[] lemma = new String[tokens.size()];
        String[] pos = new String[tokens.size()];
        String[] feat = new String[tokens.size()];
        int i = 0;
        for (Annotation tok: tokens) {
        	try {
				form[i] = document.getContent().getContent(
					tok.getStartNode().getOffset(), 
					tok.getEndNode().getOffset()
				).toString();
          System.out.println( "form = " + form[i] );
			} catch (InvalidOffsetException e) {
				throw new GateRuntimeException(e);
			}
        	lemma[i] = tok.getFeatures().get(inputLemmaFeature).toString();
        	if (lemma[i] == null)
        		throw new GateRuntimeException("Error, no '" + inputLemmaFeature + "' feature for token " + i + " in sentence " + nsent); 
          System.out.println( "lemma = " + lemma[i] );

          /* XXX kiszedjük a 'feature'-ból, ami POS+feat
             a POS-t és a feat-ok külön-külön... */
        	String annot = tok.getFeatures().get(inputMorphFeature).toString();
          String[] parts = annot.split( "\\]", 2 );
          String __pos = parts[0];
          if ( ! __pos.equals("OTHER") ) { __pos += "]"; }
          String __feat = "";
          if ( parts.length > 1 ) { __feat = parts[1]; }

          /* XXX "[]" és "/" eltüntetése */
          __pos = __pos.replace("[", "");
          __pos = __pos.replace("]", "");
          __pos = __pos.replace("/", "");
          __feat = __feat.replace("[", "");
          __feat = __feat.replace("]", "");
          /* XXX feat-ban a "." helyett "|" kell? -- ez nem befolyásolta */
          //__feat = __feat.replace(".", "|");

          System.out.println( "annot = " + annot );
          System.out.println( "POS   = " + __pos );
          System.out.println( "feat  = " + __feat );
          System.out.println();

        	pos[i] = __pos;
        	feat[i] = __feat;

        	if (pos[i] == null)
        		throw new GateRuntimeException("Error, no '" + inputPOSFeature + "' feature for token " + i + " in sentence " + nsent);
        	if (feat[i] == null)
        		throw new GateRuntimeException("Error, no '" + inputMorphFeature + "' feature for token " + i + " in sentence " + nsent);

        	i++;
        }
                
        // call parser
        String[][] pars = MyMateParser.getInstance().parseSentence(form,lemma,pos,feat);
        
        // Annotate tokens
        if (pars.length != tokens.size()) // sanity check
        	throw new GateRuntimeException("Internal error: pars.length != tokens.size() for sentence " + nsent);       
        for (i=0; i<tokens.size(); i++) {
        	int governorId = Integer.decode(pars[i][5]);
        	int governorTokenId = (governorId == 0 ? -1 : tokens.get(governorId-1).getId());
        	addFeatures(tokens.get(i), pars[i], governorTokenId);
        }
    	
	}
	
	/**
	 * Add dependency relation features to a token annotation.
	 * @param token: the Annotation unit representing the token to add these features to
	 * @param pdata: String array of {token_id, form, lemma, pos, morphfeats, governor_id, deprel_type}, as returned by MateParserWrapper.parseSentence() for this token
	 * @param governorTokenId: annotation id (int) of the governor token in the GATE sentence, or -1 if it has no governor (root, punctuations)
	 */
	private void addFeatures(Annotation token, String[] pdata, int governorTokenId) throws GateRuntimeException {
		if (pdata.length != 7)
			throw new GateRuntimeException("Internal error: parse data array size == " + pdata.length + " for token " + token.toString());
		if (governorTokenId != -1) {
			token.getFeatures().put(outputDepTypeFeature, pdata[6]);
			token.getFeatures().put(outputDepTargetFeature, governorTokenId);
		}
	}

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
			comment="The name of the pos feature on input 'Token' annotations",
			defaultValue="pos")
	public void setInputPOSFeature(String f) {
		this.inputPOSFeature = f;
	}
	public String getInputPOSFeature() {
		return this.inputPOSFeature;
	}
	private String inputPOSFeature;
		  	
	@RunTime
	@CreoleParameter(
			comment="The name of the morph feature on input 'Token' annotations",
			defaultValue="feature")
	public void setInputMorphFeature(String f) {
		this.inputMorphFeature = f;
	}
	public String getInputMorphFeature() {
		return this.inputMorphFeature;
	}
	private String inputMorphFeature;

	@RunTime
	@CreoleParameter(
			comment = "The name of the feature that will hold the dependency type on token annotation types",
			defaultValue = "depType")  
	public void setOutputDepTypeFeature(String x) {
		outputDepTypeFeature = x;
	}
	public String getOutputDepTypeFeature() {
	    return outputDepTypeFeature;
	}
	protected String outputDepTypeFeature;

	@RunTime
	@CreoleParameter(
			comment = "The name of the feature that will hold the id of the token which is the target of the dependency relation on token annotation types",
			defaultValue = "depTarget")  
	public void setOutputDepTargetFeature(String x) {
		outputDepTargetFeature = x;
	}
	public String getOutputDepTargetFeature() {
	    return outputDepTargetFeature;
	}
	protected String outputDepTargetFeature;	
	
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
