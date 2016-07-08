package hu.nytud.hfst;

import hu.nytud.hfst.Analyzer.Analyzation;
import hu.nytud.hfst.Stemmer.Stem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Main {

	private Analyzer analyzer;
	private Stemmer stemmer;
	
	public static class Result {
		String anas, lemma, tags;
		public String toString() {
			return anas + "\t" + lemma + "\t" + tags;
		}
	}
	
	public Main(String config) {
		
		try {
			File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			
			if ("bin".equals(jarFile.getName())) { // workaround for running without jar
				jarFile = jarFile.getParentFile();
			}
			
			File root = jarFile.getParentFile();

			FileInputStream is = new FileInputStream(new File(root,config));
			Properties props = new Properties();
			props.load(is);

			analyzer = new Analyzer(root, props);
			stemmer  = new Stemmer(props);

		} catch (FileNotFoundException e) {
			System.err.println("Error: configuration file not found - " + config );
		} catch (IOException e) {
			System.err.println("Error: could not parse configuration file");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void run(List<String> input) {

		Analyzer.Worker w = analyzer.process(input);

		for (String word : input) {
			List<Result> res = new ArrayList<>();
			List<Result> res_bu = new ArrayList<>();
			
			List<Analyzation> anas = w.getResult();
			if (anas==null) System.err.println("timeout for word: "+ word);
			else for (Analyzation ana: anas) {
				Result res1 = new Result();
				res1.anas  = ana.formatted;
				Stem stem  = stemmer.process(ana);
				res1.lemma = stem.szStem;
				res1.tags  = stem.getTags(false);
				if (stem.bIncorrectWord) res_bu.add(res1); else res.add(res1);
			}
			
			if (res.isEmpty()) System.out.println(word + "\t<unknown>");
			for (Result res1 : res) {
				System.out.println(word + "\t" + res1.toString());
			}
			System.out.println("");
		}

	}

	public static void main(String[] args) {
		
		Main m = new Main(args.length > 0 ? args[0] : "hfst-wrapper.props");

		if (args.length > 1) {
			List<String> input = new ArrayList<>();
			for (int i=1; i < args.length; ++i) {
				input.add(args[i]);
			}
			m.run(input);
			return;
		}

		try {
			BufferedReader is = new BufferedReader(new InputStreamReader(System.in,"UTF-8"));
			while (true) {
				List<String> input = new ArrayList<>();
				String line = is.readLine();
				while (line != null && is.ready()) {
					input.add(line);
					line = is.readLine();
				}
				if (line != null) input.add(line);
				if (!input.isEmpty()) m.run(input);
				if (line == null) { // stdin closed
					return;
				}
			}
		} catch (IOException e) {}
	}

}
