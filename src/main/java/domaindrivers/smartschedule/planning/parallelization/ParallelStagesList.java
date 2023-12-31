package domaindrivers.smartschedule.planning.parallelization;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record ParallelStagesList(List<ParallelStages> all) {

    public static ParallelStagesList empty() {
        return new ParallelStagesList(List.of());
    }

    public String print() {
        return all.stream()
                .map(ParallelStages::print)
                .collect(Collectors.joining(" | "));
    }

    public ParallelStagesList add(ParallelStages newParallelStages) {
        List<ParallelStages> result =
                Stream
                        .concat(this.all.stream(), Stream.of(newParallelStages))
                        .collect(Collectors.toList());
        return new ParallelStagesList(result);
    }
}

