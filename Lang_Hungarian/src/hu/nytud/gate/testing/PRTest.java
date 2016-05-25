package hu.nytud.gate.testing;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;

import gate.*;
//import gate.creole.ANNIEConstants;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;

/**
 * Test Lang_Hungarian plugin with GATE Embedded (GATE API)
 * (somewhat obsolete: hu.nytud.gate.pipeline.Pipeline can be used instead)
 * @author Márton Miháltz, Bálint Sass
 *
 */
public class PRTestImproved {

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
      File jarFile = new File(PRTestImproved.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      pluginDir = jarFile.getParentFile().toURI().toURL();
    }
    System.err.format("\nplugin dir to use = %s\n\n", pluginDir.toString());
    //pluginDir = new File("/home/mm/Infra2/hunlp-GATE/Lang_Hungarian").toURI().toURL();
    Gate.getCreoleRegister().registerDirectories(pluginDir);
  }

  /* testing methods */

  /**
   * Create a document, add an annotation with a String[]-valued feature, dump XML
   */
  public void testFeats() {

    try {

      init();

      Document doc = PRTestImproved.createDoc();

      FeatureMap features = Factory.newFeatureMap();
        features.put("anas", new String[] {"alma", "alom"});
        doc.getAnnotations().add(0L, 1L, "Token", features);

      System.out.println(doc.toXml());

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * Run ML KR morphanalyzer = apply ML tokenizer + KR morphanalyzer
   */
  public void testMLKRMorph() {

    try {

      this.init();

      Document doc = PRTestImproved.createDoc();

      loadLangHungarian(false);

      // Create a new MagyarlancSentenceSplitterTokenizer PR, apply it on the document
      ProcessingResource tok = (ProcessingResource)Factory.createResource(
          "hu.nytud.gate.tokenizers.MagyarlancSentenceSplitterTokenizer");
      tok.setParameterValue("document", doc);
      tok.execute();

      // Create a new MagyarlancKMorphAnalyzer PR, apply it on the document
      ProcessingResource analyzer = (ProcessingResource)Factory.createResource(
          "hu.nytud.gate.morph.MagyarlancKRMorphAnalyzer");
      analyzer.setParameterValue("document", doc);
      analyzer.execute();

      // Dump document's annotations to stdout
      System.out.println(doc.toXml());

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Run ML MSD morphanalyzer = apply ML tokenizer + MSD morphanalyzer
   */
  public void testMLMSDMorph() {

    try {

      this.init();

      Document doc = PRTestImproved.createDoc();

      loadLangHungarian(false);

      // Create a new MagyarlancSentenceSplitterTokenizer PR, apply it on the document
      ProcessingResource tok = (ProcessingResource)Factory.createResource(
          "hu.nytud.gate.tokenizers.MagyarlancSentenceSplitterTokenizer");
      tok.setParameterValue("document", doc);
      tok.execute();

      // Create a new MagyarlancMSDMorphAnalyzer PR, apply it on the document
      ProcessingResource analyzer = (ProcessingResource)Factory.createResource(
          "hu.nytud.gate.morph.MagyarlancMSDMorphAnalyzer");
      analyzer.setParameterValue("document", doc);
      analyzer.execute();

      // Dump document's annotations to stdout
      System.out.println(doc.toXml());

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * Run ML "Morphparse" = apply ML tokenizer + (MSD) POS tagger/lemmatizer
   */
  public void testMorphparse() {

    try {

      this.init();

      Document doc = PRTestImproved.createDoc();

      loadLangHungarian(false);

      // Create a new MagyarlancSentenceSplitterTokenizer PR, apply it on the document
      ProcessingResource tok = (ProcessingResource)Factory.createResource(
          "hu.nytud.gate.tokenizers.MagyarlancSentenceSplitterTokenizer");
      tok.setParameterValue("document", doc);
      tok.execute();

      // Create a new MagyarlancPOSTaggerLemmatizer PR, apply it on the document
      ProcessingResource tagger = (ProcessingResource)Factory.createResource(
          "hu.nytud.gate.postaggers.MagyarlancPOSTaggerLemmatizer");
      tagger.setParameterValue("document", doc);
      tagger.execute();

      // Dump document's annotations to stdout
      System.out.println(doc.toXml());

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * Run ML "Depparse" = apply ML tokenizer + (MSD) POS tagger/lemmatizer + dependency parser
   */
  public void testDepparse() {

    try {

      this.init();

      Document doc = PRTestImproved.createDoc();

      loadLangHungarian(false);

      // Create a new MagyarlancSentenceSplitterTokenizer PR, apply it on the document
      ProcessingResource tok = (ProcessingResource)Factory.createResource(
          "hu.nytud.gate.tokenizers.MagyarlancSentenceSplitterTokenizer");
      tok.setParameterValue("document", doc);
      tok.execute();

      // Create a new MagyarlancPOSTaggerLemmatizer PR, apply it on the document
      ProcessingResource tagger = (ProcessingResource)Factory.createResource(
          "hu.nytud.gate.postaggers.MagyarlancPOSTaggerLemmatizer");
      tagger.setParameterValue("document", doc);
      tagger.execute();

      // Create a new MagyarlancDependencyParser PR, apply it on the document
      ProcessingResource parser = (ProcessingResource)Factory.createResource(
          "hu.nytud.gate.parsers.MagyarlancDependencyParser");
      parser.setParameterValue("document", doc);
      //parser.setParameterValue("addPosTags", true);
      //parser.setParameterValue("addMorphFeatures", true);
      parser.execute();

      // Dump document's annotations to stdout
      System.out.println(doc.toXml());

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * Run ML "Depparse" = apply ML tokenizer + (MSD) POS tagger/lemmatizer + dependency parser
   * with additional parser parameters
   */
  public void testDepparseWithParams() {

    try {

      this.init();

      Document doc = PRTestImproved.createDoc();

      loadLangHungarian(false);

      // Create a new MagyarlancSentenceSplitterTokenizer PR, apply it on the document
      ProcessingResource tok = (ProcessingResource)Factory.createResource(
          "hu.nytud.gate.tokenizers.MagyarlancSentenceSplitterTokenizer");
      tok.setParameterValue("document", doc);
      tok.execute();

      // Create a new MagyarlancPOSTaggerLemmatizer PR, apply it on the document
      ProcessingResource tagger = (ProcessingResource)Factory.createResource(
          "hu.nytud.gate.postaggers.MagyarlancPOSTaggerLemmatizer");
      tagger.setParameterValue("document", doc);
      tagger.execute();

      // Create a new MagyarlancDependencyParser PR, apply it on the document
      ProcessingResource parser = (ProcessingResource)Factory.createResource(
          "hu.nytud.gate.parsers.MagyarlancDependencyParser");
      parser.setParameterValue("document", doc);
      parser.setParameterValue("addPosTags", true);
      parser.setParameterValue("addMorphFeatures", true);
      parser.execute();

      // Dump document's annotations to stdout
      System.out.println(doc.toXml());

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * Run quntoken + hunmorph
   */
  public void testQuntoken() {

    try {

      this.init();

      Document doc = PRTestImproved.createDoc();

      loadLangHungarian(false);

      // Create a new QunTokenCommandLine PR, apply it on the document
      ProcessingResource qtok = (ProcessingResource)Factory.createResource(
          "hu.nytud.gate.tokenizers.QunTokenCommandLine");
      qtok.setParameterValue("document", doc);
      qtok.execute();

      // Dump document's annotations to stdout
//      System.out.println("Quntoken tagged contents:");
//      System.out.println(doc.toXml());

      // Create a new HunMorphCommandLine PR, apply it on the document
      ProcessingResource tagger = (ProcessingResource)Factory.createResource(
          "hu.nytud.gate.morph.HunMorphCommandLine");
      tagger.setParameterValue("document", doc);
      tagger.execute();

      // Dump document's annotations to stdout
//      System.out.println("After applying HunMorph:");
      System.out.println(doc.toXml());


    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public static void main(String[] args) {
    //System.err.println("Hello, test");
    PRTestImproved t = new PRTestImproved();
    //t.testFeats(); // -- maybe not needed

    // Run ML KR morphanalyzer = apply ML tokenizer + KR morphanalyzer
    //
    //t.testMLKRMorph();
    // -- seems to be ok

    // Run ML MSD morphanalyzer = apply ML tokenizer + MSD morphanalyzer
    //
    //t.testMLMSDMorph();
    // -- seems to be ok

    // Run ML "Morphparse" = apply ML tokenizer + (MSD) POS tagger/lemmatizer
    //
    //t.testMorphparse();
    // -- seems to be ok

    // Run ML "Depparse" = apply ML tokenizer + (MSD) POS tagger/lemmatizer + dependency parser
    //
    //t.testDepparse();
    // -- seems to be ok
 
    // Run ML "Depparse" = apply ML tokenizer + (MSD) POS tagger/lemmatizer + dependency parser
    // with additional parser parameters (!)
    //
    t.testDepparseWithParams();
    // -- seems to be ok

    // Run quntoken + hunmorph
    //
    //t.testQuntoken();
    // -- seems to be ok
  }

}
