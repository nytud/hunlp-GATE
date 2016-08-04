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
import hu.u_szeged.cons.parser.MyBerkeleyParser;

import java.text.NumberFormat;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
//import hu.nytud.gate.parsers.DependencyRelation;

/** 
 *  Magyarlanc Hungarian Constituency Parser.
 *  Requires sentence and token annotations, and msd and lemma features on tokens.
 *  Produces constituency string on tokens.
 *  Tested with Magyarlánc "3.0"
 *  @author Peter Kundrath, Balint Sass
 */ 
@CreoleResource(name = "HU [OBS] 4. Consituency Parser (magyarlanc3.0, hfst)", 
				comment = "Requires sentences and tokens with lemma, pos and morph features"
				)
public class Magyarlanc3ConstituencyParserHFSTCode extends AbstractLanguageAnalyser {


	private static final long serialVersionUID = 1L;
	
	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	public Resource init() throws ResourceInstantiationException {
		//ResourceHolder.init();
		return this;
    }

	/** Requires token annotations (see inputTokenType), 
	 *  adds constituency string to tokens' outputConstFeature,
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
	            Utils.logOnce(logger, Level.INFO, "Magyarlánc Constituency Parser: " +
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
        
        // get form, lemma, pos and feat for each token into a String[][]
        String[][] tokfeats = new String[tokens.size()][4];
        int i = 0;
        for (Annotation tok: tokens) {
        	try {
        		tokfeats[i][0] = document.getContent().getContent(
					tok.getStartNode().getOffset(), 
					tok.getEndNode().getOffset()
				).toString();
			} catch (InvalidOffsetException e) {
				throw new GateRuntimeException(e);
			}
        	tokfeats[i][1] = tok.getFeatures().get(inputLemmaFeature).toString();
        	if (tokfeats[i][1] == null)
        		throw new GateRuntimeException("Error, no '" + inputLemmaFeature + "' feature for token " + i + " in sentence " + nsent); 

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

        	tokfeats[i][2] = __pos;
        	tokfeats[i][3] = __feat;

        	if (tokfeats[i][2] == null)
        		throw new GateRuntimeException("Error, no '" + inputPOSFeature + "' feature for token " + i + " in sentence " + nsent);
        	if (tokfeats[i][3] == null)
        		throw new GateRuntimeException("Error, no '" + inputMorphFeature + "' feature for token " + i + " in sentence " + nsent);

        	i++;
        }
                
        // call parser result: [[token_id, form, lemma, pos, morphfeats, const_type],...]
        String[][] pars = MyBerkeleyParser.getInstance().parseSentence(tokfeats);
        
        // Annotate tokens
        if (pars.length != tokens.size()) // sanity check
        	throw new GateRuntimeException("Internal error: pars.length != tokens.size() for sentence " + nsent);       
        for (i=0; i<tokens.size(); i++) {
        	if (pars[i].length != 5)
    			throw new GateRuntimeException("Internal error: parse data array size == " + pars[i].length +  " for token " + tokens.get(i).toString());
    		tokens.get(i).getFeatures().put(outputConstFeature, pars[i][4]);
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
			defaultValue = "cons")  
	public void setOutputConstFeature(String x) {
		outputConstFeature = x;
	}
	public String getOutputConstFeature() {
	    return outputConstFeature;
	}
	protected String outputConstFeature;
	
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
