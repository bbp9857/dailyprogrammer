package com.gapid.decoder;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;
import static java.util.stream.Collectors.*;
// http://www.reddit.com/r/dailyprogrammer/comments/38fjll/20150603_challenge_217_intermediate_space_code/cruu7iz
public class Decoder {
	static Map<String, Function<String, String>> decoders = new HashMap<>();
	static Set<String> dictionary;

	public static void main(String[] args) throws IOException {
		Path fileName = new File("dictionary.txt").toPath();
		dictionary = Files.lines(fileName).map(String::toUpperCase).collect(toSet());

		decoders.put("omicron", s -> s.chars().mapToObj(c -> "" + (char) (c ^ 16)).collect(joining()));
		decoders.put("hoth", s -> s.chars().mapToObj(c -> "" + (char) (c - 10)).collect(joining()));
		decoders.put("ryza", s -> s.chars().mapToObj(c -> "" + (char) (c + 1)).collect(joining()));
		decoders.put("htrae", s-> new StringBuilder(s).reverse().toString());
		String inputs[]  = {
				" 71 117  48 115 127 125 117  48 121 126  48  96 117 113 115 117 ",
				" 97 111  42 109 121 119 111  42 115 120  42 122 111 107 109 111 ",
				" 86 100  31  98 110 108 100  31 104 109  31 111 100  96  98 100 ",
				" 101  99  97 101 112  32 110 105  32 101 109 111  99  32 101  87 ",
				" 84 113 121 124 105  48  64  98 127 119  98 113 125 125 117  98  48 121  99  48  99  96 105 121 126 119  48 127 126  48 101  99 ",
				" 78 107 115 118 -125  42  90 124 121 113 124 107 119 119 111 124  42 115 125  42 125 122 -125 115 120 113  42 121 120  42 127 125 ",
				" 67  96 104 107 120  31  79 113 110 102 113  96 108 108 100 113  31 104 114  31 114 111 120 104 109 102  31 110 109  31 116 114 ",
				" 115 117  32 110 111  32 103 110 105 121 112 115  32 115 105  32 114 101 109 109  97 114 103 111 114  80  32 121 108 105  97  68 ",
				" 86 121  98 117  48 100 120 117  48  93 121  99  99 124 117  99 ",
				" 80 115 124 111  42 126 114 111  42  87 115 125 125 118 111 125 ",
				" 69 104 113 100  31 115 103 100  31  76 104 114 114 107 100 114 ",
				" 115 101 108 115 115 105  77  32 101 104 116  32 101 114 105  70 ",
		};

		Arrays.stream(inputs).map(i -> Arrays.stream(i.trim().split("\\s+")).map(Integer::parseInt).map(j -> "" + (char) j.intValue()).collect(joining()))
				.forEach(input -> {
					decoders.keySet().stream().filter(decoder -> countValidWords(decoders.get(decoder).apply(input).toUpperCase()) > .8).
							map(decoder -> input + " translates by " + decoder + " to: " + decoders.get(decoder).apply(input))
							.forEach(System.out::println);
				});
	}

	static String getLongestWord(String aWord) {
		return IntStream.range(0, aWord.length())
				.mapToObj(i -> aWord.substring(0, aWord.length() - i)).filter(dictionary::contains)
				.findFirst().orElse(null);
	}

	static double countValidWords(String aString) {
		double validLetters = 0;
		aString = aString.chars().filter(i -> (i >= 'A' && i<= 'Z')).mapToObj(i -> "" + (char) i).collect(joining());
		double letterCount = aString.length();
		while (aString.length() > 0) {
			String longestWord = getLongestWord(aString);
			if (longestWord == null) aString = aString.substring(1);
			else {
				validLetters += longestWord.length();
				aString = aString.substring(longestWord.length());
			}
		}
		return validLetters / letterCount;
	}
}


