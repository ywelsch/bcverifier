package de.unikl.bcverifier.specification;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import de.unikl.bcverifier.isl.parser.IslError;

public class GenerationException extends Exception {
    private static final long serialVersionUID = -7826704299347861040L;

    private List<IslError> islErrors = Collections.emptyList();
    
    public GenerationException(String msg) {
        super("Error in coupling invariant:\n" + msg);
    }
    
    public GenerationException(List<? extends IslError> errors) {
        super("Error in coupling invariant:\n" + printErrors(errors));
        this.islErrors = Lists.newArrayList(errors);
    }
    
    private static String printErrors(List<? extends IslError> errors) {
		StringBuilder sb = new StringBuilder();
		for (IslError err : errors) {
			sb.append(err.toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	public GenerationException(String msg, Throwable t) {
        super("Error in coupling invariant:\n" + msg,t);
    }

	public List<IslError> getIslErrors() {
		return islErrors;
	}

}
