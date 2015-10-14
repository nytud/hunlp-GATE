package hu.rilmta.gate.testing;

import java.io.File;

import org.apache.log4j.Logger;

import gate.*;

/**
 * Test GATE Embedded & Lang_Hungarian plugin
 * @author Márton Miháltz
 *
 */
public class PRTest {
	
	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Create a Document LR from a string and apply 2 PRs, dump annotations to stdout
	 */
	public void test() {
		
		try {

			// Initialize GATE Embedded
			Gate.init();
									
			// Load the Lang_Hungarian plugin from the user plugin dir
			String userPluginDir = Gate.getUserConfig().getString("gate.user.plugins");
			Gate.getCreoleRegister().registerDirectories( 
					new File(userPluginDir, "Lang_Hungarian").toURI().toURL());

			// Create a new GATE Document LR with the following text:
			String sentstr = "A nagy kutya gyorsan fut. Én küldtem árvíztűrő nyélen.";
			Document doc = Factory.newDocument(sentstr);
		
			// Create a new MagyarlancSentenceSplitterTokenizer PR, apply it on the document
			ProcessingResource tok = (ProcessingResource)Factory.createResource(
					"hu.rilmta.gate.tokenizers.MagyarlancSentenceSplitterTokenizer");
			tok.setParameterValue("document", doc);
			tok.execute();
			
			// Create a new MagyarlancPOSTaggerLemmatizer PR, apply it on the document
			ProcessingResource tagger = (ProcessingResource)Factory.createResource(
					"hu.rilmta.gate.postaggers.MagyarlancPOSTaggerLemmatizer");
			tagger.setParameterValue("document", doc);
			tagger.execute();			
			
			// Dump document's annotations to stdout
			System.out.println(doc.toString());
			
		
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public static void main(String[] args) {
		System.out.println("Hello, test");
		PRTest t = new PRTest();
		t.test();
	}
	
}
