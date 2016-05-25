package hu.nytud.gate.pipeline;

import java.util.HashMap;

/**
 * A class to specify a PR by name + parameters
 * @author Bálint Sass
 *
 */
public class PRSpec {

  private String name; 
  private HashMap<String, Object> params;

  public PRSpec( String name, HashMap<String, Object> params ) {
    this.name = name;
    this.params = params;
  }

  public PRSpec( String name ) {
    this.name = name;
    this.params = new HashMap<String, Object>();
  }

  public String getName() { return name; }
  public HashMap<String, Object> getParams() { return params; }
}
