package domaindrivers.smartschedule.planning.parallelization;


import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DurationCalculator implements Function<List<Stage>, Duration> {

    @Override
    public Duration apply(List<Stage> stages) {
        ParallelStagesList parallelizedStages = new StageParallelization().of(new HashSet<>(stages));
        Map<Stage, Duration> durations = stages.stream()
                .collect(Collectors.toMap(identity -> identity, Stage::duration));
        return parallelizedStages.allSorted().stream()
                .map(parallelStages -> parallelStages.stages().stream()
                        .map(durations::get)
                        .max(Duration::compareTo)
                        .orElse(Duration.ZERO)
                )
                .reduce(Duration.ZERO, Duration::plus);
    }

}