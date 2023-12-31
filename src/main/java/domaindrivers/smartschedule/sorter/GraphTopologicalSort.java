package domaindrivers.smartschedule.sorter;

import java.util.List;


public class GraphTopologicalSort<T> {

    public SortedNodes<T> sort(Nodes<T> nodes) {
        return createSortedNodesRecursively(nodes, SortedNodes.empty());
    }

    private SortedNodes<T> createSortedNodesRecursively(Nodes remainingNodes, SortedNodes<T> accumulatedSortedNodes) {
        List<Node<T>> alreadyProcessedNodes = accumulatedSortedNodes.all()
                .stream()
                .flatMap(n -> n.all().stream())
                .toList();
        Nodes nodesWithoutDependencies =
                remainingNodes
                        .withAllDependenciesPresentIn(alreadyProcessedNodes);

        if (nodesWithoutDependencies.all().isEmpty()) {
            return accumulatedSortedNodes;
        }
        SortedNodes newSortedNodes = accumulatedSortedNodes.add(nodesWithoutDependencies);
        remainingNodes = remainingNodes.removeAll(nodesWithoutDependencies.all());
        return createSortedNodesRecursively(remainingNodes, newSortedNodes);
    }
}

