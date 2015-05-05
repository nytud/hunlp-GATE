package gate.lingpipe;

import gate.AnnotationSet;
import gate.Annotation; // XXX plusz!
import gate.annotation.AnnotationSetImpl; // XXX plusz!
import gate.DocumentContent; // XXX plusz!
import gate.FeatureMap;
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.util.GateRuntimeException;
import gate.util.InvalidOffsetException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*; // XXX plusz!

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.AbstractExternalizable;

/**
 * This PR is used for recognizing named entities such as location,
 * organizaiton etc. It uses the LingPipe models to achieve that.
 * 
 * @author niraj
 * 
 */
public class NamedEntityRecognizerPR extends AbstractLanguageAnalyser implements
                                                                     ProcessingResource {

  private static final long serialVersionUID = 6157830435067675635L;

  /** File which cotains model for NE */
  protected URL modelFileUrl;

  /** Model file extracted from the URL */
  protected File modelFile;

  /** The name of the annotation set used for input */
  protected String outputASName;

  /** Chunker object */
  protected Chunker chunker;

  /**
   * Initializes this resource
   * 
   * @return Resource
   * @throws ResourceInstantiationException
   */
  public Resource init() throws ResourceInstantiationException {
    if(modelFileUrl == null)
      throw new ResourceInstantiationException("No model file provided!");

    try {
      modelFile = new File(modelFileUrl.toURI());
    }
    catch(URISyntaxException e) {
      throw new ResourceInstantiationException(e);
    }

    if(modelFile == null || !modelFile.exists()) {
      throw new ResourceInstantiationException("modelFile:"
              + modelFileUrl.toString() + " does not exists");
    }

    try {
      chunker = (Chunker)AbstractExternalizable.readObject(modelFile);
    }
    catch(IOException e) {
      throw new ResourceInstantiationException(e);
    }
    catch(ClassNotFoundException e) {
      throw new ResourceInstantiationException(e);
    }

    return this;
  }

  /**
   * Method is executed after the init() method has finished its
   * execution. <BR>
   * 
   * @throws ExecutionException
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
      String docText = document.getContent().toString();

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

  /**
   * Returns the name of the AnnotationSet that has been provided to
   * create the AnnotationSet
   */
  public String getOutputASName() {
    return outputASName;
  }

  /**
   * Sets the AnnonationSet name, that is used to create the
   * AnnotationSet
   * 
   * @param annotationSetName
   */
  public void setOutputASName(String outputAS) {
    this.outputASName = outputAS;
  }

  /**
   * gets the url of the model used for recognizing named entiries in
   * the document.
   * 
   * @return
   */
  public URL getModelFileUrl() {
    return modelFileUrl;
  }

  /**
   * sets the url of the model used for recognizing named entiries in
   * the document.
   * 
   * @param modelFileUrl
   */
  public void setModelFileUrl(URL modelFileUrl) {
    this.modelFileUrl = modelFileUrl;
  }

}
