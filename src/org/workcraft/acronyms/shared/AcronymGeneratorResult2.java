package org.workcraft.acronyms.shared;

import java.io.Serializable;

public class AcronymGeneratorResult2 implements Serializable {
	private static final long serialVersionUID = -8705080690553229481L;
	
	public short[][] acronyms;
	
	public AcronymGeneratorResult2() {}

	public AcronymGeneratorResult2(short[][] acronyms) {
		this.acronyms = acronyms;
	}
}
