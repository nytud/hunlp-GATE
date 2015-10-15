package hu.nytud.gate.sentencesplitters;

import gate.*;
import gate.creole.*; 
import gate.creole.metadata.*; 
import gate.util.InvalidOffsetException;
import hu.u_szeged.splitter.HunSplitter;

/** 
 * Hungarian sentence splitter processing resource using the HunSplitter class in Magyarlánc, 
 * which relies on Northwestern U.'s Morphadorner with some customizations.
 * Tested with Magyarlánc 2.0 (http://rgai.inf.u-szeged.hu/project/nlp/research/magyarlanc/magyarlanc-2.0.jar)
 * Note: the sentence splitter seems to leave whitespace at the beginning of sentences, I left it as it was.
 * @author Márton Miháltz
 */ 
@CreoleResource(name = "Magyarlánc Hungarian Sentence Splitter",
				icon = "sentence-splitter",
				helpURL = "http://corpus.nytud.hu/gate/doc/MagyarlancSentenceSplitter") 
public class MagyarlancSentenceSplitter extends AbstractLanguageAnalyser { 
 

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
	 * Add Sentence annotations to the document.
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

		int ss, se;
		FeatureMap fm = null;
		int[] sentOffs = hunSplitter.getSentenceOffsets(docContent);
		
		for (int i=0; i<sentOffs.length-1; i++) {
	        ss = sentOffs[i];
	        se = sentOffs[i+1];
	        fm = Factory.newFeatureMap();
	        fm.put("length", "" + (se - ss));
	        try {
	        	as.add(new Long(ss), new Long(se), "Sentence", fm);
	        }
		    catch(InvalidOffsetException e) {
		    	throw new ExecutionException(e);
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


