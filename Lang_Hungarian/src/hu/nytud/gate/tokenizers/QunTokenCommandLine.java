package hu.nytud.gate.tokenizers;

import gate.AnnotationSet;
import gate.DocumentContent;
import gate.Factory;
import gate.FeatureMap;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import gate.util.BomStrippingInputStreamReader;
import gate.util.Files;
import gate.util.ProcessManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

import org.apache.log4j.Logger;

/**
 * QunToken Hungarian sentence splitter and tokenizer,
 * using the command-line quntoken binary via a system call.
 * Source code based on taggerframework.GenericTagger (a GATE built-in plugin)
 */
@CreoleResource(name = "1. Sentence Splitter And Tokenizer [HU] [QunToken (Linux)]", 
				comment = "Creates sentence and token annotations",
				icon = "tokeniser") 
public class QunTokenCommandLine extends AbstractLanguageAnalyser {

	private static final long serialVersionUID = 1L;

	// Temp file encoding
	private String encoding = "UTF-8";

    // The annotations set used for output
	private String outputASName;
	
	// the path to the tagger binary
	private URL taggerBinary, taggerDir;
	
	// should we...
	// display debug information
	private Boolean debug;
	
    //a util class for dealing with external processes, i.e. the tagger
	private ProcessManager processManager = new ProcessManager();
	  
	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	/**
	 * This method initialises the tagger. This involves loading the pre
	 * and post processing JAPE grammars as well as a few sanity checks.
	 * 
	 * @throws ResourceInstantiationException if an error occurs while
	 *           initialising the PR
	*/
	@Override
	public Resource init() throws ResourceInstantiationException {

	    // Not sure if this is definitely needed but it makes sense that on
	    // certain platforms the external call may well fail if the paths
	    // contain spaces as the shell could interpret the space as a change
	    // in command line argument rather than part of a path.
		String tmpDir = System.getProperty("java.io.tmpdir");
	    if(tmpDir == null || tmpDir.indexOf(' ') >= 0) {
	      throw new ResourceInstantiationException(
	              "The tagger requires your temporary directory to be set to a value "
	                      + "that does not contain spaces.  Please set java.io.tmpdir to a "
	                      + "suitable value.");
	    }

	    return this;
	  }
	  
	/**
	 * This method does all the work by calling the protected methods in
	 * the right order so that an input file is written, a command line is
	 * built, the tagger is run and then finally the output of the tagger
	 * is added as annotations onto the GATE document being processed.
	 * 
	 * @throws ExecutionException if an error occurs during any stage of
	 *           running the tagger
	*/
	@Override
	public void execute() throws ExecutionException {
	
		// do some sanity checking of the runtime parameters before we start
	    // doing any work
	    if(document == null)
	      throw new ExecutionException("No document to process!");

	    if(taggerBinary == null)
	      throw new ExecutionException(
	              "Cannot proceed unless a tagger executable is specified.");

	    // get current text from GATE for the tagger
	    File textfile = getCurrentText();
	    if (textfile == null) {
	      /* This handles the null return value from getCurrentText() when
	       * there are no input annotations in the document and
	       * parameter failOnMissingInputAnnotations is false.       */
	      return;
	    }

	    // build the command line for running the tagger
	    String[] taggerCmd = buildCommandLine(textfile);

	    // run the tagger and put the output back into GATE
	    readOutput(runTagger(taggerCmd)); 

	    // delete the temporary text file
	    if(!debug) if(!textfile.delete()) textfile.deleteOnExit();
	}
		
	/**
	 * This method copies text content of the current GATE
	 * document into a file that can be read by the tagger.
	 * 
	 * @return a File object which contains the input to the tagger
	 * @throws ExecutionException if an error occurs while building the
	 *           tagger input file
	*/
	protected File getCurrentText() throws ExecutionException {
		// the file we are going to write
	    File gateTextFile = null;

	    try {
	      // create an empty temp file so we don't overwrite any existing
	      // files
	      gateTextFile = File.createTempFile("tagger", ".txt");

	      // get the character set we should be using for encoding the file
	      Charset charset = Charset.forName(encoding);

	      // depending on the failOnUnmappableCharacter parameter, we either
	      // make the output stream writer fail or replace the unmappable
	      // character with '?'
	      CharsetEncoder charsetEncoder = charset.newEncoder()
	              .onUnmappableCharacter(CodingErrorAction.REPORT);

	      // Get a stream we can write to that handles the encoding etc.
	      FileOutputStream fos = new FileOutputStream(gateTextFile);
	      OutputStreamWriter osw = new OutputStreamWriter(fos, charsetEncoder);
	      BufferedWriter bw = new BufferedWriter(osw);
	      
	      // Write document contents
	      bw.write(document.getContent().toString());
	      
	      // we have finished writing the file so close the streams
	      bw.close();
	    }
	    catch(CharacterCodingException cce) {
	      throw (ExecutionException)new ExecutionException(
	              "Document contains a character that cannot be represented "
	                      + "in " + encoding).initCause(cce);
	    }
	    catch(IOException ioe) {
	      throw (ExecutionException)new ExecutionException(
	              "Error creating temporary file for tagger").initCause(ioe);
	    }
	    return (gateTextFile);
	  }
	
