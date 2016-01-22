/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on Jan 28, 2006
 *
 * encoding bug fixed on 20090619 by bpgergo
 */

package bme.mokk.hunmorph;
import java.util.*;
import java.io.*;
/**
 * @author hp
 *
 */
public class HunmorphStub {

	public enum Guess {NoGuess, Fallback, Global};
	
	public enum Compounds {No, Allow};
	
	private long engine;
	
    private  native static void initIDs();
    static {

        System.loadLibrary ( "ocamorph" ) ;
		initIDs(); 

  }
    private  native  long init(String bin);

	//const ocamorph_engine engine, const int blocking, const int compounds, const int stop_at_first, const int guess
	// valami hiba van az ocamorph-ban, mert a stop_at_first vezerli az osszetettszosagot
    private  native  long  make_analyzer( long engine,  int blocking,  int compounds,  int stop_at_first,  int guess );
  
    private native   void analyze(long analyzer, byte[] word);
    
	private List<String> result = null;
	
    private void callback(byte[] ana) {
		//encoding bug fixed by bpgergo
		String s = new String(ana, "ISO-8859-2");
		result.add(s);
	// System.out.print("callback-kel bekerult: "+s+"\n");
    }
	
	public HunmorphStub(String bin) {
		engine = init(bin);
	}
	
	public Analyzer createAnalyzer(Compounds compounds, Guess guess) {
		int comp = 0;
		switch(compounds) {
			case No:
				comp = 0;
				break;
			case Allow:
				comp = 1;
				break;
		}
		
		int gu = 0;
		switch(guess) {
			case NoGuess:
				gu = 0;
				break;
			case Fallback:
				gu = 1;
				break;
			case Global:
				gu = 2;
				break;
		}
		long id = make_analyzer(engine, 1/*blocking */, 0/*stop at first*/, comp, gu);
		return new Analyzer(id, this);
	}
	
    public  static  void main(String args[]) throws Exception {
        HunmorphStub stub = new HunmorphStub(args[0]); 
        Analyzer analyzer = stub.createAnalyzer(Compounds.Allow, Guess.Fallback);
		List<String> result ;
		String encoding = "ISO-8859-2";
		BufferedReader        input = new BufferedReader(new InputStreamReader(System.in, encoding));
		String line = null;
		while((line = input.readLine()) != null) {
		System.out.print(line);
		result = analyzer.analyze(line);
		for(String a : result) {
			System.out.print("\t" + a);
		}
		System.out.print("\n");
		}
		
    }

   public class Analyzer {
   
		long analyzer = 0;
		HunmorphStub stub  = null;
		private Analyzer(long analyzer, HunmorphStub stub) {
			this.analyzer = analyzer;
			this.stub = stub;
		}
	
		public List<String> analyze(String word) {
			result = new LinkedList<String>();
			try{
			stub.analyze(analyzer, word.getBytes("ISO-8859-2"));
			}catch(Exception e) {};
			return result;
		
		}
   }
   
}
