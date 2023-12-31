package domaindrivers.smartschedule.sorter;

import java.util.List;


public class GraphTopologicalSort {

    public SortedNodes sort(Nodes nodes) {
        return createSortedNodesRecursively(nodes, SortedNodes.empty());
    }

    private SortedNodes createSortedNodesRecursively(Nodes remainingNodes, SortedNodes accumulatedSortedNodes) {
        List<Node> alreadyProcessedNodes = accumulatedSortedNodes.all()
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