	/**
	 * This method constructs an array of Strings which will be used as
	 * the command line for executing the external tagger through a call
	 * to Runtime.exec(). This uses the tagger binary to build
	 * the command line. If the system property <code>shell.path</code>
	 * has been set then the command line will be built so that the tagger
	 * is run by the provided shell. This is useful on Windows where you
	 * will usually need to run the tagger under Cygwin or the Command
	 * Prompt.
	 * 
	 * @param textfile the file containing the input to the tagger
	 * @return a String array containing the correctly assembled command
	 *         line
	 * @throws ExecutionException if an error occurs whilst building the
	 *           command line
	*/
	protected String[] buildCommandLine(File textfile) throws ExecutionException {
	
		// check that the file exists
	    File scriptfile = Files.fileFromURL(taggerBinary);
	    if(scriptfile.exists() == false)
	      throw new ExecutionException("Script " + scriptfile.getAbsolutePath()
	              + " does not exist");

	    // a pointer to where to stuff the flags
	    int index = 0;

	    // the array we are building
	    String[] taggerCmd;

	    // the bath to a shell under which to run the script
	    String shPath = System.getProperty("shell.path");

	    if(shPath != null) {
	      // if there is a shell then use that as the first command line
	      // argument
	      taggerCmd = new String[3];
	      taggerCmd[0] = shPath;
	      index = 1;
	    }
	    else {
	      // there is no shell so we only need an array long enough to hold
	      // the binary and file
	      taggerCmd = new String[2];
	    }

	    // add the binary and input file to the command line
	    taggerCmd[index] = scriptfile.getAbsolutePath();
	    taggerCmd[taggerCmd.length - 1] = textfile.getAbsolutePath();

	    if(debug) {
	      // if we are doing debug work then echo the command line
	      StringBuilder sanityCheck = new StringBuilder();
	      for(String s : taggerCmd)
	        sanityCheck.append(" ").append(s);
	      System.out.println(sanityCheck.toString());
	    }

	    // return the fully constructed command line
	    return taggerCmd;
	}

	/**
	 * This method is responsible for executing the external tagger. If a
	 * problem is going to occur this is likely to be the place!
	 * 
	 * @param cmdline the command line we want to execute
	 * @return an InputStream from which the output of the tagger can be
	 *         read
	 * @throws ExecutionException if an error occurs executing the tagger
	 */
	protected InputStream runTagger(String[] cmdline) throws ExecutionException {
	    
	    ByteArrayOutputStream baout = new ByteArrayOutputStream();
	    
	    try {
	      int returnCode;
	      if(taggerDir == null) {
	        returnCode = processManager.runProcess(cmdline, baout, (debug ? System.err : null));
	      }
	      else {
	        returnCode = processManager.runProcess(cmdline, Files.fileFromURL(taggerDir), baout, (debug ? System.err : null));
	      }
	      
	      if (debug) System.err.println("Return Code From Tagger: "+returnCode);
	      
	      return new ByteArrayInputStream(baout.toByteArray());
	    }
	    catch(Exception e) {
	      throw new ExecutionException(e);
	    }
	}

