package com.gapid.graph;

public class Dijkstra {
	// Dijkstra's algorithm to find shortest path from s to all other nodes
	public static int[] dijkstra(WeightedGraph G, int s) {
		final int[] dist = new int[G.size()];  // shortest known distance from "s"
		final int[] pred = new int[G.size()];  // preceeding node in path
		final boolean[] visited = new boolean[G.size()]; // all false initially

		for (int i = 0; i < dist.length; i++) {
			dist[i] = Integer.MAX_VALUE;
		}
		dist[s] = 0;

		for (int i = 0; i < dist.length; i++) {
			final int next = minVertex(dist, visited);
			visited[next] = true;

			// The shortest path to next is dist[next] and via pred[next].

			final int[] n = G.neighbors(next);
			for (int j = 0; j < n.length; j++) {
				final int v = n[j];
				final int d = dist[next] + G.getWeight(next, v);
				if (dist[v] > d) {
					dist[v] = d;
					pred[v] = next;
				}
			}
		}
		return pred;  // (ignore pred[s]==0!)
	}

	public static void main(String args[]) {
		final WeightedGraph t = new WeightedGraph(6);
		t.setLabel(0, "v0");
		t.setLabel(1, "v1");
		t.setLabel(2, "v2");
		t.setLabel(3, "v3");
		t.setLabel(4, "v4");
		t.setLabel(5, "v5");
		t.addEdge(0, 1, 2);
		t.addEdge(0, 5, 9);
		t.addEdge(1, 2, 8);
		t.addEdge(1, 3, 15);
		t.addEdge(1, 5, 6);
		t.addEdge(2, 3, 1);
		t.addEdge(4, 3, 3);
		t.addEdge(4, 2, 7);
		t.addEdge(5, 4, 3);
		t.print();

		final int[] pred = Dijkstra.dijkstra(t, 0);
		for (int n = 0; n < 6; n++) {
			Dijkstra.printPath(t, pred, 0, n);
		}
	}

	private static int minVertex(int[] dist, boolean[] v) {
		int x = Integer.MAX_VALUE;
		int y = -1;   // graph not connected, or no unvisited vertices
		for (int i = 0; i < dist.length; i++) {
			if (!v[i] && dist[i] < x) {
				y = i;
				x = dist[i];
			}
		}
		return y;
	}

	public static void printPath(WeightedGraph G, int[] pred, int s, int e) {
		final java.util.ArrayList path = new java.util.ArrayList();
		int x = e;
		while (x != s) {
			path.add(0, G.getLabel(x));
			x = pred[x];
		}
		path.add(0, G.getLabel(s));
		System.out.println(path);
	}

}

class WeightedGraph {

	private int[][] edges;  // adjacency matrix
	private Object[] labels;

	public WeightedGraph(int n) {
		edges = new int[n][n];
		labels = new Object[n];
	}


	public int size() {
		return labels.length;
	}

	public void setLabel(int vertex, Object label) {
		labels[vertex] = label;
	}

	public Object getLabel(int vertex) {
		return labels[vertex];
	}

	public void addEdge(int source, int target, int w) {
		edges[source][target] = w;
	}

	public boolean isEdge(int source, int target) {
		return edges[source][target] > 0;
	}

	public void removeEdge(int source, int target) {
		edges[source][target] = 0;
	}

	public int getWeight(int source, int target) {
		return edges[source][target];
	}

	public int[] neighbors(int vertex) {
		int count = 0;
		for (int i = 0; i < edges[vertex].length; i++) {
			if (edges[vertex][i] > 0) count++;
		}
		final int[] answer = new int[count];
		count = 0;
		for (int i = 0; i < edges[vertex].length; i++) {
			if (edges[vertex][i] > 0) answer[count++] = i;
		}
		return answer;
	}

	public void print() {
		for (int j = 0; j < edges.length; j++) {
			System.out.print(labels[j] + ": ");
			for (int i = 0; i < edges[j].length; i++) {
				if (edges[j][i] > 0) System.out.print(labels[i] + ":" + edges[j][i] + " ");
			}
			System.out.println();
		}
	}

}
