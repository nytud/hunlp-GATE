package hu.nytud.gate.parsers;

import gate.*;
import gate.creole.*; 
import gate.creole.metadata.*; 
import gate.creole.metadata.Optional;
import gate.util.*;

import java.util.*;

/** 
 *  Connects the preverb to the verb.
 *  Uses dependency analysis created by MagyarlancDependencyParser.java,
 *  namely "depType" features representing a "PREVERB".
 *  Creates "preverb" and "lemmaWithPreverb" features on verbs.
 *  @author Bálint Sass
 */ 
@CreoleResource(
  name = "5. Preverb Identifier (HU)",
  comment = "Requires dependency analysis: 'depType' and 'depTarget' features on 'Token's"
  // helpURL = "http://corpus.nytud.hu/gate/doc/PreverbIdentifier"
) 
public class PreverbIdentifier extends AbstractLanguageAnalyser { 
 
  /**
   * (Generated, by Eclipse) -- ezzel mi legyen? XXX
   */
  private static final long serialVersionUID = 4881484671828244243L;
  
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

    // get the input annotationSet name provided by the user,
    // or otherwise use the default method
    AnnotationSet inputAs =
      (inputASName == null || inputASName.trim().length() == 0)
      ? document.getAnnotations()
      : document.getAnnotations(inputASName);

    // get the output annotationSet name provided by the user,
    // or otherwise use the default method
//    AnnotationSet outputAs =
//      (outputASName == null || outputASName.trim().length() == 0)
//      ? document.getAnnotations()
//      : document.getAnnotations(outputASName);
    // XXX outputAs should be used...

    // -----
    // get/set preverb info [begin]

    // Get token annotations in document order
    // If there is no 'inputAnnotType' annot -> no new fea will be created
    List<Annotation> tokens =
      gate.Utils.inDocumentOrder( inputAs.get( inputAnnotType ) );

    // go through the tokens
    for ( Annotation a : tokens ) {

      // if a.dep/PREVERB {
      //   b = tokens/a.dep/PREVERB/ID     
      //   put ehhez a tokenhez: b
      //       ezt: preverb=a.lemma
      //            lemmaWithPreverb=a.lemma+b.lemma
      // }

      String depType = "";
      Integer depTarget = 0; // ??? XXX
      try {
        depType = a.getFeatures().get( inputAnnotDepTypeFeature ).toString();
        depTarget = (Integer)a.getFeatures().get( inputAnnotDepTargetFeature );
        // getFeatures() returns null if there is no inputAnnot.*Feature feature
      } catch( NullPointerException e ) {
        depType = "";
        depTarget = 0;
      }

      if ( depType.equals( "PREVERB" ) ) {
        String preverbLemma = a.getFeatures().get( "lemma" ).toString();
        Annotation target = inputAs.get( depTarget ); // Integer param -> byId
        FeatureMap fm = target.getFeatures();
        String verbLemma = target.getFeatures().get( "lemma" ).toString();
        fm.put( "preverb", preverbLemma );
        fm.put( "lemmaWithPreverb", preverbLemma + verbLemma );
      }
     }

    // get/set preverb info [end]
    // -----

    // process finished, acknowledge user about this.
    fireProcessFinished();
  }

  @RunTime
  @Optional
  @CreoleParameter(
    comment="The annotation set to take inputAnnotType (Token) annotation from"
  )
  public void setInputASName( String s ) {
    this.inputASName = s;
  }
  public String getInputASName() {
    return inputASName;
  }
  private String inputASName;

  @RunTime
  @Optional
  @CreoleParameter(
    comment="The annotation set to be used for the generated annotations"
  )
  public void setOutputASName( String s ) {
    this.outputASName = s;
  }
  public String getOutputASName() {
    return outputASName;
  }
  private String outputASName;

  // XXX hogy lehetne, hogy
  // XXX a Token/depType/depTarget-t csak 1x kelljen leírni alább?
  @RunTime
  @CreoleParameter(
    comment="Annotation type which has the dependency annot on it",
    defaultValue="Token"
  )
  public void setInputAnnotType( String s ) {
    this.inputAnnotType = s.trim().length() == 0 ? "Token" : s;
  }
  public String getInputAnnotType() {
    return inputAnnotType;
  }
  private String inputAnnotType;

  @RunTime
  @CreoleParameter(
    comment="Feature of 'inputAnnotType' which contains dependency type",
    defaultValue="depType"
  )
  public void setInputAnnotDepTypeFeature( String s ) {
    this.inputAnnotDepTypeFeature = s.trim().length() == 0 ? "depType" : s;
  }
  public String getInputAnnotDepTypeFeature() {
    return inputAnnotDepTypeFeature;
  }
  private String inputAnnotDepTypeFeature;

  @RunTime
  @CreoleParameter(
    comment="Feature of 'inputAnnotType' which contains dependency target token id",
    defaultValue="depTarget"
  )
  public void setInputAnnotDepTargetFeature( String s ) {
    this.inputAnnotDepTargetFeature = s.trim().length() == 0 ? "dependency" : s;
  }
  public String getInputAnnotDepTargetFeature() {
    return inputAnnotDepTargetFeature;
  }
  private String inputAnnotDepTargetFeature;

}

