package hu.nytud.gate.pipeline;

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;


import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.FileSystems;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Scanner;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import gate.*;
//import gate.creole.ANNIEConstants;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;

/**
 * Command line (GATE Embedded) version of Lang_Hungarian (based on PRTest.java)
 * @author Márton Miháltz, Bálint Sass
 *
 */
public class Pipeline {

  protected Logger logger = Logger.getLogger(this.getClass().getName());

  private List<PRSpec> PRsToRun; // PRs which will be run in the pipeline

  private String configFilePath;

  public Pipeline() {

    PRsToRun = new ArrayList<PRSpec>();

    configFilePath =
      "Lang_Hungarian" + File.separator +
      "resources" + File.separator +
      "pipeline"  + File.separator + "pipeline.config"; // XXX best practice?

  }

  public void init() throws GateException {
    // Initialize GATE Embedded
    Gate.init();
  }

  /**
   * Read PR specifications from config file,
   * create inner representation as a PRSpec List in variable PRsToRun
   */
  public void readConfig() {

    // from http://www.programcreek.com/2011/03/java-read-a-file-line-by-line-code-example
    try ( BufferedReader reader = Files.newBufferedReader(
      FileSystems.getDefault().getPath( configFilePath ),
      Charset.forName("UTF-8") // US-ASCII may be enough...
    ) ) {

      String line = null;

      // go through lines in config file
      while ( ( line = reader.readLine() ) != null ) {

        // skip empty lines and comment lines
        if ( line.equals( "" ) || line.startsWith( "#" ) ) { continue; }

        // process line = read PRSpec data from line
        Scanner ls = new Scanner( line );

        // PR name
        String PRname = ls.next();
        // PR parameters (if any)
        // 4 cases are handled currently: boolean, float, int, String
        HashMap<String, Object> params = new HashMap<String, Object>();
        while ( ls.hasNext() ) {
          String n = ls.next();
          if ( ls.hasNextBoolean() ) {
            params.put( n, ls.nextBoolean() ); // boolean
          } else if ( ls.hasNextFloat() ) {
            params.put( n, ls.nextFloat() ); // float
          } else if ( ls.hasNextInt() ) {
            params.put( n, ls.nextInt() ); // int
          } else {
            params.put( n, ls.next() ); // String
          }
        }

        // add new PRSpec to PRSpec list
        PRsToRun.add( new PRSpec( PRname, params ) );
      }
    } catch (NoSuchElementException e) {
      // XXX best practice for specifying Exception message? :)
      System.err.println( "\nError in '" + configFilePath + "'.\n" +
        "Line format should be: " +
        "PRname paramName1 paramValue1 paramName2 paramValue2 ...\n" +
        "PRname is obligatory, params are optional.\n" );
      e.printStackTrace();
      System.exit( 1 );
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * Create a new GATE Document LR with a specified String content
   */
  public static Document createDocFromString() throws ResourceInstantiationException {
    String sentstr = "A kisfiú egy szép képet rajzol meg. El fogja kezdeni.";
    return Factory.newDocument( sentstr );
  }

  /**
   * Create a new GATE Document LR from stdin
   */
  public static Document createDocFromStdin() throws ResourceInstantiationException {
    // stdin read -- from http://www.mkyong.com/java/how-to-get-the-standard-input-in-java
    // StringBuilder -- from http://www.odi.ch/prog/design/newbies.php

    StringBuilder sb = new StringBuilder(10000); // 10000? XXX

    try{
      // Read text from stdin (UTF-8!)
      BufferedReader stdin =
        new BufferedReader( new InputStreamReader( System.in, "UTF-8" ) );

      String line;

      while ( ( line = stdin.readLine() ) != null ) {
        sb.append( line );
        sb.append( "\n" );
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    return Factory.newDocument( sb.toString() );
  }

  public void loadLangHungarian(boolean useUserPluginDir) throws MalformedURLException, GateException, URISyntaxException {
    // Load the Lang_Hungarian plugin
    // useUserPluginDir: if true, load it from user plugin dir/Lang_Hungarian, otherwise use dir of current jar file
    URL pluginDir;
    if (useUserPluginDir) {
      pluginDir = new File(Gate.getUserConfig().getString("gate.user.plugins"), "Lang_Hungarian").toURI().toURL();
    }
    else {
      File jarFile = new File(Pipeline.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      pluginDir = jarFile.getParentFile().toURI().toURL();
    }
    System.err.format("\nplugin dir to use = %s\n\n", pluginDir.toString());
    //pluginDir = new File("/home/mm/Infra2/hunlp-GATE/Lang_Hungarian").toURI().toURL();
    Gate.getCreoleRegister().registerDirectories(pluginDir);
  }

  /**
   * Run any combination of PRs specified as a PRSpec array
   */
  public void runPRs( ) {

    try {

      this.init();

      Document doc = Pipeline.createDocFromStdin();

      loadLangHungarian(false);

      for ( PRSpec sp: PRsToRun ) {
        // Create the new PR
        ProcessingResource pr =
          (ProcessingResource)Factory.createResource( sp.getName() );
        pr.setParameterValue("document", doc);

        // set the PR's parameters
        for ( HashMap.Entry<String, Object> entry :
            sp.getParams().entrySet() ) {
          pr.setParameterValue( entry.getKey(), entry.getValue() );
        }

        // ... and apply it to the document
        pr.execute();
      }

    // Dump document's annotations to stdout in GATE XML format (UTF-8!)
    PrintStream stdout = new PrintStream(System.out, true, "UTF-8");
    stdout.println(doc.toXml()); // Prints as expected

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public static void main(String[] args) {
    Pipeline pipeline = new Pipeline();

    pipeline.readConfig();

    pipeline.runPRs();
  }

}
