package com.gapid.graph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * Created by bbp on 6/4/2015.
 */
public class NetworkGenerator {
	public static void main(String[] args) {
		int nodeCount = 10;
		List<Node> S = IntStream.range(0, nodeCount).mapToObj(Node::new).collect(toList());
		Set<Node> T = new HashSet<>();

		Node currentNode = S.remove((int) (Math.random() * S.size()));
		T.add(currentNode);




	}

	static class Node{int name; public Node(int i){this.name = i;}};
}
/*
# Create two partitions, S and T. Initially store all nodes in S.
S, T = set(nodes), set()

# Pick a random node, and mark it as visited and the current node.
current_node = random.sample(S, 1).pop()
S.remove(current_node)
T.add(current_node)

graph = Graph(nodes)

# Create a random connected graph.
while S:
    # Randomly pick the next node from the neighbors of the current node.
    # As we are generating a connected graph, we assume a complete graph.
    neighbor_node = random.sample(nodes, 1).pop()
    # If the new node hasn't been visited, add the edge from current to new.
    if neighbor_node not in T:
        edge = (current_node, neighbor_node)
        graph.add_edge(edge)
        S.remove(neighbor_node)
        T.add(neighbor_node)
    # Set the new node as the current node.
    current_node = neighbor_node

# Add random edges until the number of desired edges is reached.
graph.add_random_edges(num_edges)

 */