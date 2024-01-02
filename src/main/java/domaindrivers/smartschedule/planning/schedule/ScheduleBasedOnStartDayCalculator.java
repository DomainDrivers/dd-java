package domaindrivers.smartschedule.planning.schedule;

import domaindrivers.smartschedule.planning.parallelization.ParallelStages;
import domaindrivers.smartschedule.planning.parallelization.ParallelStagesList;
import domaindrivers.smartschedule.planning.parallelization.Stage;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ScheduleBasedOnStartDayCalculator {

    Map<String, TimeSlot> calculate(Instant startDate, ParallelStagesList parallelizedStages, Comparator<ParallelStages> comparing) {
        Map<String, TimeSlot> scheduleMap = new HashMap<>();
        Instant currentStart = startDate;
        List<ParallelStages> allSorted = parallelizedStages.allSorted(comparing);
        for (ParallelStages stages : allSorted) {
            Instant parallelizedStagesEnd = currentStart;
            for (Stage stage : stages.stages()) {
                Instant stageEnd = currentStart.plus(stage.duration());
                scheduleMap.put(stage.stageName(), new TimeSlot(currentStart, stageEnd));
                if (stageEnd.isAfter(parallelizedStagesEnd)) {
                    parallelizedStagesEnd = stageEnd;
                }
            }
            currentStart = parallelizedStagesEnd;

        }
        return scheduleMap;
    }

}
