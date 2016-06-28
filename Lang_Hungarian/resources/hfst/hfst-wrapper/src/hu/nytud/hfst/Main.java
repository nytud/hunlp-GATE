package hu.nytud.hfst;

import hu.nytud.hfst.Analyzer.Analyzation;
import hu.nytud.hfst.Stemmer.Stem;

import java.io.BufferedReader;
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
		Properties props = new Properties();
		try {
			FileInputStream is = new FileInputStream(config);
			props.load(is);
		} catch (FileNotFoundException e) {
			System.err.println("Error: configuration file not found");
		} catch (IOException e) {
			System.err.println("Error: could not parse configuration file - "+e.getMessage());
		}
		
		analyzer = new Analyzer(props);
		stemmer  = new Stemmer(props);
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
		
		
		if (args.length < 1)
		{
			System.err.println("Usage: java -jar hfst-wrapper.jar <config> [word]");
			return;
		}

		Main m = new Main(args[0]);

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
