package hu.nytud.gate.parsers;

/*
 * Code from gate.stanford.DependencyRelation.
 */

import java.io.Serializable;


public class DependencyRelation implements Serializable {

	private static final long serialVersionUID = 1L;

	private String type;
  
	private Integer targetId;
  
    public DependencyRelation(String type, Integer targetId) {
    	this.type = type;
	    this.targetId = targetId;
	}
	
	public String getType() {
	    return type;
	}
	
	public void setType(String type) {
	    this.type = type;
	}
	
	public Integer getTargetId() {
	    return targetId;
	}
	
	public void setTargetId(Integer targetId) {
	    this.targetId = targetId;
	}
	  
	public String toString() {
	    return type + "(" + targetId + ")";
	}

}
