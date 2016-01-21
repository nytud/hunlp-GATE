package hu.nytud.gate.tokenizers;

import java.util.List;

import gate.*;
import gate.creole.*; 
import gate.creole.metadata.*; 
import gate.util.InvalidOffsetException;
import hu.u_szeged.splitter.HunSplitter;

/** 
 * Hungarian sentence splitter and tokenizer processing resource using the HunSplitter class in Magyarlánc, 
 * which relies on Northwestern U.'s Morphadorner with some customizations.
 * Tested with Magyarlánc 2.0 (http://rgai.inf.u-szeged.hu/project/nlp/research/magyarlanc/magyarlanc-2.0.jar)
 * If you want both sentence splitting and tokenization, this PR is more efficient than sentence splitting and tokenizing separately.
 * Note: the sentence splitter seems to leave whitespace at the beginning of sentences, I left it as it was.
 * Author: Márton Miháltz
 */ 
@CreoleResource(name = "Magyarlanc Hungarian Sentence Splitter And Tokenizer",
				comment = "If you want both sentence splitting and tokenizations, this PR is more efficient than sentence splitting and tokenizing separately",
				icon = "tokeniser",
				helpURL = "http://corpus.nytud.hu/gate/doc/MagyarlancSplitterTokenizer") 
public class MagyarlancSentenceSplitterTokenizer extends AbstractLanguageAnalyser { 
 

	private static final long serialVersionUID = 1L;

	/**
     * Name of the output annotation set (CREOLE plugin runtime parameter)
     */
    private String outputASName;
    
    
    /*
     * The splitter/tokenizer engine 
     */
    private HunSplitter hunSplitter = null;
	
	/* 
	 * this method gets called whenever an object of this 
	 * class is created either from GATE Developer GUI or if 
	 * initiated using Factory.createResource() method. 
	 */ 
	public Resource init() throws ResourceInstantiationException { 
        // here initialize all required variables, and may 
        // be throw an exception if the value for any of the 
        // mandatory parameters is not provided 
 
		this.hunSplitter = new HunSplitter();
 
        return this; 
	}

	/* this method is called to reinitialize the resource */ 
	public void reInit() throws ResourceInstantiationException { 
		hunSplitter = new HunSplitter();
	} 
		
	/*
	 * Add Sentence and Token offset (and length) annotations to the document.
	 * This method mostly follows code from hu.u_szeged.splitter.HunSplitter.getTokenOffsets() 
	 */ 
	public void execute() throws ExecutionException { 

		if(document == null) {
			throw new ExecutionException("There is no loaded document");
	    }		
		
	    super.fireProgressChanged(0);
	    
	    AnnotationSet as = null;
	    if (outputASName == null || outputASName.trim().length() == 0)
	      as = document.getAnnotations();
	    else as = document.getAnnotations(outputASName);

	    String docContent = document.getContent().toString();

		int[] tokOffs = null;
		String sent = null;
		int ss, se, ts, te;
		FeatureMap fm = null;
		List<List<String>> splitted = hunSplitter.split(docContent);
		int[] sentOffs = hunSplitter.getSentenceOffsets(docContent);
		
		// TODO: assert len(sentOffs) == len(splitted) ???
		for (int i=0; i<splitted.size(); i++) {
			
			// sentence
	        ss = sentOffs[i];
	        // TODO: ss = start offset of first token in sentence?
	        se = sentOffs[i+1];
			sent = docContent.substring(ss, se);
	        fm = Factory.newFeatureMap();
	        fm.put("length", "" + (se - ss));
	        try {
	        	as.add(new Long(ss), new Long(se), "Sentence", fm);
	        }
		    catch(InvalidOffsetException e) {
		    	throw new ExecutionException(e);
			}	        

	        // tokens
			tokOffs = hunSplitter.getTokenizer().findWordOffsets(sent, splitted.get(i));
			// TODO: assert len(splitted(i)) == len(tokOffs) ???
			for (int j=0; j<splitted.get(i).size(); ++j) {
				ts = sentOffs[i] + tokOffs[j];
				te = ts + splitted.get(i).get(j).length();	
		        fm = Factory.newFeatureMap();
		        fm.put("length", "" + (te - ts));
		        fm.put("string", splitted.get(i).get(j));
		        try {
		        	as.add(new Long(ts), new Long(te), "Token", fm);
		        }
			    catch(InvalidOffsetException e) {
			    	throw new ExecutionException(e);
				}	        			
			}				
		}
	} 

	@RunTime
	@Optional
	@CreoleParameter(comment="The annotation set to be used for the generated annotations")
	public void setAnnotationSetName(String annotationSetName) {
		this.outputASName = annotationSetName;
	}
	
	public String getAnnotationSetName() {
		return outputASName;
	}	
		
   
}


