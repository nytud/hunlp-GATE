package hu.rilmta.gate.tokenizers;

import gate.*;
import gate.creole.*; 
import gate.creole.metadata.*; 
import gate.creole.metadata.Optional;
import gate.util.*;

import java.util.*;

/** 
 * Processing Resource.  The @CreoleResource annotation marks this 
 *  class as a GATE Resource, and gives the information GATE needs 
 *  to configure the resource appropriately. 
 */ 
@CreoleResource(name = "Hungarian Dummy Tokenizer",
				comment = "Dummy whitespace tokenizer, just for testing",
				icon = "tokeniser",
				helpURL = "http://corpus.nytud.hu/gate/doc/DummyTokenizer") 
public class DummyTokenizer extends AbstractLanguageAnalyser { 
 
    /**
	 * (Generated, by Eclipse)
	 */
	private static final long serialVersionUID = 4881484671828244243L;
	
    /**
     * Name of the output annotation set (CREOLE plugin runtime parameter)
     */
    private String outputASName;
	
	/* 
	 * this method gets called whenever an object of this 
	 * class is created either from GATE Developer GUI or if 
	 * initiated using Factory.createResource() method. 
	 */ 
	public Resource init() throws ResourceInstantiationException { 
        // here initialize all required variables, and may 
        // be throw an exception if the value for any of the 
        // mandatory parameters is not provided 
 
        //if (this.rulesURL == null) 
        //    throw new ResourceInstantiationException("rules URL null"); 
 
        return this; 
	}

	/* this method is called to reinitialize the resource */ 
	public void reInit() throws ResourceInstantiationException { 
		// reinitialization code 
	} 
		
	/* 
	 * this method should provide the actual functionality of the PR 
	 * (from where the main execution begins). This method 
	 * gets called when user click on the "RUN" button in the 
	 * GATE Developer GUI’s application window. 
	 */ 
	public void execute() throws ExecutionException { 

		if(document == null) {
			throw new ExecutionException("There is no loaded document");
	    }		
		
	    super.fireProgressChanged(0);
	    
	    long startOffset = 0, endOffset = 0;
	    AnnotationSet as = null;
	    if (outputASName == null || outputASName.trim().length() == 0)
	      as = document.getAnnotations();
	    else as = document.getAnnotations(outputASName);

	    String docContent = document.getContent().toString();

	    List<String> tokenList = new ArrayList<String>();
	    List<String> whiteList = new ArrayList<String>();

      // -----
      
	    // na itt van a rendes tokenizálás helyett egy
	    // "dummy", ami adott karakter mentén vág
	    String separator = " "; // regex! :)
	    String[] words = docContent.split( separator ); // "tokenizálás" :)

	    whiteList.add( "" ); // 1 db szeparátor a legelejére -- így jó lett...
	    // sztem a LingPipe-os alábbi ciklus hülyesége miatt kell

	    for ( int i = 0; i < words.length; i++ ) {
	      tokenList.add( words[i] );
	      if ( i < words.length - 1 ) { // jó ez: separator 1-gyel kevesebb van :)
	        whiteList.add( separator ); // ez gáz, XXX XXX XXX
	        // mert nem tuti, hogy ua-t teszi vissza, mint ami ott volt!
	        // pl. ha 2 db separator karakter van egymás után / a végén XXX
	      }
	    }
	    // * ha az elején szepa -> betesz egy üres tokent az elejére
	    // * 2 szepa egymás mellett -> néha betesz üres tokent, néha nem...
	    // dummy tokenizer vége

	    // -----

	    for(int i = 0; i < whiteList.size(); i++) {
	      try {

	        startOffset = endOffset;
	        endOffset = startOffset + whiteList.get(i).length();
	        if((endOffset - startOffset) != 0) {
	          FeatureMap fmSpaces = Factory.newFeatureMap();
	          fmSpaces.put("length", "" + (endOffset - startOffset));
	          as.add(new Long(startOffset), new Long(endOffset), "SpaceToken",
	                  fmSpaces);
	        }

	        if(i < tokenList.size()) {
	          startOffset = endOffset;
	          endOffset = startOffset + tokenList.get(i).length();
	          FeatureMap fmTokens = Factory.newFeatureMap();
	          fmTokens.put("length", "" + (endOffset - startOffset));
	          as.add(new Long(startOffset), new Long(endOffset), "Token", fmTokens);
	        }
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


