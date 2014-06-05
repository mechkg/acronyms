package org.workcraft.acronyms.shared;

import java.io.Serializable;

public class AcronymGeneratorResult implements Serializable {
	private static final long serialVersionUID = -5795387448764649625L;
	
	public short[][] acronyms;
	public int combinations;
	public boolean random;
	
	public AcronymGeneratorResult() {
		
	};
	
	public AcronymGeneratorResult(short[][] acronyms, int combinations,
			boolean random) {
		this.acronyms = acronyms;
		this.combinations = combinations;
		this.random = random;
	}
}
