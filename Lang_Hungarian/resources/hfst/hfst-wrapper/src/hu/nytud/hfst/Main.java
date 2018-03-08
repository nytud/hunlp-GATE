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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Main {

	private Analyzer analyzer;
	private Stemmer stemmer;

	public static class Result {

		String anas, lemma, tags;

		@Override
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

			InputStreamReader is = new InputStreamReader(new FileInputStream(new File(root, config)), "UTF-8");
			Properties props = new Properties();
			props.load(is);

			analyzer = new Analyzer(root, props);
			stemmer = new Stemmer(props);

		} catch (FileNotFoundException e) {
			System.err.println("Error: configuration file not found - " + config);
		} catch (IOException e) {
			System.err.println("Error: could not parse configuration file");
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

	}

	public void run(List<String> input, String mode) {
		if ("hfst".equals(mode)) {
			for (String pairs : input) {
				String[] p = pairs.split("\t");
				if (p.length < 2) {
					System.out.println(pairs);
				} else if (p[1].endsWith("+?")) {
					System.out.println(p[0] + "\t<unknown>");
				} else {
					Analyzation ana = analyzer.new Analyzation(p[1]);
					Stem stem = stemmer.process(ana.formatted);
					System.out.println(p[0] + "\t" + ana.formatted + "\t" + stem.szStem + "\t" + stem.getTags(false));
				}
			}
			return;
		}

		if ("stem".equals(mode)) {
			for (String anas : input) {
				Stem stem = stemmer.process(anas);
				System.out.println(anas + "\t" + stem.szStem + "\t" + stem.getTags(false));
			}
			return;
		}

		Analyzer.Worker w = analyzer.process(input);

		for (String word : input) {
			List<Result> res = new ArrayList<>();
			//List<Result> res_bu = new ArrayList<>();

			List<Analyzation> anas = w.getResult();
			if (anas == null) {
				System.err.println("timeout for word: " + word);
			} else {
				for (Analyzation ana : anas) {
					Result res1 = new Result();
					res1.anas = ana.formatted;
					Stem stem = stemmer.process(ana.formatted);
					res1.lemma = stem.szStem;
					res1.tags = stem.getTags(false);
					if (stem.bIncorrectWord) {
						//res_bu.add(res1);
					} else {
						res.add(res1);
					}
				}
			}

			if (res.isEmpty()) {
				System.out.println(word + "\t<unknown>");
			}
			for (Result res1 : res) {
				System.out.println(word + "\t" + res1.toString());
			}
			System.out.println("");
		}

	}

	public static void main(String[] args) {

		Map<String, String> params = new HashMap<>();

		List<String> input = new ArrayList<>();
		for (int i = 0; i < args.length; ++i) {
			if (args[i].startsWith("--")) {
				String[] p = args[i].substring(2).split("=");
				params.put(p[0], p.length > 1 ? p[1] : "");
			} else {
				input.add(args[i]);
			}
		}

		Main m = new Main(params.containsKey("config") ? params.get("config") : "hfst-wrapper.props");

		if (!input.isEmpty()) {
			m.run(input, params.get("mode"));
			return;
		}

		try {
			BufferedReader is = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
			while (true) {
				input = new ArrayList<>();
				String line = is.readLine();
				while (line != null && is.ready()) {
					input.add(line);
					line = is.readLine();
				}
				if (line != null) {
					input.add(line);
				}
				if (!input.isEmpty()) {
					m.run(input, params.get("mode"));
				}
				if (line == null) { // stdin closed
					return;
				}
			}
		} catch (IOException e) {
		}
	}

}
