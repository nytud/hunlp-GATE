package net.sf.hfst;

import java.util.Collection;

public interface Transducer {
    Collection<String> analyze(String str) throws NoTokenizationException;
}
