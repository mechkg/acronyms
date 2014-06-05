package org.workcraft.acronyms.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletException;

import org.workcraft.acronyms.client.AcronymGenerator;
import org.workcraft.acronyms.shared.AcronymGeneratorResult;
import org.workcraft.acronyms.shared.AcronymGeneratorResult2;
import org.workcraft.acronyms.shared.SharedUtil;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AcronymGeneratorImpl extends RemoteServiceServlet implements
		AcronymGenerator {
	private static final long serialVersionUID = -6821699427109882837L;

	private static Random rnd = new Random();
	private static HashSet<String> dictionary = new HashSet<String>();
	private static HashMap<Integer, List<String>> wordsByLength = new HashMap<Integer, List<String>>();
	
	private static BufferedWriter log;

	@Override
	public void init() throws ServletException {
		super.init();
		
		for (int i = MIN_WORD_LENGTH; i <= MAX_WORD_LENGTH; i++)
			wordsByLength.put(i, new ArrayList<String>());

		BufferedReader r;
		try {
			r = new BufferedReader(new FileReader(getServletContext().getRealPath("/data/wordsEn.txt")));
			//log = new BufferedWriter (new FileWriter("/var/log/acronyms.log", true));

			while (true) {
				String word = r.readLine();
				if (word == null)
					break;

				int len = word.length();
				if (len >= MIN_WORD_LENGTH && len <= MAX_WORD_LENGTH) {
					dictionary.add(word.toLowerCase());
					wordsByLength.get(len).add(word);
				}
			}

		} catch (FileNotFoundException e) {
			throw new ServletException(e);
		} catch (IOException e) {
			throw new ServletException(e);
		}
	}

	private short randomChar(short length, boolean allowSkipWords) {
		int index;

		if (allowSkipWords)
			index = rnd.nextInt(length + 1);
		else
			index = rnd.nextInt(length);

		if (index == length)
			return -1;
		else
			return (short) index;
	}

	private short[] randomNext(final short[] lengths, boolean allowSkipWords) {
		short[] result = new short[lengths.length];

		for (int i = 0; i < lengths.length; i++)
			result[i] = randomChar(lengths[i], allowSkipWords);

		return result;
	}

	private short[] next(short[] in, final short[] lengths,
			boolean allowSkipWords) {
		short[] out = new short[in.length];

		for (int i = 0; i < in.length; i++)
			out[i] = in[i];

		out[0]++;

		for (int i = 0; i < out.length; i++) {
			if ((out[i] == lengths[i]) || (out[i] == -1)) {
				if ((out[i] == lengths[i]) && allowSkipWords) {
					out[i] = -1;
					break;
				} else {
					out[i] = 0;
					if (i + 1 == out.length) { // wrap around
						for (int j = 0; j < out.length; j++)
							out[j] = 0;
					} else {
						out[i + 1]++;
					}
				}
			}
		}

		return out;
	}
	
	private void logRequest (String input) {
		/*String logEntry = (new Date()).toString() + " " + getThreadLocalRequest().getRemoteHost() + " " + input + "\n";
		try {
			log.write(logEntry);
			log.flush();
		} catch (IOException e) {
		}*/
	}

	@Override
	public AcronymGeneratorResult generate(String title, boolean allowSkipWords) {
		logRequest(title);
		String[] words = title.toLowerCase().split("\\s+");
		short[] lengths = new short[words.length];

		int i = 0;
		int combinations = 1;

		for (String s : words) {
			int len = s.length();
			combinations *= (len + (allowSkipWords ? 1 : 0));
			lengths[i++] = (short) len;
		}

		ArrayList<short[]> result = new ArrayList<short[]>();

		if (combinations <= MAX_COMBINATIONS) {
			HashSet<String> known = new HashSet<String>();
			short[] c = new short[words.length];
			for (int j = 0; j < combinations; j++) {
				String s = SharedUtil.mkString(c, words);
				if (!known.contains(s) && dictionary.contains(s)) {
					known.add(s);
					result.add(c);
				}
				c = next(c, lengths, allowSkipWords);
			}
		} else {
			HashSet<String> known = new HashSet<String>();
			for (int j = 0; j < MAX_COMBINATIONS; j++) {
				short[] c = randomNext(lengths, allowSkipWords);
				String s = SharedUtil.mkString(c, words);
				if (!known.contains(s) && dictionary.contains(s)) {
					known.add(s);
					result.add(c);
				}
			}
		}

		return new AcronymGeneratorResult(result.toArray(new short[result
				.size()][]), combinations, (combinations > MAX_COMBINATIONS));
	}

	private short[] subseq (String word, String target) {
		int tlen = target.length();
		int wlen = word.length();
		
		
		short[] result = new short[wlen];
		
		if (word.charAt(0) != target.charAt(0)) return null;
		
		int wi = 0;
		
		for (int i = 0; i<tlen; i++) {
			if (word.charAt(wi) == target.charAt(i)) {
				result[wi] = (short)i;
				wi++;
			}
			if (wi == wlen) return result;
		}
		
		return null;
	}

	@Override
	public AcronymGeneratorResult2 generateSubseq(String title, int length) {
		logRequest(title);
		String lowcase = title.toLowerCase();
		ArrayList<short[]> result = new ArrayList<short[]>();
		
		int lengthMin, lengthMax;
		
		if (length < 8) {
			lengthMin = length - 1;
			lengthMax = length + 1;
		} else {
			lengthMin = 8;
			lengthMax = MAX_WORD_LENGTH;
		}
		
		for (int len = lengthMin; len <= lengthMax; len ++) {
			List<String> words = wordsByLength.get(len);
		
			for (String word: words) {
				short[] acronym = subseq (word, lowcase);
				if (acronym != null)
					result.add(acronym);
			}
		}
		
		return new AcronymGeneratorResult2(result.toArray(new short[result.size()][]));
	}
}