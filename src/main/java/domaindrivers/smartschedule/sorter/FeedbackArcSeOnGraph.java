package domaindrivers.smartschedule.sorter;

import java.util.*;

public class FeedbackArcSeOnGraph<T extends Object> {

    public List<Edge> calculate(List<Node<T>> initialNodes) {
        Map<Integer, List<Integer>> adjacencyList = createAdjacencyList(initialNodes);
        int v = adjacencyList.size();
        List<Edge> feedbackEdges = new ArrayList<>();
        int[] visited = new int[v + 1];
        Iterator<Integer> nodes = adjacencyList.keySet().iterator();
        while (nodes.hasNext()) {
            Integer i = nodes.next();
            List<Integer> neighbours = adjacencyList.get(i);
            if (neighbours.size() != 0) {
                visited[i] = 1;
                for (int j = 0; j < neighbours.size(); j++) {
                    if (visited[neighbours.get(j)] == 1) {
                        feedbackEdges.add(new Edge(i, neighbours.get(j)));
                    } else {
                        visited[neighbours.get(j)] = 1;
                    }
                }
            }
        }
        return feedbackEdges;
    }

    private Map<Integer, List<Integer>> createAdjacencyList(List<Node<T>> initialNodes) {
        Map<Integer, List<Integer>> adjacencyList = new HashMap<>();

        for (int i = 1; i <= initialNodes.size(); i++) {
            adjacencyList.put(i, new LinkedList<>());
        }

        for (int i = 0; i < initialNodes.size(); i++) {
            List<Integer> dependencies = new ArrayList<>();
            for (Node<T> dependency : initialNodes.get(i).dependencies().nodes()) {
                dependencies.add(initialNodes.indexOf(dependency) + 1);
            }
            adjacencyList.put(i + 1, dependencies);
        }
        return adjacencyList;
    }
}

