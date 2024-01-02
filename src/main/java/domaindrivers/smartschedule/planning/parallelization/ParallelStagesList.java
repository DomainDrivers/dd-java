package domaindrivers.smartschedule.planning.parallelization;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record ParallelStagesList(List<ParallelStages> all) {

    public String print() {
        return all.stream()
                .map(ParallelStages::print)
                .collect(Collectors.joining(" | "));
    }

}

