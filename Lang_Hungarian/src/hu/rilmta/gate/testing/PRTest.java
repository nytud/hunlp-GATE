package hu.rilmta.gate.testing;

import java.io.File;

import org.apache.log4j.Logger;

import gate.*;
import gate.util.GateException;

public class PRTest {
	
	protected Logger logger = Logger.getLogger(this.getClass().getName());

	public void test() {
		
		try {

			Gate.init();
			
			String sentstr = "A nagy kutya gyorsan fut.";
			
			Document doc = Factory.newDocument(sentstr);
				
			Gate.getCreoleRegister().registerDirectories( 
					new File("/home/mm/GATE_plugins/Lang_Hungarian").toURI().toURL()); // TODO: get user plugin dir 
		
			ProcessingResource tok = (ProcessingResource)Factory.createResource(
					"hu.rilmta.gate.tokenizers.MagyarlancSentenceSplitterTokenizer");
			
			tok.setParameterValue("document", doc);
			tok.execute();
			
		
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
