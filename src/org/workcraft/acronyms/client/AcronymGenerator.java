package org.workcraft.acronyms.client;

import org.workcraft.acronyms.shared.AcronymGeneratorResult;
import org.workcraft.acronyms.shared.AcronymGeneratorResult2;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("AcronymGenerator")
public interface AcronymGenerator extends RemoteService {
	static final int MAX_COMBINATIONS = 50000;
	static final int MIN_WORD_LENGTH = 3;
	static final int MAX_WORD_LENGTH = 16;
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static AcronymGeneratorAsync instance;
		public static AcronymGeneratorAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(AcronymGenerator.class);
			}
			return instance;
		}
	}
	
	public AcronymGeneratorResult generate (String title, boolean allowSkipWords);
	public AcronymGeneratorResult2 generateSubseq (String title, int len);
}
