package domaindrivers.smartschedule.planning.parallelization;

import domaindrivers.smartschedule.sorter.GraphTopologicalSort;
import domaindrivers.smartschedule.sorter.Nodes;
import domaindrivers.smartschedule.sorter.SortedNodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class StageParallelization {

    private static final Function<List<Stage>, Nodes<Stage>> CREATE_NODES = stages -> new StagesToNodes().apply(stages);
    private static final Function<Nodes<Stage>, SortedNodes<Stage>> GRAPH_SORT = nodes -> new GraphTopologicalSort<Stage>().apply(nodes);
    private static final Function<SortedNodes<Stage>, ParallelStagesList> PARALLELIZE = nodes -> new SortedNodesToParallelizedStages().apply(nodes);

    private static final Function<List<Stage>, ParallelStagesList>
            WORKFLOW = CREATE_NODES.andThen(GRAPH_SORT).andThen(PARALLELIZE);

    public ParallelStagesList of(Set<Stage> stages) {
        return WORKFLOW.apply(new ArrayList<>(stages));
    }


}
