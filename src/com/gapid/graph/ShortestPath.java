package com.gapid.graph;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;

public class ShortestPath {
	public static Path shortestPath(final Map<Integer,Map<Integer, Integer>> g){
		int [][] a = new int[3][g.size()]; Arrays.fill(a[0], Integer.MAX_VALUE); a[0][0] = 0;

		g.forEach((p, q) -> {
			int next = IntStream.range(0, a[0].length).filter(j -> a[2][j] == 0).mapToObj(j -> j)
					.min((x, y) -> Integer.compare(a[0][x], a[0][y])).orElse(-1);

			a[2][next] = 1;
			g.get(next).keySet().forEach(v -> {
				int d = a[0][next] + g.get(next).get(v);
				if (a[0][v] > d) {
					a[0][v] = d;
					a[1][v] = next;
				}
			});
		});

		Path p = new Path(); p.path = a[1]; p.g  = g;
		int n = p.path.length - 1;
		while (n != 0) p.score += p.g.get(n).get(n = p.path[n]);
		return p;
	}

	public static List<int[]> getEdges() {
		return Arrays.asList(new int[][]{
				{0, 4, 2330}, {1, 31, 1090}, {1, 63, 759}, {1, 92, 1204}, {1, 97, 2103}, {2, 72, 72}, {5, 11, 2163}, {6, 95, 1234}, {7, 36, 1647}, {7, 52, 690}, {8, 27, 293}, {9, 44, 2369}, {10, 15, 103}, {10, 51, 5}, {12, 8, 2705}, {14, 82, 2587}, {15, 42, 2759}, {16, 14, 56}, {16, 70, 1264}, {17, 78, 22}, {18, 10, 2540}, {19, 37, 241}, {20, 15, 2635}, {21, 14, 1381}, {21, 17, 2953}, {21, 45, 357}, {22, 4, 1023}, {22, 23, 670}, {22, 34, 1664}, {23, 46, 1885}, {24, 89, 1965}, {25, 3, 2497}, {25, 40, 2087}, {25, 47, 2091}, {26, 38, 2008}, {27, 33, 2271}, {27, 91, 2915}, {28, 60, 2349}, {29, 89, 2822}, {32, 77, 1089}, {32, 97, 210}, {33, 57, 23}, {33, 59, 2752}, {33, 87, 2108}, {34, 7, 2621}, {37, 31, 7}, {41, 16, 990}, {45, 67, 2632}, {45, 90, 456}, {46, 80, 901}, {47, 99, 437}, {49, 97, 1067}, {50, 78, 1695}, {52, 60, 2519}, {52, 98, 2926}, {53, 28, 1245}, {53, 37, 1628}, {55, 36, 1176}, {55, 73, 812}, {55, 75, 2529}, {56, 23, 2635}, {56, 78, 1952}, {57, 45, 2976}, {58, 6, 364}, {60, 14, 1610}, {61, 31, 733}, {61, 39, 2063}, {63, 11, 1780}, {63, 30, 832}, {63, 94, 561}, {64, 68, 243}, {65, 1, 1572}, {67, 81, 517}, {67, 87, 375}, {69, 30, 995}, {69, 37, 1639}, {69, 47, 2977}, {70, 9, 849}, {70, 32, 342}, {71, 26, 2132}, {71, 75, 2243}, {72, 54, 562}, {75, 13, 1589}, {75, 43, 737}, {75, 61, 1090}, {75, 89, 289}, {76, 37, 1984}, {76, 66, 552}, {77, 9, 1790}, {77, 45, 1642}, {79, 20, 798}, {79, 26, 619}, {80, 57, 2444}, {80, 67, 1818}, {81, 31, 2119}, {82, 35, 1220}, {82, 37, 546}, {83, 12, 572}, {83, 77, 2156}, {84, 57, 624}, {84, 91, 423}, {85, 66, 979}, {86, 59, 102}, {87, 74, 935}, {89, 2, 2412}, {89, 36, 889}, {90, 95, 544}, {91, 72, 1201}, {92, 9, 79}, {92, 40, 1329}, {92, 88, 82}, {93, 56, 875}, {93, 62, 1425}, {93, 64, 2400}, {94, 2, 2209}, {96, 60, 1116}, {97, 37, 2921}, {97, 48, 2488}, {98, 44, 2609}, {98, 56, 1335}
		});
	}

	public static void main(String[] args) {
		List<int[]> edges = getEdges();
		Path path = edges.parallelStream().map(e -> {
			List<int[]> e2 = new ArrayList<>(edges);
			e2.remove(e);
			e2.add(new int[]{e[0], e[1], 0});
			e2.addAll(e2.stream().map(a -> new int[]{a[1], a[0], a[2]}).collect(toList()));
			return e2;
		})
			.map(e -> e.stream().collect(groupingBy(a -> a[0], groupingBy(a -> a[1], summingInt(a -> a[2])))))
			.map(ShortestPath::shortestPath).min((a, b) -> Integer.compare(a.score, b.score)).get();

		int n = path.path.length - 1;
		while (n != 0) {
			int zombies = path.g.get(n).get(path.path[n]);
			if (zombies == 0) System.out.print(" *BANG* ");
			System.out.print(path.path[n] + " (" + zombies + ") -> " );
			n = path.path[n];
		}

		System.out.println( " - encountered " + path.score + " zombies!");
	}

}
class Path{int[] path; Map<Integer, Map<Integer, Integer>> g; public int score; }
