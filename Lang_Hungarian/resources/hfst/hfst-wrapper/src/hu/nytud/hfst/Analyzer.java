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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

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
	private List<WorkerProcess> myWorkers;
	private int maxProcess, initTimeout, processTimeout;
	
	public Analyzer(File root, Properties props) {

		cmdline = " " + props.getProperty("analyzer.params","");
		myWorkers = new ArrayList<WorkerProcess>();
		maxProcess =  Integer.parseInt(props.getProperty("analyzer.max_count","5"));
		initTimeout = Integer.parseInt(props.getProperty("analyzer.init_timeout_ms","60000"));
		processTimeout = Integer.parseInt(props.getProperty("analyzer.process_timeout_ms","5000"));

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

	private boolean interrupted = false;
		
    synchronized public Worker getWorker() {
    	try {
	    	for (float t=0; t<initTimeout && !interrupted; t+=50) {
		    	for(Iterator<WorkerProcess> it = myWorkers.iterator(); it.hasNext(); ) {
		    		WorkerProcess p = it.next();
		    		if (!p.in_use) {
		    			return new Worker(p);
		    		}
		    	}
		    	if (myWorkers.size() < maxProcess) { 
		    		WorkerProcess p = new WorkerProcess();
		    		myWorkers.add(p);
		    		return new Worker(p);
		    	}
				Thread.sleep(50);
	    	}
    	} catch (Exception e) {
    		System.err.println("Exception in getProcess():");
    		e.printStackTrace();
    	}

    	return null;
    }
    
    private class WorkerProcess implements Runnable {
    	private OutputStreamWriter stdin;
    	private ConcurrentLinkedQueue<String> wQueue;
    	private BufferedReader stdout, stderr;
    	public boolean in_use = false;
    	private Process process = null;
    	
    	public WorkerProcess() {
    		wQueue = new ConcurrentLinkedQueue<>();
    		reload();
    	}
    	
    	public void reload() {
    		try {
    			if (process != null) process.destroy();
    			process = Runtime.getRuntime().exec(cmdline,null,hfst.getParentFile());
    			stdin = new OutputStreamWriter(process.getOutputStream(),"UTF-8");
    			stdout = new BufferedReader(new InputStreamReader(process.getInputStream(),"UTF-8"));
    			stderr = new BufferedReader(new InputStreamReader(process.getErrorStream(),"UTF-8"));
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    	
    	public void write(String word) throws IOException {
    		wQueue.add(word);
    	}    	
    	
    	public String read() throws IOException {
    		String line = stdout.readLine();
    		while (stderr.ready()) try {
					String err = stderr.readLine();
    			if (err != null) System.err.println("Error in HFST: " + err);
    		} catch (IOException e) {}
    		return line;
    	}

    	@Override
    	public void run() {
    		while (this.in_use) {
    			try {
    				if (wQueue.isEmpty()) { Thread.sleep(1); continue; }
    				stdin.write(wQueue.poll());
    	    		stdin.write(LINE_SEP);
    	    		stdin.flush();
    			} catch (Exception e) {
    				e.printStackTrace();
    				reload();
    			}
    		}
    	}
    }
        
    public class Worker extends Thread {
    	
    	private WorkerProcess worker;
    	private boolean initialized = false;
    	
    	private ConcurrentLinkedQueue<String> queue;
    	private LinkedBlockingQueue<List<Analyzation>> result;
    	private AtomicInteger skipped;
    	    	
    	protected Worker(WorkerProcess worker) {
    		queue = new ConcurrentLinkedQueue<>();
    		result = new LinkedBlockingQueue<>();
    		skipped = new AtomicInteger(0);
    		worker.in_use = true;
    		this.worker = worker;
    		init();
    	}

    	private void init() {
    		initialized = false;
    		try {
    			worker.write("");
	    		skipped.set(1);
		    	for (String q : queue) worker.write(q);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
 
    	public Worker addWord(String word) {
    		word = word.trim();
    		queue.add(word);
    		if (!this.isAlive()) this.start();
    		try {
    			worker.write(word);
    		} catch (IOException e) {
    			worker.reload();
    			init();
    		}
    		return this;
    	}
    	    	
    	public List<Analyzation> getResult() {
	    	try {
	            for (int t=initTimeout/60; t>0; --t) { 
	            	if (initialized || !isAlive()) break;
	            	Thread.sleep(60);
	            }
	            for (int t=processTimeout/5; t>0; --t) {
	            	if (result.size() > 0) return result.take();
	            	Thread.sleep(5);
	            }
	        } catch (InterruptedException e) {
	        }
	    	skipped.incrementAndGet();
	    	queue.poll();
            return null;
    	}
    	
    	@Override
    	public void run() {
    		List<Analyzation> anas = new ArrayList<>();
			int error_count = 0; String last_word = "";
			
			Thread feeder = new Thread(worker);
			feeder.start();
			while (!isInterrupted() && !queue.isEmpty()) try { 
	    		String line = worker.read();
	    		if (line == null) {
 					if (!initialized || isInterrupted()) break;
 					++error_count;
 					throw new Exception("closed stdout");
 				}
	    		initialized = true;
 				error_count = 0;
 				String[] l = line.split("\t");
 				if (line.isEmpty() || !last_word.equals(l[0]) && !anas.isEmpty()) {
 					if (skipped.get() > 0) { // handle the "is it ready" query
 	 					skipped.decrementAndGet();
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
			} catch (InterruptedException e) {
				break;
    		} catch (Exception e) {
    			System.err.println("Exception in MyProcess.run():");
    			e.printStackTrace();
    			if (!initialized || error_count > 2) break; // could not repair
    			worker.reload();
    			init();
    		}
			
			worker.in_use = false;
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
