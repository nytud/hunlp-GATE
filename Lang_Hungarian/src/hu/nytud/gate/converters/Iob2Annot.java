package hu.nytud.gate.converters;

import gate.*;
import gate.creole.*; 
import gate.creole.metadata.*; 
import gate.creole.metadata.Optional;
import gate.util.*;
import gate.annotation.*;

import java.util.*;

/** 
 *  IOB (inside/outside/begin) code to regular annotation converter.
 *  Takes IOB code which is present on some annot (e.g. Tokens),
 *  and creates new annotations based on the info encoded in IOB codes.
 *  @author Bálint Sass
 */ 
@CreoleResource(name = "Hungarian IOB 2 Annot",
				comment = "Requires 'Token' annotation with IOB codes in ne_iob attrib"
				// helpURL = "http://corpus.nytud.hu/gate/doc/Iob2Annot"
        ) 
public class Iob2Annot extends AbstractLanguageAnalyser { 
 
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
   * Creates new annotations based on the info encoded in IOB codes.
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

    try {
      // -----
      // convert IOB to regular annot begin
      // simple functionality: codes can be "I", "O" or "B"
  
      // Get token annotations in document order
      // XXX XXX XXX hibát kéne dobni, ha nincs 'Token' annotáció
      List<Annotation> tokens = gate.Utils.inDocumentOrder(outputAs.get("Token"));
  
      DocumentContent doc = document.getContent();
  
      // XXX igaziból vmi cont-ban kéne gyűjteni és a végén join
      // XXX de a String.join() miért nem megy vajon?
      String neTokenIds = "";
  
      Long start = 0l; // startOffset
      Long end = 0l; // endOffset
  
      // go through the tokens
      for ( Annotation a : tokens ) {
  
        // if ( B )
        //   annotStartOffset = B.begin
        //   annotEndOffset = B.end
        //   ArrayList tokens = B.id
        // if ( I )
        //   annotEndOffset = I.end
        //   tokens.add( I.id )
        // if ( O or none )
        //   create annot: annotStartOffset..annotEndOffset,
        //     contentFromOffsets, tokenIdsAsString   
  
        String iobCode = a.getFeatures().get( "ne_iob" ).toString();
        if ( iobCode == null ) { iobCode = "O"; } // maybe needed
        // XXX ha nincs 'ne_iob' attrib, akkor nullt ad a getFeatures()?
  
        if ( iobCode.equals( "B" ) ) {
          start = a.getStartNode().getOffset();
          end = a.getEndNode().getOffset();
          neTokenIds += a.getId().toString();
        } else if ( iobCode.equals( "I" ) ) {
          end = a.getEndNode().getOffset();
          neTokenIds += "," + a.getId().toString();
        } else { // if "O" or anything else
          if ( ! neTokenIds.equals("") ) { // XXX nem szép
            createAnnotation( start, end, doc, neTokenIds, outputAs );
            neTokenIds = "";
            start = 0l;
            end = 0l;
          }
        }
      }
      // handling last NE -- needed if the last code is not "O"
      // XXX ez most így szép külön fgvénybe véve 1000 paraméterrel?? :)
      if ( ! neTokenIds.equals("") ) { // XXX nem szép
        createAnnotation( start, end, doc, neTokenIds, outputAs );
      }

      // convert IOB to regular annot end
      // -----
    } catch(InvalidOffsetException e) {
      throw new ExecutionException(e);
    }

    // process finished, acknowledge user about this.
    fireProcessFinished();
  }

	/** 
   * Creates one annotation.
	 */ 
  public void createAnnotation ( Long start, Long end, DocumentContent doc,
      String neTokenIds, AnnotationSet outputAs )
      throws InvalidOffsetException {

    String content = doc.getContent( start, end ).toString();

    FeatureMap fm = gate.Factory.newFeatureMap();
    fm.put( "content", "[" + content + "]" );
    fm.put( "tokenIds", "[" + neTokenIds + "]" );

    outputAs.add( start, end, "NE", fm); // XXX ... és ha van már NE?
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

