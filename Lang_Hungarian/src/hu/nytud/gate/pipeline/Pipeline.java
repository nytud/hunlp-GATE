package hu.nytud.gate.pipeline;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.HashMap;

import org.apache.log4j.Logger;

import gate.*;
//import gate.creole.ANNIEConstants;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;

/**
 * Command line (GATE Embedded) version of Lang_Hungarian (based on PRTest.java)
 * @author Márton Miháltz, Bálint Sass
 *
 */
public class Pipeline {

  protected Logger logger = Logger.getLogger(this.getClass().getName());

  public void init() throws GateException {
    // Initialize GATE Embedded
    Gate.init();
  }

  public static Document createDoc() throws ResourceInstantiationException {
    // Create a new GATE Document LR with the following text:
    //String sentstr = "Alma";
    String sentstr = "A kisfiú egy szép képet rajzol meg.";
    //String sentstr = "The President is in Washington, DC. He is safe.";
    return Factory.newDocument(sentstr);
  }

  public void loadLangHungarian(boolean useUserPluginDir) throws MalformedURLException, GateException, URISyntaxException {
    // Load the Lang_Hungarian plugin
    // useUserPluginDir: if true, load it from user plugin dir/Lang_Hungarian, otherwise use dir of current jar file
    URL pluginDir;
    if (useUserPluginDir) {
      pluginDir = new File(Gate.getUserConfig().getString("gate.user.plugins"), "Lang_Hungarian").toURI().toURL();
    }
    else {
      File jarFile = new File(Pipeline.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      pluginDir = jarFile.getParentFile().toURI().toURL();
    }
    System.err.format("\nplugin dir to use = %s\n\n", pluginDir.toString());
    //pluginDir = new File("/home/mm/Infra2/hunlp-GATE/Lang_Hungarian").toURI().toURL();
    Gate.getCreoleRegister().registerDirectories(pluginDir);
  }

  /**
   * Run any combination of PRs specified as a PRSpec array
   */
  public void runPRs( PRSpec[] PRSpecs ) {

    try {

      this.init();

      Document doc = Pipeline.createDoc();

      loadLangHungarian(false);

      for ( PRSpec sp: PRSpecs ) {
        // Create the new PR
        ProcessingResource pr =
          (ProcessingResource)Factory.createResource( sp.getName() );
        pr.setParameterValue("document", doc);

        // set the PR's parameters
        for ( HashMap.Entry<String, Object> entry :
            sp.getParams().entrySet() ) {
          pr.setParameterValue( entry.getKey(), entry.getValue() );
        }

        // ... and apply it to the document
        pr.execute();
      }

      // Dump document's annotations to stdout in GATE XML format
      System.out.println(doc.toXml());

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public static void main(String[] args) {
    Pipeline t = new Pipeline();

    // Run ML KR morphanalyzer = apply ML tokenizer + KR morphanalyzer
    //
    //t.runPRs( new PRSpec[]{
    //  new PRSpec( "hu.nytud.gate.tokenizers.MagyarlancSentenceSplitterTokenizer" ),
    //  new PRSpec( "hu.nytud.gate.morph.MagyarlancKRMorphAnalyzer" )
    //} );

    // Run ML MSD morphanalyzer = apply ML tokenizer + MSD morphanalyzer
    //
    //t.runPRs( new PRSpec[]{
    //  new PRSpec( "hu.nytud.gate.tokenizers.MagyarlancSentenceSplitterTokenizer" ),
    //  new PRSpec( "hu.nytud.gate.morph.MagyarlancMSDMorphAnalyzer" )
    //} );

    // Run ML "Morphparse" = apply ML tokenizer + (MSD) POS tagger/lemmatizer
    //
    //t.runPRs( new PRSpec[]{
    //  new PRSpec( "hu.nytud.gate.tokenizers.MagyarlancSentenceSplitterTokenizer" ),
    //  new PRSpec( "hu.nytud.gate.postaggers.MagyarlancPOSTaggerLemmatizer" )
    //} );

    // Run ML "Depparse" = apply ML tokenizer + (MSD) POS tagger/lemmatizer + dependency parser
    //
    //t.runPRs( new PRSpec[]{
    //  new PRSpec( "hu.nytud.gate.tokenizers.MagyarlancSentenceSplitterTokenizer" ),
    //  new PRSpec( "hu.nytud.gate.postaggers.MagyarlancPOSTaggerLemmatizer" ),
    //  new PRSpec( "hu.nytud.gate.parsers.MagyarlancDependencyParser" )
    //} );
 
    // Run ML "Depparse" = apply ML tokenizer + (MSD) POS tagger/lemmatizer + dependency parser
    // with additional parser parameters (!)
    //
    //HashMap<String, Object> parser_params = new HashMap<String, Object>();
    //parser_params.put( "addPosTags", true );
    //parser_params.put( "addMorphFeatures", true );
    //t.runPRs( new PRSpec[]{
    //  new PRSpec( "hu.nytud.gate.tokenizers.MagyarlancSentenceSplitterTokenizer" ),
    //  new PRSpec( "hu.nytud.gate.postaggers.MagyarlancPOSTaggerLemmatizer" ),
    //  new PRSpec( "hu.nytud.gate.parsers.MagyarlancDependencyParser", parser_params )
    //} );

    // Run quntoken + hunmorph
    //
    //t.runPRs( new PRSpec[]{
    //  new PRSpec( "hu.nytud.gate.tokenizers.QunTokenCommandLine" ),
    //  new PRSpec( "hu.nytud.gate.morph.HunMorphCommandLine" )
    //} );

    // Run full available Lang_Hungarian pipeline :)
    //
    HashMap<String, Object> iob_params = new HashMap<String, Object>();
      iob_params.put( "inputIobAnnotAttrib", "NP-BIO" );
      iob_params.put( "outputAnnotationName", "NP" );
    HashMap<String, Object> qt_params = new HashMap<String, Object>();
      qt_params.put( "outputASName", "qt_hfst_as" );
    HashMap<String, Object> hfst_params = new HashMap<String, Object>();
      hfst_params.put( "inputASName", "qt_hfst_as" );
      hfst_params.put( "outputASName", "qt_hfst_as" );
    t.runPRs( new PRSpec[]{
      new PRSpec( "hu.nytud.gate.tokenizers.MagyarlancSentenceSplitterTokenizer" ),
      new PRSpec( "hu.nytud.gate.postaggers.MagyarlancPOSTaggerLemmatizer" ),
      new PRSpec( "hu.nytud.gate.parsers.MagyarlancDependencyParser" ),
      new PRSpec( "hu.nytud.gate.parsers.PreverbIdentifier" ),
      new PRSpec( "hu.nytud.gate.othertaggers.Huntag3NPChunkerCommandLine" ),
      new PRSpec( "hu.nytud.gate.converters.Iob2Annot", iob_params ),
      new PRSpec( "hu.nytud.gate.tokenizers.QunTokenCommandLine", qt_params ),
      new PRSpec( "hu.nytud.gate.morph.HFSTMorphJava", hfst_params )
    } );
 
  }

}
