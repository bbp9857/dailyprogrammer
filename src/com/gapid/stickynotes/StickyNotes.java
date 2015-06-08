package com.gapid.stickynotes;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;
// http://www.reddit.com/r/dailyprogrammer/comments/35s2ds/20150513_challenge_214_intermediate_pile_of_paper/crss5yr

public class StickyNotes {
	public final static void main(String[] args) throws IOException {
		args = new String[]{"10Krects100Kx100K.in"};
		Path fileName = new File(args[0]).toPath();
		Coordinate base = Files.lines(fileName).limit(1).map(s -> Arrays.stream(s.split(" ")).map(Integer::parseInt).collect(toList())).map(s -> new Coordinate(0, new Rectangle(0, 0, s.get(0), s.get(1)),0)).findFirst().get();

		List<Coordinate> notes = new ArrayList<>();
		notes.add(base);
		notes.addAll(Files.lines(fileName).skip(1).filter(s -> !s.startsWith("#")).map(s -> Arrays.stream(s.split(" ")).map(Integer::parseInt).collect(toList())).map(Coordinate::new).collect(toList()));

		IntStream.range(0, notes.size()).forEach(i -> {
			notes.get(i).index = i;
		});
		List<Coordinate> newCoordinates = new Vector<>(notes);

		for (int i = 0; i < notes.size(); i++ ){
			final Coordinate c = notes.get(i);
			new ArrayList<>(newCoordinates).parallelStream().filter(n-> n.index < c.index && n.r.intersects(c.r)).forEach( n -> {
					newCoordinates.remove(n);
					Rectangle intersection = n.r.intersection(c.r);
					intersection.translate(-n.r.x, -n.r.y);
					Arrays.stream(new int[][]{
							new int[]{n.r.x, n.r.y, n.r.width, intersection.y}, // top
							new int[]{n.r.x, n.r.y + intersection.y, intersection.x, n.r.height - intersection.y}, //left

							new int[]{n.r.x + intersection.x, n.r.y + intersection.y + intersection.height, //bottom
									n.r.width - (intersection.x), n.r.height - (intersection.y + intersection.height)},

							new int[]{n.r.x + intersection.x + intersection.width, n.r.y + intersection.y,
									n.r.width - (intersection.x + intersection.width), intersection.height},

					}).map(a -> new Rectangle(a[0], a[1], a[2], a[3])).filter(r -> r.height > 0 && r.width > 0).map(r -> new Coordinate(n.color, r, n.index)).forEach(newCoordinates::add);
				});
		}

		Map<Integer, Long> colorMap = newCoordinates.stream().collect(groupingBy(Coordinate::getColor, summingLong(c -> c.r.width * c.r.height)));
		colorMap.keySet().stream().forEach(i -> System.out.println(i + ": " + colorMap.get(i)));
	}
	static class Coordinate {
		Rectangle r;
		int color, index;
		public Coordinate(List<Integer> meta){
			color = meta.get(0); r = new Rectangle(meta.get(1), meta.get(2), meta.get(3), meta.get(4));
		}
		public Coordinate(int color, Rectangle r, int index) {
			this.color = color; this.r = r; this.index = index;
		}
		public int getArea(){ return r.width * r.height; }
		public int getColor() {return color;}
	}
}