package hu.nytud.gate.tokenizers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import hu.nytud.gate.tokenizers.dummyctokenizer.*;
import hu.nytud.gate.util.*;
import gate.*;
import gate.creole.*; 
import gate.creole.metadata.*; 
import gate.util.*;


/** 
 */ 
@CreoleResource(name = "Hungarian Dummy Tokenizer in C (Linux)",
				comment = "Dummy whitespace tokenizer implemented in C, native shared library called via JNI",
				icon = "tokeniser",
				helpURL = "http://corpus.nytud.hu/gate/doc/DummyCTokenizer") 
public class DummyCTokenizer extends AbstractLanguageAnalyser { 
 
	static {
		/* In the static block we load the native shared library
		 * from ./resources/dummyctokenizer/lib/ relative to hungarian.jar 
		 * (which should contain this class)
		 * The name of the library file is platform-dependent:
		 * - Linux: libdummyctokenizer.so
		 * - Mac OS X: libdummyctokenizer.dylib ? (TODO)
		 * - Windows: dummyctokenizer.dll ? (TODO)
		 * We use the static block to make sure that the library does not
		 * get reloaded when the plugin is reloaded in GATE (which would throw an exception).
		 * We also check explicitly if it has not been loaded yet.
		 */
		
		if (!DummyCTokenizer.isNativeLibraryLoaded()) { // exception1 when run after unload/load
		//if (true) { // exception2 when new PR created after unload/load
			try {
				// Get the path of this jar file
				File jarFile = new File(DummyCTokenizer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
				String jarDir = jarFile.getParentFile().getPath();
				// set library file name depending on OS
				String libFileName = null;
				String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
				if (osName.contains("linux")) {
				    libFileName = "libdummyctokenizer.so";
				}
				else if (osName.contains("windows")) {
				    libFileName = "dummyctokenizer.dll";
				}
				else if (osName.contains("mac os") || osName.contains("macos") || osName.contains("darwin")) {
					libFileName = "libdummyctokenizer.dylib";
				}
				// load native library
				Path libAbsFileName = Paths.get(jarDir, "resources", "dummyctokenizer", "lib", libFileName);
				System.load(libAbsFileName.toString());
				
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		
	}
	
    /**
	 * (Generated, by Eclipse)
	 */
	private static final long serialVersionUID = -7891126754041078162L;
	
	/**
     * Name of the output annotation set (CREOLE plugin runtime parameter)
     */
    private String outputASName;
    
    /**
     * Path of dir containing native shared library containing the C tokenizer functions
     */
    private URL nativeLibPath;
    
	/* 
	 * this method gets called whenever an object of this 
	 * class is created either from GATE Developer GUI or if 
	 * initiated using Factory.createResource() method. 
	 */ 
	public Resource init() throws ResourceInstantiationException { 
        // here initialize all required variables, and may 
        // be throw an exception if the value for any of the 
        // mandatory parameters is not provided 
 
		// Set the path to the C tokenizer's native library using the nativeLibPath CREOLE property:
		/*
		try {
			DummyCTokenizer.addLibPath(nativeLibPath.getPath());
		} catch (IOException e) {
			throw new ResourceInstantiationException(e);
		}
		// load the native library
		System.loadLibrary("dummyctokenizer");		
		//System.load("/home/mm/GATE_plugins/Lang_Hungarian/resources/dummyctokenizer/lib/libdummyctokenizer.so");
		// TODO:
		// - bug: exception if class is loaded more than once (=> static block)
		// - Mac OS X lib
		*/
		
        return this; 
	}

	/* this method is called to reinitialize the resource */ 
	public void reInit() throws ResourceInstantiationException { 
		// reinitialization code 
	} 
		
	/* 
	 * this method should provide the actual functionality of the PR 
	 * (from where the main execution begins). This method 
	 * gets called when user click on the "RUN" button in the 
	 * GATE Developer GUI’s application window. 
	 */ 
	public void execute() throws ExecutionException { 

		if(document == null) {
			throw new ExecutionException("There is no loaded document");
	    }		
		
	    super.fireProgressChanged(0);
	    
	    // Aham
	    AnnotationSet as = null;
	    if (outputASName == null || outputASName.trim().length() == 0)
	      as = document.getAnnotations();
	    else as = document.getAnnotations(outputASName);

	    // Get doc
	    String docContent = document.getContent().toString();
		
	    // Call C tokenizer
		int maxtoks = 1000;
		OffsPairArray toks = new OffsPairArray(maxtoks);
		int[] ntoks = {0};
		int maxwhs = 1000;
		OffsPairArray whs = new OffsPairArray(maxwhs);
		int[] nwhs = {0};
		//DummyCTokenizerWrapper tokenizer = new DummyCTokenizerWrapper();
		DummyCTokenizerWrapper.tokenize(docContent, toks.cast(), ntoks, maxtoks, whs.cast(), nwhs, maxwhs);
	    
		// Now save annotations
		long start, end;
		try {
			for (int i=0; i<java.lang.Math.max(ntoks[0], nwhs[0]); i++) {
				if (i<ntoks[0]) {
					start = toks.getitem(i).getStart();
					end = toks.getitem(i).getEnd();
					FeatureMap fmTokens = Factory.newFeatureMap();
					fmTokens.put("length", "" + (end - start));
			        as.add(new Long(start), new Long(end), "Token", fmTokens);
				}
				if (i<nwhs[0]) {
					start = whs.getitem(i).getStart();
					end = whs.getitem(i).getEnd();
					FeatureMap fmTokens = Factory.newFeatureMap();
					fmTokens.put("length", "" + (end - start));
			        as.add(new Long(start), new Long(end), "SpaceToken", fmTokens);					
				}
			}
		} 
		catch(InvalidOffsetException e) {
	        throw new ExecutionException(e);
	    }
    		
	}
	
	/**
	 * @return true if the native library is already loaded
	 */
	public static boolean isNativeLibraryLoaded() {
		final String[] libraries = ClassScope.getLoadedLibraries(ClassLoader.getSystemClassLoader());
		//final String[] libraries = ClassScope.getLoadedLibraries(DummyCTokenizer.class.getClassLoader());
		for (int j=0; j<libraries.length; j++)
			if (libraries[j].contains("dummyctokenizer"))
				return true;
		return false;
	}

	/**
	 * Add a library path to load.library.path to be used by System.load()
	 * http://stackoverflow.com/questions/5419039/is-djava-library-path-equivalent-to-system-setpropertyjava-library-path
	 */
	public static void addLibPath(String s) throws IOException {
	    try {
	        // This enables the java.library.path to be modified at runtime
	        // From a Sun engineer at http://forums.sun.com/thread.jspa?threadID=707176
	        //
	        Field field = ClassLoader.class.getDeclaredField("usr_paths");
	        field.setAccessible(true);
	        String[] paths = (String[])field.get(null);
	        for (int i = 0; i < paths.length; i++) {
	            if (s.equals(paths[i])) {
	                return;
	            }
	        }
	        String[] tmp = new String[paths.length+1];
	        System.arraycopy(paths,0,tmp,0,paths.length);
	        tmp[paths.length] = s;
	        field.set(null,tmp);
	        System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator + s);
	    } catch (IllegalAccessException e) {
	        throw new IOException("Failed to get permissions to set library path");
	    } catch (NoSuchFieldException e) {
	        throw new IOException("Failed to get field handle to set library path");
	    }
	}	
	
	@RunTime
	@Optional
	@CreoleParameter(comment="The annotation set to be used for the generated annotations")
	public void setAnnotationSetName(String annotationSetName) {
		this.outputASName = annotationSetName;
	}
	public String getAnnotationSetName() {
		return outputASName;
	}	
	
	@CreoleParameter( 
			comment = "Path of dir containing native shared library containing the C tokenizer functions", 
			defaultValue = "resources/dummyctokenizer/lib")
	public void setNativeLibPath(URL libpath) {
		this.nativeLibPath = libpath;
	}
	public URL getNativeLibPath() {
		return nativeLibPath;
	}
	
	/* Run test if called from command line */
	public static void main(String[] args) throws Exception {
				
		System.out.print("THIS is a test\n\n");

		System.out.print("Loaded libraries:\n");
		final String[] libraries = ClassScope.getLoadedLibraries(ClassLoader.getSystemClassLoader()); //MyClassName.class.getClassLoader()
		for (int j=0; j<libraries.length; j++) {
			System.out.print(libraries[j] + "\n");
		}
		
		String text = " This is a sentence.  ";
        int maxtoks = 1000;
        OffsPairArray toks = new OffsPairArray(maxtoks);
        int[] ntoks = {0};
        int maxwhs = 1000;
        OffsPairArray whs = new OffsPairArray(maxwhs);
        int[] nwhs = {0};

        DummyCTokenizerWrapper.tokenize(text, toks.cast(), ntoks, maxtoks, whs.cast(), nwhs, maxwhs);

        System.out.format("\nText: '%s'\n", text);
        System.out.println("Tokens:");
        for (int i=0; i<ntoks[0]; i++)
                System.out.format("(%d, %d)\n", toks.getitem(i).getStart(), toks.getitem(i).getEnd());
        System.out.println("Whitespaces:");
        for (int i=0; i<nwhs[0]; i++)
                System.out.format("(%d, %d)\n", whs.getitem(i).getStart(), whs.getitem(i).getEnd());
		
	}
	
}


