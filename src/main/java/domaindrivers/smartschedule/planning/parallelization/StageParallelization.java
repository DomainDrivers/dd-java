package domaindrivers.smartschedule.planning.parallelization;

import domaindrivers.smartschedule.sorter.GraphTopologicalSort;
import domaindrivers.smartschedule.sorter.Nodes;
import domaindrivers.smartschedule.sorter.SortedNodes;

import java.util.ArrayList;
import java.util.Set;

public class StageParallelization {

    public ParallelStagesList of(Set<Stage> stages) {
        Nodes<Stage> nodes = new StagesToNodes().calculate(new ArrayList<>(stages));
        SortedNodes<Stage> sortedNodes = new GraphTopologicalSort<Stage>().sort(nodes);
        return new SortedNodesToParallelizedStages().calculate(sortedNodes);
    }

}
