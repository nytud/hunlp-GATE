package hu.nytud.gate.testing;

import java.io.File;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import gate.*;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;

/**
 * Test Lang_Hungarian plugin with GATE Embedded (GATE API)
 * @author Márton Miháltz
 *
 */
public class PRTest {
	
	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	public void init() throws GateException {
		// Initialize GATE Embedded
		Gate.init();		
	}
	
	public static Document createDoc() throws ResourceInstantiationException {
		// Create a new GATE Document LR with the following text:
		String sentstr = "A nagy kutya gyorsan fut. Én küldtem árvíztűrő nyélen.";
		return Factory.newDocument(sentstr);		
	}
	
	public void loadLangHungarian() throws MalformedURLException, GateException {
		// Load the Lang_Hungarian plugin from the user plugin dir
		String userPluginDir = Gate.getUserConfig().getString("gate.user.plugins");
		Gate.getCreoleRegister().registerDirectories( 
				new File(userPluginDir, "Lang_Hungarian").toURI().toURL());
	}
	
	/**
	 * Create a Document, apply tokenizer and POS-tagger/lemmatizer, dump annotations to stdout
	 */
	public void testMLTokPOS() {
		
		try {
			
			this.init();
			
			Document doc = PRTest.createDoc();
									
			loadLangHungarian();
		
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
			System.out.println(doc.toString());		
		
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Create a document, add an annotation with a String[]-valued feature, dump XML
	 */
	public void testFeats() {

		try {

			init();

			Document doc = PRTest.createDoc();
			
			FeatureMap features = Factory.newFeatureMap();
		    features.put("anas", new String[] {"alma", "alom"});
		    doc.getAnnotations().add(0L, 1L, "Token", features);
		    
			System.out.println(doc.toXml());
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}

	/**
	 * Create a Document, apply tokenizer, morphanalizer, dump annotations to stdout
	 */
	public void testMLMoraAna() {
		
		try {
			
			this.init();
			
			Document doc = PRTest.createDoc();
									
			loadLangHungarian();
		
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
			System.out.println(doc.toString());
			//System.out.println(doc.toXml());
		
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
		
	/**
	 * Create a Document, apply tokenizer, KR morphanalizer, dump annotations to stdout
	 */
	public void testMLKRMoraAna() {
		
		try {
			
			this.init();
			
			Document doc = PRTest.createDoc();
									
			loadLangHungarian();
		
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
			System.out.println(doc.toString());
			//System.out.println(doc.toXml());
		
		} catch (Exception e) {
			e.printStackTrace();
		}

	}	
	
	public static void main(String[] args) {
		System.out.println("Hello, test");
		PRTest t = new PRTest();
		//t.testMLTokPOS();
		//t.testFeats();
		//t.testMLMoraAna();
		t.testMLKRMoraAna();
	}
	
}
