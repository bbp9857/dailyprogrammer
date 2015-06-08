package com.gapid.logs;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.IntStream;
import static java.util.stream.Collectors.*;
//http://www.reddit.com/r/dailyprogrammer/comments/3840rp/20150601_challenge_217_easy_lumberjack_pile/crs8bse
public class Logs {
	public static void main(String[] args) throws IOException {
		args = new String[]{"foo"};
		Path fileName = new File(args[0]).toPath();
		int logsToPlace = Files.lines(fileName).skip(1).limit(1).map(Integer::parseInt).findFirst().get();

		List<List<Log>> piles = Files.lines(fileName).skip(2).map(s-> Arrays.stream(s.split(" ")).
				map(i -> new Log(Integer.parseInt(i))).collect(toList())).collect(toList());

		PriorityQueue<Log> queue = new PriorityQueue<Log>(new Comparator<Log>(){
			public int compare(Log arg0, Log arg1) {
				return Integer.compare(arg0.count, arg1.count);
			}});

		piles.forEach(queue::addAll);
		IntStream.range(0, logsToPlace).forEach(i -> {
			Log log = queue.remove();
			log.count++;
			queue.add(log);
		});

		piles.stream().map(l -> l.stream().map(log -> "" + log.count)
				.collect(joining(" "))).forEach(System.out::println);
	}

	static class Log { int count; public Log(int count) { this.count = count; }}
}