	/**
	 * This method reads the output from the tagger adding the information
	 * back into the GATE document. If the tagger doesn't produce one line
	 * per output type then you will need to override this to do something
	 * different.
	 * 
	 * @param in the InputStream frmo which the method will read the
	 *          output from the tagger
	 * @throws ExecutionException if an error occurs while handling the
	 *           output from the tagger
	*/
	protected void readOutput(InputStream in) throws ExecutionException {
		
		// output annotation set to write to
		AnnotationSet outputAS = null;
	    if (outputASName == null || outputASName.trim().length() == 0)
	      outputAS = document.getAnnotations();
	    else outputAS = document.getAnnotations(outputASName);
		
	    // get a decoder using the charset
	    Charset charset = Charset.forName(encoding);
	    CharsetDecoder charsetDecoder = charset.newDecoder();

	    try {
	    
		      // get a reader over the output from the tagger remembering to
		      // handle the encoding
		      BufferedReader input = new BomStrippingInputStreamReader(in,
		              charsetDecoder);
		      
		      // Read all the contents of tagger's output into a single string.
		      // There may be more efficient ways to do this.
		      // Optimal solution would be to implement a stream line reader function
		      // that keeps the line terminating character(s) at end of each read line.
		      StringBuilder sb = new StringBuilder();
		      int nextchar;
		      while ((nextchar = input.read()) != -1) {
		    	  sb.append((char)nextchar);  
		      }
		      String fileContents = sb.toString();
	
     	      // if we are debugging then dump the file contents
		      if (debug) System.out.println("Dumping tagger output file contents:\n" + fileContents);
		      
		      // now parse the file contents
		      int offs = 0; // current absolute character offset in the text content of the file
		      int currSentStart = -1; // start offset of current sentence
		      int currWordStart = -1; // start offset of current word token
		      int currWSStart   = -1; // start offset of current whitespace token
		      int currPuncStart = -1; // start offset of current punctuation token
		      int pos = 0; // current character position in file contents
		      DocumentContent docContents = document.getContent(); // shortcut for the GATE doc contents
		      while (pos < fileContents.length()) {				        
					        	
		        	if (strAt(fileContents, pos, "<s>")) {
		        		currSentStart = offs;
		        		pos += 3;
		        	}
		        	else if (strAt(fileContents, pos, "<w>")) {
		        		currWordStart = offs;
		        		pos += 3;
		        	}
		        	else if (strAt(fileContents, pos, "<ws>")) {
		        		currWSStart = offs;
		        		pos += 4;
		        	}
		        	else if (strAt(fileContents, pos, "<c>")) {
		        		currPuncStart = offs;
		        		pos += 3;
		        	}
		        	else if (strAt(fileContents, pos, "</s>")) {
		        		long start = new Long(currSentStart);
		        		long end = new Long(offs);
		        		FeatureMap features = Factory.newFeatureMap();
		        		features.put("length", end-start);
		        		features.put("string", docContents.getContent(start, end).toString());
		        		outputAS.add(start, end, "Sentence", features);
		        		currSentStart = -1;
		        		pos += 4;
		        	}
		        	else if (strAt(fileContents, pos, "</w>")) {
		        		long start = new Long(currWordStart);
		        		long end = new Long(offs);
		        		FeatureMap features = Factory.newFeatureMap();
		        		features.put("length", end-start);
		        		features.put("string", docContents.getContent(start, end).toString());
		        		features.put("kind", "word");
		        		outputAS.add(start, end, "Token", features);
		        		currWordStart = -1;
		        		pos += 4;
		        	}
		        	else if (strAt(fileContents, pos, "</ws>")) {
		        		long start = new Long(currWSStart);
		        		long end = new Long(offs);
		        		FeatureMap features = Factory.newFeatureMap();
		        		features.put("length", end-start);
		        		features.put("string", docContents.getContent(start, end).toString());
		        		outputAS.add(start, end, "SpaceToken", features);
		        		// TODO: kind=control|space
		        		currWSStart = -1;
		        		pos += 5;
		        	}
		        	else if (strAt(fileContents, pos, "</c>")) {
		        		long start = new Long(currPuncStart);
		        		long end = new Long(offs);
		        		FeatureMap features = Factory.newFeatureMap();
		        		features.put("length", end-start);
		        		features.put("string", docContents.getContent(start, end).toString());
		        		features.put("kind", "punctuation");
		        		outputAS.add(start, end, "Token", features);
		        		currPuncStart = -1;
		        		pos += 4;
		        	}
		        	else { // word or punct or whitespace character
		        		pos += 1;
		        		offs += 1;
		        	}
			        			    	
			  } // while in file contents
	        
	      // Finished reading input, close it
	      input.close();
	      
	    } // try {

	    catch(Exception err) {
	      err.printStackTrace();
	      throw (ExecutionException)new ExecutionException(
	              "Error occurred running tagger").initCause(err);
	    }
		
	}
	
	/**
	 * Return true iff what is found inside str at position index
	 */
	static boolean strAt(String str, int index, String what) {
		return str.substring(index, Math.min(index+what.length(), str.length())).equals(what);
	}

	public String getOutputASName() {
		return outputASName;
	}

    @Optional
    @RunTime
    @CreoleParameter(comment = "Annotation set in which annotations are created")
    public void setOutputASName(String outputASName) {
    	this.outputASName = outputASName;
    }	
	
	public URL getTaggerBinary() {
		return taggerBinary;
	}

	@RunTime
	@CreoleParameter(comment = "Name of the tagger command file",
					 defaultValue = "resources/quntoken/bin/quntoken")
	public void setTaggerBinary(URL taggerBinary) {
		this.taggerBinary = taggerBinary;
	}

	public URL getTaggerDir() {
		return taggerDir;
	}

    @Optional
    @RunTime
    @CreoleParameter(comment = "directory in which to run the tagger",
    				defaultValue = "resources/quntoken/bin/")
    public void setTaggerDir(URL taggerDir) {
    	this.taggerDir = taggerDir;
    }
	
    public Boolean getDebug() {
        return debug;
      }
	
    @RunTime
    @CreoleParameter(defaultValue = "false", comment = "turn on debugging options")
    public void setDebug(Boolean debug) {
      this.debug = debug;
    }
	
}

