package hu.nytud.gate.NERs;

import gate.*;
import gate.creole.*; 
import gate.creole.metadata.*; 
import gate.creole.metadata.Optional;
import gate.util.*;

import java.util.*;

/** 
 *  Dummy IOB Named Entity Recognizer.
 *  Puts IOB (inside/outside/begin) NER annotation on Token annotations
 *  based solely on capitalization of Tokens.
 *  @author Bálint Sass
 */ 
@CreoleResource(name = "Hungarian Dummy IOB NER",
				comment = "Requires 'Token' annotation"
				// helpURL = "http://corpus.nytud.hu/gate/doc/DummyIobNer"
        ) 
public class DummyIobNer extends AbstractLanguageAnalyser { 
 
  /**
	 * (Generated, by Eclipse) -- ezzel mi legyen? XXX
	 */
	private static final long serialVersionUID = 4881484671828244243L;
	
    /**
     * Name of the output annotation set (CREOLE plugin runtime parameter)
     */
    private String outputASName;
	
	public Resource init() throws ResourceInstantiationException { 
    return this; 
	}

	public void reInit() throws ResourceInstantiationException { 
	} 
		
	/** 
	 * Adds ne_iob attrib to 'Token' annots.
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
    // the default method
    AnnotationSet outputAs = (outputASName == null || outputASName.trim()
            .length() == 0) ? document.getAnnotations() : document
            .getAnnotations(outputASName);

    // -----
    // dummy IOB NER begin
    // simple functionality: go through the Tokens, if uppercase -> NE :)

    // Get token annotations in document order
    // XXX XXX XXX hibát kéne dobni, ha nincs 'Token' annotáció
    List<Annotation> tokens = gate.Utils.inDocumentOrder(outputAs.get("Token"));

    DocumentContent doc = document.getContent();

    Annotation prev = null;

    // go through the tokens
    for ( Annotation a : tokens ) {

      //ha nem uc -> O
      //ha uc: 
      //if ( prev == null [nincs előző]
      //     VAGY előző nem uc ) { // eleje
      //   -> B
      //} else { // = van előző és uc :)
      //   -> I
      //}

      // dummy IOB NER annotator = if uppercase -> NE :)
      String annot = null;
      if ( ! isUppercase( a, doc ) ) { // not uc
        annot = "O";
      } else { // uc
        if ( prev == null || ! isUppercase( prev, doc ) ) { // at the beginning OR prev is not uc
          annot = "B";
        } else { // there is a previous token and it is uc
          annot = "I";
        }
      }

      a.getFeatures().put( "ne_iob", annot );

      prev = a;
    }
    // dummy IOB NER end
    // -----

    // process finished, acknowledge user about this.
    fireProcessFinished();
  }

  /**
   * Whether the text of 'a' annot in document 'doc' is uppercase.
   */
  public Boolean isUppercase ( Annotation a, DocumentContent doc )
      throws ExecutionException {
    // XXX ez a hibadobás nem biztos, hogy így illendő itt javastílusilag

    // a.toString() -- sajnos nem a Token "szövegét" adja, hm egy leírást,
    // úgyhogy a token szövegét ki kell bányászni a dokumentumból...

    Long s = a.getStartNode().getOffset();

    Character firstLetter = null;
    try {
      firstLetter = doc.getContent(s, s+1).toString().charAt(0);
    } catch(InvalidOffsetException e) {
      throw new ExecutionException(e);
    }

    return Character.isUpperCase( firstLetter );  
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

