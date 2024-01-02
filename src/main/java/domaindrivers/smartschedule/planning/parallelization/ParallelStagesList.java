package domaindrivers.smartschedule.planning.parallelization;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;

public record ParallelStagesList(List<ParallelStages> all) {


    public static ParallelStagesList of(ParallelStages ... stages) {
        return new ParallelStagesList(List.of(stages));
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

    public List<ParallelStages> allSorted(Comparator<ParallelStages> comparing) {
        return all
                .stream()
                .sorted(comparing)
                .collect(Collectors.toList());
    }

    public List<ParallelStages> allSorted() {
        return allSorted(comparing(ParallelStages::print));
    }



}

