package hu.nytud.gate.othertaggers;

import gate.Annotation;
import gate.AnnotationSet;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * NP chunking with the huntag3 tagger.
 */
@CreoleResource(name = "4. NP Chunker (HU) [HunTag3 Native MSD]", 
				comment = "Expects tokenized, msd-tagged text. Outputs BIOE1-* codes."
						+ "Calls huntag3 command line through Process with pipes") 
public class Huntag3NPChunkerPipe extends AbstractLanguageAnalyser {

	private static final long serialVersionUID = 1L;

	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	protected static Process huntag = null;
	protected static OutputStreamWriter ht_os = null;
	protected static BufferedReader ht_is = null, ht_es = null;
	protected static Object lock = new Object();


	@Override
	public Resource init() throws ResourceInstantiationException {
		try {
			// Get the path of this jar file
			File jarFile = new File(Huntag3NERPipe.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			String jarDir = jarFile.getParentFile().getPath();
			// Get OS name
			String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
			if (osName.contains("windows")) {
			    URL url = new File(jarDir, "resources/huntag3/run_huntag.cmd").toURI().toURL();
			    System.out.println("Windows detected, overriding HFST wrapper script name: " + url.toString());
			    setHunTagBinary(url);
			}
		} catch (Exception e) {
			throw new ResourceInstantiationException(e);
		}
		return this;
    }

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
	    
	    // Get sentence annotations
	    List<Annotation> sentences = gate.Utils.inDocumentOrder(inputAS.get(inputSentenceType));
	    if (sentences.size() == 0) {
	    	if(failOnMissingInputAnnotations) {
	    		throw new ExecutionException("No sentences to process in document " + document.getName() + "\n" +
	                                         "Please run a sentence splitter & tokenizer first!");
	        } else {
	            Utils.logOnce(logger, Level.INFO, "Magyarlánc Dependency Parser: " +
	            								  "no sentence annotations in input document");
	            logger.debug("No input sentence annotations in document " + document.getName());	        	
	        	return;
	        }	    	
	    }

	    // start the work:
	    fireStatusChanged("Analysing " + document.getName());
	    fireProgressChanged(0);

	    int sentencesDone = 0;
	    for (Annotation sentence : sentences) {
	    	// parse sentence:
	    	parseSentence(inputAS, sentence, sentencesDone); 	    	
	        
	    	//checkInterruption();
	        fireProgressChanged(++sentencesDone * 100 / sentences.size());
        }
	    
	    // Finished
	    fireProcessFinished();
	    fireStatusChanged(document.getName() + " analyzed in " +
	    	    NumberFormat.getInstance().format(
	            (double)(System.currentTimeMillis() - startTime) / 1000) +
	            " seconds!");
	}
	    
	private void parseSentence(AnnotationSet annotationSet, Annotation sentence, int nsent) throws GateRuntimeException {
	    	
		long sentenceStartOffset = sentence.getStartNode().getOffset();
        long sentenceEndOffset   = sentence.getEndNode().getOffset();
        List<Annotation> tokens = Utils.inDocumentOrder(annotationSet.getContained(sentenceStartOffset, sentenceEndOffset).get(baseTokenAnnotationType));
        if (tokens.size() == 0)
        	throw new GateRuntimeException("No tokens found in sentence " + nsent);

        try {
        	synchronized (lock) { 
	        	// initialize process	    
		    	if (huntag == null || !huntag.isAlive()) {
		        	File bin = new File(huntagBinary.toURI());
		    	    if (!bin.exists()) throw new ExecutionException("Missing HunTag binary!");
		    		ProcessBuilder pb = new ProcessBuilder(bin.getCanonicalPath(),"NP");
		    		pb.directory(bin.getParentFile());
		    		if (huntag != null) huntag.destroy();
		    		huntag = pb.start();
	
			        ht_os = new OutputStreamWriter(huntag.getOutputStream(),"UTF-8");
			        ht_is = new BufferedReader(new InputStreamReader(huntag.getInputStream(),"UTF-8"));
			        ht_es = new BufferedReader(new InputStreamReader(huntag.getErrorStream(),"UTF-8"));
		    	}
	    	    	
		    	for (Annotation tok : tokens) {
		    		FeatureMap feats = tok.getFeatures();
	    	    	
		        	String input = document.getContent().getContent(
		        		tok.getStartNode().getOffset(),
		        		tok.getEndNode().getOffset()
					).toString();
			        	
		        	{
		        		Object tmp = feats.get("pos");
		        		input += "\t" + (tmp == null ? "" : tmp.toString());
		        	}
		        	
		        	{
		        		Object tmp = feats.get("feature");
		        		input += "\t[" + (tmp == null ? "" : tmp.toString()) + "]";
		        	}
	
		        	ht_os.write(input);
			    	ht_os.write(System.getProperty("line.separator"));
		        }
		        ht_os.write(System.getProperty("line.separator"));
		        ht_os.flush();
	
		        for (Annotation tok : tokens) {
			        try {
				    	for (int t=0; t<60000; ++t) {
			    			while (ht_es.ready()) try {
								String err = ht_es.readLine();
								if (err != null) logger.error("Error in HunTag3: " + err);
							} catch (IOException e) {}
				    		if (!huntag.isAlive()) {
				    			logger.error("Error in HunTag3: closed");
				    			return;
				    		}
				    		if (ht_is.ready()) {
				    			String output = ht_is.readLine();
				    			if (output == null) {
				    				logger.error("Error in HunTag3: closed stdout");
				    				return;
				    			}
				    			String[] data = output.split("\t");
					    		if (data.length > 3) tok.getFeatures().put(outputAnnotationType, data[3]);
					    		break;
							}
							Thread.sleep(1);
					    }
			    	} catch (IOException e) {
			            logger.info("Error in HunTag3: IO exception");
			    	}
		        }
		        for (int t=0; t<2000; ++t) {
		    		if (ht_is.ready()) { ht_is.readLine(); break; } // read closing line
		    		Thread.sleep(1);
		        }
        	}
	    } catch (Exception e) {
		   	e.printStackTrace();
		   	throw new GateRuntimeException("HunTag3: something went terribly wrong");
		}
    }

	@RunTime
	@CreoleParameter(
			comment="The name of the base 'Token' annotation type",
			defaultValue="resources/huntag3/run_huntag.sh")
	public void setHunTagBinary(URL huntagBinary) {
		this.huntagBinary = huntagBinary;
	}
	public URL getHunTagBinary() {
		return this.huntagBinary;
	}
	URL huntagBinary;


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
			comment="The name of the input 'Sentence' annotation type",
			defaultValue="Sentence")
	public void setInputSentenceType(String newBaseSentenceAnnotationType) {
		this.inputSentenceType = newBaseSentenceAnnotationType;
	}
	public String getInputSentenceType() {
		return this.inputSentenceType;
	}
	private String inputSentenceType;

	  	  
	@RunTime
	@CreoleParameter(
			comment="The name of the annotation type where the new features should be added",
			defaultValue="NP-BIO")
	public void setOutputAnnotationType(String outputAnnotationType) {
		this.outputAnnotationType = outputAnnotationType;
	}
	public String getOutputAnnotationType() {
		return this.outputAnnotationType;
	}
	private String outputAnnotationType;

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
