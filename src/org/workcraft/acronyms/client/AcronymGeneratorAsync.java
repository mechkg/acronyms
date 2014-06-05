package org.workcraft.acronyms.client;

import org.workcraft.acronyms.shared.AcronymGeneratorResult;
import org.workcraft.acronyms.shared.AcronymGeneratorResult2;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AcronymGeneratorAsync {
	void generate(String title, boolean allowSkipWords, AsyncCallback<AcronymGeneratorResult> callback);
	void generateSubseq(String title, int len, AsyncCallback<AcronymGeneratorResult2> callback);
}
