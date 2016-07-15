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
	private static List<WorkerProcess> myWorkers = new ArrayList<>();
	private int maxProcess, initTimeout, processTimeout;
	
	public Analyzer(File root, Properties props) {

		cmdline = " " + props.getProperty("analyzer.params","");
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
		
    public Worker getWorker() {
    	try {
    		synchronized (myWorkers) {
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
			}
    	} catch (Exception e) {
    		System.err.println("Exception in getProcess():");
    		e.printStackTrace();
    	}

    	return null;
    }
    
    private class WorkerProcess implements Runnable {
    	private OutputStreamWriter stdin;
    	private ConcurrentLinkedQueue<String> iQueue, wQueue;
    	private BufferedReader stdout, stderr;
    	public boolean in_use = false;
    	private boolean initialized = false;
    	private int error_count = 0;
    	private Process process = null;
    	
    	public WorkerProcess() {
    		iQueue = new ConcurrentLinkedQueue<>();
    		wQueue = new ConcurrentLinkedQueue<>();
    		reload();
    	}
    	
    	synchronized public void reload() {
    		try {
	    		initialized = false;
    			wQueue.clear();
    			if (process != null) process.destroy();
    			process = Runtime.getRuntime().exec(cmdline,null,hfst.getParentFile());
    			stdin = new OutputStreamWriter(process.getOutputStream(),"UTF-8");
    			stdout = new BufferedReader(new InputStreamReader(process.getInputStream(),"UTF-8"));
    			stderr = new BufferedReader(new InputStreamReader(process.getErrorStream(),"UTF-8"));
    			write("");
	    		wQueue.addAll(iQueue);
	    		for (int t=initTimeout/60; t>0; --t) { 
	            	if (stdout.ready() || stderr.ready()) {
	            		read();
	            		initialized = true;
	            		break;
	            	}
	            	Thread.sleep(60);
	            }
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    	
    	public void add(String word) {
    		iQueue.add(word);
    		wQueue.add(word);
    	}    	
    	
    	public String[] read() {
    		String line = null;
    		try {
    			line = stdout.readLine();
        		while (stderr.ready()) try {
    					String err = stderr.readLine();
        			if (err != null) System.err.println("Error in HFST: " + err);
        		} catch (IOException e) {}
    		} catch (IOException e) {
    			System.err.println("IO Exception");
    		}
    		if (line != null) {
    			String[] l = line.split("\t");
    			return l.length > 1 ? l : new String[]{l[0],""};
    		}
    		if (!initialized || ++error_count > 2) return null;
			reload();
			return read();
    	}
    	
    	public String next() {
    		return iQueue.poll();
    	}

    	public boolean hasNext() {
    		return !iQueue.isEmpty();
    	}
    	
    	@Override
    	public void run() {
    		while (this.in_use) {
    			try {
    				if (wQueue.isEmpty()) { Thread.sleep(1); continue; }
    				write(wQueue.poll());
    			} catch (Exception e) {
    				e.printStackTrace();
    				reload();
    			}
    		}
    	}
    	
    	private void write(String word) throws IOException {
	    	stdin.write(word);
			stdin.write(LINE_SEP);
			stdin.flush();
    	}
    }
        
    public class Worker extends Thread {
    	
    	private WorkerProcess worker;
    	
    	private LinkedBlockingQueue<List<Analyzation>> result;
    	    	
    	protected Worker(WorkerProcess worker) {
    		result = new LinkedBlockingQueue<>();
    		worker.in_use = true;
    		this.worker = worker;
    	}

    	public Worker addWord(String word) {
    		if (!this.isAlive()) this.start();
    		worker.add(word.trim());
    		return this;
    	}
    	    	
    	public List<Analyzation> getResult() {
	    	try {
	            for (int t=processTimeout/5; t>0; --t) {
	            	if (result.size() > 0) return result.take();
	            	Thread.sleep(5);
	            }
	        } catch (InterruptedException e) {
	        }
	    	worker.next();
	    	worker.reload();
            return null;
    	}
    	
    	@Override
    	public void run() {
    		List<Analyzation> anas = new ArrayList<>();
			String last_word = "";
			
			Thread feeder = new Thread(worker);
			feeder.start();
			while (!isInterrupted() && worker.hasNext()) {
	    		String[] line = worker.read();
	    		if (line == null || (line[0].isEmpty() || !last_word.equals(line[0])) && (!anas.isEmpty() || line[1].endsWith("+?"))) {
 					if (worker.next() == null) for (Analyzation ana:anas) {
		    			System.err.println("Warning: Unmatched Analyzation: "+ana.formatted);
		    		} else {
		    			result.add(anas);
		    			anas = new ArrayList<>();
		    		}
 				}
				if (line != null && !"".equals(line[0]) && !line[1].endsWith("+?")) try {
					last_word = line[0];
 					anas.add(new Analyzation(line[1]));
				} catch (Exception e) {
					System.err.println("Exception in Analyzation: ");
					e.printStackTrace();
				}
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
