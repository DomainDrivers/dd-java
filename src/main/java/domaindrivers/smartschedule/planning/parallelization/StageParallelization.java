package domaindrivers.smartschedule.planning.parallelization;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class StageParallelization {

    public ParallelStagesList of(Set<Stage> stages) {
        return createSortedNodesRecursively(stages, ParallelStagesList.empty());
    }

    private ParallelStagesList createSortedNodesRecursively(Set<Stage> remainingNodes, ParallelStagesList accumulatedSortedNodes) {
        List<Stage> alreadyProcessedNodes = accumulatedSortedNodes.all()
                .stream()
                .flatMap(n -> n.stages().stream())
                .toList();
        Set<Stage> nodesWithoutDependencies =
                withAllDependenciesPresentIn(remainingNodes, alreadyProcessedNodes);

        if (nodesWithoutDependencies.isEmpty()) {
            return accumulatedSortedNodes;
        }

        ParallelStagesList newSortedNodes = accumulatedSortedNodes.add(new ParallelStages(nodesWithoutDependencies));
        Set<Stage> newRemainingNodes = new HashSet<>(remainingNodes);
        newRemainingNodes.removeAll(nodesWithoutDependencies);
        return createSortedNodesRecursively(newRemainingNodes, newSortedNodes);
    }

    Set<Stage> withAllDependenciesPresentIn(Set<Stage> toCheck, Collection<Stage> presentIn) {
        return
                toCheck
                        .stream()
                        .filter(n -> presentIn.containsAll(n.dependencies()))
                        .collect(toSet());
    }

}
