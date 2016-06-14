package hu.nytud.gate.morph;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.Resource;
import gate.Utils;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import gate.util.GateRuntimeException;
import gate.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/** 
 *  HFST Morphological Analyzer.
 *  Produces a list of alternative lemma with analyzations code pairs.
 *  Based on MagyarlancKRMorphAnalyzer
 *  @author Peter K
 */ 
@CreoleResource(name = "2. Morphological Analyzer (HU) [hfst-ol Native]", 
				comment = "Expects tokenized text. Outputs lemmas and analyzations. Calls hfst command line via I/O redirection"
				) // TODO icon?
public class HFSTMorphPipe extends AbstractLanguageAnalyser {


	private static final long serialVersionUID = 1L;
	
	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Override
	public Resource init() throws ResourceInstantiationException {
		try {
			// Get the path of this jar file
			File jarFile = new File(HFSTMorphPipe.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			String jarDir = jarFile.getParentFile().getPath();
			// Get OS name
			String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
			if (osName.contains("linux")) {
			    // Just leave the default value
			}
			else if (osName.contains("mac os") || osName.contains("macos") || osName.contains("darwin")) {
			    URL url = new File(jarDir, "resources/hfst/osx/hfst-optimized-lookup").toURI().toURL();
			    System.out.println("Mac OS detected, overriding HFST wrapper script name: " + url.toString());
				setHFSTBinary(url);
			}
			else if (osName.contains("windows")) {
			    URL url = new File(jarDir, "resources/hfst/win32/hfst-optimized-lookup.exe").toURI().toURL();
			    System.out.println("Windows detected, overriding HFST wrapper script name: " + url.toString());
			    setHFSTBinary(url);
			}
			else {
			    System.err.println("Warning: HFST binary is not supported on your operating system!");
			}
		} catch (Exception e) {
			throw new ResourceInstantiationException(e);
		}
		return this;
    }
	
	/** Requires token annotations (see baseTokenAnnotationType), 
	 *  adds morphological analyzes with feature key outputAnasFeatureName,
	 *  value is an ArrayList<String> of "$lemma[TAGS]" alternatives.
	 *  Based on code from gate.creole.POSTagger.execute()
	 */
	@Override
	public void execute() throws ExecutionException { 
	    
	    long startTime = System.currentTimeMillis();
		
		// sanity checks:
		if(document == null) throw new ExecutionException(
	    	      "No document to process!");

	    if(inputASName != null && inputASName.equals("")) inputASName = null;
	    AnnotationSet inputAS = (inputASName == null) ?
	                            document.getAnnotations() :
	                            document.getAnnotations(inputASName);
	                           
	    if(baseTokenAnnotationType == null || baseTokenAnnotationType.trim().length()==0) {
	        throw new ExecutionException("No base Token Annotation Type provided!");
	    }

	    if(outputASName != null && outputASName.equals("")) outputASName = null;
	            
	    if(outputAnnotationType == null || outputAnnotationType.trim().length()==0) {
	        throw new ExecutionException("No AnnotationType provided to store the new feature!");
	    }
	    
	    // get sentence and token annotations
	    AnnotationSet tokensAS = inputAS.get(baseTokenAnnotationType);
	    int tokenCnt = tokensAS.size();

	    if(tokensAS == null || tokensAS.size() == 0) {
	    	if(failOnMissingInputAnnotations) {
	    		throw new ExecutionException("No tokens to process in document " + document.getName() + "\n" +
	                                         "Please run a tokeniser first!");
	        } else {
	            Utils.logOnce(logger, Level.INFO, "HFST Morphological Analyzer: " +
	            								  "no token annotations in input document");
	            logger.debug("No input annotations in document "+document.getName());	        	
	        	return;
	        }
	    }
	    
	    // start the work:
	    fireStatusChanged("Analysing " + document.getName());
	    fireProgressChanged(0);
	    	    
        try {
    	    // initialize process
        	
        	File bin = new File(hfstBinary.toURI());
    	    if (!bin.exists()) throw new ExecutionException("Missing HFST binary!");
    	    
    	    File lex = new File(hfstLex.toURI());
    	    if (!lex.exists()) throw new ExecutionException("Missing HFST transducer!");

    	    ProcessBuilder pb = new ProcessBuilder(bin.getCanonicalPath(),lex.getCanonicalPath());
		    pb.directory(bin.getParentFile());
	        Process process = pb.start();

	        OutputStreamWriter os = new OutputStreamWriter(process.getOutputStream(),"UTF-8");
	        BufferedReader is = new BufferedReader(new InputStreamReader(process.getInputStream(),"UTF-8"));
	        
	        Iterator<Annotation> tokenIter = tokensAS.iterator(); int num = 0;
	        		
	        while (tokenIter.hasNext()) {

		        List<Pair> queue = new ArrayList<Pair>();
		        
		        for (int n=0; tokenIter.hasNext() && n < bufferCount; ++n) {
			    	Annotation currentToken = tokenIter.next();
			    	String tok = document.getContent().getContent(
						currentToken.getStartNode().getOffset(),
						currentToken.getEndNode().getOffset()
					).toString();
			    
			    	//logger.info("HFST input: "+tok);
			    	os.write(tok);
			    	os.write(System.getProperty("line.separator"));		    	
			        queue.add(new Pair(tok,currentToken));
		        }
		        os.flush();

		        Iterator<Pair> queueIter = queue.iterator();
			    
		        if (!queueIter.hasNext()) throw new ExecutionException("Called HFST without input!");
			    
		    	// receive all analyzation
		    	Collection<String> anas = new ArrayList<String>(); Pair queueItem = queueIter.next();
	
			    for (float t=0; t<timeout && !isInterrupted() && process.isAlive(); t+=0.00005) {
			    	if (!is.ready()) { Thread.sleep(0,50); continue; }
			    	t = 0; // reset timer
			    	String line = is.readLine();
			    	//logger.info("HFST output: "+line);
			    	if (line.isEmpty()) {
			    		if (!anas.isEmpty()) {			    	
				    		addFeatures((Annotation)queueItem.second, anas);
				    		if (!queueIter.hasNext()) break; // finished
				    		anas = new ArrayList<String>(); queueItem = queueIter.next();
					    	fireProgressChanged(++num * 100 / tokenCnt);
			    		}
			    		continue;
			    	}
			    	String[] l = line.split("\t");
			    	if (l.length != 2) throw new ExecutionException("HFST invalid line: "+line); 
			    	if (!l[0].equals((String)queueItem.first)) {
			    		logger.info("HFST Ignored line: "+(String)queueItem.first+" => "+line);
			    	} else anas.add(l[1]);
			    }
		    
		    	if (isInterrupted()) { logger.info("HFST interrupted"); break; }
		    	if (!process.isAlive()) { logger.info("HFST process stopped"); break; }
	        }
	        
	        process.destroy();

		} catch (Exception e) {
		    	throw new ExecutionException(e);
		}
        
	    // Finished
	    fireProcessFinished();
	    fireStatusChanged(document.getName() + " analyzed in " +
	    	    NumberFormat.getInstance().format(
	            (double)(System.currentTimeMillis() - startTime) / 1000) +
	            " seconds!");
	}
	
	/**
	 * Add lemma+MSD code pairs to a token annotation.
	 * Code from gate.creole.POSTagger.addFeatures()
	 */
	private void addFeatures(Annotation annot, Collection<String> anas) throws GateRuntimeException {
		// TODO: assert that anas is of type ArrayList<String>?
			    
 	  	String tempIASN = inputASName == null ? "" : inputASName;
	    String tempOASN = outputASName == null ? "" : outputASName;

	    if(outputAnnotationType.equals(baseTokenAnnotationType) && tempIASN.equals(tempOASN)) {
	    	annot.getFeatures().put(outputAnasFeatureName, anas);
	        return;
	    } else {
	    	int start = annot.getStartNode().getOffset().intValue();
	        int end = annot.getEndNode().getOffset().intValue();
	        // get the annotations of type outputAnnotationType
	        AnnotationSet outputAS = (outputASName == null) ?
	        		document.getAnnotations() :
	                document.getAnnotations(outputASName);
	        AnnotationSet annotations = outputAS.get(outputAnnotationType);
	        if(annotations == null || annotations.size() == 0) {
	        	// add new annotation
	            FeatureMap features = Factory.newFeatureMap();
	            features.put(outputAnasFeatureName, anas);
	            try {
	            	outputAS.add(new Long(start), new Long(end), outputAnnotationType, features);
	            } catch(Exception e) {
	                throw new GateRuntimeException("Invalid Offsets");
	            }
	        } else {
	        	// search for the annotation if there is one with the same start and end offsets
	            List<Annotation> tempList = new ArrayList<Annotation>(annotations.get());
	            boolean found = false;
	            for (int i=0; i<tempList.size(); i++) {
	            	Annotation annotation = tempList.get(i);
	                if (annotation.getStartNode().getOffset().intValue() == start 
	                    && annotation.getEndNode().getOffset().intValue() == end) {
	                	// this is the one
	        	        annot.getFeatures().put(outputAnasFeatureName, anas);
                        found = true;
	                    break;
	                }
	            }
	            if(!found) {
	            	// add new annotation
	                FeatureMap features = Factory.newFeatureMap();
		            features.put(outputAnasFeatureName, anas);
	                try {
	                	outputAS.add(new Long(start), new Long(end), outputAnnotationType, features);
	                } catch(Exception e) {
	                    throw new GateRuntimeException("Invalid Offsets");
	                }
	            }
	        }
	    }
	    
	}

	@CreoleParameter(
			comment="Path to the HFST executable",
			defaultValue = "resources/hfst/linux32/hfst-optimized-lookup") 
	public void setHFSTBinary(URL url) { 
	    hfstBinary = url; 
	}
	public URL getHFSTBinary() { 
		return hfstBinary; 
	}
	private URL hfstBinary;	
	
	@CreoleParameter(
			comment="Path to the HFST transducer",
			defaultValue = "resources/hfst/hu.hfstol")
	public void setHFSTLex(URL url) {
		hfstLex = url;
	}
	public URL getHFSTLex() { 
		return hfstLex; 
	}
	private URL hfstLex;	
	
	@RunTime
	@CreoleParameter(
			comment="Maximum number of items sent at the same time",
			defaultValue = "500")
	public void setBufferCount(Integer num) {
		bufferCount = num;
	}
	public Integer getBufferCount() { 
		return bufferCount; 
	}
	private Integer bufferCount;	

	@RunTime
	@CreoleParameter(
			comment="Timeout for receiving data (milliseconds)",
			defaultValue = "3000")
	public void setTimeout(Integer num) {
		timeout = num;
	}
	public Integer getTimeout() { 
		return timeout; 
	}
	private Integer timeout;	

	@RunTime
	@Optional
	@CreoleParameter(comment="The annotation set to be used as input that must contain 'Token' and 'Sentence' annotations")
	public void setInputASName(String newInputASName) {
	    inputASName = newInputASName;
	}
	public String getInputASName() {
		return inputASName;
	}
	private String inputASName;	
	
	@Optional 
	@RunTime 
	@CreoleParameter( 
			comment = "The annotation set to be used as output")
	public void setOutputASName(String setName) { 
		this.outputASName = setName; 
	} 
	public String getOutputASName() { 
		return outputASName; 
	}
	private String outputASName;
	
	@RunTime
	@CreoleParameter(
			comment="The name of the base 'Token' annotation type",
			defaultValue="Token")
	public void setBaseTokenAnnotationType(String baseTokenAnnotationType) {
		this.baseTokenAnnotationType = baseTokenAnnotationType;
	}
	public String getBaseTokenAnnotationType() {
		return this.baseTokenAnnotationType;
	}
	private String baseTokenAnnotationType;
	  	  
	@RunTime
	@CreoleParameter(
			comment="The name of the annotation type where the new features should be added",
			defaultValue="Token")
	public void setOutputAnnotationType(String outputAnnotationType) {
		this.outputAnnotationType = outputAnnotationType;
	}
	public String getOutputAnnotationType() {
		return this.outputAnnotationType;
	}
	private String outputAnnotationType;
		  	
	@RunTime
	@CreoleParameter(
			comment = "The name of the feature that will hold the morphological analysis strings on output annotation types (tokens)",
			defaultValue = "anas")  
	public void setOutputAnasFeatureName(String newOutputAnasFeatureName) {
		outputAnasFeatureName = newOutputAnasFeatureName;
	}
	public String getOutputAnasFeatureName() {
	    return outputAnasFeatureName;
	}
	protected String outputAnasFeatureName;

	@RunTime
	@Optional
	@CreoleParameter(
			comment = "Throw an exception when there are none of the required input annotations",
			defaultValue = "true")  
	public void setFailOnMissingInputAnnotations(Boolean fail) {
		failOnMissingInputAnnotations = fail;
	}
	public Boolean getFailOnMissingInputAnnotations() {
		return failOnMissingInputAnnotations;
	}
	protected Boolean failOnMissingInputAnnotations = true;

}
