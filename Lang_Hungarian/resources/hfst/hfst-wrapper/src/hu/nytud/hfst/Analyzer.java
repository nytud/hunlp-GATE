package hu.nytud.hfst;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Analyzer {

	public static class Morpheme {
		public String lexical, surface, tag;
		
		public Morpheme() { lexical = surface = tag = ""; }
		
		public String toString() {
			return lexical + "[" + tag + "]" + (!surface.isEmpty() ? "=" + surface : "");
		}		
	}
	
	public class Analyzation {
		public List<Morpheme> morphs;
		public String formatted;
		
		protected Analyzation(String pairs) { 
			morphs = parse(pairs);
			formatted = format();
		}
		
		protected String format() {
			String res = "";
			for (Morpheme m : morphs) { res += "+" + m.toString(); }
			return res.substring(1);
		}
	}
	
	private static final String LINE_SEP;
	
	static {
		LINE_SEP = System.getProperty("line.separator");
	}

	private File hfst; 
	private String cmdline;
	private List<Worker> myProcesses;
	private int max_process, timeout;
	
	public Analyzer(File root, Properties props) {

		cmdline = " " + props.getProperty("analyzer.params","");
		myProcesses = new ArrayList<Worker>();
		max_process =  Integer.parseInt(props.getProperty("analyzer.max_count","5"));
		timeout = Integer.parseInt(props.getProperty("analyzer.timeout_ms","60000"));

		// Get OS name
		String binary = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);

		if (binary.contains("windows")) {
			binary = props.getProperty("analyzer.win32");
		} else if (binary.contains("mac os") || binary.contains("macos") || binary.contains("darwin")) {
			binary = props.getProperty("analyzer.osx");
		} else if (binary.contains("linux")) { 
			binary = props.getProperty("analyzer.linux");
		} else {
		    System.err.println("Warning: HFST binary is not supported on your operating system!");
		    return;
		}

		try {
			hfst = new File(root.getPath(), binary);
			
			if (!hfst.exists()) {
				cmdline = "";
				System.err.println("Warning: HFST binary is not found! ("+hfst.getCanonicalPath()+")");
			} else {
				cmdline = hfst.getCanonicalPath() + cmdline;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<Analyzation> process(String input) {
		return getWorker().addWord(input).getResult();
	}

	public Worker process(List<String> input) {
		Worker w = getWorker();
		for (String word : input) w.addWord(word);
		return w;
	}

	public void interrupt() { interrupted = true; }
	private boolean interrupted = false;
		
    synchronized public Worker getWorker() {
    	try {
	    	for (float t=0; t<timeout && !interrupted; t+=50) {
		    	for(Iterator<Worker> it = myProcesses.iterator(); it.hasNext(); ) {
		    		Worker p = it.next();
		    		if (!p.isAlive()) {
		    			it.remove();
		    		} else if (!p.in_use()) {
		    			p.reserve();
		    			return p;
		    		}
		    	}
		    	if (myProcesses.size() < max_process) { 
		    		Worker p = new Worker();
		    		p.reserve();
		    		myProcesses.add(p);
					return p;
		    	}
				Thread.sleep(50);
	    	}
    	} catch (Exception e) {
    		System.err.println("Exception in getProcess():");
    		e.printStackTrace();
    	}

    	return null;
    }
    
    
    public class Worker extends Thread {
    	private OutputStreamWriter os;
    	private BufferedReader is, es;
    	private Process process;
    	
    	private boolean initialized = false;
    	private boolean reserved = false;
    	
    	private ConcurrentLinkedQueue<String> queue;
    	private LinkedBlockingQueue<List<Analyzation>> result;
    	    	
    	public Worker() {
    		queue = new ConcurrentLinkedQueue<>();
    		result = new LinkedBlockingQueue<>();
    		init();
    	}
    	
    	private void init() {
    		process = null;
    		initialized = false;
    		try {
    			process = Runtime.getRuntime().exec(cmdline,null,hfst.getParentFile());
	    		os = new OutputStreamWriter(process.getOutputStream(),"UTF-8");
	    		is = new BufferedReader(new InputStreamReader(process.getInputStream(),"UTF-8"));
	    		es = new BufferedReader(new InputStreamReader(process.getErrorStream(),"UTF-8"));
	    		
	    		if (!this.isAlive()) this.start();
	    		
	    		os.write(LINE_SEP); // send an "is it ready" query
		    	for (String q : queue) {
					os.write(q);
			    	os.write(LINE_SEP);
				}
	    		os.flush();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    	
    	public void reserve() {
    		reserved = true;
    	}
    	
    	public boolean in_use() {
    		return !reserved && !queue.isEmpty();
    	}
    	
    	public Worker addWord(String word) {
    		word = word.trim();
    		queue.add(word);
    		reserved = false;
    		try {
	    		os.write(word);
		    	os.write(LINE_SEP);
		    	os.flush();
    		} catch (IOException e) {
    			process.destroy();
    			init();
    		}
    		return this;
    	}
    	
    	protected class Poll implements Callable<List<Analyzation>> {
    		@Override
    		public List<Analyzation> call() throws Exception {
    		      return result.take();
    		}
    	}
    	
    	public List<Analyzation> getResult() {
	    	ExecutorService executor = Executors.newSingleThreadExecutor();
	        Future<List<Analyzation>> future = executor.submit(new Poll());

	        try {
	            for (int t=0; t<1000; ++t) { 
	            	if (initialized || !isAlive()) break;
	            	Thread.sleep(60);
	            }
	        	return future.get(timeout, TimeUnit.MILLISECONDS);
	        } catch (TimeoutException e) {
	            future.cancel(true);
	            process.destroy(); // something went wrong
	        } catch (InterruptedException e) {
	        } catch (ExecutionException e) {
	        	System.err.println("Exception in getResult(): could not run task");
	        }
            queue.poll();
            return new ArrayList<>();
    	}
    	
    	@Override
    	public void run() {
    		List<Analyzation> anas = new ArrayList<>();
			int error_count = 0; String last_word = "";
			while (!isInterrupted()) try { 
	    		String line = is.readLine();
	    		while (es.ready()) try {
 					String err = es.readLine();
	    			if (err != null) System.err.println("Error in HFST: " + err);
	    		} catch (IOException e) {}
 				if (line == null) {
 					if (!initialized) break;
 					++error_count;
 					throw new Exception("closed stdout");
 				}
 				error_count = 0;
 				String[] l = line.split("\t");
 				if (line.isEmpty() || !last_word.equals(l[0]) && !anas.isEmpty()) {
 					if (!initialized) { // handle the "is it ready" query
 	 					initialized = true;
 					} else if (queue.poll() == null) for (Analyzation ana:anas) {
		    			System.err.println("Warning: Unmatched Analyzation: "+ana.formatted);
		    		} else {
		    			result.add(anas);
		    			anas = new ArrayList<>();
		    		}
 				}
				if (l.length > 1 && !l[1].endsWith("+?")) try {
					last_word = l[0];
 					anas.add(new Analyzation(l[1]));
				} catch (Exception e) {
					System.err.println("Exception in Analyzation: ");
					e.printStackTrace();
				}
    		} catch (Exception e) {
    			System.err.println("Exception in MyProcess.run():");
    			e.printStackTrace();
    			if (!initialized || error_count > 2) break; // could not repair
    			process.destroy();
    			init();
    		}
			
			process.destroy();
    	}
    }
    
    
	List<Morpheme> parse(String input) {
		List<Morpheme> items = new ArrayList<>();
		
		Morpheme item = new Morpheme();
		int state = 0;
		for (char ch : input.toCharArray()) switch (state) {
			case 0:
			case 2:
				switch (ch) {
					case ':': // switch sides
						state += 1;
						break;
					case ' ': // spaces are part of the surface
						item.surface += ch;
						break;
					default:
						if (!item.tag.isEmpty()) {
							items.add(item);
							item = new Morpheme();
						}
						item.surface += ch;
				}
				break;
			case 1:
				switch (ch) {
				case '[': // tag opening
					state = 3;
					if (!item.tag.isEmpty()) {
						items.add(item);
						item = new Morpheme();
					}
					break;
				case ' ': // beginning of next pair
					state = 0;
					break;
				default:
					item.lexical += ch;
				}
				break;
			case 3:
				switch (ch) {
				case ']': // tag closing
					state = 1; 
					break;
				case ' ': // beginning of next pair (remember we are inside a tag)
					state = 2;
					break;
				default:
					item.tag += ch;
				}
				break;
		}

		if (!item.tag.isEmpty() || !item.lexical.isEmpty() || !item.surface.isEmpty()) {
			items.add(item);
		}

		return items;
	}
    
}
