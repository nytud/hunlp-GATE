package hu.nytud.gate.parsers;

import gate.*;
import gate.creole.*; 
import gate.creole.metadata.*; 
import gate.creole.metadata.Optional;
import gate.util.*;

import java.util.*;
import java.util.regex.*;

/** 
 *  Connects the preverb to the verb.
 *  Uses dependency analysis created by MagyarlancDependencyParser.java,
 *  namely "dependency" features representing a "PREVERB".
 *  Creates "preverb" and "lemmaWithPreverb" features on verbs.
 *  @author Bálint Sass
 */ 
@CreoleResource(
  name = "Hungarian Preverb Identifier",
  comment = "Requires dependency analysis: 'dependency' feature on 'Token's"
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

    // regex to get the id from dependency annotation
    Pattern idPattern = Pattern.compile("\\d+");

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

      String dep = "";
      try {
        dep = a.getFeatures().get( inputAnnotFeature ).toString();
        // getFeatures() returns null if there is no inputAnnotFeature feature
      } catch( NullPointerException e ) {
        dep = "";
      }

      Integer targetId = -1; // XXX hm.. Exception-nel kéne???
      if ( dep.startsWith( "PREVERB" ) ) {
        Matcher m = idPattern.matcher( dep );
        // XXX while-lal szokás nézni, mert elvben lehet több is,
        //     de feltesszük, hogy mindenképp 1 van
        if (m.find()) {
          targetId = Integer.valueOf( m.group() );
        }
        // if targetId is not found -> no new fea will be created
        if ( targetId != -1 ) {
          String preverbLemma = a.getFeatures().get( "lemma" ).toString();
          Annotation target = inputAs.get( targetId ); // Integer param -> byId
          FeatureMap fm = target.getFeatures();
          String verbLemma = target.getFeatures().get( "lemma" ).toString();
          fm.put( "preverb", preverbLemma );
          fm.put( "lemmaWithPreverb", preverbLemma + verbLemma );
        }
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
  // XXX a Token/dependency-t csak 1x kelljen leírni alább?
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
    comment="Feature of 'inputAnnotType' which contains dependency annot",
    defaultValue="dependency"
  )
  public void setInputAnnotFeature( String s ) {
    this.inputAnnotFeature = s.trim().length() == 0 ? "dependency" : s;
  }
  public String getInputAnnotFeature() {
    return inputAnnotFeature;
  }
  private String inputAnnotFeature;

}

