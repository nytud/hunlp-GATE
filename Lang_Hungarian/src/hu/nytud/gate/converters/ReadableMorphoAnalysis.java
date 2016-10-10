package hu.nytud.gate.converters;

import gate.*;
import gate.creole.*;
import gate.creole.metadata.*;
import gate.creole.metadata.Optional;
import gate.util.*;

import java.util.*;

/**
 *  Convert morpho analysis into a more human readable format.
 *  Examples:
 *   in:  amely[/N|Pro|Rel]=amely+ek[Pl]=ek+ről[Del]=ről
 *   out: amely[/N|Pro|Rel] + ek[Pl] + ről[Del]
 *   in:  ki[/Prev]=ki+fizet[/V]=fizet+és[_Ger/N]=és+e[Poss.3Sg]=é+t[Acc]=t
 *   out: ki[/Prev] + fizet[/V] + és[_Ger/N] + e[Poss.3Sg]=é + t[Acc]
 *  Take 'ana' feature from 'anas' attrib of 'Token' annotations
 *  and put the result into 'ana/readable_ana'.
 *  @author Bálint Sass
 */
@CreoleResource(name = "HU 5. Human readable morpho analysis",
  comment = "Takes 'ana' feature from 'anas' attrib' on 'Token' annotations and puts the result into 'ana/readable_ana'"
  // helpURL = "http://corpus.nytud.hu/gate/doc/ReadableMorphoAnalysis"
)
public class ReadableMorphoAnalysis extends AbstractLanguageAnalyser {

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
    AnnotationSet outputAs =
      (outputASName == null || outputASName.trim().length() == 0)
      ? document.getAnnotations()
      : document.getAnnotations(outputASName);

    // -----
    // convert morpho analysis to human readable format begin
    // XXX miért is nem kell most köré try?
    // XXX vö: Magyarlanc3POSTaggerLemmatizer.java (ott miért kell?)

    // Get token annotations in document order
    // If there is no 'inputAnnotType' annot no new annot will be created
    List<Annotation> tokens =
      gate.Utils.inDocumentOrder( inputAs.get( inputAnnotType ));

    DocumentContent doc = document.getContent();

    // go through the tokens
    for ( Annotation token : tokens ) {

      // XXX kell a getOverlappingAnnotations()? minek?
      // XXX vö: Magyarlanc3POSTaggerLemmatizer.java (ott kell?)

      // get inputAnnotAttrib
      // get inputAnnotAttrib/inputAnnotAttribFeat
      // foreach inputAnnotAttrib/inputAnnotAttribFeat {
      //   convert! :)
      //   put result into inputAnnotAttrib/outputAnnotAttribFeat
      // }

      Object anas_obj = token.getFeatures().get(inputAnnotAttrib);

      if (!(anas_obj instanceof List<?>)) {
        System.err.println("ReadableMorphoAnalysis ERROR: '" + inputAnnotAttrib + "' is not List.");
      } else {
        @SuppressWarnings("unchecked")
        List<Map<String,String>> anas = (List<Map<String,String>>)anas_obj;

        List<String> opts = new ArrayList<>();
        for(Map<String,String> anas1 : anas) {
          if (!anas1.containsKey(inputAnnotAttribFeat)) {
            System.err.println("ReadableMorphoAnalysis ERROR: no '" + inputAnnotAttribFeat + "' feature in '" + inputAnnotAttrib + "'.");
          } else {
            // konvertálni és hozzátenni
            String ana = anas1.get(inputAnnotAttribFeat);
            anas1.put(outputAnnotAttribFeat, convert_morpho(ana));
          }
        }
      }
    }

    // convert morpho analysis to human readable format end
    // -----

    // process finished, acknowledge user about this.
    fireProcessFinished();
  }

  /**
   * Do the hard work: convert a morpho analysis into human readable format.
   */
  public String convert_morpho( String analysis ) {

    // * in:  ki[/Prev]=ki+fizet[/V]=fizet+és[_Ger/N]=és+e[Poss.3Sg]=é+t[Acc]=t
    // * out: ki[/Prev] + fizet[/V] + és[_Ger/N] + e[Poss.3Sg]=é + t[Acc]
    //
    // split /+/
    // for each {
    //   if ( substr az első '['-ig == substr az utsó '='-től ) {
    //     nem kell az =-s, azaz: morph = substr az utsó '='-ig
    //   } kül {
    //     marad
    //   }
    // join ' + '

    List<String> morphs = Arrays.asList(analysis.split("\\+"));
    for (final ListIterator<String> i = morphs.listIterator(); i.hasNext();) {
      final String morph = i.next();
      int bra = morph.indexOf("[");
      int equ = morph.lastIndexOf("=");
      // XXX ugye sose lesz '=' a végén (ti. utána jön a morfmegvalósulás),
      // azaz sose fog az alábbi outOfBounds lenni? :)
      // System.err.println( "morph=" + morph + " [ @ " + bra + " = @ " + equ );
      if ( morph.substring(0,bra).equals( morph.substring(equ+1) ) ) {
        String modif = morph.substring(0,equ);
        i.set(modif); // így lehet menet közben módosítani
        //System.err.println( "modified morph=" + modif );
      }
    }

    return String.join(" + ", morphs);
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
  // XXX a defaultValue értékét csak 1x kelljen leírni alább?
  @RunTime
  @CreoleParameter(
    comment="Annotation type which has an attrib containing the morpho analyses",
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
    comment="Attribute of 'inputAnnotType' which contains morpho analyses",
    defaultValue="anas"
  )
  public void setInputAnnotAttrib( String s ) {
    this.inputAnnotAttrib = s.trim().length() == 0 ? "anas" : s;
  }
  public String getInputAnnotAttrib() {
    return inputAnnotAttrib;
  }
  private String inputAnnotAttrib;

  @RunTime
  @CreoleParameter(
    comment="Feature of 'inputAnnotAttrib' which contains one morpho analysis",
    defaultValue="ana"
  )
  public void setInputAnnotAttribFeat( String s ) {
    this.inputAnnotAttribFeat = s.trim().length() == 0 ? "ana" : s;
  }
  public String getInputAnnotAttribFeat() {
    return inputAnnotAttribFeat;
  }
  private String inputAnnotAttribFeat;

  @RunTime
  @CreoleParameter(
    comment="Feature of 'inputAnnotAttrib' which contains teh converted human readable morpho analysis",
    defaultValue="readable_ana"
  )
  public void setOutputAnnotAttribFeat( String s ) {
    this.outputAnnotAttribFeat = s.trim().length() == 0 ? "readable_ana" : s;
  }
  public String getOutputAnnotAttribFeat() {
    return outputAnnotAttribFeat;
  }
  private String outputAnnotAttribFeat;

}

