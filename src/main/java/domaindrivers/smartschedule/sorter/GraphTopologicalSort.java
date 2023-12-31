package domaindrivers.smartschedule.sorter;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;


public class GraphTopologicalSort<T> implements Function<Nodes<T>, SortedNodes<T>> {

    private final BiFunction<Nodes<T>, SortedNodes<T>, SortedNodes<T>> createSortedNodesRecursively;

    public GraphTopologicalSort() {
        this.createSortedNodesRecursively = new IntermediateSortedNodesCreator<>();
    }

    @Override
    public SortedNodes<T> apply(Nodes<T> stages) {
        return createSortedNodesRecursively.apply(stages, SortedNodes.empty());
    }
}

class IntermediateSortedNodesCreator<T> implements BiFunction<Nodes<T>, SortedNodes<T>, SortedNodes<T>> {
    @Override
    public SortedNodes<T> apply(Nodes<T> remainingNodes, SortedNodes<T> accumulatedSortedNodes) {
        List<Node<T>> alreadyProcessedNodes = accumulatedSortedNodes.all()
                .stream()
                .flatMap(n -> n.all().stream())
                .toList();
        Nodes<T> nodesWithoutDependencies =
                remainingNodes
                        .withAllDependenciesPresentIn(alreadyProcessedNodes);

        if (nodesWithoutDependencies.all().isEmpty()) {
            return accumulatedSortedNodes;
        }

        SortedNodes<T> newSortedNodes = accumulatedSortedNodes.add(nodesWithoutDependencies);
        remainingNodes = remainingNodes.removeAll(nodesWithoutDependencies.all());
        return this.apply(remainingNodes, newSortedNodes);
    }
}

