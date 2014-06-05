package org.workcraft.acronyms.shared;

public class SharedUtil {
	public static String mkString(short[] acronym, String[] words) {
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < acronym.length; i++)
			if (acronym[i] != -1)
				result.append(words[i].charAt(acronym[i]));

		return result.toString();
	}

	public static String mkStringFromSubseq(short[] acronym, String title) {
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < acronym.length; i++)
			result.append(title.charAt(acronym[i]));

		return result.toString();
	}
}