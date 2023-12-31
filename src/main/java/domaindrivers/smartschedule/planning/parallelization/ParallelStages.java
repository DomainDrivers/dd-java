package domaindrivers.smartschedule.planning.parallelization;

import java.util.Set;
import java.util.stream.Collectors;

public record ParallelStages(Set<Stage> stages) {

    public String print() {
        return stages.stream()
                .map(Stage::name)
                .sorted()
                .collect(Collectors.joining(", "));
    }
}
