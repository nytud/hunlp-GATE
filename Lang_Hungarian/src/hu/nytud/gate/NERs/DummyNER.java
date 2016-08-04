package hu.nytud.gate.NERs;

import gate.*;
import gate.creole.*; 
import gate.creole.metadata.*; 
import gate.creole.metadata.Optional;
import gate.util.*;
import gate.annotation.*;

import java.util.*;

/** 
 *  Dummy Named Entity Recognizer.
 *  Based solely on capitalization of Tokens.
 *  @author Bálint Sass
 */ 
@CreoleResource(name = "HU [DEMO] 4. Named Entity Recognizer (dummy)",
				comment = "Dummy NER = 2 adjacent uppercase words :)",
				icon = "tokeniser",
				helpURL = "http://corpus.nytud.hu/gate/doc/DummyNER") 
public class DummyNER extends AbstractLanguageAnalyser { 
 
    /**
	 * (Generated, by Eclipse) -- ezzel mi legyen? XXX
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

    // lets start the progress and initialize the progress counter
    fireProgressChanged(0);

    // If no document provided to process throw an exception
    if(document == null) {
      fireProcessFinished();
      throw new GateRuntimeException("No document to process!");
    }

    // get the annotationSet name provided by the user, or otherwise use
    // the
    // default method
    AnnotationSet outputAs = (outputASName == null || outputASName.trim()
            .length() == 0) ? document.getAnnotations() : document
            .getAnnotations(outputASName);

    try {
      //String docText = document.getContent().toString();

      // -----

      // rendes NER: kikapcs
//      Chunking chunking = chunker.chunk(docText);
//      for(Chunk c : chunking.chunkSet()) {
//        FeatureMap fm = gate.Factory.newFeatureMap();
//        outputAs.add(new Long(c.start()), new Long(c.end()), c.type(), fm);
//      }

      // dummy NER = két egymás melletti nagybetűs szó

      // [1] 1-2 karakterből csináljon egy NE-t -- megy :)
      //FeatureMap fm = gate.Factory.newFeatureMap();
      //fm.put("type", "NAME");
      //outputAs.add(new Long(1), new Long(2), "NE", fm);

      // [2] végig a tokeneken, ha nagybetűs => NE -- megy :)
      //FeatureMap fm = gate.Factory.newFeatureMap();
      //fm.put("type", "NAME");
      //AnnotationSet tmp_AS = new AnnotationSetImpl(outputAs);
      //for ( Annotation a : tmp_AS ) {
      //  if ( a.getType().equals( "Token" ) ) {
      //    Long s = a.getStartNode().getOffset();
      //    Long e = a.getEndNode().getOffset();
      //    Character firstLetter =
      //      document.getContent().getContent(s, s+1).toString().charAt(0);
      //    if ( Character.isUpperCase( firstLetter ) ) {
      //      outputAs.add( s, e, "NE", fm );
      //    }
      //  }
      //}

      // [3] végig a tokeneken, ha 2 egymás melletti nagybetűs => NE
      // XXX egyáltalán hogy lehet megmondani adott Token-re,
      //     hogy ki szomszédos vele -- külön el kéne tárolni a Token-ben?
      AnnotationSet tmp_AS = new AnnotationSetImpl(outputAs);
      // XXX meg lehet úszni a lemásolást? így illik másolni?
      // [ua-n nem lehet végigmenni, mert ConcurrentModificationException()]

      // sorban végig a tokeneken
      // XXX van erre ennél jobb mód a rendezésnél?
      // szerencsére az Annotation Comparable [remélem, h kezdőoffset szerint!] :)
      List<Annotation> tmp_AS_list = new ArrayList<Annotation>(tmp_AS);
      Collections.sort(tmp_AS_list);

      Boolean isPreviousUpperCase = false; // az előző szó nagybetűs-e
      Long previousStart = 0l; // előző szó kezdőoffsete

      DocumentContent doc = document.getContent();

      for ( Annotation a : tmp_AS_list ) {
        if ( a.getType().equals( "Token" ) ) {
        // XXX van vmi hash-szerű, az örökös if-nél hatékonyabb hozzáférés?
        // XXX vagy vmi map-szerű dolog?

          Long s = a.getStartNode().getOffset();
          Long e = a.getEndNode().getOffset();

          Character firstLetter =
            doc.getContent(s, s+1).toString().charAt(0);

          Boolean isCurrentUpperCase = Character.isUpperCase( firstLetter );  

          if ( isCurrentUpperCase && isPreviousUpperCase ) {
            String content = 
              doc.getContent(previousStart, e).toString();
            FeatureMap fm = gate.Factory.newFeatureMap();
            fm.put( "type", "NAME" );
            fm.put( "content", "[" + content + "]" );
            outputAs.add( previousStart, e, "NE", fm );
          }

          previousStart = s;
          isPreviousUpperCase = isCurrentUpperCase;
          // XXX azáltal, hogy ezt itt csináljuk,
          // figyelmen kívül hagyjuk (értsd belevesszük!)
          // a két token közti bármit (ált. SpaceToken-t)
        }
      }
      //
      // dummy NER vége

      // -----

    }
    catch(InvalidOffsetException e) {
      throw new ExecutionException(e);
    }

    // process finished, acknowledge user about this.
    fireProcessFinished();
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

