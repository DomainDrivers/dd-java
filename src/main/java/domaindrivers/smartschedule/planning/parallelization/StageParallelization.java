package domaindrivers.smartschedule.planning.parallelization;

import domaindrivers.smartschedule.sorter.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StageParallelization {

    public ParallelStagesList of(Set<Stage> stages) {
        Nodes<Stage> nodes = new StagesToNodes().calculate(new ArrayList<>(stages));
        SortedNodes<Stage> sortedNodes = new GraphTopologicalSort<Stage>().sort(nodes);
        return new SortedNodesToParallelizedStages().calculate(sortedNodes);
    }

    public RemovalSuggestion whatToRemove(Set<Stage> stages) {
        Nodes<Stage> nodes = new StagesToNodes().calculate(new ArrayList<>(stages));
        List<Edge> result = new FeedbackArcSeOnGraph<Stage>().calculate(new ArrayList<>(nodes.nodes()));
        return new RemovalSuggestion(result);
    }

}

record RemovalSuggestion(List<Edge> edges) {
    @Override
    public String toString() {
        return edges.toString();
    }
}
