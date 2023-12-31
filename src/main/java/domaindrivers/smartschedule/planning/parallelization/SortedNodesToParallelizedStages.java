package domaindrivers.smartschedule.planning.parallelization;

import domaindrivers.smartschedule.sorter.Node;
import domaindrivers.smartschedule.sorter.SortedNodes;

import java.util.List;
import java.util.stream.Collectors;


class SortedNodesToParallelizedStages {

    ParallelStagesList calculate(SortedNodes sortedNodes) {
        List<ParallelStages> parallelized = sortedNodes
                .all()
                .stream()
                .map(nodes -> new ParallelStages(nodes
                        .nodes()
                        .stream()
                        .map(Node::content)
                        .collect(Collectors.toSet())))
                .collect(Collectors.toList());
        return new ParallelStagesList(parallelized);
    }

}