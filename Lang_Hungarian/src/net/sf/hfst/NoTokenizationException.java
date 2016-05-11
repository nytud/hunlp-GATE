package net.sf.hfst;

public class NoTokenizationException extends Exception {

	private static final long serialVersionUID = 1L;
	
	String attempted;
    public NoTokenizationException(String str)
	{
	    super();
	    attempted = str;
	}
    public String message()
    {
    	return "Failed to tokenize " + attempted;
    }
}
