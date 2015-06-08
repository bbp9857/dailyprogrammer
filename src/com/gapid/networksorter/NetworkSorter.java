package com.gapid.networksorter;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

//http://www.reddit.com/r/dailyprogrammer/comments/36m83a/20150520_challenge_215_intermediate_validating/crkqwgq
public class NetworkSorter {
	static List<List<Integer>> connectors;
	static int MAP[]; 
	static double ln = Math.log(2);

	public static void main(String[] args) throws IOException {
		Path fileName = new File(args[0]).toPath(); 
		Function<String, List<Integer>> parse = s -> Arrays.asList(s.split(" ")).stream().map(Integer::parseInt).collect(toList());

		Optional<List<Integer>> meta = Files.lines(fileName).limit(1).map(parse).findFirst();
		MAP = new int[meta.get().get(0)];
		
		connectors = Files.lines(fileName).skip(1).map(parse).collect(toList());
		connectors.forEach(l -> l.stream().forEach(i -> MAP[i] = 1 << i));
		
		int total = (int) Math.pow(2, meta.get().get(0)) - 1;
		boolean valid = total == IntStream.rangeClosed(1, total).parallel().map(NetworkSorter::process).reduce(Integer::sum).getAsInt();
		System.out.println((valid ? "Valid" : "Invalid") + " network");
	}

	private static int process(final int index) {
		class Wrapper {int n;}; final Wrapper mutable = new Wrapper(); mutable.n = index;

		connectors.stream().sequential().forEach(l -> { 
			if ((mutable.n & MAP[l.get(0)]) == 0 && (mutable.n & MAP[l.get(1)]) != 0) { mutable.n ^= MAP[l.get(0)] | MAP[l.get(1)]; }
		}); 
		
		return (mutable.n + 1 == 1 << ((int) (Math.log(mutable.n + 1) / ln))) ? 1 : 0 ;
	}
}
