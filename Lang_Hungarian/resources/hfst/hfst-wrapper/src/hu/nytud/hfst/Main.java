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
	
	public List<Result> run(String input) {
		List<Result> res = new ArrayList<>();
		List<Result> res_bu = new ArrayList<>();
		
		List<Analyzation> anas = analyzer.process(input);
		if (anas != null) for (Analyzation ana: anas) {
			Result res1 = new Result();
			res1.anas = ana.formatted;
			Stem stem = stemmer.process(ana);
			res1.lemma = stem.szStem;
			res1.tags  = stem.getTags(false);
			if (stem.bIncorrectWord) res_bu.add(res1); else res.add(res1);
		}
		
		return res.isEmpty() ? res_bu : res;
	}
	
	public void dump(String input) {
		List<Result> res = run(input);
		
		if (res.isEmpty()) System.out.println("\t <unknown>");
		for (Result res1: res) {
			System.out.println("\t" + res1.toString());
		}
	}

	public static void main(String[] args) {
		
		Main m = new Main(args.length > 0 ? args[0] : "hfst-wrapper.props");

		if (args.length > 1) {
			m.dump(args[1]);
			System.exit(0);
			return;
		}
		
		try {
			BufferedReader is = new BufferedReader(new InputStreamReader(System.in,"UTF-8"));
			while (true) {
				String line = is.readLine();
				m.dump(line);
			}
		} catch (IOException e) {}
	}

}
