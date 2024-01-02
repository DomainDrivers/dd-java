package domaindrivers.smartschedule.planning.parallelization;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.*;

public record ParallelStages(Set<Stage> stages) {

    public String print() {
        return stages.stream()
                .map(Stage::name)
                .sorted()
                .collect(Collectors.joining(", "));
    }


    public static ParallelStages of(Stage... stages) {
        return new ParallelStages(new HashSet<>(asList(stages)));
    }

    public Duration duration() {
        return stages
                .stream()
                .map(Stage::duration)
                .max(Duration::compareTo)
                .orElse(Duration.ZERO);
    }
}
